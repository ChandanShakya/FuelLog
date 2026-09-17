package com.chandanshakya.fuellog.util

import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.VolumeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/**
 * Documents the intended per-card mileage attribution: each fill-up shows
 * distance-since-previous / volume-added-at-this-fill ("what this refuel earned").
 */
class EntryMileageAlignmentTest {

    private fun entry(id: Long, odo: Double, vol: Double) = FuelEntry(
        id = id, vehicleId = 1, date = LocalDate.of(2024, 1, 1).plusDays(id),
        odometer = odo, fuelVolume = vol, fuelCost = vol * 2.0
    )

    @Test
    fun `pair i belongs to entry i+1`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0),
            entry(2, 1500.0, 40.0),  // 500/40 = 12.5
            entry(3, 1800.0, 30.0)   // 300/30 = 10.0
        )
        val pairs = entries.adjacentMileagePairs(
            { it.odometer }, { it.fuelVolume }, DistanceUnit.KM, VolumeUnit.LITERS
        )
        assertEquals(2, pairs.size)

        // Same mapping as FuelLogViewModel: entry[index] gets pairs[index - 1]
        val mileageForEntry = entries.mapIndexed { index, _ ->
            pairs.getOrNull(index - 1)?.mileage
        }

        assertNull(mileageForEntry[0])   // first fill has no previous
        assertEquals(12.5, mileageForEntry[1]!!, 0.001)
        assertEquals(10.0, mileageForEntry[2]!!, 0.001)
    }
}
