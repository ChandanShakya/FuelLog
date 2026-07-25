package com.chandanshakya.fuellog.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.chandanshakya.fuellog.ui.screens.FuelLogScreen
import com.chandanshakya.fuellog.ui.screens.InsightsScreen
import com.chandanshakya.fuellog.ui.screens.OdometerLogsScreen
import com.chandanshakya.fuellog.ui.screens.PumpDetailScreen
import com.chandanshakya.fuellog.ui.screens.SettingsScreen
import com.chandanshakya.fuellog.ui.screens.VehiclesScreen

@Composable
fun AppNavHost() {
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
        transitionSpec = {
            fadeIn(tween(200)) + slideInHorizontally(tween(200)) { it } togetherWith
                    fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { -it / 3 }
        },
        label = "nav"
    ) { screen ->
        when (screen) {
            is Screen.Vehicles -> VehiclesScreen(
                onVehicleSelected = { vehicleId ->
                    navigate(Screen.FuelLog(vehicleId))
                },
                onNavigateToSettings = {
                    navigate(Screen.Settings)
                }
            )

            is Screen.FuelLog -> FuelLogScreen(
                vehicleId = screen.vehicleId,
                onNavigateToInsights = {
                    navigate(Screen.Insights(screen.vehicleId))
                },
                onNavigateToVehicles = {
                    popBack()
                },
                onNavigateToOdometerLogs = {
                    navigate(Screen.OdometerLogs(screen.vehicleId))
                }
            )

            is Screen.Insights -> InsightsScreen(
                vehicleId = screen.vehicleId,
                onNavigateToLog = {
                    popBack()
                },
                onNavigateToPumpDetail = { vId, pumpId ->
                    navigate(Screen.PumpDetail(vId, pumpId))
                }
            )

            is Screen.OdometerLogs -> OdometerLogsScreen(
                vehicleId = screen.vehicleId,
                onNavigateBack = {
                    popBack()
                },
                onAddReading = {}
            )

            is Screen.PumpDetail -> PumpDetailScreen(
                vehicleId = screen.vehicleId,
                pumpId = screen.pumpId,
                onNavigateBack = {
                    popBack()
                }
            )

            is Screen.Settings -> SettingsScreen(
                onNavigateToVehicles = {
                    popBack()
                }
            )
        }
    }
}
