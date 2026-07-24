package com.chandanshakya.fuellog.viewmodel

import com.chandanshakya.fuellog.data.db.FuelEntryDao
import com.chandanshakya.fuellog.data.db.FuelPumpDao
import com.chandanshakya.fuellog.data.db.OdometerReadingDao
import com.chandanshakya.fuellog.data.db.UserSettingsDao
import com.chandanshakya.fuellog.data.db.VehicleDao
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.FuelPump
import com.chandanshakya.fuellog.data.model.OdometerReading
import com.chandanshakya.fuellog.data.model.UserSettings
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class FuelLogViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeFuelEntryDao: FakeFuelEntryDao
    private lateinit var fakeVehicleDao: FakeVehicleDao
    private lateinit var fakeUserSettingsDao: FakeUserSettingsDao
    private lateinit var fakeFuelPumpDao: FakeFuelPumpDao
    private lateinit var fakeOdometerReadingDao: FakeOdometerReadingDao

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeFuelEntryDao = FakeFuelEntryDao()
        fakeVehicleDao = FakeVehicleDao()
        fakeUserSettingsDao = FakeUserSettingsDao()
        fakeFuelPumpDao = FakeFuelPumpDao()
        fakeOdometerReadingDao = FakeOdometerReadingDao()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun resolveOrCreatePump_newPump_insertsAndReturnsId() = runTest {
        val vm = createViewModel()
        val id = vm.resolveOrCreatePump("Shell")
        assertTrue(id > 0)
        assertEquals("Shell", fakeFuelPumpDao.pumps.first().name)
    }

    @Test
    fun resolveOrCreatePump_existingPump_returnsExistingId() = runTest {
        val existingPump = FuelPump(id = 42, name = "Shell")
        fakeFuelPumpDao.pumps.add(existingPump)

        val vm = createViewModel()
        val id = vm.resolveOrCreatePump("Shell")
        assertEquals(42L, id)
        assertEquals(1, fakeFuelPumpDao.pumps.size) // no new insert
    }

    @Test
    fun resolveOrCreatePump_emptyString_returnsZero() = runTest {
        val vm = createViewModel()
        val id = vm.resolveOrCreatePump("")
        assertEquals(0L, id)
    }

    @Test
    fun resolveOrCreatePump_whitespaceOnly_returnsZero() = runTest {
        val vm = createViewModel()
        val id = vm.resolveOrCreatePump("   ")
        assertEquals(0L, id)
    }

    @Test
    fun resolveOrCreatePump_trimsWhitespace() = runTest {
        val vm = createViewModel()
        val id = vm.resolveOrCreatePump("  Shell  ")
        assertTrue(id > 0)
        assertEquals("Shell", fakeFuelPumpDao.pumps.first().name)
    }

    @Test
    fun addFuelEntry_withPumpName_resolvesPump() = runTest {
        val vm = createViewModel()
        vm.addFuelEntry(
            date = LocalDate.of(2024, 1, 1),
            odometer = 1000.0,
            fuelVolume = 50.0,
            fuelCost = 100.0,
            pumpName = "Shell"
        )
        advanceUntilIdle()

        assertEquals(1, fakeFuelEntryDao.entries.size)
        val entry = fakeFuelEntryDao.entries.first()
        assertTrue(entry.fuelPumpId!! > 0)
    }

    @Test
    fun addFuelEntry_withoutPumpName_pumpIdIsNull() = runTest {
        val vm = createViewModel()
        vm.addFuelEntry(
            date = LocalDate.of(2024, 1, 1),
            odometer = 1000.0,
            fuelVolume = 50.0,
            fuelCost = 100.0,
            pumpName = null
        )
        advanceUntilIdle()

        assertEquals(1, fakeFuelEntryDao.entries.size)
        assertNull(fakeFuelEntryDao.entries.first().fuelPumpId)
    }

    @Test
    fun addFuelEntry_blankPumpName_pumpIdIsNull() = runTest {
        val vm = createViewModel()
        vm.addFuelEntry(
            date = LocalDate.of(2024, 1, 1),
            odometer = 1000.0,
            fuelVolume = 50.0,
            fuelCost = 100.0,
            pumpName = "  "
        )
        advanceUntilIdle()

        assertEquals(1, fakeFuelEntryDao.entries.size)
        assertNull(fakeFuelEntryDao.entries.first().fuelPumpId)
    }

    @Test
    fun addFuelEntry_invalidData_doesNotInsert() = runTest {
        val vm = createViewModel()
        vm.addFuelEntry(
            date = LocalDate.of(2024, 1, 1),
            odometer = -1.0,
            fuelVolume = 50.0,
            fuelCost = 100.0,
            pumpName = "Shell"
        )
        advanceUntilIdle()

        assertTrue(fakeFuelEntryDao.entries.isEmpty())
    }

    private fun createViewModel(): FuelLogViewModel {
        val savedStateHandle = androidx.lifecycle.SavedStateHandle(
            mapOf("vehicleId" to 1L)
        )
        return FuelLogViewModel(
            fuelEntryDao = fakeFuelEntryDao,
            vehicleDao = fakeVehicleDao,
            userSettingsDao = fakeUserSettingsDao,
            fuelPumpDao = fakeFuelPumpDao,
            odometerReadingDao = fakeOdometerReadingDao,
            savedStateHandle = savedStateHandle
        )
    }
}

