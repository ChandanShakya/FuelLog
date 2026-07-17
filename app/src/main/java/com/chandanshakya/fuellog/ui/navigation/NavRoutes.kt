package com.chandanshakya.fuellog.ui.navigation

/**
 * Navigation route constants for FuelLog.
 */
object NavRoutes {
    const val VEHICLES = "vehicles"
    const val FUEL_LOG = "log"
    const val FUEL_LOG_WITH_ARG = "log/{vehicleId}"
    const val INSIGHTS = "insights"
    const val INSIGHTS_WITH_ARG = "insights/{vehicleId}"
    const val SETTINGS = "settings"
}

/**
 * Argument keys for navigation.
 */
object NavArgs {
    const val VEHICLE_ID = "vehicleId"
}
