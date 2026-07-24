package com.chandanshakya.fuellog.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.FuelPump
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.ui.components.AddFuelEntryDialog
import org.junit.Rule
import org.junit.Test

class AddFuelEntryDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dialog_displaysTitle() {
        composeTestRule.setContent {
            AddFuelEntryDialog(
                vehicleId = 1,
                distanceUnit = DistanceUnit.KM,
                volumeUnit = VolumeUnit.LITERS,
                currency = "USD",
                onDismiss = {},
                onSave = { _, _, _, _, _ -> }
            )
        }
        composeTestRule.onNodeWithText("Add Fuel Entry").assertIsDisplayed()
    }

    @Test
    fun dialog_displaysEditTitle() {
        composeTestRule.setContent {
            AddFuelEntryDialog(
                vehicleId = 1,
                entry = com.chandanshakya.fuellog.data.model.FuelEntry(
                    id = 1,
                    vehicleId = 1,
                    date = java.time.LocalDate.now(),
                    odometer = 1000.0,
                    fuelVolume = 50.0,
                    fuelCost = 100.0,
                    fuelPumpId = 1L
                ),
                distanceUnit = DistanceUnit.KM,
                volumeUnit = VolumeUnit.LITERS,
                currency = "USD",
                existingPumps = listOf(FuelPump(id = 1, name = "Shell")),
                onDismiss = {},
                onSave = { _, _, _, _, _ -> }
            )
        }
        composeTestRule.onNodeWithText("Edit Fuel Entry").assertIsDisplayed()
    }

    @Test
    fun pumpField_displaysPlaceholder() {
        composeTestRule.setContent {
            AddFuelEntryDialog(
                vehicleId = 1,
                distanceUnit = DistanceUnit.KM,
                volumeUnit = VolumeUnit.LITERS,
                currency = "USD",
                onDismiss = {},
                onSave = { _, _, _, _, _ -> }
            )
        }
        composeTestRule.onNodeWithText("Fuel Pump (optional)").assertIsDisplayed()
    }

    @Test
    fun pumpField_showsSuggestionsWhenTyping() {
        val pumps = listOf(
            FuelPump(id = 1, name = "Shell"),
            FuelPump(id = 2, name = "BP"),
            FuelPump(id = 3, name = "Mobil")
        )

        composeTestRule.setContent {
            AddFuelEntryDialog(
                vehicleId = 1,
                distanceUnit = DistanceUnit.KM,
                volumeUnit = VolumeUnit.LITERS,
                currency = "USD",
                existingPumps = pumps,
                onDismiss = {},
                onSave = { _, _, _, _, _ -> }
            )
        }

        // Type in the pump field
        composeTestRule.onNodeWithText("Fuel Pump (optional)")
            .performClick()
            .performTextInput("Sh")

        // Should show Shell as suggestion, but not BP or Mobil
        composeTestRule.onNodeWithText("Shell").assertIsDisplayed()
    }

    @Test
    fun pumpField_selectingSuggestion_fillsField() {
        val pumps = listOf(
            FuelPump(id = 1, name = "Shell"),
            FuelPump(id = 2, name = "BP")
        )
        var savedPumpName: String? = "NOT_SET"

        composeTestRule.setContent {
            AddFuelEntryDialog(
                vehicleId = 1,
                distanceUnit = DistanceUnit.KM,
                volumeUnit = VolumeUnit.LITERS,
                currency = "USD",
                existingPumps = pumps,
                onDismiss = {},
                onSave = { _, _, _, _, pumpName -> savedPumpName = pumpName }
            )
        }

        composeTestRule.onNodeWithText("Fuel Pump (optional)").performClick()
        composeTestRule.onNodeWithText("Shell").performClick()

        // Verify the field is filled
        composeTestRule.onNodeWithText("Shell").assertIsDisplayed()
    }

    @Test
    fun dialog_cancelButton_callsOnDismiss() {
        var dismissed = false
        composeTestRule.setContent {
            AddFuelEntryDialog(
                vehicleId = 1,
                distanceUnit = DistanceUnit.KM,
                volumeUnit = VolumeUnit.LITERS,
                currency = "USD",
                onDismiss = { dismissed = true },
                onSave = { _, _, _, _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("Cancel").performClick()
        assert(dismissed) { "onDismiss should have been called" }
    }
}
