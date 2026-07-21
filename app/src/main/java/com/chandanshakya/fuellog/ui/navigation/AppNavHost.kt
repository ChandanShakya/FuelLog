package com.chandanshakya.fuellog.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
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

@Composable
fun AppNavHost(
    navController: NavHostController,
    onNavigateToVehicle: (Long) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.VEHICLES,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
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
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.SETTINGS) {
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
                    )
                },
                onNavigateToVehicles = {
                    if (!navController.popBackStack()) {
                        navController.navigate(NavRoutes.VEHICLES) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.SETTINGS) {
                        launchSingleTop = true
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
                    navController.popBackStack()
                },
                onNavigateToVehicles = {
                    navController.navigate(NavRoutes.VEHICLES) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.SETTINGS) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                onNavigateToVehicles = {
                    navController.popBackStack()
                }
            )
        }
    }
}

val bottomNavItems = listOf(
    BottomNavItem(route = NavRoutes.VEHICLES, label = "Vehicles"),
    BottomNavItem(route = NavRoutes.FUEL_LOG, label = "Fuel Log"),
    BottomNavItem(route = NavRoutes.INSIGHTS, label = "Insights"),
    BottomNavItem(route = NavRoutes.SETTINGS, label = "Settings")
)

data class BottomNavItem(
    val route: String,
    val label: String
)

fun NavHostController.shouldShowBottomBar(route: String): Boolean {
    val currentDestination = currentBackStackEntry?.destination
    return currentDestination?.hierarchy?.any { it.route == route } == true
}
