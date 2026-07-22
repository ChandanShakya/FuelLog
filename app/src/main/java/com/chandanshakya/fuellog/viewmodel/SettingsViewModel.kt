package com.chandanshakya.fuellog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandanshakya.fuellog.data.db.UserSettingsDao
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.UserSettings
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.util.Validation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userSettingsDao: UserSettingsDao
) : ViewModel() {

    val settingsState: StateFlow<SettingsState> = userSettingsDao.getSettings()
        .map { settings ->
            SettingsState(settings = settings)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsState()
        )

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
}

data class SettingsState(
    val settings: UserSettings? = null
)
