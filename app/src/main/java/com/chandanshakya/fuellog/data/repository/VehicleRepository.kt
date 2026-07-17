package com.chandanshakya.fuellog.data.repository

import com.chandanshakya.fuellog.data.model.Vehicle
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Vehicle operations.
 */
interface VehicleRepository {
    fun getAll(): Flow<List<Vehicle>>
    suspend fun getById(id: Long): Vehicle?
    suspend fun insert(vehicle: Vehicle): Long
    suspend fun update(vehicle: Vehicle)
    suspend fun delete(vehicle: Vehicle)
    suspend fun deleteById(id: Long)
}
