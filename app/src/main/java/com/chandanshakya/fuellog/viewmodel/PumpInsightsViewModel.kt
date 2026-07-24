package com.chandanshakya.fuellog.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandanshakya.fuellog.data.db.FuelEntryDao
import com.chandanshakya.fuellog.data.db.UserSettingsDao
import com.chandanshakya.fuellog.data.db.VehicleDao
import com.chandanshakya.fuellog.ui.navigation.NavArgs
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.util.PumpFillDetail
import com.chandanshakya.fuellog.util.PumpMileageStat
import com.chandanshakya.fuellog.util.computePumpFillHistory
import com.chandanshakya.fuellog.util.computePumpMileageStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PumpInsightsViewModel @Inject constructor(
    private val fuelEntryDao: FuelEntryDao,
    private val vehicleDao: VehicleDao,
    private val userSettingsDao: UserSettingsDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val currentVehicleId = savedStateHandle.getStateFlow(NavArgs.VEHICLE_ID, -1L)
    private val vehicleFlow = currentVehicleId.flatMapLatest { vehicleDao.getByIdFlow(it) }

    private val vehicleState = vehicleFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val entriesWithPump = currentVehicleId
        .flatMapLatest { fuelEntryDao.getAllByVehicleWithPump(it) }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pumpStats: StateFlow<List<PumpMileageStat>> = combine(
        entriesWithPump,
        vehicleState
    ) { entries, vehicle ->
        val distanceUnit = vehicle?.distanceUnit ?: com.chandanshakya.fuellog.data.model.DistanceUnit.KM
        val volumeUnit = vehicle?.volumeUnit ?: com.chandanshakya.fuellog.data.model.VolumeUnit.LITERS
        computePumpMileageStats(entries, distanceUnit, volumeUnit)
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val distanceUnit: StateFlow<DistanceUnit> = vehicleState.map { it?.distanceUnit ?: DistanceUnit.KM }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = DistanceUnit.KM)

    val volumeUnit: StateFlow<VolumeUnit> = vehicleState.map { it?.volumeUnit ?: VolumeUnit.LITERS }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = VolumeUnit.LITERS)

    val currency: StateFlow<String> = userSettingsDao.getSettings()
        .map { it?.defaultCurrency ?: "USD" }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "USD"
        )

    fun getPumpDetail(pumpId: Long?): List<PumpFillDetail> {
        val vehicle = vehicleState.value
        val distanceUnit = vehicle?.distanceUnit ?: com.chandanshakya.fuellog.data.model.DistanceUnit.KM
        val volumeUnit = vehicle?.volumeUnit ?: com.chandanshakya.fuellog.data.model.VolumeUnit.LITERS
        return computePumpFillHistory(entriesWithPump.value, pumpId, distanceUnit, volumeUnit)
    }

    fun getAllEntriesForPump(pumpId: Long?): StateFlow<List<FuelEntry>> {
        return if (pumpId != null) {
            fuelEntryDao.getAllByPumpId(pumpId)
        } else {
            currentVehicleId.flatMapLatest { fuelEntryDao.getAllByVehicleWithNullPump(it) }
        }.flowOn(Dispatchers.Default).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }
}
