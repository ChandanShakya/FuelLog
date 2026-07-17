package com.chandanshakya.fuellog.data.model

/**
 * Distance units for fuel efficiency calculations.
 */
enum class DistanceUnit {
    KM,
    MILES
}

/**
 * Volume units for fuel quantity.
 */
enum class VolumeUnit {
    LITERS,
    GALLONS
}

/**
 * Currency codes supported for display formatting.
 */
val SUPPORTED_CURRENCIES = setOf("USD", "EUR", "INR", "NPR")
