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
        // entry2: distance 100 / 40 = 2.5 km/L, attributed to entry2 (pump 1)
        assertEquals(2.5, result[0].mileage, 0.001)
        assertEquals(100.0, result[0].distanceSinceLastFill, 0.001)
        // entry4: distance 100 / 35 = 2.857 km/L, attributed to entry4 (pump 1)
        // BUT distance is from entry3 (odometer 1200) to entry4 (odometer 1300) = 100
        assertEquals(100.0 / 35.0, result[1].mileage, 0.001)
        // Sorted by date ascending
        assertTrue(result[0].date < result[1].date)
    }

    @Test
    fun `computePumpFillHistory - pairing runs across all pumps before filtering`() {
        // This verifies that distance calculation uses the actual previous entry,
        // not just the previous entry at the same pump
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, 1L, LocalDate.of(2024, 1, 1)),
            entry(2, 1100.0, 40.0, 80.0, 2L, LocalDate.of(2024, 1, 15)), // different pump
            entry(3, 1250.0, 50.0, 100.0, 1L, LocalDate.of(2024, 2, 1))  // back to pump 1
        )

        val result = computePumpFillHistory(entries, 1L)

        assertEquals(1, result.size)
        // entry3: distance 1250 - 1100 = 150 (from entry2, not entry1)
        assertEquals(150.0, result[0].distanceSinceLastFill, 0.001)
        assertEquals(150.0 / 50.0, result[0].mileage, 0.001)
    }

    @Test
    fun `computePumpFillHistory - single fill at pump returns empty`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, 1L),
            entry(2, 1100.0, 40.0, 80.0, 2L)
        )

        val result = computePumpFillHistory(entries, 1L)

        // Only one fill at pump 1, no pair → no mileage data
        assertTrue(result.isEmpty())
    }

    @Test
    fun `computePumpFillHistory - null pumpId entries included when filtering for null`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, null, LocalDate.of(2024, 1, 1)),
            entry(2, 1100.0, 40.0, 80.0, null, LocalDate.of(2024, 1, 15))
        )

        val result = computePumpFillHistory(entries, null)

        assertEquals(1, result.size)
        assertEquals(2.5, result[0].mileage, 0.001)
    }

    @Test
    fun `computePumpFillHistory - bad data guard non-increasing odometer`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, 1L),
            entry(2, 950.0, 40.0, 80.0, 1L),  // odometer went backwards
            entry(3, 1100.0, 30.0, 60.0, 1L)
        )

        val result = computePumpFillHistory(entries, 1L)

        // Pair 1-2 skipped (distance -50), pair 2-3 skipped (distance 150 but pair 1-2 was bad)
        // Actually pair 1-2: distance = 950-1000 = -50 → skip
        // Pair 2-3: distance = 1100-950 = 150, fuelVolume 30 > 0 → valid, attributed to entry3
        assertEquals(1, result.size)
        assertEquals(150.0 / 30.0, result[0].mileage, 0.001)
    }

    @Test
    fun `computePumpFillHistory - bad data guard zero fuel volume`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, 1L),
            entry(2, 1100.0, 0.0, 0.0, 1L)  // zero fuel volume
        )

        val result = computePumpFillHistory(entries, 1L)

        assertTrue(result.isEmpty())
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

        // Shell (pump 1): pair1=2.5, pair3=2.857 → avg=2.679, best=2.857, worst=2.5, count=2
        val shell = stats.first { it.pumpId == 1L }
        assertEquals("Shell", shell.pumpName)
        assertEquals(2, shell.fillCount)
        assertEquals(2.5, shell.worstMileage, 0.001)
        assertEquals(100.0 / 35.0, shell.bestMileage, 0.001)
        assertEquals((2.5 + 100.0 / 35.0) / 2, shell.avgMileage, 0.001)

        // BP (pump 2): pair2=100/45=2.222 → avg=2.222, best=2.222, worst=2.222, count=1
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

        assertEquals(2, stats.size)

        val unknown = stats.first { it.pumpId == null }
        assertEquals("Unknown / Not recorded", unknown.pumpName)
        assertEquals(1, unknown.fillCount)
        assertEquals(2.5, unknown.avgMileage, 0.001)

        val shell = stats.first { it.pumpId == 1L }
        assertEquals(1, shell.fillCount)
    }

    @Test
    fun `computePumpMileageStats - single fill pump excluded`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, 1L),
            entry(2, 1100.0, 40.0, 80.0, 2L),
            entry(3, 1200.0, 30.0, 60.0, 2L)
        )

        val stats = computePumpMileageStats(entries)

        // Pump 1 only has one fill (entry1) → no pair → excluded
        // Pump 2 has two fills (entry2, entry3) → one pair → included
        assertEquals(1, stats.size)
        assertEquals(2L, stats[0].pumpId)
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
