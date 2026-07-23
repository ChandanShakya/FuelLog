package com.chandanshakya.fuellog.data.db

import android.content.ContentValues
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.Vehicle
import com.chandanshakya.fuellog.data.model.VehicleType
import com.chandanshakya.fuellog.data.model.VolumeUnit
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate

class VehicleDao(private val dbHelper: FuelLogDbHelper) {

    private val _dataChanged = Channel<Unit>(Channel.BUFFERED)

    fun getAll(): Flow<List<Vehicle>> = flow {
        emit(queryAll())
        for (Unit in _dataChanged) emit(queryAll())
    }

    suspend fun getById(id: Long): Vehicle? {
        val db = dbHelper.readableDatabase
        val cursor = db.query("vehicles", null, "id = ?", arrayOf(id.toString()), null, null, null)
        return cursor.use {
            if (it.moveToFirst()) cursorToVehicle(it) else null
        }
    }

    fun getByIdFlow(id: Long): Flow<Vehicle?> = flow {
        emit(getById(id))
        for (Unit in _dataChanged) emit(getById(id))
    }

    suspend fun insert(vehicle: Vehicle): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", vehicle.name)
            put("vehicleType", vehicle.vehicleType.name)
            put("distanceUnit", vehicle.distanceUnit.name)
            put("volumeUnit", vehicle.volumeUnit.name)
            put("createdAt", vehicle.createdAt.toString())
        }
        val id = db.insert("vehicles", null, values)
        _dataChanged.send(Unit)
        return id
    }

    suspend fun update(vehicle: Vehicle) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", vehicle.name)
            put("vehicleType", vehicle.vehicleType.name)
            put("distanceUnit", vehicle.distanceUnit.name)
            put("volumeUnit", vehicle.volumeUnit.name)
            put("createdAt", vehicle.createdAt.toString())
        }
        db.update("vehicles", values, "id = ?", arrayOf(vehicle.id.toString()))
        _dataChanged.send(Unit)
    }

    suspend fun deleteById(id: Long) {
        dbHelper.writableDatabase.delete("vehicles", "id = ?", arrayOf(id.toString()))
        _dataChanged.send(Unit)
    }

    private fun queryAll(): List<Vehicle> {
        val db = dbHelper.readableDatabase
        val cursor = db.query("vehicles", null, null, null, null, null, "name ASC")
        return cursor.use {
            val list = mutableListOf<Vehicle>()
            while (it.moveToNext()) list.add(cursorToVehicle(it))
            list
        }
    }

    private fun cursorToVehicle(cursor: android.database.Cursor): Vehicle {
        return Vehicle(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
            vehicleType = VehicleType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("vehicleType"))),
            distanceUnit = DistanceUnit.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("distanceUnit"))),
            volumeUnit = VolumeUnit.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("volumeUnit"))),
            createdAt = LocalDate.parse(cursor.getString(cursor.getColumnIndexOrThrow("createdAt")))
        )
    }
}
