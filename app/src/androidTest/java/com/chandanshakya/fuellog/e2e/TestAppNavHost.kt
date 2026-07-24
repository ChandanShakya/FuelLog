package com.chandanshakya.fuellog.e2e

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.chandanshakya.fuellog.ui.screens.FuelLogScreen
import com.chandanshakya.fuellog.ui.screens.InsightsScreen
import com.chandanshakya.fuellog.ui.screens.PumpDetailScreen
import com.chandanshakya.fuellog.ui.screens.VehiclesScreen
import com.chandanshakya.fuellog.ui.navigation.NavArgs
import com.chandanshakya.fuellog.ui.navigation.NavRoutes
import com.chandanshakya.fuellog.ui.navigation.UNKNOWN_PUMP_SENTINEL
import com.chandanshakya.fuellog.viewmodel.FuelLogViewModel
import com.chandanshakya.fuellog.viewmodel.InsightsViewModel
import com.chandanshakya.fuellog.viewmodel.PumpInsightsViewModel
import com.chandanshakya.fuellog.viewmodel.VehiclesViewModel

/**
 * Test navigation host that accepts pre-built ViewModels,
 * bypassing Hilt dependency injection for integration testing.
 */
@Composable
fun TestAppNavHost(
    navController: NavHostController,
    vehiclesViewModel: VehiclesViewModel,
    fuelLogViewModelFactory: (Long) -> FuelLogViewModel,
    insightsViewModelFactory: (Long) -> InsightsViewModel,
    pumpInsightsViewModelFactory: (Long) -> PumpInsightsViewModel
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
                    navController.navigate(
                        NavRoutes.FUEL_LOG_WITH_ARG.replace("{vehicleId}", vehicleId.toString())
                    ) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToSettings = {},
                viewModel = vehiclesViewModel
            )
        }

        composable(
            route = NavRoutes.FUEL_LOG_WITH_ARG,
            arguments = listOf(
                navArgument(NavArgs.VEHICLE_ID) { type = androidx.navigation.NavType.LongType }
            )
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getLong(NavArgs.VEHICLE_ID) ?: return@composable
            FuelLogScreen(
                vehicleId = vehicleId,
                onNavigateToInsights = {
                    navController.navigate(
                        NavRoutes.INSIGHTS_WITH_ARG.replace("{vehicleId}", vehicleId.toString())
                    )
                },
                onNavigateToVehicles = {
                    if (!navController.popBackStack()) {
                        navController.navigate(NavRoutes.VEHICLES) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                viewModel = fuelLogViewModelFactory(vehicleId)
            )
        }

        composable(
            route = NavRoutes.INSIGHTS_WITH_ARG,
            arguments = listOf(
                navArgument(NavArgs.VEHICLE_ID) { type = androidx.navigation.NavType.LongType }
            )
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getLong(NavArgs.VEHICLE_ID) ?: return@composable
            InsightsScreen(
                vehicleId = vehicleId,
                onNavigateToLog = { navController.popBackStack() },
                onNavigateToPumpDetail = { vId, pumpId ->
                    navController.navigate(
                        NavRoutes.PUMP_DETAIL_WITH_ARG
                            .replace("{vehicleId}", vId.toString())
                            .replace("{pumpId}", pumpId.toString())
                    )
                },
                viewModel = insightsViewModelFactory(vehicleId),
                pumpInsightsViewModel = pumpInsightsViewModelFactory(vehicleId)
            )
        }

        composable(
            route = NavRoutes.PUMP_DETAIL_WITH_ARG,
            arguments = listOf(
                navArgument(NavArgs.VEHICLE_ID) { type = androidx.navigation.NavType.LongType },
                navArgument(NavArgs.PUMP_ID) { type = androidx.navigation.NavType.LongType }
            )
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getLong(NavArgs.VEHICLE_ID) ?: return@composable
            val pumpIdArg = backStackEntry.arguments?.getLong(NavArgs.PUMP_ID) ?: UNKNOWN_PUMP_SENTINEL
            val pumpId = if (pumpIdArg == UNKNOWN_PUMP_SENTINEL) null else pumpIdArg
            PumpDetailScreen(
                vehicleId = vehicleId,
                pumpId = pumpId,
                onNavigateBack = { navController.popBackStack() },
                pumpInsightsViewModel = pumpInsightsViewModelFactory(vehicleId)
            )
        }
    }
}
