package com.chandanshakya.fuellog.util

import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.SUPPORTED_CURRENCIES
import com.chandanshakya.fuellog.data.model.VolumeUnit

object Validation {
    private const val MAX_ODOMETER_KM = 9999999.99
    private const val MAX_FUEL_VOLUME_LITERS = 9999.99
    private const val MAX_FUEL_COST = 999999.99

    fun getVehicleNameError(name: String): String? = when {
        name.isBlank() -> "Vehicle name cannot be empty"
        name.length > 100 -> "Vehicle name must be 100 characters or less"
        else -> null
    }

    fun getOdometerError(
        odometer: Double,
        distanceUnit: DistanceUnit = DistanceUnit.KM
    ): String? {
        if (odometer < 0) return "Odometer cannot be negative"
        val maxOdometer = UnitConverter.fromKilometers(MAX_ODOMETER_KM, distanceUnit)
        if (odometer > maxOdometer) return "Odometer value too large"
        return null
    }

    fun getFuelVolumeError(
        volume: Double,
        volumeUnit: VolumeUnit = VolumeUnit.LITERS
    ): String? {
        if (volume <= 0) return "Fuel volume must be greater than 0"
        val maxVolume = UnitConverter.fromLiters(MAX_FUEL_VOLUME_LITERS, volumeUnit)
        if (volume > maxVolume) return "Fuel volume too large"
        return null
    }

    fun getFuelCostError(cost: Double): String? = when {
        cost < 0 -> "Cost cannot be negative"
        cost > MAX_FUEL_COST -> "Cost value too large"
        else -> null
    }

    fun getCurrencyCodeError(code: String): String? = when {
        code.isBlank() -> "Currency code cannot be empty"
        code.length != 3 -> "Currency code must be 3 characters"
        !code.all { it.isLetter() } -> "Currency code must contain only letters"
        code.uppercase() !in SUPPORTED_CURRENCIES -> "Currency must be USD, EUR, INR, or NPR"
        else -> null
    }

    fun validateVehicleName(name: String): Boolean = getVehicleNameError(name) == null
    fun validateOdometer(
        odometer: Double,
        distanceUnit: DistanceUnit = DistanceUnit.KM
    ): Boolean = getOdometerError(odometer, distanceUnit) == null
    fun validateFuelVolume(
        volume: Double,
        volumeUnit: VolumeUnit = VolumeUnit.LITERS
    ): Boolean = getFuelVolumeError(volume, volumeUnit) == null
    fun validateFuelCost(cost: Double): Boolean = getFuelCostError(cost) == null
    fun validateCurrencyCode(code: String): Boolean = getCurrencyCodeError(code) == null

    fun validateFuelEntry(
        odometer: Double,
        fuelVolume: Double,
        fuelCost: Double,
        distanceUnit: DistanceUnit = DistanceUnit.KM,
        volumeUnit: VolumeUnit = VolumeUnit.LITERS
    ): Boolean = validateOdometer(odometer, distanceUnit) &&
            validateFuelVolume(fuelVolume, volumeUnit) &&
            validateFuelCost(fuelCost)
}
