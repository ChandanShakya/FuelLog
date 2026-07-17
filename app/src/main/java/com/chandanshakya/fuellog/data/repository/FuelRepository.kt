package com.chandanshakya.fuellog.data.repository

import com.chandanshakya.fuellog.data.model.FuelEntry
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for FuelEntry operations.
 */
interface FuelRepository {
    fun getAllByVehicle(vehicleId: Long): Flow<List<FuelEntry>>
    suspend fun getPaginatedByVehicle(
        vehicleId: Long,
        limit: Int,
        offset: Int
    ): List<FuelEntry>
    suspend fun getById(id: Long): FuelEntry?
    suspend fun getAllByVehicleSuspend(vehicleId: Long): List<FuelEntry>
    suspend fun insert(entry: FuelEntry): Long
    suspend fun update(entry: FuelEntry)
    suspend fun delete(entry: FuelEntry)
    suspend fun deleteById(id: Long)
    suspend fun deleteAllByVehicle(vehicleId: Long)
    suspend fun countByVehicle(vehicleId: Long): Int
}
