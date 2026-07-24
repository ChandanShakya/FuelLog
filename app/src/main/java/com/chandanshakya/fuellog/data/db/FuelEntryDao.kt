package com.chandanshakya.fuellog.data.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chandanshakya.fuellog.data.model.FuelEntry
import kotlinx.coroutines.flow.Flow

data class FuelEntryWithPump(
    @Embedded val entry: FuelEntry,
    @ColumnInfo(name = "pumpName") val pumpName: String?
)

@Dao
interface FuelEntryDao {
    @Query("SELECT * FROM fuel_entries WHERE vehicleId = :vehicleId ORDER BY odometer ASC, date ASC")
    fun getAllByVehicle(vehicleId: Long): Flow<List<FuelEntry>>

    @Query("SELECT * FROM fuel_entries WHERE vehicleId = :vehicleId ORDER BY odometer ASC, date ASC")
    suspend fun getAllByVehicleList(vehicleId: Long): List<FuelEntry>

    @Query("SELECT fe.*, fp.name AS pumpName FROM fuel_entries fe LEFT JOIN fuel_pumps fp ON fe.fuelPumpId = fp.id WHERE fe.vehicleId = :vehicleId ORDER BY fe.odometer ASC, fe.date ASC")
    fun getAllByVehicleWithPump(vehicleId: Long): Flow<List<FuelEntryWithPump>>

    @Query("SELECT * FROM fuel_entries WHERE id = :id")
    suspend fun getById(id: Long): FuelEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: FuelEntry): Long

    @Update
    suspend fun update(entry: FuelEntry)

    @Update
    suspend fun updateAll(entries: List<FuelEntry>)

    @Query("DELETE FROM fuel_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
