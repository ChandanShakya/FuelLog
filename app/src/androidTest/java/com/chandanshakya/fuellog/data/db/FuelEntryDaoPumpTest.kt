package com.chandanshakya.fuellog.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.FuelPump
import com.chandanshakya.fuellog.data.model.Vehicle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class FuelEntryDaoPumpTest {

    private lateinit var database: AppDatabase
    private lateinit var entryDao: FuelEntryDao
    private lateinit var pumpDao: FuelPumpDao
    private lateinit var vehicleDao: VehicleDao

    private val vehicleId = 1L

    @Before
    fun setup() = runTest {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        entryDao = database.fuelEntryDao()
        pumpDao = database.fuelPumpDao()
        vehicleDao = database.vehicleDao()

        // Insert a test vehicle (required by foreign key)
        vehicleDao.insert(Vehicle(id = vehicleId, name = "Test Car"))
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun getAllByVehicleWithPump_includes_entries_with_pump() = runTest {
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

        val result = entryDao.getAllByVehicleWithPump(vehicleId).first()
        assertEquals(1, result.size)
        assertEquals("Shell", result[0].pumpName)
        assertEquals(1000.0, result[0].entry.odometer, 0.001)
    }

    @Test
    fun getAllByVehicleWithPump_includes_entries_without_pump() = runTest {
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

        val result = entryDao.getAllByVehicleWithPump(vehicleId).first()
        assertEquals(1, result.size)
        assertNull(result[0].pumpName)
    }

    @Test
    fun getAllByVehicleWithPump_mixed_pumps() = runTest {
        val shellId = pumpDao.insert(FuelPump(name = "Shell"))
        val bpId = pumpDao.insert(FuelPump(name = "BP"))

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
                fuelPumpId = bpId
            )
        )
        entryDao.insert(
            FuelEntry(
                vehicleId = vehicleId,
                date = LocalDate.of(2024, 2, 1),
                odometer = 1200.0,
                fuelVolume = 45.0,
                fuelCost = 90.0,
                fuelPumpId = null
            )
        )

        val result = entryDao.getAllByVehicleWithPump(vehicleId).first()
        assertEquals(3, result.size)
        assertEquals("Shell", result[0].pumpName)
        assertEquals("BP", result[1].pumpName)
        assertNull(result[2].pumpName)
    }

    @Test
    fun getAllByVehicleWithPump_ordered_by_odometer_asc() = runTest {
        val shellId = pumpDao.insert(FuelPump(name = "Shell"))

        entryDao.insert(
            FuelEntry(
                vehicleId = vehicleId,
                date = LocalDate.of(2024, 2, 1),
                odometer = 1200.0,
                fuelVolume = 45.0,
                fuelCost = 90.0,
                fuelPumpId = shellId
            )
        )
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

        val result = entryDao.getAllByVehicleWithPump(vehicleId).first()
        assertEquals(2, result.size)
        assertEquals(1000.0, result[0].entry.odometer, 0.001)
        assertEquals(1200.0, result[1].entry.odometer, 0.001)
    }

    @Test
    fun deleting_pump_sets_fuelPumpId_to_null() = runTest {
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

        pumpDao.deleteById(pumpId)

        val entries = entryDao.getAllByVehicleList(vehicleId)
        assertEquals(1, entries.size)
        assertNull(entries[0].fuelPumpId)
    }

    @Test
    fun getAllByVehicleWithPump_different_vehicles_isolated() = runTest {
        val vehicleId2 = 2L
        vehicleDao.insert(Vehicle(id = vehicleId2, name = "Other Car"))

        val shellId = pumpDao.insert(FuelPump(name = "Shell"))
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
                vehicleId = vehicleId2,
                date = LocalDate.of(2024, 1, 1),
                odometer = 5000.0,
                fuelVolume = 40.0,
                fuelCost = 80.0,
                fuelPumpId = shellId
            )
        )

        val result1 = entryDao.getAllByVehicleWithPump(vehicleId).first()
        val result2 = entryDao.getAllByVehicleWithPump(vehicleId2).first()

        assertEquals(1, result1.size)
        assertEquals(1, result2.size)
        assertEquals(1000.0, result1[0].entry.odometer, 0.001)
        assertEquals(5000.0, result2[0].entry.odometer, 0.001)
    }
}
