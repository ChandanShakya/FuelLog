package com.chandanshakya.fuellog.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.chandanshakya.fuellog.ui.screens.FuelLogScreen
import com.chandanshakya.fuellog.ui.screens.InsightsScreen
import com.chandanshakya.fuellog.ui.screens.PumpDetailScreen
import com.chandanshakya.fuellog.ui.screens.SettingsScreen
import com.chandanshakya.fuellog.ui.screens.VehiclesScreen

@Composable
fun AppNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.VEHICLES,
        enterTransition = { fadeIn(tween(200)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(200)) },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(200)) },
        popExitTransition = { fadeOut(tween(200)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(200)) }
    ) {
        composable(NavRoutes.VEHICLES) {
            VehiclesScreen(
                onVehicleSelected = { vehicleId ->
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
                onNavigateToPumpDetail = { vId, pumpId ->
                    navController.navigate(
                        NavRoutes.PUMP_DETAIL_WITH_ARG
                            .replace("{vehicleId}", vId.toString())
                            .replace("{pumpId}", pumpId.toString())
                    )
                }
            )
        }

        composable(
            route = NavRoutes.PUMP_DETAIL_WITH_ARG,
            arguments = listOf(
                navArgument(NavArgs.VEHICLE_ID) {
                    type = androidx.navigation.NavType.LongType
                },
                navArgument(NavArgs.PUMP_ID) {
                    type = androidx.navigation.NavType.LongType
                }
            )
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getLong(NavArgs.VEHICLE_ID) ?: return@composable
            val pumpIdArg = backStackEntry.arguments?.getLong(NavArgs.PUMP_ID) ?: UNKNOWN_PUMP_SENTINEL
            // Convert sentinel back to nullable: -1L means "Unknown / Not recorded" pump group
            val pumpId = if (pumpIdArg == UNKNOWN_PUMP_SENTINEL) null else pumpIdArg
            PumpDetailScreen(
                vehicleId = vehicleId,
                pumpId = pumpId,
                onNavigateBack = {
                    navController.popBackStack()
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
