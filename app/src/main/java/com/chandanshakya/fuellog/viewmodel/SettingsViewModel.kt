package com.chandanshakya.fuellog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.UserSettings
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.data.repository.SettingsRepository
import com.chandanshakya.fuellog.util.Validation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing user settings.
 * 
 * Handles global settings that serve as defaults for new vehicles.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    /**
     * State containing current user settings.
     */
    val settingsState: StateFlow<SettingsState> = settingsRepository.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsState()
        )

    /**
     * Update user settings.
     */
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
            settingsRepository.saveSettings(settings)
        }
    }

    /**
     * Get current settings.
     */
    suspend fun getCurrentSettings(): UserSettings? = settingsRepository.getSettingsSuspend()
}

/**
 * State for settings screen.
 */
data class SettingsState(
    val settings: UserSettings? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
