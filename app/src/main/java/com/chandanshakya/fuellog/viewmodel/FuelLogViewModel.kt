package com.chandanshakya.fuellog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandanshakya.fuellog.data.db.FuelEntryDao
import com.chandanshakya.fuellog.data.db.FuelPumpDao
import com.chandanshakya.fuellog.data.db.OdometerReadingDao
import com.chandanshakya.fuellog.data.db.UserSettingsDao
import com.chandanshakya.fuellog.data.db.VehicleDao
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.FuelPump
import com.chandanshakya.fuellog.data.model.OdometerReading
import com.chandanshakya.fuellog.data.model.Vehicle
import com.chandanshakya.fuellog.util.CapacitySuggestion
import com.chandanshakya.fuellog.util.FillUpPrediction
import com.chandanshakya.fuellog.util.MileageCalculator
import com.chandanshakya.fuellog.util.Validation
import com.chandanshakya.fuellog.util.predictNextFillUp
import com.chandanshakya.fuellog.util.shouldSuggestUpdate
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
    private val odometerReadingDao: OdometerReadingDao,
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

    val odometerReadings: StateFlow<List<OdometerReading>> = currentVehicleId
        .flatMapLatest { odometerReadingDao.getByVehicle(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val fuelLogState: StateFlow<FuelLogState> = combine(
        currentVehicleId.flatMapLatest { fuelEntryDao.getAllByVehicleWithPump(it) },
        vehicleFlow,
        settingsFlow
    ) { entriesWithPump, v, settings ->
        val sortedEntries = entriesWithPump.sortedBy { it.entry.odometer }

        val entriesWithMileage = sortedEntries.mapIndexed { index, entryWithPump ->
            val next = if (index < sortedEntries.size - 1) sortedEntries[index + 1] else null
            val mileage = if (next != null && v != null && entryWithPump.entry.fuelVolume > 0) {
                val distance = next.entry.odometer - entryWithPump.entry.odometer
                if (distance > 0) {
                    when (v.distanceUnit) {
                        com.chandanshakya.fuellog.data.model.DistanceUnit.KM -> distance / entryWithPump.entry.fuelVolume
                        com.chandanshakya.fuellog.data.model.DistanceUnit.MILES -> distance / entryWithPump.entry.fuelVolume
                    }
                } else null
            } else null
            EntryWithMileage(entry = entryWithPump.entry, mileage = mileage, pumpName = entryWithPump.pumpName)
        }.reversed()

        val rawEntries = sortedEntries.map { it.entry }

        FuelLogState(
            vehicle = v,
            entries = entriesWithMileage,
            averageMileage = if (v != null) MileageCalculator.calculateAverageMileage(rawEntries, v.distanceUnit, v.volumeUnit) else null,
            totalDistance = if (v != null) MileageCalculator.calculateTotalDistance(rawEntries, v.distanceUnit) else 0.0,
            totalFuel = if (v != null) MileageCalculator.calculateTotalFuel(rawEntries, v.volumeUnit) else 0.0,
            totalCost = MileageCalculator.calculateTotalCost(rawEntries),
            currency = settings?.defaultCurrency ?: "USD"
        )
    }.flowOn(Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FuelLogState()
    )

    val nextFillUpPrediction: StateFlow<FillUpPrediction?> = combine(
        currentVehicleId.flatMapLatest { fuelEntryDao.getAllByVehicle(it) },
        vehicleFlow,
        odometerReadings
    ) { entries, vehicle, readings ->
        if (vehicle == null) return@combine null
        predictNextFillUp(
            entries = entries,
            odometerReadings = readings,
            tankCapacity = vehicle.tankCapacity,
            distanceUnit = vehicle.distanceUnit,
            volumeUnit = vehicle.volumeUnit
        )
    }.flowOn(Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val capacitySuggestion: StateFlow<CapacitySuggestion?> = combine(
        currentVehicleId.flatMapLatest { fuelEntryDao.getAllByVehicle(it) },
        vehicleFlow
    ) { entries, vehicle ->
        val fullTankEntries = entries.filter { it.isFullTank }.sortedBy { it.odometer }
        shouldSuggestUpdate(vehicle?.tankCapacity, fullTankEntries)
    }.flowOn(Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
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
        pumpName: String? = null,
        isFullTank: Boolean = false
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
                fuelPumpId = resolvedPumpId,
                isFullTank = isFullTank
            )
            fuelEntryDao.insert(entry)
        }
    }

    fun updateFuelEntry(
        id: Long, date: LocalDate, odometer: Double, fuelVolume: Double, fuelCost: Double,
        pumpName: String? = null, isFullTank: Boolean = false
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
                fuelPumpId = resolvedPumpId,
                isFullTank = isFullTank
            )
            fuelEntryDao.update(entry)
        }
    }

    fun deleteFuelEntry(id: Long) {
        viewModelScope.launch { fuelEntryDao.deleteById(id) }
    }

    fun addOdometerReading(date: LocalDate, odometer: Double) {
        if (odometer < 0) return
        viewModelScope.launch {
            odometerReadingDao.insert(
                OdometerReading(
                    vehicleId = currentVehicleId.value,
                    date = date,
                    odometer = odometer
                )
            )
        }
    }

    fun applySuggestedCapacity(capacity: Double) {
        viewModelScope.launch {
            val vehicle = vehicleDao.getById(currentVehicleId.value) ?: return@launch
            vehicleDao.update(vehicle.copy(tankCapacity = capacity))
        }
    }

    fun updatePump(pump: FuelPump) {
        viewModelScope.launch { fuelPumpDao.update(pump) }
    }

    fun deletePump(id: Long) {
        viewModelScope.launch { fuelPumpDao.deleteById(id) }
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
    val mileage: Double?,
    val pumpName: String? = null
)
