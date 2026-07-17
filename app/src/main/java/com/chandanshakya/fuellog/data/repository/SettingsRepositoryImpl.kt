package com.chandanshakya.fuellog.data.repository

import com.chandanshakya.fuellog.data.db.UserSettingsDao
import com.chandanshakya.fuellog.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Room-based implementation of SettingsRepository.
 */
class SettingsRepositoryImpl @Inject constructor(
    private val userSettingsDao: UserSettingsDao
) : SettingsRepository {
    override fun getSettings(): Flow<UserSettings?> = userSettingsDao.getSettings()

    override suspend fun getSettingsSuspend(): UserSettings? = userSettingsDao.getSettingsSuspend()

    override suspend fun saveSettings(settings: UserSettings) {
        val existing = userSettingsDao.getSettingsSuspend()
        if (existing == null) {
            userSettingsDao.insert(settings)
        } else {
            userSettingsDao.update(settings)
        }
    }
}
