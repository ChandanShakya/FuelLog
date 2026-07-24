package com.chandanshakya.fuellog.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chandanshakya.fuellog.data.model.FuelPump
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelPumpDao {
    @Query("SELECT * FROM fuel_pumps ORDER BY name ASC")
    fun getAll(): Flow<List<FuelPump>>

    @Query("SELECT * FROM fuel_pumps ORDER BY name ASC")
    suspend fun getAllList(): List<FuelPump>

    @Query("SELECT * FROM fuel_pumps WHERE name = :name")
    suspend fun findByName(name: String): FuelPump?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pump: FuelPump): Long

    @Query("DELETE FROM fuel_pumps WHERE id = :id")
    suspend fun deleteById(id: Long)
}
