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
    data class NearbyPumps(val vehicleId: Long) : Screen()
    data object Settings : Screen()
}
