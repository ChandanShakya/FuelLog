package com.chandanshakya.fuellog.util

import com.chandanshakya.fuellog.data.model.FuelEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class FullTankMileageTest {

    private fun entry(id: Long, odo: Double, vol: Double, full: Boolean) = FuelEntry(
        id = id, vehicleId = 1, date = LocalDate.of(2024, 1, 1).plusDays(id),
        odometer = odo, fuelVolume = vol, fuelCost = vol * 2.0, isFullTank = full
    )

    @Test
    fun `partial top-up is counted in the full-to-full interval`() {
        val entries = listOf(
            entry(1, 1000.0, 40.0, true),
            entry(2, 1200.0, 10.0, false),  // partial top-up
            entry(3, 1400.0, 10.0, true)    // 400 km on 10 + 10 L = 20 km/L
        )
        val segments = entries.mileageSegments()
        assertEquals(1, segments.size)
        assertEquals(20.0, segments[0].mileage, 0.001)
        assertEquals(0, segments[0].startIndex)
        assertEquals(2, segments[0].endIndex)
    }

    @Test
    fun `falls back to consecutive fills when none are marked full`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, false),
            entry(2, 1500.0, 40.0, false)
        )
        val segments = entries.mileageSegments()
        assertEquals(1, segments.size)
        assertEquals(12.5, segments[0].mileage, 0.001)
    }

    @Test
    fun `average mileage ignores partial fills`() {
        val entries = listOf(
            entry(1, 1000.0, 40.0, true),
            entry(2, 1200.0, 10.0, false),
            entry(3, 1400.0, 10.0, true)
        )
        val avg = MileageCalculator.calculateAverageMileage(
            entries,
            com.chandanshakya.fuellog.data.model.DistanceUnit.KM,
            com.chandanshakya.fuellog.data.model.VolumeUnit.LITERS
        )
        assertEquals(20.0, avg!!, 0.001)
    }

    @Test
    fun `capacity learning adds partial top-ups between full fills`() {
        val full = listOf(entry(1, 1000.0, 40.0, true), entry(2, 1400.0, 30.0, true))
        val partial = listOf(entry(3, 1200.0, 10.0, false))
        val suggestion = suggestCapacity(full, partialFills = partial)
        assertNotNull(suggestion)
        assertEquals(40.0, suggestion!!.learnedCapacity, 0.001)
        assertTrue(suggestion.basedOnFills == 1)
    }
}
