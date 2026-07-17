package com.chandanshakya.fuellog.data.repository

import com.chandanshakya.fuellog.data.db.VehicleDao
import com.chandanshakya.fuellog.data.model.Vehicle
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Room-based implementation of VehicleRepository.
 */
class VehicleRepositoryImpl @Inject constructor(
    private val vehicleDao: VehicleDao
) : VehicleRepository {
    override fun getAll(): Flow<List<Vehicle>> = vehicleDao.getAll()

    override suspend fun getById(id: Long): Vehicle? = vehicleDao.getById(id)

    override suspend fun insert(vehicle: Vehicle): Long = vehicleDao.insert(vehicle)

    override suspend fun update(vehicle: Vehicle) = vehicleDao.update(vehicle)

    override suspend fun delete(vehicle: Vehicle) = vehicleDao.delete(vehicle)

    override suspend fun deleteById(id: Long) = vehicleDao.deleteById(id)
}
