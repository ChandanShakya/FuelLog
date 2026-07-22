package com.chandanshakya.fuellog.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chandanshakya.fuellog.data.model.FuelEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelEntryDao {
    @Query("SELECT * FROM fuel_entries WHERE vehicleId = :vehicleId ORDER BY odometer ASC, date ASC")
    fun getAllByVehicle(vehicleId: Long): Flow<List<FuelEntry>>

    @Query("SELECT * FROM fuel_entries WHERE vehicleId = :vehicleId ORDER BY odometer ASC, date ASC")
    suspend fun getAllByVehicleList(vehicleId: Long): List<FuelEntry>

    @Query("SELECT * FROM fuel_entries WHERE id = :id")
    suspend fun getById(id: Long): FuelEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: FuelEntry): Long

    @Update
    suspend fun update(entry: FuelEntry)

    @Update
    suspend fun updateAll(entries: List<FuelEntry>)

    @Delete
    suspend fun delete(entry: FuelEntry)

    @Query("DELETE FROM fuel_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
