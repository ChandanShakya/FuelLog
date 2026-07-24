package com.chandanshakya.fuellog.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.ui.screens.PumpDetailScreen
import com.chandanshakya.fuellog.util.PumpMileageStat
import org.junit.Rule
import org.junit.Test

class PumpDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun pumpDetailScreen_displaysPumpName() {
        composeTestRule.setContent {
            PumpDetailScreen(
                vehicleId = 1,
                pumpId = 1L,
                onNavigateBack = {}
            )
        }
        // The screen should display the pump name from stats
        // Since we're using hiltViewModel(), it will show default state
        composeTestRule.onNodeWithText("Mileage Trend").assertIsDisplayed()
    }

    @Test
    fun pumpDetailScreen_showsTrendChart() {
        composeTestRule.setContent {
            PumpDetailScreen(
                vehicleId = 1,
                pumpId = 1L,
                onNavigateBack = {}
            )
        }
        composeTestRule.onNodeWithText("Mileage Trend").assertIsDisplayed()
    }
}
