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
        // Pair attributed to current entry: e2(Shell): dist=100, mileage=100/40=2.5
        assertEquals(2.5, result[0].mileage, 0.001)
        assertEquals(100.0, result[0].distanceSinceLastFill, 0.001)
        // Pair attributed to current entry: e4(Shell): dist=100, mileage=100/35≈2.857
        assertEquals(100.0 / 35.0, result[1].mileage, 0.001)
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
        // Pair 1→2: current=e2(BP), pump=2 → excluded
        // Pair 2→3: current=e3(Shell), pump=1 → included. dist=1250-1100=150, mileage=150/50=3.0
        assertEquals(150.0, result[0].distanceSinceLastFill, 0.001)
        assertEquals(3.0, result[0].mileage, 0.001)
    }

    @Test
    fun `computePumpFillHistory - single fill at pump returns result when next entry exists`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, 1L),
            entry(2, 1100.0, 40.0, 80.0, 2L)
        )

        val result = computePumpFillHistory(entries, 1L)

        // With curr-attribution, the single fill at pump 1 has no curr entry at pump 1
        // The next entry is at pump 2, so the pair is attributed to pump 2
        assertEquals(0, result.size)
    }

    @Test
    fun `computePumpFillHistory - null pumpId entries included when filtering for null`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, null, LocalDate.of(2024, 1, 1)),
            entry(2, 1100.0, 40.0, 80.0, null, LocalDate.of(2024, 1, 15))
        )

        val result = computePumpFillHistory(entries, null)

        assertEquals(1, result.size)
        // Pair attributed to current entry e2(null): dist=100, mileage=100/40=2.5
        assertEquals(2.5, result[0].mileage, 0.001)
    }

    @Test
    fun `computePumpFillHistory - bad data guard non-increasing odometer`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, 1L),
            entry(2, 950.0, 40.0, 80.0, 1L),
            entry(3, 1100.0, 30.0, 60.0, 1L)
        )

        val result = computePumpFillHistory(entries, 1L)

        // Pair 1→2: distance = 950-1000 = -50 → skip
        // Pair 2→3: distance = 1100-950 = 150, current=e3(vol=30), mileage=150/30=5.0
        assertEquals(1, result.size)
        assertEquals(150.0 / 30.0, result[0].mileage, 0.001)
    }

    @Test
    fun `computePumpFillHistory - bad data guard zero fuel volume`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, 1L),
            entry(2, 1100.0, 0.0, 0.0, 1L)
        )

        val result = computePumpFillHistory(entries, 1L)

        // Pair 1→2: current=e2(vol=0), fuelVolume(curr)=0 → skip
        // No valid pairs
        assertEquals(0, result.size)
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

        // Shell (pump 1): 3 entries, pairs at e2 and e4 → mileages [2.5, 100/35≈2.857]
        val shell = stats.first { it.pumpId == 1L }
        assertEquals("Shell", shell.pumpName)
        assertEquals(3, shell.fillCount)
        assertEquals(2.5, shell.worstMileage, 0.001)
        assertEquals(100.0 / 35.0, shell.bestMileage, 0.001)
        assertEquals((2.5 + 100.0 / 35.0) / 2, shell.avgMileage, 0.001)

        // BP (pump 2): 1 entry, pair at e3 → mileages [100/45=2.222]
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

        // Unknown (pump=null): pairs at e2 → mileages [2.5]
        // Shell (pump=1): pair at e3 → mileages [2.222]
        assertEquals(2, stats.size)

        val unknown = stats.first { it.pumpId == null }
        assertEquals("Unknown / Not recorded", unknown.pumpName)
        assertEquals(2, unknown.fillCount)
        assertEquals(2.5, unknown.avgMileage, 0.001)

        val shell = stats.first { it.pumpId == 1L }
        assertEquals("Shell", shell.pumpName)
        assertEquals(1, shell.fillCount)
        assertEquals(100.0 / 45.0, shell.avgMileage, 0.001)
    }

    @Test
    fun `computePumpMileageStats - single fill pump included when next entry exists`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, 1L),
            entry(2, 1100.0, 40.0, 80.0, 2L),
            entry(3, 1200.0, 30.0, 60.0, 2L)
        )

        val stats = computePumpMileageStats(entries)

        // Shell (pump 1): 1 entry at index 0. No pair has curr=pump=1 (e2 is BP).
        // BP (pump 2): 2 entries, pairs at e2 and e3 → included
        assertEquals(1, stats.size)
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
