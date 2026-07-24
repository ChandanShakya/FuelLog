package com.chandanshakya.fuellog.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chandanshakya.fuellog.data.model.OdometerReading
import kotlinx.coroutines.flow.Flow

@Dao
interface OdometerReadingDao {
    @Query("SELECT * FROM odometer_readings WHERE vehicleId = :vehicleId ORDER BY odometer ASC, date ASC")
    fun getByVehicle(vehicleId: Long): Flow<List<OdometerReading>>

    @Query("SELECT * FROM odometer_readings WHERE vehicleId = :vehicleId ORDER BY odometer ASC, date ASC")
    suspend fun getByVehicleList(vehicleId: Long): List<OdometerReading>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reading: OdometerReading): Long

    @Query("DELETE FROM odometer_readings WHERE id = :id")
    suspend fun deleteById(id: Long)
}
