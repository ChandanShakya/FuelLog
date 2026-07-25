package com.chandanshakya.fuellog.e2e

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.chandanshakya.fuellog.ui.screens.FuelLogScreen
import com.chandanshakya.fuellog.ui.screens.InsightsScreen
import com.chandanshakya.fuellog.ui.screens.PumpDetailScreen
import com.chandanshakya.fuellog.ui.screens.VehiclesScreen
import com.chandanshakya.fuellog.ui.navigation.Screen
import com.chandanshakya.fuellog.viewmodel.FuelLogViewModel
import com.chandanshakya.fuellog.viewmodel.InsightsViewModel
import com.chandanshakya.fuellog.viewmodel.PumpInsightsViewModel
import com.chandanshakya.fuellog.viewmodel.VehiclesViewModel

/**
 * Test navigation host that accepts pre-built ViewModels,
 * bypassing dependency injection for integration testing.
 */
@Composable
fun TestAppNavHost(
    vehiclesViewModel: VehiclesViewModel,
    fuelLogViewModel: FuelLogViewModel,
    insightsViewModel: InsightsViewModel,
    pumpInsightsViewModel: PumpInsightsViewModel
) {
    val backStack = remember { mutableStateListOf<Screen>(Screen.Vehicles) }
    val currentScreen = remember { mutableStateOf<Screen>(Screen.Vehicles) }

    fun navigate(screen: Screen) {
        backStack.add(screen)
        currentScreen.value = screen
    }

    fun popBack(): Boolean {
        if (backStack.size > 1) {
            backStack.removeLast()
            currentScreen.value = backStack.last()
            return true
        }
        return false
    }

    AnimatedContent(
        targetState = currentScreen.value,
        transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        label = "test_nav"
    ) { screen ->
        when (screen) {
            is Screen.Vehicles -> VehiclesScreen(
                onVehicleSelected = { vehicleId ->
                    navigate(Screen.FuelLog(vehicleId))
                },
                onNavigateToSettings = {},
                viewModel = vehiclesViewModel
            )

            is Screen.FuelLog -> FuelLogScreen(
                vehicleId = screen.vehicleId,
                onNavigateToInsights = {
                    navigate(Screen.Insights(screen.vehicleId))
                },
                onNavigateToVehicles = {
                    popBack()
                },
                viewModel = fuelLogViewModel
            )

            is Screen.Insights -> InsightsScreen(
                vehicleId = screen.vehicleId,
                onNavigateToLog = { popBack() },
                onNavigateToPumpDetail = { vId, pumpId ->
                    navigate(Screen.PumpDetail(vId, pumpId))
                },
                viewModel = insightsViewModel,
                pumpInsightsViewModel = pumpInsightsViewModel
            )

            is Screen.PumpDetail -> PumpDetailScreen(
                vehicleId = screen.vehicleId,
                pumpId = screen.pumpId,
                onNavigateBack = { popBack() },
                pumpInsightsViewModel = pumpInsightsViewModel
            )

            else -> {}
        }
    }
}
