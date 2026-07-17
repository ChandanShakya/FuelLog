package com.chandanshakya.fuellog.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.Vehicle
import com.chandanshakya.fuellog.data.repository.FuelRepository
import com.chandanshakya.fuellog.data.repository.VehicleRepository
import com.chandanshakya.fuellog.util.MileageCalculator
import com.chandanshakya.fuellog.util.Validation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for managing fuel entries for a specific vehicle.
 * 
 * Handles fuel entry CRUD operations and calculates mileage.
 */
@HiltViewModel
class FuelLogViewModel @Inject constructor(
    private val fuelRepository: FuelRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {
    
    private var currentVehicleId: Long = -1
    private var currentVehicle: Vehicle? = null
    
    /**
     * State containing fuel entries and calculated mileage for the current vehicle.
     */
    val fuelLogState: StateFlow<FuelLogState> = combine(
        fuelRepository.getAllByVehicle(currentVehicleId),
        vehicleRepository.getAll()
    ) { entries, vehicles ->
        val vehicle = vehicles.find { it.id == currentVehicleId }
        val sortedEntries = entries.sortedBy { it.odometer }
        
        // Calculate mileage for each entry by zipping with previous
        val entriesWithMileage = sortedEntries.mapIndexed { index, entry ->
            val previous = if (index > 0) sortedEntries[index - 1] else null
            val mileage = if (previous != null && vehicle != null) {
                MileageCalculator.calculateMileage(
                    current = entry,
                    previous = previous,
                    distanceUnit = vehicle.distanceUnit,
                    volumeUnit = vehicle.volumeUnit
                )
            } else {
                null
            }
            EntryWithMileage(entry = entry, mileage = mileage)
        }
        
        FuelLogState(
            vehicle = vehicle,
            entries = entriesWithMileage,
            averageMileage = if (vehicle != null) {
                MileageCalculator.calculateAverageMileage(
                    entries = sortedEntries,
                    distanceUnit = vehicle.distanceUnit,
                    volumeUnit = vehicle.volumeUnit
                )
            } else null,
            totalDistance = if (vehicle != null) {
                MileageCalculator.calculateTotalDistance(
                    entries = sortedEntries,
                    distanceUnit = vehicle.distanceUnit
                )
            } else 0.0,
            totalFuel = if (vehicle != null) {
                MileageCalculator.calculateTotalFuel(
                    entries = sortedEntries,
                    volumeUnit = vehicle.volumeUnit
                )
            } else 0.0,
            totalCost = MileageCalculator.calculateTotalCost(sortedEntries)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FuelLogState()
    )

    /**
     * Set the current vehicle and refresh data.
     */
    fun setVehicleId(vehicleId: Long) {
        if (currentVehicleId != vehicleId) {
            currentVehicleId = vehicleId
            refreshData()
        }
    }

    /**
     * Refresh data for the current vehicle.
     */
    private fun refreshData() {
        viewModelScope.launch {
            currentVehicle = vehicleRepository.getById(currentVehicleId)
        }
    }

    /**
     * Add a new fuel entry.
     */
    fun addFuelEntry(
        date: LocalDate,
        odometer: Double,
        fuelVolume: Double,
        fuelCost: Double,
        isFullTank: Boolean = false,
        notes: String? = null
    ) {
        if (!Validation.validateFuelEntry(odometer, fuelVolume, fuelCost)) {
            return
        }
        
        viewModelScope.launch {
            val entry = FuelEntry(
                vehicleId = currentVehicleId,
                date = date,
                odometer = odometer,
                fuelVolume = fuelVolume,
                fuelCost = fuelCost,
                isFullTank = isFullTank,
                notes = notes
            )
            fuelRepository.insert(entry)
        }
    }

    /**
     * Update an existing fuel entry.
     */
    fun updateFuelEntry(
        id: Long,
        date: LocalDate,
        odometer: Double,
        fuelVolume: Double,
        fuelCost: Double,
        isFullTank: Boolean = false,
        notes: String? = null
    ) {
        if (!Validation.validateFuelEntry(odometer, fuelVolume, fuelCost)) {
            return
        }
        
        viewModelScope.launch {
            val existing = fuelRepository.getById(id) ?: return@launch
            val entry = existing.copy(
                date = date,
                odometer = odometer,
                fuelVolume = fuelVolume,
                fuelCost = fuelCost,
                isFullTank = isFullTank,
                notes = notes
            )
            fuelRepository.update(entry)
        }
    }

    /**
     * Delete a fuel entry.
     */
    fun deleteFuelEntry(id: Long) {
        viewModelScope.launch {
            fuelRepository.deleteById(id)
        }
    }

    /**
     * Get the current vehicle.
     */
    fun getCurrentVehicle(): Vehicle? = currentVehicle

    /**
     * Get fuel entries count for the current vehicle.
     */
    suspend fun getFuelEntriesCount(): Int = 
        fuelRepository.countByVehicle(currentVehicleId)
}

/**
 * State for fuel log screen.
 */
data class FuelLogState(
    val vehicle: Vehicle? = null,
    val entries: List<EntryWithMileage> = emptyList(),
    val averageMileage: Double? = null,
    val totalDistance: Double = 0.0,
    val totalFuel: Double = 0.0,
    val totalCost: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Fuel entry with calculated mileage.
 */
data class EntryWithMileage(
    val entry: FuelEntry,
    val mileage: Double?
)
