package com.chandanshakya.fuellog.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.chandanshakya.fuellog.ui.screens.FuelLogScreen
import com.chandanshakya.fuellog.ui.screens.InsightsScreen
import com.chandanshakya.fuellog.ui.screens.OdometerLogsScreen
import com.chandanshakya.fuellog.ui.screens.PumpDetailScreen
import com.chandanshakya.fuellog.ui.screens.SettingsScreen
import com.chandanshakya.fuellog.ui.screens.VehiclesScreen

private fun encodeRoute(screen: Screen): String = when (screen) {
    is Screen.Vehicles -> "V"
    is Screen.FuelLog -> "F:${screen.vehicleId}"
    is Screen.Insights -> "I:${screen.vehicleId}"
    is Screen.OdometerLogs -> "O:${screen.vehicleId}"
    is Screen.PumpDetail -> "P:${screen.vehicleId}:${screen.pumpId ?: -1}"
    is Screen.Settings -> "S"
}

private fun decodeRoute(str: String): Screen {
    val parts = str.split(":")
    return when (parts.getOrNull(0)) {
        "V" -> Screen.Vehicles
        "F" -> parts.getOrNull(1)?.toLongOrNull()?.let { Screen.FuelLog(it) } ?: Screen.Vehicles
        "I" -> parts.getOrNull(1)?.toLongOrNull()?.let { Screen.Insights(it) } ?: Screen.Vehicles
        "O" -> parts.getOrNull(1)?.toLongOrNull()?.let { Screen.OdometerLogs(it) } ?: Screen.Vehicles
        "P" -> {
            val vehicleId = parts.getOrNull(1)?.toLongOrNull()
            val pumpId = parts.getOrNull(2)?.toLongOrNull()
            if (vehicleId != null) Screen.PumpDetail(vehicleId, pumpId?.takeIf { it != -1L })
            else Screen.Vehicles
        }
        "S" -> Screen.Settings
        else -> Screen.Vehicles
    }
}

@Composable
fun AppNavHost() {
    var backStackStr by rememberSaveable { mutableStateOf("V") }

    val routes = remember(backStackStr) {
        backStackStr.split("|").mapNotNull { token ->
            token.takeIf { it.isNotBlank() }?.let { decodeRoute(it) }
        }.ifEmpty { listOf(Screen.Vehicles) }
    }
    val currentScreen = routes.last()

    fun navigate(screen: Screen) {
        val encoded = encodeRoute(screen)
        if (encodeRoute(currentScreen) == encoded) return
        backStackStr = backStackStr + "|" + encoded
    }

    fun popBack(): Boolean {
        val parts = backStackStr.split("|").filter { it.isNotBlank() }
        if (parts.size > 1) {
            backStackStr = parts.dropLast(1).joinToString("|")
            return true
        }
        return false
    }

    BackHandler(enabled = routes.size > 1) {
        popBack()
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            fadeIn(tween(200)) + slideInHorizontally(tween(200)) { it } togetherWith
                    fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { -it / 3 }
        },
        contentKey = { screen -> encodeRoute(screen) },
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
                }
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
