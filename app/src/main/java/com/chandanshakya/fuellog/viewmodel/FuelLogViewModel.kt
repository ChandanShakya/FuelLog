package com.chandanshakya.fuellog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandanshakya.fuellog.data.db.FuelEntryDao
import com.chandanshakya.fuellog.data.db.FuelPumpDao
import com.chandanshakya.fuellog.data.db.UserSettingsDao
import com.chandanshakya.fuellog.data.db.VehicleDao
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.FuelPump
import com.chandanshakya.fuellog.data.model.Vehicle
import com.chandanshakya.fuellog.util.MileageCalculator
import com.chandanshakya.fuellog.util.Validation
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject

import androidx.lifecycle.SavedStateHandle
import com.chandanshakya.fuellog.ui.navigation.NavArgs

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FuelLogViewModel @Inject constructor(
    private val fuelEntryDao: FuelEntryDao,
    private val vehicleDao: VehicleDao,
    private val userSettingsDao: UserSettingsDao,
    private val fuelPumpDao: FuelPumpDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val currentVehicleId = savedStateHandle.getStateFlow(NavArgs.VEHICLE_ID, -1L)
    private val vehicleFlow = currentVehicleId.flatMapLatest { vehicleDao.getByIdFlow(it) }
    private val settingsFlow = userSettingsDao.getSettings()

    val fuelPumps: StateFlow<List<FuelPump>> = fuelPumpDao.getAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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

    suspend fun resolveOrCreatePump(name: String): Long {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return 0
        val existing = fuelPumpDao.findByName(trimmed)
        if (existing != null) return existing.id
        return fuelPumpDao.insert(FuelPump(name = trimmed))
    }

    fun addFuelEntry(
        date: LocalDate,
        odometer: Double,
        fuelVolume: Double,
        fuelCost: Double,
        pumpName: String? = null
    ) {
        if (!Validation.validateFuelEntry(odometer, fuelVolume, fuelCost)) return

        viewModelScope.launch {
            val resolvedPumpId = if (!pumpName.isNullOrBlank()) {
                resolveOrCreatePump(pumpName)
            } else null

            val entry = FuelEntry(
                vehicleId = currentVehicleId.value,
                date = date,
                odometer = odometer,
                fuelVolume = fuelVolume,
                fuelCost = fuelCost,
                fuelPumpId = resolvedPumpId
            )
            fuelEntryDao.insert(entry)
        }
    }

    fun updateFuelEntry(
        id: Long, date: LocalDate, odometer: Double, fuelVolume: Double, fuelCost: Double,
        pumpName: String? = null
    ) {
        if (!Validation.validateFuelEntry(odometer, fuelVolume, fuelCost)) return

        viewModelScope.launch {
            val resolvedPumpId = if (!pumpName.isNullOrBlank()) {
                resolveOrCreatePump(pumpName)
            } else null

            val entry = FuelEntry(
                id = id,
                vehicleId = currentVehicleId.value,
                date = date,
                odometer = odometer,
                fuelVolume = fuelVolume,
                fuelCost = fuelCost,
                fuelPumpId = resolvedPumpId
            )
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
