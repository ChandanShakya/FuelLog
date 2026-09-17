package com.chandanshakya.fuellog.util

import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.VolumeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/**
 * Mileage for segment (entry[i] → entry[i+1]) uses volume at entry[i+1] and is
 * shown on entry[i]. The latest fill has no completed tank yet (null).
 */
class EntryMileageAlignmentTest {

    private fun entry(id: Long, odo: Double, vol: Double) = FuelEntry(
        id = id, vehicleId = 1, date = LocalDate.of(2024, 1, 1).plusDays(id),
        odometer = odo, fuelVolume = vol, fuelCost = vol * 2.0
    )

    @Test
    fun `pair i is shown on earlier entry i`() {
        val entries = listOf(
            entry(1, 1000.0, 50.0),
            entry(2, 1500.0, 40.0),  // 500/40 = 12.5 — earned by entry 1
            entry(3, 1800.0, 30.0)   // 300/30 = 10.0 — earned by entry 2
        )
        val pairs = entries.adjacentMileagePairs(
            { it.odometer }, { it.fuelVolume }, DistanceUnit.KM, VolumeUnit.LITERS
        )
        assertEquals(2, pairs.size)

        // Same mapping as FuelLogViewModel: entry[index] gets pairs[index]
        val mileageForEntry = entries.mapIndexed { index, _ ->
            pairs.getOrNull(index)?.mileage
        }

        assertEquals(12.5, mileageForEntry[0]!!, 0.001)  // first fill, once tank used
        assertEquals(10.0, mileageForEntry[1]!!, 0.001)  // second fill
        assertNull(mileageForEntry[2])                   // latest fill still calculating
    }
}
