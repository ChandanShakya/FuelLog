package com.chandanshakya.fuellog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandanshakya.fuellog.data.db.FuelEntryDao
import com.chandanshakya.fuellog.data.db.UserSettingsDao
import com.chandanshakya.fuellog.data.db.VehicleDao
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.Vehicle
import com.chandanshakya.fuellog.util.MileageCalculator
import com.chandanshakya.fuellog.util.Validation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

import androidx.lifecycle.SavedStateHandle
import com.chandanshakya.fuellog.ui.navigation.NavArgs

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FuelLogViewModel @Inject constructor(
    private val fuelEntryDao: FuelEntryDao,
    private val vehicleDao: VehicleDao,
    private val userSettingsDao: UserSettingsDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val currentVehicleId = MutableStateFlow(savedStateHandle.get<Long>(NavArgs.VEHICLE_ID) ?: -1L)
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
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FuelLogState()
    )

    fun setVehicleId(vehicleId: Long) {
        currentVehicleId.value = vehicleId
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
            fuelEntryDao.insert(entry)
        }
    }

    fun updateFuelEntry(
        id: Long, date: LocalDate, odometer: Double, fuelVolume: Double, fuelCost: Double, notes: String? = null
    ) {
        if (!Validation.validateFuelEntry(odometer, fuelVolume, fuelCost)) return

        viewModelScope.launch {
            val existing = fuelEntryDao.getById(id) ?: return@launch
            val entry = existing.copy(date = date, odometer = odometer, fuelVolume = fuelVolume, fuelCost = fuelCost, notes = notes)
            fuelEntryDao.update(entry)
        }
    }

    fun deleteFuelEntry(id: Long) {
        viewModelScope.launch { fuelEntryDao.deleteById(id) }
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
