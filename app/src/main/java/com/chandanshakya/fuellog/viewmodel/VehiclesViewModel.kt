package com.chandanshakya.fuellog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.UserSettings
import com.chandanshakya.fuellog.data.model.Vehicle
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.data.repository.SettingsRepository
import com.chandanshakya.fuellog.data.repository.VehicleRepository
import com.chandanshakya.fuellog.util.Validation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing vehicles.
 * 
 * Handles vehicle CRUD operations and provides vehicle list state.
 */
@HiltViewModel
class VehiclesViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    /**
     * State containing all vehicles and settings for defaults.
     */
    val vehiclesState: StateFlow<VehiclesState> = combine(
        vehicleRepository.getAll(),
        settingsRepository.getSettings()
    ) { vehicles, settings ->
        VehiclesState(
            vehicles = vehicles,
            defaultCurrency = settings?.defaultCurrency ?: "USD",
            defaultDistanceUnit = settings?.defaultDistanceUnit ?: DistanceUnit.KM,
            defaultVolumeUnit = settings?.defaultVolumeUnit ?: VolumeUnit.LITERS
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VehiclesState()
    )

    /**
     * Add a new vehicle.
     * 
     * @param name Vehicle name
     * @param currency Currency code (optional, uses settings default)
     * @param distanceUnit Distance unit (optional, uses settings default)
     * @param volumeUnit Volume unit (optional, uses settings default)
     */
    fun addVehicle(
        name: String,
        currency: String? = null,
        distanceUnit: DistanceUnit? = null,
        volumeUnit: VolumeUnit? = null
    ) {
        if (!Validation.validateVehicleName(name)) {
            return
        }
        
        viewModelScope.launch {
            val settings = settingsRepository.getSettingsSuspend()
            val vehicle = Vehicle(
                name = name,
                defaultCurrency = currency ?: settings?.defaultCurrency ?: "USD",
                distanceUnit = distanceUnit ?: settings?.defaultDistanceUnit ?: DistanceUnit.KM,
                volumeUnit = volumeUnit ?: settings?.defaultVolumeUnit ?: VolumeUnit.LITERS
            )
            vehicleRepository.insert(vehicle)
        }
    }

    /**
     * Update an existing vehicle.
     */
    fun updateVehicle(vehicle: Vehicle) {
        if (!Validation.validateVehicleName(vehicle.name)) {
            return
        }
        viewModelScope.launch {
            vehicleRepository.update(vehicle)
        }
    }

    /**
     * Delete a vehicle by ID.
     */
    fun deleteVehicle(id: Long) {
        viewModelScope.launch {
            vehicleRepository.deleteById(id)
        }
    }

    /**
     * Get a vehicle by ID.
     */
    suspend fun getVehicleById(id: Long): Vehicle? = vehicleRepository.getById(id)
}

/**
 * State for vehicles screen.
 */
data class VehiclesState(
    val vehicles: List<Vehicle> = emptyList(),
    val defaultCurrency: String = "USD",
    val defaultDistanceUnit: DistanceUnit = DistanceUnit.KM,
    val defaultVolumeUnit: VolumeUnit = VolumeUnit.LITERS,
    val isLoading: Boolean = false,
    val error: String? = null
)
