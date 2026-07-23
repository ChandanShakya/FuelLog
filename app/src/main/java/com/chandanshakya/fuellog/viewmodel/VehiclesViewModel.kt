package com.chandanshakya.fuellog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandanshakya.fuellog.data.db.FuelEntryDao
import com.chandanshakya.fuellog.data.db.UserSettingsDao
import com.chandanshakya.fuellog.data.db.VehicleDao
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.Vehicle
import com.chandanshakya.fuellog.data.model.VehicleType
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.util.UnitConverter
import com.chandanshakya.fuellog.util.Validation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehiclesViewModel @Inject constructor(
    private val vehicleDao: VehicleDao,
    private val userSettingsDao: UserSettingsDao,
    private val fuelEntryDao: FuelEntryDao
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
    }.flowOn(Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VehiclesState()
    )

    fun addVehicle(
        name: String,
        vehicleType: VehicleType = VehicleType.CAR,
        distanceUnit: DistanceUnit? = null,
        volumeUnit: VolumeUnit? = null
    ) {
        if (!Validation.validateVehicleName(name)) return

        viewModelScope.launch {
            val settings = userSettingsDao.getSettingsSuspend()
            val vehicle = Vehicle(
                name = name,
                vehicleType = vehicleType,
                distanceUnit = distanceUnit ?: settings?.defaultDistanceUnit ?: DistanceUnit.KM,
                volumeUnit = volumeUnit ?: settings?.defaultVolumeUnit ?: VolumeUnit.LITERS
            )
            vehicleDao.insert(vehicle)
        }
    }

    fun updateVehicle(vehicle: Vehicle) {
        if (!Validation.validateVehicleName(vehicle.name)) return
        viewModelScope.launch {
            val oldVehicle = vehicleDao.getById(vehicle.id)
            if (oldVehicle != null) {
                val distanceChanged = oldVehicle.distanceUnit != vehicle.distanceUnit
                val volumeChanged = oldVehicle.volumeUnit != vehicle.volumeUnit

                if (distanceChanged || volumeChanged) {
                    val entries = fuelEntryDao.getAllByVehicleList(vehicle.id)
                    val convertedEntries = entries.map { entry ->
                        val newOdometer = if (distanceChanged) {
                            UnitConverter.convertDistance(entry.odometer, oldVehicle.distanceUnit, vehicle.distanceUnit)
                        } else entry.odometer
                        val newFuelVolume = if (volumeChanged) {
                            UnitConverter.convertVolume(entry.fuelVolume, oldVehicle.volumeUnit, vehicle.volumeUnit)
                        } else entry.fuelVolume
                        entry.copy(odometer = newOdometer, fuelVolume = newFuelVolume)
                    }
                    fuelEntryDao.updateAll(convertedEntries)
                }
            }
            vehicleDao.update(vehicle)
        }
    }

    fun deleteVehicle(id: Long) {
        viewModelScope.launch { vehicleDao.deleteById(id) }
    }
}

data class VehiclesState(
    val vehicles: List<Vehicle> = emptyList(),
    val defaultDistanceUnit: DistanceUnit = DistanceUnit.KM,
    val defaultVolumeUnit: VolumeUnit = VolumeUnit.LITERS
)
