package com.chandanshakya.fuellog.data.db

import android.content.ContentValues
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.UserSettings
import com.chandanshakya.fuellog.data.model.VolumeUnit
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UserSettingsDao(private val dbHelper: FuelLogDbHelper) {

    private val _dataChanged = Channel<Unit>(Channel.BUFFERED)

    fun getSettings(): Flow<UserSettings?> = flow {
        emit(querySettings())
        for (Unit in _dataChanged) emit(querySettings())
    }

    suspend fun getSettingsSuspend(): UserSettings? = querySettings()

    suspend fun insert(settings: UserSettings) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id", settings.id)
            put("defaultCurrency", settings.defaultCurrency)
            put("defaultDistanceUnit", settings.defaultDistanceUnit.name)
            put("defaultVolumeUnit", settings.defaultVolumeUnit.name)
        }
        db.insert("user_settings", null, values)
        _dataChanged.send(Unit)
    }

    suspend fun update(settings: UserSettings) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("defaultCurrency", settings.defaultCurrency)
            put("defaultDistanceUnit", settings.defaultDistanceUnit.name)
            put("defaultVolumeUnit", settings.defaultVolumeUnit.name)
        }
        db.update("user_settings", values, "id = ?", arrayOf(settings.id.toString()))
        _dataChanged.send(Unit)
    }

    private fun querySettings(): UserSettings? {
        val db = dbHelper.readableDatabase
        val cursor = db.query("user_settings", null, "id = 1", null, null, null, null)
        return cursor.use {
            if (it.moveToFirst()) UserSettings(
                id = it.getLong(it.getColumnIndexOrThrow("id")),
                defaultCurrency = it.getString(it.getColumnIndexOrThrow("defaultCurrency")),
                defaultDistanceUnit = DistanceUnit.valueOf(it.getString(it.getColumnIndexOrThrow("defaultDistanceUnit"))),
                defaultVolumeUnit = VolumeUnit.valueOf(it.getString(it.getColumnIndexOrThrow("defaultVolumeUnit")))
            ) else null
        }
    }
}
