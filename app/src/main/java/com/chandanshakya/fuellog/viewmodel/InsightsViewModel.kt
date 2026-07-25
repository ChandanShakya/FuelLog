package com.chandanshakya.fuellog.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.chandanshakya.fuellog.FuelLogApplication
import com.chandanshakya.fuellog.data.db.FuelEntryDao
import com.chandanshakya.fuellog.data.db.UserSettingsDao
import com.chandanshakya.fuellog.data.db.VehicleDao
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.Vehicle
import com.chandanshakya.fuellog.util.CapacitySuggestion
import com.chandanshakya.fuellog.util.adjacentMileagePairs
import com.chandanshakya.fuellog.util.MileageCalculator
import com.chandanshakya.fuellog.util.computeRecencyWeightedMileage
import com.chandanshakya.fuellog.util.observeCapacitySuggestion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class InsightsViewModel(
    private val fuelEntryDao: FuelEntryDao,
    private val vehicleDao: VehicleDao,
    private val userSettingsDao: UserSettingsDao,
    vehicleId: Long
) : ViewModel() {

    companion object {
        fun factory(vehicleId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as FuelLogApplication
                InsightsViewModel(app.container.fuelEntryDao, app.container.vehicleDao, app.container.userSettingsDao, vehicleId)
            }
        }
    }

    private val currentVehicleId = MutableStateFlow(vehicleId)
    private val vehicleFlow = currentVehicleId.flatMapLatest { vehicleDao.getByIdFlow(it) }
    private val settingsFlow = userSettingsDao.getSettings().distinctUntilChanged()

    private val allEntries: StateFlow<List<FuelEntry>> = currentVehicleId
        .flatMapLatest { fuelEntryDao.getAllByVehicle(it) }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val insightsState: StateFlow<InsightsState> = combine(
        allEntries,
        vehicleFlow,
        settingsFlow
    ) { entries, v, settings ->
        val sortedEntries = entries.sortedBy { it.odometer }
        val distanceUnit = v?.distanceUnit ?: com.chandanshakya.fuellog.data.model.DistanceUnit.KM
        val volumeUnit = v?.volumeUnit ?: com.chandanshakya.fuellog.data.model.VolumeUnit.LITERS

        val pairs = sortedEntries.adjacentMileagePairs({ it.odometer }, { it.fuelVolume }, distanceUnit, volumeUnit)
        val mileages = pairs.map { it.mileage }
        val dataPoints = pairs.mapIndexed { index, pair ->
            ChartDataPoint(
                odometer = sortedEntries[index + 1].odometer,
                mileage = pair.mileage,
                date = sortedEntries[index + 1].date
            )
        }

        val priceDataPoints = sortedEntries.map { entry ->
            PriceChartDataPoint(
                pricePerUnit = if (entry.fuelVolume > 0) entry.fuelCost / entry.fuelVolume else 0.0,
                date = entry.date
            )
        }

        val averageMileage = mileages.average().takeIf { mileages.isNotEmpty() }
        val bestMileage = mileages.maxOrNull()
        val worstMileage = mileages.minOrNull()

        val totalDistanceRaw = if (sortedEntries.isNotEmpty()) {
            sortedEntries.last().odometer - sortedEntries.first().odometer
        } else 0.0

        val totalFuelRaw = sortedEntries.sumOf { it.fuelVolume }
        val totalCost = sortedEntries.sumOf { it.fuelCost }

        InsightsState(
            vehicle = v,
            entries = sortedEntries,
            averageMileageKmPerLiter = averageMileage,
            bestMileageKmPerLiter = bestMileage,
            worstMileageKmPerLiter = worstMileage,
            totalDistanceKm = totalDistanceRaw,
            totalFuelLiters = totalFuelRaw,
            totalCost = totalCost,
            costPerKm = if (totalDistanceRaw > 0) totalCost / totalDistanceRaw else null,
            mileageTrend = calculateTrend(mileages),
            entriesCount = sortedEntries.size,
            mileageDataPoints = dataPoints,
            priceDataPoints = priceDataPoints,
            currency = settings?.defaultCurrency ?: "USD"
        )
    }.flowOn(Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InsightsState()
    )

    private fun calculateTrend(mileages: List<Double>): MileageTrend {
        if (mileages.size < 3) return MileageTrend.STABLE

        val splitIndex = mileages.size / 3
        val firstThird = mileages.take(splitIndex).average()
        val lastThird = mileages.takeLast(splitIndex).average()

        val difference = lastThird - firstThird

        return when {
            difference > 0.5 -> MileageTrend.IMPROVING
            difference < -0.5 -> MileageTrend.DECLINING
            else -> MileageTrend.STABLE
        }
    }

    val capacitySuggestion: StateFlow<CapacitySuggestion?> = observeCapacitySuggestion(
        entriesFlow = allEntries,
        vehicleFlow = vehicleFlow
    ).flowOn(Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val recentMileage: StateFlow<Double?> = combine(
        allEntries,
        vehicleFlow
    ) { entries, vehicle ->
        val recentEntries = entries.filter { it.fuelVolume > 0 }.sortedBy { it.odometer }
        val distanceUnit = vehicle?.distanceUnit ?: com.chandanshakya.fuellog.data.model.DistanceUnit.KM
        val volumeUnit = vehicle?.volumeUnit ?: com.chandanshakya.fuellog.data.model.VolumeUnit.LITERS
        computeRecencyWeightedMileage(recentEntries, 3, distanceUnit, volumeUnit)
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun applySuggestedCapacity(capacity: Double) {
        viewModelScope.launch {
            val vehicle = vehicleDao.getById(currentVehicleId.value) ?: return@launch
            vehicleDao.update(vehicle.copy(tankCapacity = capacity))
        }
    }
}

data class InsightsState(
    val vehicle: Vehicle? = null,
    val entries: List<FuelEntry> = emptyList(),
    val averageMileageKmPerLiter: Double? = null,
    val bestMileageKmPerLiter: Double? = null,
    val worstMileageKmPerLiter: Double? = null,
    val totalDistanceKm: Double = 0.0,
    val totalFuelLiters: Double = 0.0,
    val totalCost: Double = 0.0,
    val costPerKm: Double? = null,
    val mileageTrend: MileageTrend = MileageTrend.STABLE,
    val entriesCount: Int = 0,
    val mileageDataPoints: List<ChartDataPoint> = emptyList(),
    val priceDataPoints: List<PriceChartDataPoint> = emptyList(),
    val currency: String = "USD"
)

enum class MileageTrend {
    IMPROVING,
    DECLINING,
    STABLE
}

data class ChartDataPoint(
    val odometer: Double,
    val mileage: Double,
    val date: java.time.LocalDate
)

data class PriceChartDataPoint(
    val pricePerUnit: Double,
    val date: java.time.LocalDate
)