// --- Fakes for testing without Hilt/Room ---

class FakeFuelEntryDao : FuelEntryDao {
    val entries = mutableListOf<FuelEntry>()
    private val entriesFlow = MutableStateFlow<List<FuelEntry>>(emptyList())

    private fun updateFlow() { entriesFlow.value = entries.toList() }

    override fun getAllByVehicle(vehicleId: Long) = kotlinx.coroutines.flow.flowOf(
        entries.filter { it.vehicleId == vehicleId }
    )
    override suspend fun getAllByVehicleList(vehicleId: Long) = entries.filter { it.vehicleId == vehicleId }
    override fun getAllByVehicleWithPump(vehicleId: Long) = flowOf(
        entries.filter { it.vehicleId == vehicleId }.map { entry ->
            com.chandanshakya.fuellog.data.db.FuelEntryWithPump(entry, null)
        }
    )
    override suspend fun getById(id: Long) = entries.find { it.id == id }
    override suspend fun insert(entry: FuelEntry): Long {
        entries.add(entry)
        updateFlow()
        return entry.id
    }
    override suspend fun update(entry: FuelEntry) {
        val idx = entries.indexOfFirst { it.id == entry.id }
        if (idx >= 0) entries[idx] = entry
        updateFlow()
    }
    override suspend fun updateAll(entries: List<FuelEntry>) {
        this.entries.clear()
        this.entries.addAll(entries)
        updateFlow()
    }
    override suspend fun deleteById(id: Long) {
        entries.removeAll { it.id == id }
        updateFlow()
    }
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

class FakeVehicleDao : VehicleDao {
    private val vehicles = mutableListOf<com.chandanshakya.fuellog.data.model.Vehicle>()
    override fun getAll() = flowOf(vehicles)
    override fun getByIdFlow(id: Long) = flowOf(vehicles.find { it.id == id })
    override suspend fun getById(id: Long) = vehicles.find { it.id == id }
    override suspend fun insert(vehicle: com.chandanshakya.fuellog.data.model.Vehicle): Long {
        vehicles.add(vehicle)
        return vehicle.id
    }
    override suspend fun update(vehicle: com.chandanshakya.fuellog.data.model.Vehicle) {
        val idx = vehicles.indexOfFirst { it.id == vehicle.id }
        if (idx >= 0) vehicles[idx] = vehicle
    }
    override suspend fun deleteById(id: Long) { vehicles.removeAll { it.id == id } }
}

class FakeUserSettingsDao : UserSettingsDao {
    private var settings: UserSettings? = null
    override fun getSettings() = flowOf(settings)
    override suspend fun getSettingsSuspend() = settings
    override suspend fun insert(settings: UserSettings) { this.settings = settings }
    override suspend fun update(settings: UserSettings) { this.settings = settings }
}

class FakeFuelPumpDao : FuelPumpDao {
    val pumps = mutableListOf<FuelPump>()
    override fun getAll() = flowOf(pumps.sortedBy { it.name })
    override suspend fun getAllList() = pumps.sortedBy { it.name }
    override suspend fun findByName(name: String) = pumps.find { it.name == name }
    override suspend fun insert(pump: FuelPump): Long {
        val existing = pumps.indexOfFirst { it.id == pump.id }
        if (existing >= 0) {
            pumps[existing] = pump
        } else {
            val newId = if (pumps.isEmpty()) 1L else pumps.maxOf { it.id } + 1
            val withId = pump.copy(id = newId)
            pumps.add(withId)
            return newId
        }
        return pump.id
    }
    override suspend fun deleteById(id: Long) { pumps.removeAll { it.id == id } }
    override suspend fun update(pump: FuelPump) {
        val idx = pumps.indexOfFirst { it.id == pump.id }
        if (idx >= 0) pumps[idx] = pump
    }
}

class FakeOdometerReadingDao : OdometerReadingDao {
    val readings = mutableListOf<OdometerReading>()
    override fun getByVehicle(vehicleId: Long) = flowOf(readings.filter { it.vehicleId == vehicleId })
    override suspend fun getByVehicleList(vehicleId: Long) = readings.filter { it.vehicleId == vehicleId }
    override suspend fun insert(reading: OdometerReading): Long { readings.add(reading); return reading.id }
    override suspend fun deleteById(id: Long) { readings.removeAll { it.id == id } }
}
