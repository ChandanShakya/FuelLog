package com.chandanshakya.fuellog.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandanshakya.fuellog.data.db.FuelEntryDao
import com.chandanshakya.fuellog.data.db.UserSettingsDao
import com.chandanshakya.fuellog.data.db.VehicleDao
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.Vehicle
import com.chandanshakya.fuellog.util.MileageCalculator
import com.chandanshakya.fuellog.util.Validation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class FuelLogViewModel(
    private val fuelEntryDao: FuelEntryDao,
    private val vehicleDao: VehicleDao,
    private val userSettingsDao: UserSettingsDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val currentVehicleId = savedStateHandle.getStateFlow("vehicleId", -1L)
    private val vehicleFlow = currentVehicleId.flatMapLatest { vehicleDao.getByIdFlow(it) }
    private val settingsFlow = userSettingsDao.getSettings()

    val fuelLogState: StateFlow<FuelLogState> = combine(
        currentVehicleId.flatMapLatest { fuelEntryDao.getAllByVehicle(it) },
        vehicleFlow,
        settingsFlow
    ) { entries, v, settings ->
        val sortedEntries = entries.sortedBy { it.odometer }

        val entriesWithMileage = sortedEntries.mapIndexed { index, entry ->
            val previous = if (index > 0) sortedEntries[index - 1] else null
            val mileage = if (previous != null && v != null) {
                MileageCalculator.calculateMileage(entry, previous, v.distanceUnit, v.volumeUnit)
            } else null
            EntryWithMileage(entry = entry, mileage = mileage)
        }.reversed()

        FuelLogState(
            vehicle = v,
            entries = entriesWithMileage,
            averageMileage = if (v != null) MileageCalculator.calculateAverageMileage(sortedEntries, v.distanceUnit, v.volumeUnit) else null,
            totalDistance = if (v != null) MileageCalculator.calculateTotalDistance(sortedEntries, v.distanceUnit) else 0.0,
            totalFuel = if (v != null) MileageCalculator.calculateTotalFuel(sortedEntries, v.volumeUnit) else 0.0,
            totalCost = MileageCalculator.calculateTotalCost(sortedEntries),
            currency = settings?.defaultCurrency ?: "USD"
        )
    }.flowOn(Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FuelLogState()
    )

    fun addFuelEntry(
        date: LocalDate,
        odometer: Double,
        fuelVolume: Double,
        fuelCost: Double
    ) {
        if (!Validation.validateFuelEntry(odometer, fuelVolume, fuelCost)) return

        viewModelScope.launch(Dispatchers.IO) {
            val entry = FuelEntry(
                vehicleId = currentVehicleId.value,
                date = date,
                odometer = odometer,
                fuelVolume = fuelVolume,
                fuelCost = fuelCost
            )
            fuelEntryDao.insert(entry)
        }
    }

    fun updateFuelEntry(
        id: Long, date: LocalDate, odometer: Double, fuelVolume: Double, fuelCost: Double
    ) {
        if (!Validation.validateFuelEntry(odometer, fuelVolume, fuelCost)) return

        viewModelScope.launch(Dispatchers.IO) {
            val entry = FuelEntry(
                id = id,
                vehicleId = currentVehicleId.value,
                date = date,
                odometer = odometer,
                fuelVolume = fuelVolume,
                fuelCost = fuelCost
            )
            fuelEntryDao.update(entry)
        }
    }

    fun deleteFuelEntry(id: Long) {
        viewModelScope.launch(Dispatchers.IO) { fuelEntryDao.deleteById(id) }
    }
}

data class FuelLogState(
    val vehicle: Vehicle? = null,
    val entries: List<EntryWithMileage> = emptyList(),
    val averageMileage: Double? = null,
    val totalDistance: Double = 0.0,
    val totalFuel: Double = 0.0,
    val totalCost: Double = 0.0,
    val currency: String = "USD"
)

data class EntryWithMileage(
    val entry: FuelEntry,
    val mileage: Double?
)
