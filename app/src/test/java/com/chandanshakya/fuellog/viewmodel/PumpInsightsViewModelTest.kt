package com.chandanshakya.fuellog.viewmodel

import com.chandanshakya.fuellog.data.db.FuelEntryDao
import com.chandanshakya.fuellog.data.db.FuelEntryWithPump
import com.chandanshakya.fuellog.data.db.UserSettingsDao
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.UserSettings
import com.chandanshakya.fuellog.util.computePumpMileageStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class PumpInsightsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeFuelEntryDao: FakeFuelEntryDaoForPump
    private lateinit var fakeUserSettingsDao: FakeUserSettingsDaoForPump

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeFuelEntryDao = FakeFuelEntryDaoForPump()
        fakeUserSettingsDao = FakeUserSettingsDaoForPump()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun pumpStats_computedCorrectly() = runTest {
        val entriesWithPump = listOf(
            FuelEntryWithPump(
                FuelEntry(1, 1, LocalDate.of(2024, 1, 1), 1000.0, 50.0, 100.0, 1L),
                "Shell"
            ),
            FuelEntryWithPump(
                FuelEntry(2, 1, LocalDate.of(2024, 1, 15), 1100.0, 40.0, 80.0, 1L),
                "Shell"
            ),
            FuelEntryWithPump(
                FuelEntry(3, 1, LocalDate.of(2024, 2, 1), 1200.0, 45.0, 90.0, 2L),
                "BP"
            )
        )
        fakeFuelEntryDao.entriesWithPump = entriesWithPump

        val stats = computePumpMileageStats(entriesWithPump)

        // Shell: 2 entries, 2 pairs → included
        // BP: 1 entry, no next entry → no pairs → excluded
        assertEquals(1, stats.size)
        val shell = stats.first { it.pumpId == 1L }
        assertEquals("Shell", shell.pumpName)
        assertEquals(2, shell.fillCount)
        assertEquals(2.25, shell.avgMileage, 0.001)
    }

    @Test
    fun pumpStats_emptyInput() = runTest {
        val stats = computePumpMileageStats(emptyList())
        assertTrue(stats.isEmpty())
    }

    @Test
    fun pumpStats_unknownPumpGroupedCorrectly() = runTest {
        val entriesWithPump = listOf(
            FuelEntryWithPump(
                FuelEntry(1, 1, LocalDate.of(2024, 1, 1), 1000.0, 50.0, 100.0, null),
                null
            ),
            FuelEntryWithPump(
                FuelEntry(2, 1, LocalDate.of(2024, 1, 15), 1100.0, 40.0, 80.0, null),
                null
            )
        )

        val stats = computePumpMileageStats(entriesWithPump)

        assertEquals(1, stats.size)
        assertEquals("Unknown / Not recorded", stats[0].pumpName)
        assertEquals(2, stats[0].fillCount)
    }

    @Test
    fun pumpStats_multipleFillsPerPump() = runTest {
        val entriesWithPump = listOf(
            FuelEntryWithPump(
                FuelEntry(1, 1, LocalDate.of(2024, 1, 1), 1000.0, 50.0, 100.0, 1L),
                "Shell"
            ),
            FuelEntryWithPump(
                FuelEntry(2, 1, LocalDate.of(2024, 1, 15), 1100.0, 40.0, 80.0, 1L),
                "Shell"
            ),
            FuelEntryWithPump(
                FuelEntry(3, 1, LocalDate.of(2024, 2, 1), 1200.0, 50.0, 100.0, 1L),
                "Shell"
            )
        )

        val stats = computePumpMileageStats(entriesWithPump)

        assertEquals(1, stats.size)
        val shell = stats[0]
        assertEquals(3, shell.fillCount)
        // pair1: 100/50=2.0, pair2: 100/40=2.5
        assertEquals(2.25, shell.avgMileage, 0.001)
        assertEquals(2.5, shell.bestMileage, 0.001)
        assertEquals(2.0, shell.worstMileage, 0.001)
    }
}

class FakeFuelEntryDaoForPump : FuelEntryDao {
    var entriesWithPump: List<FuelEntryWithPump> = emptyList()
    private val entries = mutableListOf<FuelEntry>()

    override fun getAllByVehicle(vehicleId: Long) = flowOf(entries)
    override suspend fun getAllByVehicleList(vehicleId: Long) = entries
    override fun getAllByVehicleWithPump(vehicleId: Long) = flowOf(entriesWithPump)
    override suspend fun getById(id: Long) = entries.find { it.id == id }
    override suspend fun insert(entry: FuelEntry): Long { entries.add(entry); return entry.id }
    override suspend fun update(entry: FuelEntry) {}
    override suspend fun updateAll(entries: List<FuelEntry>) {}
    override suspend fun deleteById(id: Long) {}
    override suspend fun getFullTankEntriesByVehicle(vehicleId: Long) =
        entries.filter { it.vehicleId == vehicleId && it.isFullTank }.sortedBy { it.odometer }
    override suspend fun getLatestEntryByVehicle(vehicleId: Long) =
        entries.filter { it.vehicleId == vehicleId }.maxByOrNull { it.odometer }
    override fun getAllByPumpId(pumpId: Long) = flowOf(
        entries.filter { it.fuelPumpId == pumpId }
    )
    override fun getAllByVehicleWithNullPump(vehicleId: Long) = flowOf(
        entries.filter { it.vehicleId == vehicleId && it.fuelPumpId == null }
    )
}

class FakeUserSettingsDaoForPump : UserSettingsDao {
    private var settings: UserSettings? = null
    override fun getSettings() = flowOf(settings)
    override suspend fun getSettingsSuspend() = settings
    override suspend fun insert(settings: UserSettings) { this.settings = settings }
    override suspend fun update(settings: UserSettings) { this.settings = settings }
}
