package com.chandanshakya.fuellog.data.repository

import com.chandanshakya.fuellog.data.model.UserSettings
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for UserSettings operations.
 */
interface SettingsRepository {
    fun getSettings(): Flow<UserSettings?>
    suspend fun getSettingsSuspend(): UserSettings?
    suspend fun saveSettings(settings: UserSettings)
}
