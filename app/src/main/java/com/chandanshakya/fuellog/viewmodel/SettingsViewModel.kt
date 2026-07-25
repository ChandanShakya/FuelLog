package com.chandanshakya.fuellog.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandanshakya.fuellog.data.backup.BackupManager
import com.chandanshakya.fuellog.data.db.FuelEntryDao
import com.chandanshakya.fuellog.data.db.FuelPumpDao
import com.chandanshakya.fuellog.data.db.OdometerReadingDao
import com.chandanshakya.fuellog.data.db.UserSettingsDao
import com.chandanshakya.fuellog.data.db.VehicleDao
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.UserSettings
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.util.Validation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userSettingsDao: UserSettingsDao,
    private val vehicleDao: VehicleDao,
    private val fuelEntryDao: FuelEntryDao,
    private val fuelPumpDao: FuelPumpDao,
    private val odometerReadingDao: OdometerReadingDao
) : ViewModel() {

    private val backupManager = BackupManager(
        vehicleDao, fuelEntryDao, fuelPumpDao, odometerReadingDao, userSettingsDao
    )

    val settingsState: StateFlow<SettingsState> = userSettingsDao.getSettings()
        .map { settings ->
            SettingsState(settings = settings)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsState()
        )

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun clearMessage() {
        _message.value = null
    }

    fun updateSettings(
        currency: String,
        distanceUnit: DistanceUnit,
        volumeUnit: VolumeUnit
    ) {
        if (!Validation.validateCurrencyCode(currency)) {
            return
        }

        viewModelScope.launch {
            val settings = UserSettings(
                defaultCurrency = currency,
                defaultDistanceUnit = distanceUnit,
                defaultVolumeUnit = volumeUnit
            )
            val existing = userSettingsDao.getSettingsSuspend()
            if (existing == null) {
                userSettingsDao.insert(settings)
            } else {
                userSettingsDao.update(settings)
            }
        }
    }

    fun exportData(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    backupManager.exportToStream(stream)
                }
                _message.value = "Data exported successfully"
            } catch (e: Exception) {
                _message.value = "Export failed: ${e.message}"
            }
        }
    }

    fun importData(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    backupManager.importFromStream(stream)
                }
                _message.value = "Data imported successfully"
            } catch (e: Exception) {
                _message.value = "Import failed: ${e.message}"
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            try {
                backupManager.clearAll()
                _message.value = "All data cleared"
            } catch (e: Exception) {
                _message.value = "Clear failed: ${e.message}"
            }
        }
    }
}

data class SettingsState(
    val settings: UserSettings? = null
)
