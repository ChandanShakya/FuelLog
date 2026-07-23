package com.chandanshakya.fuellog.util

import com.chandanshakya.fuellog.data.model.SUPPORTED_CURRENCIES

object Validation {
    fun getVehicleNameError(name: String): String? = when {
        name.isBlank() -> "Vehicle name cannot be empty"
        name.length > 100 -> "Vehicle name must be 100 characters or less"
        else -> null
    }

    fun getOdometerError(odometer: Double): String? = when {
        odometer < 0 -> "Odometer cannot be negative"
        odometer > 9999999.99 -> "Odometer value too large"
        else -> null
    }

    fun getFuelVolumeError(volume: Double): String? = when {
        volume <= 0 -> "Fuel volume must be greater than 0"
        volume > 9999.99 -> "Fuel volume too large"
        else -> null
    }

    fun getFuelCostError(cost: Double): String? = when {
        cost < 0 -> "Cost cannot be negative"
        cost > 999999.99 -> "Cost value too large"
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
    fun validateOdometer(odometer: Double): Boolean = getOdometerError(odometer) == null
    fun validateFuelVolume(volume: Double): Boolean = getFuelVolumeError(volume) == null
    fun validateFuelCost(cost: Double): Boolean = getFuelCostError(cost) == null
    fun validateCurrencyCode(code: String): Boolean = getCurrencyCodeError(code) == null

    fun validateFuelEntry(
        odometer: Double,
        fuelVolume: Double,
        fuelCost: Double
    ): Boolean = validateOdometer(odometer) &&
            validateFuelVolume(fuelVolume) &&
            validateFuelCost(fuelCost)
}
