package com.chandanshakya.fuellog.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.chandanshakya.fuellog.data.db.AppDatabase
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.Vehicle
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.ui.theme.FuelLogTheme
import com.chandanshakya.fuellog.viewmodel.FuelLogViewModel
import com.chandanshakya.fuellog.viewmodel.InsightsViewModel
import com.chandanshakya.fuellog.viewmodel.PumpInsightsViewModel
import com.chandanshakya.fuellog.viewmodel.VehiclesViewModel
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import com.chandanshakya.fuellog.data.model.VehicleType
import com.chandanshakya.fuellog.data.model.FuelPump

class FuelPumpNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var database: AppDatabase
    private lateinit var vehiclesViewModel: VehiclesViewModel

    private val testVehicleId = 1L

    @Before
    fun setup() = runTest {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        database.vehicleDao().insert(
            Vehicle(
                id = testVehicleId,
                name = "Test Car",
                vehicleType = VehicleType.CAR,
                distanceUnit = DistanceUnit.KM,
                volumeUnit = VolumeUnit.LITERS
            )
        )

        vehiclesViewModel = VehiclesViewModel(
            vehicleDao = database.vehicleDao(),
            userSettingsDao = database.userSettingsDao(),
            fuelEntryDao = database.fuelEntryDao()
        )
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun createFuelLogViewModel(vehicleId: Long): FuelLogViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("vehicleId" to vehicleId))
        return FuelLogViewModel(
            fuelEntryDao = database.fuelEntryDao(),
            vehicleDao = database.vehicleDao(),
            userSettingsDao = database.userSettingsDao(),
            fuelPumpDao = database.fuelPumpDao(),
            savedStateHandle = savedStateHandle
        )
    }

    private fun createInsightsViewModel(vehicleId: Long): InsightsViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("vehicleId" to vehicleId))
        return InsightsViewModel(
            fuelEntryDao = database.fuelEntryDao(),
            vehicleDao = database.vehicleDao(),
            userSettingsDao = database.userSettingsDao(),
            savedStateHandle = savedStateHandle
        )
    }

    private fun createPumpInsightsViewModel(vehicleId: Long): PumpInsightsViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("vehicleId" to vehicleId))
        return PumpInsightsViewModel(
            fuelEntryDao = database.fuelEntryDao(),
            userSettingsDao = database.userSettingsDao(),
            savedStateHandle = savedStateHandle
        )
    }

    private fun waitForNode(text: String, timeout: Long = 5000) {
        composeTestRule.waitUntil(timeout) {
            composeTestRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForNodeGone(text: String, timeout: Long = 5000) {
        composeTestRule.waitUntil(timeout) {
            composeTestRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isEmpty()
        }
    }

    private fun setContent() {
        composeTestRule.setContent {
            FuelLogTheme {
                val navController = rememberNavController()
                TestAppNavHost(
                    navController = navController,
                    vehiclesViewModel = vehiclesViewModel,
                    fuelLogViewModelFactory = { createFuelLogViewModel(it) },
                    insightsViewModelFactory = { createInsightsViewModel(it) },
                    pumpInsightsViewModelFactory = { createPumpInsightsViewModel(it) }
                )
            }
        }
    }

    // ===== Test 1: Full navigation flow =====

    @Test
    fun fullNavigationFlow_vehicleToFuelLogToInsightsAndBack() = runTest {
        setContent()

        // Start on Vehicles screen
        composeTestRule.onNodeWithText("Test Car").assertIsDisplayed()

        // Click vehicle → Fuel Log screen
        composeTestRule.onNodeWithText("Test Car").performClick()
        waitForNode("No Fuel Entries")
        composeTestRule.onNodeWithText("No Fuel Entries").assertIsDisplayed()

        // Click Insights icon → Insights screen
        composeTestRule.onNodeWithContentDescription("Insights").performClick()
        waitForNode("No Data Available")

        // Back to Fuel Log
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        waitForNode("No Fuel Entries")

        // Back to Vehicles
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        waitForNode("Test Car")
        composeTestRule.onNodeWithText("Test Car").assertIsDisplayed()
    }

    // ===== Test 2: Add fuel entry with pump name =====

    @Test
    fun addFuelEntry_withPumpName_savesPumpAssociation() = runTest {
        setContent()

        // Navigate to Fuel Log
        composeTestRule.onNodeWithText("Test Car").performClick()
        waitForNode("No Fuel Entries")

        // Open Add dialog
        composeTestRule.onNodeWithContentDescription("Add fuel entry").performClick()
        waitForNode("Add Fuel Entry")

        // Fill fields
        composeTestRule.onNodeWithText("Odometer (km)").performTextInput("1000")
        composeTestRule.onNodeWithText("Fuel Volume (L)").performTextInput("50")
        composeTestRule.onNodeWithText("Rate (USD/L)").performTextInput("2")

        // Enter pump name
        composeTestRule.onNodeWithText("Fuel Pump (optional)").performClick()
        composeTestRule.onNodeWithText("Fuel Pump (optional)").performTextInput("Shell")

        // Save
        composeTestRule.onNodeWithText("Save").performClick()
        waitForNode("1000.00")

        // Verify entry saved
        composeTestRule.onNodeWithText("1000.00").assertIsDisplayed()

        // Verify pump created in DB
        val pumps = database.fuelPumpDao().getAllList()
        assertEquals(1, pumps.size)
        assertEquals("Shell", pumps[0].name)

        // Verify entry has pump reference
        val entries = database.fuelEntryDao().getAllByVehicleList(testVehicleId)
        assertEquals(1, entries.size)
        assertNotNull(entries[0].fuelPumpId)
        assertEquals(pumps[0].id, entries[0].fuelPumpId)
    }

    // ===== Test 3: Pump autocomplete shows existing pumps =====

    @Test
    fun pumpAutocomplete_showsExistingPumpsAsSuggestions() = runTest {
        // Pre-create pumps
        database.fuelPumpDao().insert(FuelPump(name = "Shell"))
        database.fuelPumpDao().insert(FuelPump(name = "BP"))

        setContent()

        // Navigate to Fuel Log and open dialog
        composeTestRule.onNodeWithText("Test Car").performClick()
        waitForNode("No Fuel Entries")
        composeTestRule.onNodeWithContentDescription("Add fuel entry").performClick()
        waitForNode("Add Fuel Entry")

        // Click pump field to open dropdown
        composeTestRule.onNodeWithText("Fuel Pump (optional)").performClick()

        // Should see both pumps
        composeTestRule.onNodeWithText("Shell").assertIsDisplayed()
        composeTestRule.onNodeWithText("BP").assertIsDisplayed()
    }

    // ===== Test 4: Insights shows pump stats =====

    @Test
    fun insightsPumpSection_showsPumpStats() = runTest {
        val shellId = database.fuelPumpDao().insert(FuelPump(name = "Shell"))
        val bpId = database.fuelPumpDao().insert(FuelPump(name = "BP"))

        database.fuelEntryDao().insert(
            com.chandanshakya.fuellog.data.model.FuelEntry(
                vehicleId = testVehicleId, date = LocalDate.of(2024, 1, 1),
                odometer = 1000.0, fuelVolume = 50.0, fuelCost = 100.0, fuelPumpId = shellId
            )
        )
        database.fuelEntryDao().insert(
            com.chandanshakya.fuellog.data.model.FuelEntry(
                vehicleId = testVehicleId, date = LocalDate.of(2024, 1, 15),
                odometer = 1100.0, fuelVolume = 40.0, fuelCost = 80.0, fuelPumpId = shellId
            )
        )
        database.fuelEntryDao().insert(
            com.chandanshakya.fuellog.data.model.FuelEntry(
                vehicleId = testVehicleId, date = LocalDate.of(2024, 2, 1),
                odometer = 1200.0, fuelVolume = 45.0, fuelCost = 90.0, fuelPumpId = bpId
            )
        )

        setContent()

        // Navigate to Insights
        composeTestRule.onNodeWithText("Test Car").performClick()
        waitForNode("1000.00")
        composeTestRule.onNodeWithContentDescription("Insights").performClick()
        waitForNode("Mileage by Fuel Pump")

        // Verify pump stats
        composeTestRule.onNodeWithText("Mileage by Fuel Pump").assertIsDisplayed()
        composeTestRule.onNodeWithText("Shell").assertIsDisplayed()
        composeTestRule.onNodeWithText("BP").assertIsDisplayed()
    }

    // ===== Test 5: Click pump navigates to pump detail =====

    @Test
    fun insightsPumpClick_navigatesToPumpDetail() = runTest {
        val shellId = database.fuelPumpDao().insert(FuelPump(name = "Shell"))
        database.fuelEntryDao().insert(
            com.chandanshakya.fuellog.data.model.FuelEntry(
                vehicleId = testVehicleId, date = LocalDate.of(2024, 1, 1),
                odometer = 1000.0, fuelVolume = 50.0, fuelCost = 100.0, fuelPumpId = shellId
            )
        )
        database.fuelEntryDao().insert(
            com.chandanshakya.fuellog.data.model.FuelEntry(
                vehicleId = testVehicleId, date = LocalDate.of(2024, 1, 15),
                odometer = 1100.0, fuelVolume = 40.0, fuelCost = 80.0, fuelPumpId = shellId
            )
        )

        setContent()

        // Navigate: Vehicle → Fuel Log → Insights
        composeTestRule.onNodeWithText("Test Car").performClick()
        waitForNode("1000.00")
        composeTestRule.onNodeWithContentDescription("Insights").performClick()
        waitForNode("Shell")

        // Click Shell pump
        composeTestRule.onNodeWithText("Shell").performClick()
        waitForNode("Mileage Trend")

        // Should be on Pump Detail screen
        composeTestRule.onNodeWithText("Shell").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mileage Trend").assertIsDisplayed()
    }

    // ===== Test 6: Pump detail back navigation =====

    @Test
    fun pumpDetail_backButton_navigatesToInsights() = runTest {
        val shellId = database.fuelPumpDao().insert(FuelPump(name = "Shell"))
        database.fuelEntryDao().insert(
            com.chandanshakya.fuellog.data.model.FuelEntry(
                vehicleId = testVehicleId, date = LocalDate.of(2024, 1, 1),
                odometer = 1000.0, fuelVolume = 50.0, fuelCost = 100.0, fuelPumpId = shellId
            )
        )
        database.fuelEntryDao().insert(
            com.chandanshakya.fuellog.data.model.FuelEntry(
                vehicleId = testVehicleId, date = LocalDate.of(2024, 1, 15),
                odometer = 1100.0, fuelVolume = 40.0, fuelCost = 80.0, fuelPumpId = shellId
            )
        )

        setContent()

        // Navigate to Pump Detail
        composeTestRule.onNodeWithText("Test Car").performClick()
        waitForNode("1000.00")
        composeTestRule.onNodeWithContentDescription("Insights").performClick()
        waitForNode("Shell")
        composeTestRule.onNodeWithText("Shell").performClick()
        waitForNode("Mileage Trend")

        // Back to Insights
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        waitForNode("Mileage by Fuel Pump")
        composeTestRule.onNodeWithText("Mileage by Fuel Pump").assertIsDisplayed()
    }

    // ===== Test 7: Full round trip =====

    @Test
    fun fullRoundTrip_addEntryAndViewPumpDetailAndBack() = runTest {
        setContent()

        // 1. Vehicles → Fuel Log
        composeTestRule.onNodeWithText("Test Car").performClick()
        waitForNode("No Fuel Entries")

        // 2. Add entry with Shell pump
        composeTestRule.onNodeWithContentDescription("Add fuel entry").performClick()
        waitForNode("Add Fuel Entry")
        composeTestRule.onNodeWithText("Odometer (km)").performTextInput("1000")
        composeTestRule.onNodeWithText("Fuel Volume (L)").performTextInput("50")
        composeTestRule.onNodeWithText("Rate (USD/L)").performTextInput("2")
        composeTestRule.onNodeWithText("Fuel Pump (optional)").performClick()
        composeTestRule.onNodeWithText("Fuel Pump (optional)").performTextInput("Shell")
        composeTestRule.onNodeWithText("Save").performClick()
        waitForNode("1000.00")

        // 3. Add second entry with Shell (reuse via autocomplete)
        composeTestRule.onNodeWithContentDescription("Add fuel entry").performClick()
        waitForNode("Add Fuel Entry")
        composeTestRule.onNodeWithText("Odometer (km)").performTextInput("1100")
        composeTestRule.onNodeWithText("Fuel Volume (L)").performTextInput("40")
        composeTestRule.onNodeWithText("Rate (USD/L)").performTextInput("2")
        composeTestRule.onNodeWithText("Fuel Pump (optional)").performClick()
        composeTestRule.onNodeWithText("Shell").performClick()
        composeTestRule.onNodeWithText("Save").performClick()
        waitForNode("1100.00")

        // 4. Navigate to Insights
        composeTestRule.onNodeWithContentDescription("Insights").performClick()
        waitForNode("Mileage by Fuel Pump")

        // 5. Verify Shell in pump stats
        composeTestRule.onNodeWithText("Shell").assertIsDisplayed()

        // 6. Click Shell → Pump Detail
        composeTestRule.onNodeWithText("Shell").performClick()
        waitForNode("Mileage Trend")
        composeTestRule.onNodeWithText("Shell").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mileage Trend").assertIsDisplayed()

        // 7. Back to Insights
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        waitForNode("Mileage by Fuel Pump")

        // 8. Back to Fuel Log
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        waitForNode("1100.00")

        // 9. Back to Vehicles
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        waitForNode("Test Car")
        composeTestRule.onNodeWithText("Test Car").assertIsDisplayed()
    }

    // ===== Test 8: Unknown pump entries =====

    @Test
    fun unknownPumpEntries_showInInsights() = runTest {
        database.fuelEntryDao().insert(
            com.chandanshakya.fuellog.data.model.FuelEntry(
                vehicleId = testVehicleId, date = LocalDate.of(2024, 1, 1),
                odometer = 1000.0, fuelVolume = 50.0, fuelCost = 100.0, fuelPumpId = null
            )
        )
        database.fuelEntryDao().insert(
            com.chandanshakya.fuellog.data.model.FuelEntry(
                vehicleId = testVehicleId, date = LocalDate.of(2024, 1, 15),
                odometer = 1100.0, fuelVolume = 40.0, fuelCost = 80.0, fuelPumpId = null
            )
        )

        setContent()

        composeTestRule.onNodeWithText("Test Car").performClick()
        waitForNode("1000.00")
        composeTestRule.onNodeWithContentDescription("Insights").performClick()
        waitForNode("Mileage by Fuel Pump")

        composeTestRule.onNodeWithText("Unknown / Not recorded").assertIsDisplayed()
    }

    // ===== Test 9: Delete pump clears entry references =====

    @Test
    fun deletePump_clearsEntryReferences() = runTest {
        val shellId = database.fuelPumpDao().insert(FuelPump(name = "Shell"))
        database.fuelEntryDao().insert(
            com.chandanshakya.fuellog.data.model.FuelEntry(
                vehicleId = testVehicleId, date = LocalDate.of(2024, 1, 1),
                odometer = 1000.0, fuelVolume = 50.0, fuelCost = 100.0, fuelPumpId = shellId
            )
        )

        // Verify pump reference exists
        val before = database.fuelEntryDao().getAllByVehicleList(testVehicleId)
        assertEquals(shellId, before[0].fuelPumpId)

        // Delete pump
        database.fuelPumpDao().deleteById(shellId)

        // Verify entry still exists but pump reference is null
        val after = database.fuelEntryDao().getAllByVehicleList(testVehicleId)
        assertEquals(1, after.size)
        assertEquals(null, after[0].fuelPumpId)
    }

    // ===== Test 10:泵 autocomplete select fills field correctly =====

    @Test
    fun pumpAutocomplete_selectSuggestion_fillsTextField() = runTest {
        database.fuelPumpDao().insert(FuelPump(name = "Shell"))
        database.fuelPumpDao().insert(FuelPump(name = "BP"))
        database.fuelPumpDao().insert(FuelPump(name = "Mobil"))

        setContent()

        composeTestRule.onNodeWithText("Test Car").performClick()
        waitForNode("No Fuel Entries")
        composeTestRule.onNodeWithContentDescription("Add fuel entry").performClick()
        waitForNode("Add Fuel Entry")

        // Open pump dropdown
        composeTestRule.onNodeWithText("Fuel Pump (optional)").performClick()

        // All 3 should be visible
        composeTestRule.onNodeWithText("Shell").assertIsDisplayed()
        composeTestRule.onNodeWithText("BP").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mobil").assertIsDisplayed()

        // Select Shell
        composeTestRule.onNodeWithText("Shell").performClick()

        // Dropdown should close, Shell should be in field
        composeTestRule.onNodeWithText("Shell").assertIsDisplayed()
    }
}
