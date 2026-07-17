package com.chandanshakya.fuellog.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chandanshakya.fuellog.data.model.FuelEntry
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for FuelEntry entities.
 */
@Dao
interface FuelEntryDao {
    @Query("SELECT * FROM fuel_entries WHERE vehicleId = :vehicleId ORDER BY odometer ASC, date ASC")
    fun getAllByVehicle(vehicleId: Long): Flow<List<FuelEntry>>

    @Query("SELECT * FROM fuel_entries WHERE vehicleId = :vehicleId ORDER BY date DESC, odometer DESC LIMIT :limit OFFSET :offset")
    suspend fun getPaginatedByVehicle(
        vehicleId: Long,
        limit: Int,
        offset: Int
    ): List<FuelEntry>

    @Query("SELECT * FROM fuel_entries WHERE id = :id")
    suspend fun getById(id: Long): FuelEntry?

    @Query("SELECT * FROM fuel_entries WHERE vehicleId = :vehicleId")
    suspend fun getAllByVehicleSuspend(vehicleId: Long): List<FuelEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: FuelEntry): Long

    @Update
    suspend fun update(entry: FuelEntry)

    @Delete
    suspend fun delete(entry: FuelEntry)

    @Query("DELETE FROM fuel_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM fuel_entries WHERE vehicleId = :vehicleId")
    suspend fun deleteAllByVehicle(vehicleId: Long)

    @Query("SELECT COUNT(*) FROM fuel_entries WHERE vehicleId = :vehicleId")
    suspend fun countByVehicle(vehicleId: Long): Int
}
