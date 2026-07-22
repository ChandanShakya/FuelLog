package com.chandanshakya.fuellog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandanshakya.fuellog.data.db.UserSettingsDao
import com.chandanshakya.fuellog.data.db.VehicleDao
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.Vehicle
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.util.Validation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehiclesViewModel @Inject constructor(
    private val vehicleDao: VehicleDao,
    private val userSettingsDao: UserSettingsDao
) : ViewModel() {

    val vehiclesState: StateFlow<VehiclesState> = combine(
        vehicleDao.getAll(),
        userSettingsDao.getSettings()
    ) { vehicles, settings ->
        VehiclesState(
            vehicles = vehicles,
            defaultDistanceUnit = settings?.defaultDistanceUnit ?: DistanceUnit.KM,
            defaultVolumeUnit = settings?.defaultVolumeUnit ?: VolumeUnit.LITERS
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VehiclesState()
    )

    fun addVehicle(
        name: String,
        distanceUnit: DistanceUnit? = null,
        volumeUnit: VolumeUnit? = null
    ) {
        if (!Validation.validateVehicleName(name)) return

        viewModelScope.launch {
            val settings = userSettingsDao.getSettingsSuspend()
            val vehicle = Vehicle(
                name = name,
                distanceUnit = distanceUnit ?: settings?.defaultDistanceUnit ?: DistanceUnit.KM,
                volumeUnit = volumeUnit ?: settings?.defaultVolumeUnit ?: VolumeUnit.LITERS
            )
            vehicleDao.insert(vehicle)
        }
    }

    fun updateVehicle(vehicle: Vehicle) {
        if (!Validation.validateVehicleName(vehicle.name)) return
        viewModelScope.launch { vehicleDao.update(vehicle) }
    }

    fun deleteVehicle(id: Long) {
        viewModelScope.launch { vehicleDao.deleteById(id) }
    }

    suspend fun getVehicleById(id: Long): Vehicle? = vehicleDao.getById(id)
}

data class VehiclesState(
    val vehicles: List<Vehicle> = emptyList(),
    val defaultDistanceUnit: DistanceUnit = DistanceUnit.KM,
    val defaultVolumeUnit: VolumeUnit = VolumeUnit.LITERS
)
