package com.chandanshakya.fuellog.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.chandanshakya.fuellog.ui.screens.FuelLogScreen
import com.chandanshakya.fuellog.ui.screens.InsightsScreen
import com.chandanshakya.fuellog.ui.screens.SettingsScreen
import com.chandanshakya.fuellog.ui.screens.VehiclesScreen

/**
 * Main navigation host for FuelLog.
 * 
 * @param navController Navigation controller from rememberNavController()
 * @param onNavigateToVehicle Callback for vehicle selection
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    onNavigateToVehicle: (Long) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.VEHICLES
    ) {
        composable(NavRoutes.VEHICLES) {
            VehiclesScreen(
                onVehicleSelected = { vehicleId ->
                    onNavigateToVehicle(vehicleId)
                    navController.navigate(NavRoutes.FUEL_LOG_WITH_ARG.replace(
                        "{vehicleId}",
                        vehicleId.toString()
                    )) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onAddVehicle = {
                    navController.navigate(NavRoutes.VEHICLES) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = NavRoutes.FUEL_LOG_WITH_ARG,
            arguments = listOf(
                navArgument(NavArgs.VEHICLE_ID) { 
                    type = androidx.navigation.NavType.LongType
                }
            )
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getLong(NavArgs.VEHICLE_ID) ?: return@composable
            FuelLogScreen(
                vehicleId = vehicleId,
                onNavigateToInsights = {
                    navController.navigate(
                        NavRoutes.INSIGHTS_WITH_ARG.replace(
                            "{vehicleId}",
                            vehicleId.toString()
                        )
                    ) {
                        popUpTo(NavRoutes.FUEL_LOG_WITH_ARG.replace(
                            "{vehicleId}",
                            vehicleId.toString()
                        )) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToVehicles = {
                    navController.navigate(NavRoutes.VEHICLES) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(
            route = NavRoutes.INSIGHTS_WITH_ARG,
            arguments = listOf(
                navArgument(NavArgs.VEHICLE_ID) { 
                    type = androidx.navigation.NavType.LongType
                }
            )
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getLong(NavArgs.VEHICLE_ID) ?: return@composable
            InsightsScreen(
                vehicleId = vehicleId,
                onNavigateToLog = {
                    navController.navigate(
                        NavRoutes.FUEL_LOG_WITH_ARG.replace(
                            "{vehicleId}",
                            vehicleId.toString()
                        )
                    ) {
                        popUpTo(NavRoutes.INSIGHTS_WITH_ARG.replace(
                            "{vehicleId}",
                            vehicleId.toString()
                        )) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToVehicles = {
                    navController.navigate(NavRoutes.VEHICLES) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                onNavigateToVehicles = {
                    navController.navigate(NavRoutes.VEHICLES) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

/**
 * Get the list of bottom navigation items.
 */
val bottomNavItems = listOf(
    BottomNavItem(
        route = NavRoutes.VEHICLES,
        label = "Vehicles"
    ),
    BottomNavItem(
        route = NavRoutes.FUEL_LOG,
        label = "Fuel Log"
    ),
    BottomNavItem(
        route = NavRoutes.INSIGHTS,
        label = "Insights"
    ),
    BottomNavItem(
        route = NavRoutes.SETTINGS,
        label = "Settings"
    )
)

/**
 * Bottom navigation item data.
 */
data class BottomNavItem(
    val route: String,
    val label: String
)

/**
 * Check if a route matches the current destination.
 */
fun NavHostController.shouldShowBottomBar(route: String): Boolean {
    val currentDestination = currentBackStackEntry?.destination
    return currentDestination?.hierarchy?.any { it.route == route } == true
}
