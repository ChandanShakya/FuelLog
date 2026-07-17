package com.chandanshakya.fuellog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.Vehicle
import com.chandanshakya.fuellog.data.repository.FuelRepository
import com.chandanshakya.fuellog.data.repository.VehicleRepository
import com.chandanshakya.fuellog.util.MileageCalculator
import com.chandanshakya.fuellog.util.Validation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class FuelLogViewModel @Inject constructor(
    private val fuelRepository: FuelRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val currentVehicleId = MutableStateFlow(-1L)
    private var currentVehicle: Vehicle? = null

    val fuelLogState: StateFlow<FuelLogState> = currentVehicleId.flatMapLatest { vehicleId ->
        combine(
            fuelRepository.getAllByVehicle(vehicleId),
            vehicleRepository.getAll()
        ) { entries, vehicles ->
            val vehicle = vehicles.find { it.id == vehicleId }
            val sortedEntries = entries.sortedBy { it.odometer }

            val entriesWithMileage = sortedEntries.mapIndexed { index, entry ->
                val previous = if (index > 0) sortedEntries[index - 1] else null
                val mileage = if (previous != null && vehicle != null) {
                    MileageCalculator.calculateMileage(entry, previous, vehicle.distanceUnit, vehicle.volumeUnit)
                } else null
                EntryWithMileage(entry = entry, mileage = mileage)
            }

            FuelLogState(
                vehicle = vehicle,
                entries = entriesWithMileage,
                averageMileage = if (vehicle != null) MileageCalculator.calculateAverageMileage(sortedEntries, vehicle.distanceUnit, vehicle.volumeUnit) else null,
                totalDistance = if (vehicle != null) MileageCalculator.calculateTotalDistance(sortedEntries, vehicle.distanceUnit) else 0.0,
                totalFuel = if (vehicle != null) MileageCalculator.calculateTotalFuel(sortedEntries, vehicle.volumeUnit) else 0.0,
                totalCost = MileageCalculator.calculateTotalCost(sortedEntries)
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FuelLogState()
    )

    fun setVehicleId(vehicleId: Long) {
        if (currentVehicleId.value != vehicleId) {
            currentVehicleId.value = vehicleId
            viewModelScope.launch {
                currentVehicle = vehicleRepository.getById(vehicleId)
            }
        }
    }

    fun addFuelEntry(
        date: LocalDate,
        odometer: Double,
        fuelVolume: Double,
        fuelCost: Double,
        notes: String? = null
    ) {
        if (!Validation.validateFuelEntry(odometer, fuelVolume, fuelCost)) return

        viewModelScope.launch {
            val entry = FuelEntry(
                vehicleId = currentVehicleId.value,
                date = date,
                odometer = odometer,
                fuelVolume = fuelVolume,
                fuelCost = fuelCost,
                notes = notes
            )
            fuelRepository.insert(entry)
        }
    }

    fun updateFuelEntry(
        id: Long, date: LocalDate, odometer: Double, fuelVolume: Double, fuelCost: Double, notes: String? = null
    ) {
        if (!Validation.validateFuelEntry(odometer, fuelVolume, fuelCost)) return

        viewModelScope.launch {
            val existing = fuelRepository.getById(id) ?: return@launch
            val entry = existing.copy(date = date, odometer = odometer, fuelVolume = fuelVolume, fuelCost = fuelCost, notes = notes)
            fuelRepository.update(entry)
        }
    }

    fun deleteFuelEntry(id: Long) {
        viewModelScope.launch { fuelRepository.deleteById(id) }
    }

    fun getCurrentVehicle(): Vehicle? = currentVehicle

    suspend fun getFuelEntriesCount(): Int = fuelRepository.countByVehicle(currentVehicleId.value)
}

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

data class EntryWithMileage(
    val entry: FuelEntry,
    val mileage: Double?
)
