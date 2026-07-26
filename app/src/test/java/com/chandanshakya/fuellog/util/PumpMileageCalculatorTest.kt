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

        assertEquals(3, result.size)
        // e1(Shell): mileage=100/e2.vol(40)=2.5, distanceSinceLast=null (first)
        assertEquals(2.5, result[0].mileage!!, 0.001)
        assertNull(result[0].distanceSinceLastFill)
        // e2(Shell): mileage=100/e3.vol(45)≈2.222, distanceSinceLast=100
        assertEquals(100.0 / 45.0, result[1].mileage!!, 0.001)
        assertEquals(100.0, result[1].distanceSinceLastFill!!, 0.001)
        // e4(Shell): mileage=null (last), distanceSinceLast=100
        assertNull(result[2].mileage)
        assertEquals(100.0, result[2].distanceSinceLastFill!!, 0.001)
        assertTrue(result[0].date < result[1].date && result[1].date < result[2].date)
    }

    @Test
    fun `computePumpFillHistory - pairing runs across all pumps before filtering`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, 1L, LocalDate.of(2024, 1, 1)),
            entry(2, 1100.0, 40.0, 80.0, 2L, LocalDate.of(2024, 1, 15)),
            entry(3, 1250.0, 50.0, 100.0, 1L, LocalDate.of(2024, 2, 1))
        )

        val result = computePumpFillHistory(entries, 1L)

        assertEquals(2, result.size)
        // e1(Shell): mileage=100/e2.vol(40)=2.5
        assertEquals(2.5, result[0].mileage!!, 0.001)
        assertNull(result[0].distanceSinceLastFill)
        // e3(Shell): mileage=null (last), distanceSinceLast=150
        assertNull(result[1].mileage)
        assertEquals(150.0, result[1].distanceSinceLastFill!!, 0.001)
    }

    @Test
    fun `computePumpFillHistory - single fill at pump includes mileage from forward pair`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, 1L),
            entry(2, 1100.0, 40.0, 80.0, 2L)
        )

        val result = computePumpFillHistory(entries, 1L)

        // e1(Shell): mileage=100/e2.vol(40)=2.5
        assertEquals(1, result.size)
        assertEquals(2.5, result[0].mileage!!, 0.001)
    }

    @Test
    fun `computePumpFillHistory - null pumpId entries included when filtering for null`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, null, LocalDate.of(2024, 1, 1)),
            entry(2, 1100.0, 40.0, 80.0, null, LocalDate.of(2024, 1, 15))
        )

        val result = computePumpFillHistory(entries, null)

        assertEquals(2, result.size)
        // e1(null): mileage=100/e2.vol(40)=2.5
        assertEquals(2.5, result[0].mileage!!, 0.001)
        // e2(null): distanceSinceLast=100
        assertEquals(100.0, result[1].distanceSinceLastFill!!, 0.001)
    }

    @Test
    fun `computePumpFillHistory - bad data guard non-increasing odometer`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, 1L),
            entry(2, 950.0, 40.0, 80.0, 1L),
            entry(3, 1100.0, 30.0, 60.0, 1L)
        )

        val result = computePumpFillHistory(entries, 1L)

        // e1: forward dist=-50 → mileage=null, distanceSinceLast=null (first) → excluded
        // e2: forward dist=150, mileage=150/e3.vol(30)=5.0, backward dist=-50 → distanceSinceLast=null
        // e3: backward dist=150, distanceSinceLast=150, mileage=null (last)
        // Sorted by date: e3(now-3), e2(now-2)
        assertEquals(2, result.size)
        assertNull(result[0].mileage)
        assertEquals(150.0, result[0].distanceSinceLastFill!!, 0.001)
        assertEquals(150.0 / 30.0, result[1].mileage!!, 0.001)
        assertNull(result[1].distanceSinceLastFill)
    }

    @Test
    fun `computePumpFillHistory - bad data guard zero fuel volume`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, 1L),
            entry(2, 1100.0, 0.0, 0.0, 1L)
        )

        val result = computePumpFillHistory(entries, 1L)

        // e1: forward dist=100, mileage=100/e2.vol(0) → e2.vol=0, so mileage=null
        //     distanceSinceLast=null (first) → both null → excluded
        // e2: backward dist=100 → distanceSinceLast=100, mileage=null (last)
        assertEquals(1, result.size)
        assertEquals(100.0, result[0].distanceSinceLastFill!!, 0.001)
        assertNull(result[0].mileage)
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

        // Shell (pump 1): e1(mileage=2.5), e2(mileage=2.222), e4(mileage=null) → [2.5, 2.222]
        val shell = stats.first { it.pumpId == 1L }
        assertEquals("Shell", shell.pumpName)
        assertEquals(3, shell.fillCount)
        assertEquals(100.0 / 45.0, shell.worstMileage, 0.001)
        assertEquals(2.5, shell.bestMileage, 0.001)
        assertEquals((2.5 + 100.0 / 45.0) / 2, shell.avgMileage, 0.001)

        // BP (pump 2): e3(mileage=100/e4.vol(35)≈2.857) → [2.857]
        val bp = stats.first { it.pumpId == 2L }
        assertEquals("BP", bp.pumpName)
        assertEquals(1, bp.fillCount)
        assertEquals(100.0 / 35.0, bp.avgMileage, 0.001)
    }

    @Test
    fun `computePumpMileageStats - null pumpId entries grouped under Unknown`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, null, LocalDate.of(2024, 1, 1)),
            entry(2, 1100.0, 40.0, 80.0, null, LocalDate.of(2024, 1, 15)),
            entry(3, 1200.0, 45.0, 90.0, 1L, LocalDate.of(2024, 2, 1))
        )

        val stats = computePumpMileageStats(entries)

        // Unknown (pump=null): e1(mileage=2.5), e2(mileage=2.222) → [2.5, 2.222]
        // Shell (pump=1): e3 is last entry, mileage=null → excluded
        assertEquals(1, stats.size)

        val unknown = stats.first { it.pumpId == null }
        assertEquals("Unknown / Not recorded", unknown.pumpName)
        assertEquals(2, unknown.fillCount)
        assertEquals((2.5 + 100.0 / 45.0) / 2, unknown.avgMileage, 0.001)
    }

    @Test
    fun `computePumpMileageStats - single fill pump included when next entry exists`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, 1L),
            entry(2, 1100.0, 40.0, 80.0, 2L),
            entry(3, 1200.0, 30.0, 60.0, 2L)
        )

        val stats = computePumpMileageStats(entries)

        // Shell (pump 1): e1(mileage=100/e2.vol(40)=2.5) → [2.5]
        // BP (pump 2): e2(mileage=100/e3.vol(30)≈3.333), e3 is last → [3.333]
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
