package com.chandanshakya.fuellog.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chandanshakya.fuellog.data.model.FuelPump
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FuelPumpDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: FuelPumpDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.fuelPumpDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insert_and_getAll() = runTest {
        dao.insert(FuelPump(name = "Shell"))
        dao.insert(FuelPump(name = "BP"))
        dao.insert(FuelPump(name = "Mobil"))

        val pumps = dao.getAll().first()
        assertEquals(3, pumps.size)
        // Ordered by name ASC
        assertEquals("BP", pumps[0].name)
        assertEquals("Mobil", pumps[1].name)
        assertEquals("Shell", pumps[2].name)
    }

    @Test
    fun insert_returns_id() = runTest {
        val id = dao.insert(FuelPump(name = "Shell"))
        assert(id > 0)
    }

    @Test
    fun findByName_exact_match() = runTest {
        dao.insert(FuelPump(name = "Shell"))
        dao.insert(FuelPump(name = "BP"))

        val found = dao.findByName("Shell")
        assertNotNull(found)
        assertEquals("Shell", found!!.name)

        val notFound = dao.findByName("shell")
        assertNull(notFound)
    }

    @Test
    fun findByName_returns_null_for_missing() = runTest {
        assertNull(dao.findByName("Nonexistent"))
    }

    @Test
    fun getAllList_returns_all_pumps() = runTest {
        dao.insert(FuelPump(name = "Shell"))
        dao.insert(FuelPump(name = "BP"))

        val list = dao.getAllList()
        assertEquals(2, list.size)
    }

    @Test
    fun deleteById_removes_pump() = runTest {
        val id = dao.insert(FuelPump(name = "Shell"))
        dao.deleteById(id)

        val pumps = dao.getAll().first()
        assertEquals(0, pumps.size)
    }

    @Test
    fun insert_replace_existing_name() = runTest {
        val id1 = dao.insert(FuelPump(name = "Shell"))
        val id2 = dao.insert(FuelPump(name = "Shell"))
        // REPLACE strategy: second insert replaces first
        assertEquals(id1, id2)
    }

    @Test
    fun getAll_orders_by_name_asc() = runTest {
        dao.insert(FuelPump(name = "Zebra"))
        dao.insert(FuelPump(name = "Alpha"))
        dao.insert(FuelPump(name = "Middle"))

        val pumps = dao.getAll().first()
        assertEquals("Alpha", pumps[0].name)
        assertEquals("Middle", pumps[1].name)
        assertEquals("Zebra", pumps[2].name)
    }
}
