package com.chandanshakya.fuellog.data.repository

import com.chandanshakya.fuellog.data.db.FuelEntryDao
import com.chandanshakya.fuellog.data.model.FuelEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Room-based implementation of FuelRepository.
 */
class FuelRepositoryImpl @Inject constructor(
    private val fuelEntryDao: FuelEntryDao
) : FuelRepository {
    override fun getAllByVehicle(vehicleId: Long): Flow<List<FuelEntry>> = 
        fuelEntryDao.getAllByVehicle(vehicleId)

    override suspend fun getPaginatedByVehicle(
        vehicleId: Long,
        limit: Int,
        offset: Int
    ): List<FuelEntry> = fuelEntryDao.getPaginatedByVehicle(vehicleId, limit, offset)

    override suspend fun getById(id: Long): FuelEntry? = fuelEntryDao.getById(id)

    override suspend fun getAllByVehicleSuspend(vehicleId: Long): List<FuelEntry> = 
        fuelEntryDao.getAllByVehicleSuspend(vehicleId)

    override suspend fun insert(entry: FuelEntry): Long = fuelEntryDao.insert(entry)

    override suspend fun update(entry: FuelEntry) = fuelEntryDao.update(entry)

    override suspend fun delete(entry: FuelEntry) = fuelEntryDao.delete(entry)

    override suspend fun deleteById(id: Long) = fuelEntryDao.deleteById(id)

    override suspend fun deleteAllByVehicle(vehicleId: Long) = 
        fuelEntryDao.deleteAllByVehicle(vehicleId)

    override suspend fun countByVehicle(vehicleId: Long): Int = 
        fuelEntryDao.countByVehicle(vehicleId)
}
