package com.chandanshakya.fuellog.util

object Validation {
    fun validateVehicleName(name: String): Boolean = name.isNotBlank() && name.length <= 100
    fun validateOdometer(odometer: Double): Boolean = odometer >= 0 && odometer <= 9999999.99
    fun validateFuelVolume(volume: Double): Boolean = volume > 0 && volume <= 9999.99
    fun validateFuelCost(cost: Double): Boolean = cost >= 0 && cost <= 999999.99
    fun validateCurrencyCode(code: String): Boolean = code.length == 3 && code.all { it.isLetter() }
    fun validateNotes(notes: String?): Boolean = notes == null || notes.length <= 500

    fun validateFuelEntry(
        odometer: Double,
        fuelVolume: Double,
        fuelCost: Double
    ): Boolean = validateOdometer(odometer) &&
            validateFuelVolume(fuelVolume) &&
            validateFuelCost(fuelCost)

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
}

private val SUPPORTED_CURRENCIES = setOf("USD", "EUR", "INR", "NPR")
