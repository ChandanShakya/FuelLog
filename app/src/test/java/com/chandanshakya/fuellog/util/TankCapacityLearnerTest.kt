package com.chandanshakya.fuellog.util

import com.chandanshakya.fuellog.data.model.FuelEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class TankCapacityLearnerTest {

    private fun fullTankEntry(
        id: Long,
        odometer: Double,
        fuelVolume: Double,
        date: LocalDate = LocalDate.of(2024, 1, 1).plusDays(id)
    ) = FuelEntry(
        id = id,
        vehicleId = 1,
        date = date,
        odometer = odometer,
        fuelVolume = fuelVolume,
        fuelCost = fuelVolume * 2.0,
        isFullTank = true
    )

    private fun partialEntry(
        id: Long,
        odometer: Double,
        fuelVolume: Double,
        date: LocalDate = LocalDate.of(2024, 1, 1).plusDays(id)
    ) = FuelEntry(
        id = id,
        vehicleId = 1,
        date = date,
        odometer = odometer,
        fuelVolume = fuelVolume,
        fuelCost = fuelVolume * 2.0,
        isFullTank = false
    )

    @Test
    fun `suggestCapacity - normal consecutive full-tank fills`() {
        val entries = listOf(
            fullTankEntry(1, 1000.0, 40.0, LocalDate.of(2024, 1, 1)),
            fullTankEntry(2, 1500.0, 42.0, LocalDate.of(2024, 1, 15)),
            fullTankEntry(3, 2000.0, 38.0, LocalDate.of(2024, 2, 1)),
            fullTankEntry(4, 2500.0, 41.0, LocalDate.of(2024, 2, 15))
        )

        val result = suggestCapacity(entries)

        assertNotNull(result)
        // Volumes: 42, 38, 41 → sorted: 38, 41, 42 → median = 41
        assertEquals(41.0, result!!.learnedCapacity, 0.001)
        assertEquals(3, result.basedOnFills)
        assertEquals("Low", result.confidence)
    }

    @Test
    fun `suggestCapacity - even number of pairs uses average of middle two`() {
        val entries = listOf(
            fullTankEntry(1, 1000.0, 40.0),
            fullTankEntry(2, 1500.0, 42.0),
            fullTankEntry(3, 2000.0, 38.0),
            fullTankEntry(4, 2500.0, 44.0)
        )

        val result = suggestCapacity(entries)

        assertNotNull(result)
        // Volumes: 42, 38, 44 → sorted: 38, 42, 44 → median = 42
        assertEquals(42.0, result!!.learnedCapacity, 0.001)
        assertEquals(3, result.basedOnFills)
    }

    @Test
    fun `suggestCapacity - single full-tank fill returns null`() {
        val entries = listOf(fullTankEntry(1, 1000.0, 40.0))
        assertNull(suggestCapacity(entries))
    }

    @Test
    fun `suggestCapacity - only full-tank entries passed in`() {
        // suggestCapacity expects pre-filtered full-tank entries
        // When caller filters isFullTank=1, entries 1 and 3 remain, but they're not
        // consecutive in the original list — the caller should provide only full-tank entries
        val fullTankOnly = listOf(
            fullTankEntry(1, 1000.0, 40.0),
            fullTankEntry(3, 2000.0, 45.0)
        )

        val result = suggestCapacity(fullTankOnly)

        assertNotNull(result)
        // Only one pair: volume = 45
        assertEquals(45.0, result!!.learnedCapacity, 0.001)
        assertEquals(1, result.basedOnFills)
    }

    @Test
    fun `suggestCapacity - empty input returns null`() {
        assertNull(suggestCapacity(emptyList()))
    }

    @Test
    fun `suggestCapacity - non-increasing odometer skipped`() {
        val entries = listOf(
            fullTankEntry(1, 1000.0, 40.0),
            fullTankEntry(2, 950.0, 42.0),  // odometer went backwards
            fullTankEntry(3, 1100.0, 38.0)
        )

        val result = suggestCapacity(entries)

        assertNotNull(result)
        // Pair 1-2: distance -50, skipped. Pair 2-3: distance 150, volume 38 → valid
        assertEquals(38.0, result!!.learnedCapacity, 0.001)
        assertEquals(1, result.basedOnFills)
    }

    @Test
    fun `shouldSuggestUpdate - no stored capacity always suggests`() {
        val entries = listOf(
            fullTankEntry(1, 1000.0, 40.0),
            fullTankEntry(2, 1500.0, 42.0)
        )

        val result = shouldSuggestUpdate(null, entries)
        assertNotNull(result)
    }

    @Test
    fun `shouldSuggestUpdate - capacity within 5 percent no suggestion`() {
        val entries = listOf(
            fullTankEntry(1, 1000.0, 40.0),
            fullTankEntry(2, 1500.0, 42.0)
        )
        // learned = 42, stored = 41 → diff = 1/41 = 2.4% → no suggestion
        assertNull(shouldSuggestUpdate(41.0, entries))
    }

    @Test
    fun `shouldSuggestUpdate - capacity differs more than 5 percent suggests`() {
        val entries = listOf(
            fullTankEntry(1, 1000.0, 40.0),
            fullTankEntry(2, 1500.0, 50.0)
        )
        // learned = 50, stored = 40 → diff = 10/40 = 25% → suggestion
        val result = shouldSuggestUpdate(40.0, entries)
        assertNotNull(result)
        assertEquals(50.0, result!!.learnedCapacity, 0.001)
    }

    @Test
    fun `suggestCapacity - confidence levels`() {
        // 2 pairs → Low
        val entries2 = listOf(
            fullTankEntry(1, 1000.0, 40.0),
            fullTankEntry(2, 1500.0, 42.0),
            fullTankEntry(3, 2000.0, 38.0)
        )
        assertEquals("Low", suggestCapacity(entries2)!!.confidence)

        // 4 pairs → Medium
        val entries4 = listOf(
            fullTankEntry(1, 1000.0, 40.0),
            fullTankEntry(2, 1500.0, 42.0),
            fullTankEntry(3, 2000.0, 38.0),
            fullTankEntry(4, 2500.0, 44.0),
            fullTankEntry(5, 3000.0, 41.0)
        )
        assertEquals("Medium", suggestCapacity(entries4)!!.confidence)

        // 7+ pairs → High
        val entries7 = (1L..8L).map { i ->
            fullTankEntry(i, 1000.0 + i * 500, 40.0 + (i % 3))
        }
        assertEquals("High", suggestCapacity(entries7)!!.confidence)
    }
}
