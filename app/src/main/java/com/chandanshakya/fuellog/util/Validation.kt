package com.chandanshakya.fuellog.util

/**
 * Validation utilities for FuelLog.
 * 
 * Pure functions for validating input data, independent of Android framework.
 */
object Validation {
    /**
     * Validate vehicle name.
     * 
     * @param name The vehicle name to validate
     * @return true if valid, false otherwise
     */
    fun validateVehicleName(name: String): Boolean = name.isNotBlank() && name.length <= 100

    /**
     * Validate fuel entry odometer reading.
     * 
     * @param odometer The odometer value to validate
     * @return true if valid, false otherwise
     */
    fun validateOdometer(odometer: Double): Boolean = odometer >= 0 && odometer <= 9999999.99

    /**
     * Validate fuel volume.
     * 
     * @param volume The fuel volume to validate
     * @return true if valid, false otherwise
     */
    fun validateFuelVolume(volume: Double): Boolean = volume > 0 && volume <= 9999.99

    /**
     * Validate fuel cost.
     * 
     * @param cost The fuel cost to validate
     * @return true if valid, false otherwise
     */
    fun validateFuelCost(cost: Double): Boolean = cost >= 0 && cost <= 999999.99

    /**
     * Validate currency code.
     * 
     * @param code The currency code to validate
     * @return true if valid ISO 4217 code, false otherwise
     */
    fun validateCurrencyCode(code: String): Boolean = code.length == 3 && code.all { it.isLetter() }

    /**
     * Validate notes field.
     * 
     * @param notes The notes to validate
     * @return true if valid, false otherwise
     */
    fun validateNotes(notes: String?): Boolean = notes == null || notes.length <= 500

    /**
     * Validate a complete fuel entry.
     * 
     * @param odometer Odometer reading
     * @param fuelVolume Fuel volume added
     * @param fuelCost Total cost
     * @return true if all fields are valid, false otherwise
     */
    fun validateFuelEntry(
        odometer: Double,
        fuelVolume: Double,
        fuelCost: Double
    ): Boolean = validateOdometer(odometer) && 
            validateFuelVolume(fuelVolume) && 
            validateFuelCost(fuelCost)

    /**
     * Get validation error message for vehicle name.
     */
    fun getVehicleNameError(name: String): String? = when {
        name.isBlank() -> "Vehicle name cannot be empty"
        name.length > 100 -> "Vehicle name must be 100 characters or less"
        else -> null
    }

    /**
     * Get validation error message for odometer.
     */
    fun getOdometerError(odometer: Double): String? = when {
        odometer < 0 -> "Odometer cannot be negative"
        odometer > 9999999.99 -> "Odometer value too large"
        else -> null
    }

    /**
     * Get validation error message for fuel volume.
     */
    fun getFuelVolumeError(volume: Double): String? = when {
        volume <= 0 -> "Fuel volume must be greater than 0"
        volume > 9999.99 -> "Fuel volume too large"
        else -> null
    }

    /**
     * Get validation error message for fuel cost.
     */
    fun getFuelCostError(cost: Double): String? = when {
        cost < 0 -> "Cost cannot be negative"
        cost > 999999.99 -> "Cost value too large"
        else -> null
    }

    /**
     * Get validation error message for currency code.
     */
    fun getCurrencyCodeError(code: String): String? = when {
        code.isBlank() -> "Currency code cannot be empty"
        code.length != 3 -> "Currency code must be 3 characters"
        !code.all { it.isLetter() } -> "Currency code must contain only letters"
        !code.uppercase() in setOf("USD", "EUR", "INR", "NPR") -> "Currency must be USD, EUR, INR, or NPR"
        else -> null
    }
}
