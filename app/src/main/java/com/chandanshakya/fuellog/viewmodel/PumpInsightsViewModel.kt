package com.chandanshakya.fuellog.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandanshakya.fuellog.data.db.FuelEntryDao
import com.chandanshakya.fuellog.data.db.UserSettingsDao
import com.chandanshakya.fuellog.ui.navigation.NavArgs
import com.chandanshakya.fuellog.util.PumpFillDetail
import com.chandanshakya.fuellog.util.PumpMileageStat
import com.chandanshakya.fuellog.util.computePumpFillHistory
import com.chandanshakya.fuellog.util.computePumpMileageStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    private val userSettingsDao: UserSettingsDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val currentVehicleId = savedStateHandle.getStateFlow(NavArgs.VEHICLE_ID, -1L)

    private val entriesWithPump = currentVehicleId
        .flatMapLatest { fuelEntryDao.getAllByVehicleWithPump(it) }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pumpStats: StateFlow<List<PumpMileageStat>> = entriesWithPump
        .map { computePumpMileageStats(it) }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currency: StateFlow<String> = userSettingsDao.getSettings()
        .map { it?.defaultCurrency ?: "USD" }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "USD"
        )

    fun getPumpDetail(pumpId: Long?): List<PumpFillDetail> {
        return computePumpFillHistory(entriesWithPump.value, pumpId)
    }
}
