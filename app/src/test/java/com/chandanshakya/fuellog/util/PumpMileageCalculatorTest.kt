package com.chandanshakya.fuellog.util

import com.chandanshakya.fuellog.data.db.FuelEntryWithPump
import com.chandanshakya.fuellog.data.model.FuelEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PumpMileageCalculatorTest {

    private fun entry(
        id: Long,
        odometer: Double,
        fuelVolume: Double,
        fuelCost: Double,
        fuelPumpId: Long? = null,
        date: LocalDate = LocalDate.now().minusDays(id)
    ) = FuelEntryWithPump(
        entry = FuelEntry(
            id = id,
            vehicleId = 1,
            date = date,
            odometer = odometer,
            fuelVolume = fuelVolume,
            fuelCost = fuelCost,
            fuelPumpId = fuelPumpId
        ),
        pumpName = when (fuelPumpId) {
            1L -> "Shell"
            2L -> "BP"
            3L -> "Mobil"
            else -> null
        }
    )

    @Test
    fun `computePumpFillHistory - normal multi-pump sequence`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, 1L, LocalDate.of(2024, 1, 1)),
            entry(2, 1100.0, 40.0, 80.0, 1L, LocalDate.of(2024, 1, 15)),
            entry(3, 1200.0, 45.0, 90.0, 2L, LocalDate.of(2024, 2, 1)),
            entry(4, 1300.0, 35.0, 70.0, 1L, LocalDate.of(2024, 2, 15))
        )

        val result = computePumpFillHistory(entries, 1L)

        assertEquals(2, result.size)
        // pair(1→2): prev=Shell(50L), distance=100, mileage=100/50=2.0
        assertEquals(2.0, result[0].mileage, 0.001)
        assertEquals(100.0, result[0].distanceSinceLastFill, 0.001)
        // pair(2→3): prev=Shell(40L), distance=100, mileage=100/40=2.5
        assertEquals(2.5, result[1].mileage, 0.001)
        assertEquals(100.0, result[1].distanceSinceLastFill, 0.001)
        // Sorted by date ascending
        assertTrue(result[0].date < result[1].date)
    }

    @Test
    fun `computePumpFillHistory - pairing runs across all pumps before filtering`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, 1L, LocalDate.of(2024, 1, 1)),
            entry(2, 1100.0, 40.0, 80.0, 2L, LocalDate.of(2024, 1, 15)),
            entry(3, 1250.0, 50.0, 100.0, 1L, LocalDate.of(2024, 2, 1))
        )

        val result = computePumpFillHistory(entries, 1L)

        assertEquals(1, result.size)
        // pair(1→2): prev=Shell(50L), distance=1100-1000=100, mileage=100/50=2.0
        assertEquals(100.0, result[0].distanceSinceLastFill, 0.001)
        assertEquals(100.0 / 50.0, result[0].mileage, 0.001)
    }

    @Test
    fun `computePumpFillHistory - single fill at pump returns result when next entry exists`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, 1L),
            entry(2, 1100.0, 40.0, 80.0, 2L)
        )

        val result = computePumpFillHistory(entries, 1L)

        // pair(1→2): prev=Shell(50L), distance=100, mileage=100/50=2.0
        assertEquals(1, result.size)
        assertEquals(2.0, result[0].mileage, 0.001)
    }

    @Test
    fun `computePumpFillHistory - null pumpId entries included when filtering for null`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, null, LocalDate.of(2024, 1, 1)),
            entry(2, 1100.0, 40.0, 80.0, null, LocalDate.of(2024, 1, 15))
        )

        val result = computePumpFillHistory(entries, null)

        assertEquals(1, result.size)
        // pair(1→2): prev=null(50L), distance=100, mileage=100/50=2.0
        assertEquals(2.0, result[0].mileage, 0.001)
    }

    @Test
    fun `computePumpFillHistory - bad data guard non-increasing odometer`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, 1L),
            entry(2, 950.0, 40.0, 80.0, 1L),
            entry(3, 1100.0, 30.0, 60.0, 1L)
        )

        val result = computePumpFillHistory(entries, 1L)

        // pair 1→2: distance = 950-1000 = -50 → skip
        // pair 2→3: distance = 1100-950 = 150, prev=entry2(40L), mileage=150/40=3.75
        assertEquals(1, result.size)
        assertEquals(150.0 / 40.0, result[0].mileage, 0.001)
    }

    @Test
    fun `computePumpFillHistory - bad data guard zero fuel volume`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, 1L),
            entry(2, 1100.0, 0.0, 0.0, 1L)
        )

        val result = computePumpFillHistory(entries, 1L)

        // pair(1→2): prev=Shell(50L), prev fuelVolume=50 > 0 → valid
        // distance=100, mileage=100/50=2.0
        assertEquals(1, result.size)
        assertEquals(2.0, result[0].mileage, 0.001)
    }

    @Test
    fun `computePumpMileageStats - normal multi-pump sequence`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, 1L, LocalDate.of(2024, 1, 1)),
            entry(2, 1100.0, 40.0, 80.0, 1L, LocalDate.of(2024, 1, 15)),
            entry(3, 1200.0, 45.0, 90.0, 2L, LocalDate.of(2024, 2, 1)),
            entry(4, 1300.0, 35.0, 70.0, 1L, LocalDate.of(2024, 2, 15))
        )

        val stats = computePumpMileageStats(entries)

        assertEquals(2, stats.size)

        // Shell (pump 1): 3 entries, 2 pairs → mileages [2.0, 2.5]
        val shell = stats.first { it.pumpId == 1L }
        assertEquals("Shell", shell.pumpName)
        assertEquals(3, shell.fillCount)
        assertEquals(2.0, shell.worstMileage, 0.001)
        assertEquals(2.5, shell.bestMileage, 0.001)
        assertEquals((2.0 + 2.5) / 2, shell.avgMileage, 0.001)

        // BP (pump 2): 1 entry, 1 pair → mileages [100/45=2.222]
        val bp = stats.first { it.pumpId == 2L }
        assertEquals("BP", bp.pumpName)
        assertEquals(1, bp.fillCount)
        assertEquals(100.0 / 45.0, bp.avgMileage, 0.001)
    }

    @Test
    fun `computePumpMileageStats - null pumpId entries grouped under Unknown`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, null, LocalDate.of(2024, 1, 1)),
            entry(2, 1100.0, 40.0, 80.0, null, LocalDate.of(2024, 1, 15)),
            entry(3, 1200.0, 45.0, 90.0, 1L, LocalDate.of(2024, 2, 1))
        )

        val stats = computePumpMileageStats(entries)

        // Unknown: 2 entries, 2 pairs → mileages [2.0, 2.5]
        // Shell: 1 entry, no next entry → no pairs → excluded
        assertEquals(1, stats.size)

        val unknown = stats.first { it.pumpId == null }
        assertEquals("Unknown / Not recorded", unknown.pumpName)
        assertEquals(2, unknown.fillCount)
        assertEquals(2.25, unknown.avgMileage, 0.001)
    }

    @Test
    fun `computePumpMileageStats - single fill pump included when next entry exists`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, 1L),
            entry(2, 1100.0, 40.0, 80.0, 2L),
            entry(3, 1200.0, 30.0, 60.0, 2L)
        )

        val stats = computePumpMileageStats(entries)

        // Shell: 1 entry, has next entry → 1 pair → included
        // BP: 2 entries → 1 pair → included
        assertEquals(2, stats.size)
        assertTrue(stats.any { it.pumpId == 1L })
        assertTrue(stats.any { it.pumpId == 2L })
    }

    @Test
    fun `computePumpMileageStats - empty input`() {
        val stats = computePumpMileageStats(emptyList())
        assertTrue(stats.isEmpty())
    }

    @Test
    fun `computePumpFillHistory - empty input`() {
        val result = computePumpFillHistory(emptyList(), 1L)
        assertTrue(result.isEmpty())
    }
}
