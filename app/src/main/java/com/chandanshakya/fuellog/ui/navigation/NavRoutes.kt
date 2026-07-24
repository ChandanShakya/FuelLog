package com.chandanshakya.fuellog.ui.navigation

/**
 * Navigation route constants for FuelLog.
 */
object NavRoutes {
    const val VEHICLES = "vehicles"
    const val FUEL_LOG_WITH_ARG = "log/{vehicleId}"
    const val INSIGHTS_WITH_ARG = "insights/{vehicleId}"
    const val SETTINGS = "settings"
    const val PUMP_DETAIL_WITH_ARG = "pump_detail/{vehicleId}/{pumpId}"
}

/**
 * Argument keys for navigation.
 */
object NavArgs {
    const val VEHICLE_ID = "vehicleId"
    const val PUMP_ID = "pumpId"
}

/**
 * Sentinel value for pumpId when representing the "Unknown / Not recorded" pump group.
 * Jetpack Navigation Compose cannot pass null Long args, so -1L is used as a stand-in.
 * Convert back to null at the point of use: if (pumpId == -1L) null else pumpId
 */
const val UNKNOWN_PUMP_SENTINEL = -1L
