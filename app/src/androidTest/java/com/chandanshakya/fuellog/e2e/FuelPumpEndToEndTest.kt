package com.chandanshakya.fuellog.e2e

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chandanshakya.fuellog.data.db.AppDatabase
import com.chandanshakya.fuellog.data.db.FuelEntryDao
import com.chandanshakya.fuellog.data.db.FuelPumpDao
import com.chandanshakya.fuellog.data.db.VehicleDao
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.FuelPump
import com.chandanshakya.fuellog.data.model.Vehicle
import com.chandanshakya.fuellog.util.computePumpFillHistory
import com.chandanshakya.fuellog.util.computePumpMileageStats
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class FuelPumpEndToEndTest {

    private lateinit var database: AppDatabase
    private lateinit var entryDao: FuelEntryDao
    private lateinit var pumpDao: FuelPumpDao
    private lateinit var vehicleDao: VehicleDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        entryDao = database.fuelEntryDao()
        pumpDao = database.fuelPumpDao()
        vehicleDao = database.vehicleDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun fullFlow_createPump_addEntries_computeStats() = runTest {
        // 1. Create vehicle
        val vehicleId = vehicleDao.insert(Vehicle(name = "My Car"))

        // 2. Create pumps
        val shellId = pumpDao.insert(FuelPump(name = "Shell"))
        val bpId = pumpDao.insert(FuelPump(name = "BP"))

        // 3. Add fuel entries with pump references
        entryDao.insert(
            FuelEntry(
                vehicleId = vehicleId,
                date = LocalDate.of(2024, 1, 1),
                odometer = 1000.0,
                fuelVolume = 50.0,
                fuelCost = 100.0,
                fuelPumpId = shellId
            )
        )
        entryDao.insert(
            FuelEntry(
                vehicleId = vehicleId,
                date = LocalDate.of(2024, 1, 15),
                odometer = 1100.0,
                fuelVolume = 40.0,
                fuelCost = 80.0,
                fuelPumpId = shellId
            )
        )
        entryDao.insert(
            FuelEntry(
                vehicleId = vehicleId,
                date = LocalDate.of(2024, 2, 1),
                odometer = 1200.0,
                fuelVolume = 45.0,
                fuelCost = 90.0,
                fuelPumpId = bpId
            )
        )
        entryDao.insert(
            FuelEntry(
                vehicleId = vehicleId,
                date = LocalDate.of(2024, 2, 15),
                odometer = 1300.0,
                fuelVolume = 35.0,
                fuelCost = 70.0,
                fuelPumpId = shellId
            )
        )

        // 4. Query with pump data
        val entriesWithPump = entryDao.getAllByVehicleWithPump(vehicleId).first()
        assertEquals(4, entriesWithPump.size)

        // 5. Compute pump mileage stats
        val stats = computePumpMileageStats(entriesWithPump)
        assertEquals(2, stats.size)

        // Shell: entry2 mileage = 100/40 = 2.5, entry4 mileage = 100/35 = 2.857
        val shell = stats.first { it.pumpId == shellId }
        assertEquals("Shell", shell.pumpName)
        assertEquals(2, shell.fillCount)
        assertEquals(2.5, shell.worstMileage, 0.001)
        assertEquals(100.0 / 35.0, shell.bestMileage, 0.001)

        // BP: entry3 mileage = 100/45 = 2.222
        val bp = stats.first { it.pumpId == bpId }
        assertEquals("BP", bp.pumpName)
        assertEquals(1, bp.fillCount)
        assertEquals(100.0 / 45.0, bp.avgMileage, 0.001)

        // 6. Compute fill history for Shell
        val shellHistory = computePumpFillHistory(entriesWithPump, shellId)
        assertEquals(2, shellHistory.size)
        assertEquals(2.5, shellHistory[0].mileage, 0.001)
        assertEquals(100.0 / 35.0, shellHistory[1].mileage, 0.001)

        // 7. Compute fill history for BP
        val bpHistory = computePumpFillHistory(entriesWithPump, bpId)
        assertEquals(1, bpHistory.size)
    }

    @Test
    fun fullFlow_deletePump_clearsEntryReferences() = runTest {
        val vehicleId = vehicleDao.insert(Vehicle(name = "My Car"))
        val pumpId = pumpDao.insert(FuelPump(name = "Shell"))

        entryDao.insert(
            FuelEntry(
                vehicleId = vehicleId,
                date = LocalDate.of(2024, 1, 1),
                odometer = 1000.0,
                fuelVolume = 50.0,
                fuelCost = 100.0,
                fuelPumpId = pumpId
            )
        )

        // Verify pump reference exists
        val before = entryDao.getAllByVehicleList(vehicleId)
        assertEquals(pumpId, before[0].fuelPumpId)

        // Delete pump
        pumpDao.deleteById(pumpId)

        // Verify entry still exists but pump reference is null (SET_NULL)
        val after = entryDao.getAllByVehicleList(vehicleId)
        assertEquals(1, after.size)
        assertNull(after[0].fuelPumpId)
    }

    @Test
    fun fullFlow_unknownPumpEntriesIncluded() = runTest {
        val vehicleId = vehicleDao.insert(Vehicle(name = "My Car"))

        entryDao.insert(
            FuelEntry(
                vehicleId = vehicleId,
                date = LocalDate.of(2024, 1, 1),
                odometer = 1000.0,
                fuelVolume = 50.0,
                fuelCost = 100.0,
                fuelPumpId = null
            )
        )
        entryDao.insert(
            FuelEntry(
                vehicleId = vehicleId,
                date = LocalDate.of(2024, 1, 15),
                odometer = 1100.0,
                fuelVolume = 40.0,
                fuelCost = 80.0,
                fuelPumpId = null
            )
        )

        val entriesWithPump = entryDao.getAllByVehicleWithPump(vehicleId).first()
        val stats = computePumpMileageStats(entriesWithPump)

        assertEquals(1, stats.size)
        assertEquals("Unknown / Not recorded", stats[0].pumpName)
        assertEquals(1, stats[0].fillCount)
        assertEquals(2.5, stats[0].avgMileage, 0.001)
    }

    @Test
    fun fullFlow_resolveOrCreatePump_deduplicates() = runTest {
        val vehicleId = vehicleDao.insert(Vehicle(name = "My Car"))

        // Simulate resolve-or-create pattern
        val existingPump = pumpDao.findByName("Shell")
        val pumpId: Long = if (existingPump != null) {
            existingPump.id
        } else {
            pumpDao.insert(FuelPump(name = "Shell"))
        }

        // Second call should find existing
        val existingPump2 = pumpDao.findByName("Shell")
        assertNotNull(existingPump2)
        assertEquals(pumpId, existingPump2!!.id)

        // Only one pump in DB
        val allPumps = pumpDao.getAllList()
        assertEquals(1, allPumps.size)
    }

    @Test
    fun fullFlow_findByName_caseSensitive() = runTest {
        pumpDao.insert(FuelPump(name = "Shell"))

        assertNotNull(pumpDao.findByName("Shell"))
        assertNull(pumpDao.findByName("shell"))
        assertNull(pumpDao.findByName("SHELL"))
    }
}
