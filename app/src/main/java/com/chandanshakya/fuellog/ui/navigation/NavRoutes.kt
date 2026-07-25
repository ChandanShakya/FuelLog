package com.chandanshakya.fuellog.ui.navigation

/**
 * Sealed class representing all navigation destinations.
 * Each screen is a data class with its arguments.
 */
sealed class Screen {
    data object Vehicles : Screen()
    data class FuelLog(val vehicleId: Long) : Screen()
    data class Insights(val vehicleId: Long) : Screen()
    data class OdometerLogs(val vehicleId: Long) : Screen()
    data class PumpDetail(val vehicleId: Long, val pumpId: Long?) : Screen()
    data object Settings : Screen()
}

/**
 * Sentinel value for pumpId when representing the "Unknown / Not recorded" pump group.
 * Nullable types handle this natively now, but kept for backward compatibility.
 */
const val UNKNOWN_PUMP_SENTINEL = -1L
