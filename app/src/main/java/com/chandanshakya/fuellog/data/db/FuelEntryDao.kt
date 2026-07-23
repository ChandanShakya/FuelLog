package com.chandanshakya.fuellog.data.db

import android.content.ContentValues
import com.chandanshakya.fuellog.data.model.FuelEntry
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate

class FuelEntryDao(private val dbHelper: FuelLogDbHelper) {

    private val _dataChanged = Channel<Unit>(Channel.BUFFERED)

    fun getAllByVehicle(vehicleId: Long): Flow<List<FuelEntry>> = flow {
        emit(queryByVehicle(vehicleId))
        for (Unit in _dataChanged) emit(queryByVehicle(vehicleId))
    }

    suspend fun getAllByVehicleList(vehicleId: Long): List<FuelEntry> = queryByVehicle(vehicleId)

    suspend fun getById(id: Long): FuelEntry? {
        val db = dbHelper.readableDatabase
        val cursor = db.query("fuel_entries", null, "id = ?", arrayOf(id.toString()), null, null, null)
        return cursor.use {
            if (it.moveToFirst()) cursorToFuelEntry(it) else null
        }
    }

    suspend fun insert(entry: FuelEntry): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("vehicleId", entry.vehicleId)
            put("date", entry.date.toString())
            put("odometer", entry.odometer)
            put("fuelVolume", entry.fuelVolume)
            put("fuelCost", entry.fuelCost)
        }
        val id = db.insert("fuel_entries", null, values)
        _dataChanged.send(Unit)
        return id
    }

    suspend fun update(entry: FuelEntry) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("vehicleId", entry.vehicleId)
            put("date", entry.date.toString())
            put("odometer", entry.odometer)
            put("fuelVolume", entry.fuelVolume)
            put("fuelCost", entry.fuelCost)
        }
        db.update("fuel_entries", values, "id = ?", arrayOf(entry.id.toString()))
        _dataChanged.send(Unit)
    }

    suspend fun updateAll(entries: List<FuelEntry>) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            for (entry in entries) {
                val values = ContentValues().apply {
                    put("vehicleId", entry.vehicleId)
                    put("date", entry.date.toString())
                    put("odometer", entry.odometer)
                    put("fuelVolume", entry.fuelVolume)
                    put("fuelCost", entry.fuelCost)
                }
                db.update("fuel_entries", values, "id = ?", arrayOf(entry.id.toString()))
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        _dataChanged.send(Unit)
    }

    suspend fun deleteById(id: Long) {
        dbHelper.writableDatabase.delete("fuel_entries", "id = ?", arrayOf(id.toString()))
        _dataChanged.send(Unit)
    }

    private fun queryByVehicle(vehicleId: Long): List<FuelEntry> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "fuel_entries", null, "vehicleId = ?",
            arrayOf(vehicleId.toString()), null, null, "odometer ASC, date ASC"
        )
        return cursor.use {
            val list = mutableListOf<FuelEntry>()
            while (it.moveToNext()) list.add(cursorToFuelEntry(it))
            list
        }
    }

    private fun cursorToFuelEntry(cursor: android.database.Cursor): FuelEntry {
        return FuelEntry(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            vehicleId = cursor.getLong(cursor.getColumnIndexOrThrow("vehicleId")),
            date = LocalDate.parse(cursor.getString(cursor.getColumnIndexOrThrow("date"))),
            odometer = cursor.getDouble(cursor.getColumnIndexOrThrow("odometer")),
            fuelVolume = cursor.getDouble(cursor.getColumnIndexOrThrow("fuelVolume")),
            fuelCost = cursor.getDouble(cursor.getColumnIndexOrThrow("fuelCost"))
        )
    }
}
