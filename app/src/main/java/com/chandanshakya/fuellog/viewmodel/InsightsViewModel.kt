package com.chandanshakya.fuellog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandanshakya.fuellog.data.db.FuelEntryDao
import com.chandanshakya.fuellog.data.db.UserSettingsDao
import com.chandanshakya.fuellog.data.db.VehicleDao
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.Vehicle
import com.chandanshakya.fuellog.util.MileageCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

import androidx.lifecycle.SavedStateHandle
import com.chandanshakya.fuellog.ui.navigation.NavArgs

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val fuelEntryDao: FuelEntryDao,
    private val vehicleDao: VehicleDao,
    private val userSettingsDao: UserSettingsDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val currentVehicleId = MutableStateFlow(savedStateHandle.get<Long>(NavArgs.VEHICLE_ID) ?: -1L)
    private val vehicleFlow = currentVehicleId.flatMapLatest { vehicleDao.getByIdFlow(it) }
    private val settingsFlow = userSettingsDao.getSettings()

    val insightsState: StateFlow<InsightsState> = combine(
        currentVehicleId.flatMapLatest { fuelEntryDao.getAllByVehicle(it) },
        vehicleFlow,
        settingsFlow
    ) { entries, v, settings ->
        val sortedEntries = entries.sortedBy { it.odometer }

        val mileages = mutableListOf<Double>()
        val dataPoints = mutableListOf<ChartDataPoint>()
        for (i in 1 until sortedEntries.size) {
            val mileage = MileageCalculator.calculateMileageBase(
                sortedEntries[i],
                sortedEntries[i - 1]
            )
            mileage?.let {
                mileages.add(it)
                dataPoints.add(
                    ChartDataPoint(
                        odometer = sortedEntries[i].odometer,
                        mileage = it,
                        date = sortedEntries[i].date
                    )
                )
            }
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

        val totalDistanceKm = if (sortedEntries.isNotEmpty()) {
            sortedEntries.last().odometer - sortedEntries.first().odometer
        } else 0.0

        val totalFuelLiters = sortedEntries.sumOf { it.fuelVolume }
        val totalCost = sortedEntries.sumOf { it.fuelCost }

        val costPerKm = if (totalDistanceKm > 0) totalCost / totalDistanceKm else null

        InsightsState(
            vehicle = v,
            entries = sortedEntries,
            averageMileageKmPerLiter = averageMileage,
            bestMileageKmPerLiter = bestMileage,
            worstMileageKmPerLiter = worstMileage,
            totalDistanceKm = totalDistanceKm,
            totalFuelLiters = totalFuelLiters,
            totalCost = totalCost,
            costPerKm = costPerKm,
            mileageTrend = calculateTrend(mileages),
            entriesCount = sortedEntries.size,
            mileageDataPoints = dataPoints,
            priceDataPoints = priceDataPoints,
            currency = settings?.defaultCurrency ?: "USD"
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InsightsState()
    )

    fun setVehicleId(vehicleId: Long) {
        currentVehicleId.value = vehicleId
    }

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
