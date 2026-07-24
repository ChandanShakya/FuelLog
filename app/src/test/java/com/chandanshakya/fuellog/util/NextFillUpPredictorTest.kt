package com.chandanshakya.fuellog.util

import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.OdometerReading
import com.chandanshakya.fuellog.data.model.VolumeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class NextFillUpPredictorTest {

    private fun fullEntry(
        id: Long, odometer: Double, fuelVolume: Double,
        date: LocalDate = LocalDate.of(2024, 1, 1).plusDays(id * 5)
    ) = FuelEntry(
        id = id, vehicleId = 1, date = date,
        odometer = odometer, fuelVolume = fuelVolume,
        fuelCost = fuelVolume * 2.0, isFullTank = true
    )

    private fun partialEntry(
        id: Long, odometer: Double, fuelVolume: Double,
        date: LocalDate = LocalDate.of(2024, 1, 1).plusDays(id * 5)
    ) = FuelEntry(
        id = id, vehicleId = 1, date = date,
        odometer = odometer, fuelVolume = fuelVolume,
        fuelCost = fuelVolume * 2.0, isFullTank = false
    )

    private fun reading(
        id: Long, odometer: Double,
        date: LocalDate = LocalDate.of(2024, 1, 1).plusDays(id * 5)
    ) = OdometerReading(id = id, vehicleId = 1, date = date, odometer = odometer)

    @Test
    fun `computeRecencyWeightedMileage - normal full-tank pairs`() {
        val entries = listOf(
            fullEntry(1, 1000.0, 50.0),
            fullEntry(2, 1500.0, 50.0),
            fullEntry(3, 2000.0, 50.0)
        )

        // Mileages: 500/50=10, 500/50=10 → avg=10
        val result = computeRecencyWeightedMileage(entries)
        assertNotNull(result)
        assertEquals(10.0, result!!, 0.001)
    }

    @Test
    fun `computeRecencyWeightedMileage - window size limits to recent pairs`() {
        val entries = listOf(
            fullEntry(1, 1000.0, 50.0),   // mileage 10
            fullEntry(2, 1500.0, 50.0),   // mileage 10
            fullEntry(3, 2000.0, 50.0),   // mileage 10
            fullEntry(4, 2600.0, 50.0),   // mileage 12
            fullEntry(5, 3200.0, 50.0)    // mileage 12
        )

        // With windowSize=2: last 2 pairs are (3→4: 12, 4→5: 12) → avg=12
        val result = computeRecencyWeightedMileage(entries, windowSize = 2)
        assertNotNull(result)
        assertEquals(12.0, result!!, 0.001)
    }

    @Test
    fun `computeRecencyWeightedMileage - single entry returns null`() {
        val entries = listOf(fullEntry(1, 1000.0, 50.0))
        assertNull(computeRecencyWeightedMileage(entries))
    }

    @Test
    fun `computeRecencyWeightedMileage - empty input returns null`() {
        assertNull(computeRecencyWeightedMileage(emptyList()))
    }

    @Test
    fun `computeRecencyWeightedMileage - skips bad data pairs`() {
        val entries = listOf(
            fullEntry(1, 1000.0, 50.0),   // mileage 10
            fullEntry(2, 1500.0, 0.0),    // zero volume → skip
            fullEntry(3, 2000.0, 50.0)    // mileage from entry2→3: 500/50=10
        )

        val result = computeRecencyWeightedMileage(entries)
        assertNotNull(result)
        assertEquals(10.0, result!!, 0.001)
    }

    @Test
    fun `predictNextFillUp - normal prediction`() {
        val entries = listOf(
            fullEntry(1, 1000.0, 50.0, LocalDate.of(2024, 1, 1)),
            fullEntry(2, 1500.0, 50.0, LocalDate.of(2024, 1, 15)),
            fullEntry(3, 2000.0, 50.0, LocalDate.of(2024, 2, 1))
        )

        // recentMileage = 10 km/L, tankCapacity = 40 L
        // remainingDistance = 40 * 10 = 400 km
        val result = predictNextFillUp(
            entries = entries,
            odometerReadings = emptyList(),
            tankCapacity = 40.0,
            distanceUnit = DistanceUnit.KM,
            volumeUnit = VolumeUnit.LITERS
        )

        assertNotNull(result)
        assertEquals(400.0, result!!.remainingDistance, 0.001)
        assertEquals(10.0, result.recentMileage, 0.001)
        assertEquals(40.0, result.tankCapacity, 0.001)
    }

    @Test
    fun `predictNextFillUp - no tank capacity returns null`() {
        val entries = listOf(
            fullEntry(1, 1000.0, 50.0),
            fullEntry(2, 1500.0, 50.0)
        )

        assertNull(predictNextFillUp(
            entries = entries,
            odometerReadings = emptyList(),
            tankCapacity = null,
            distanceUnit = DistanceUnit.KM,
            volumeUnit = VolumeUnit.LITERS
        ))
    }

    @Test
    fun `predictNextFillUp - insufficient full-tank entries returns null`() {
        val entries = listOf(
            fullEntry(1, 1000.0, 50.0)
        )

        assertNull(predictNextFillUp(
            entries = entries,
            odometerReadings = emptyList(),
            tankCapacity = 40.0,
            distanceUnit = DistanceUnit.KM,
            volumeUnit = VolumeUnit.LITERS
        ))
    }

    @Test
    fun `predictNextFillUp - uses odometer reading when more recent`() {
        val entries = listOf(
            fullEntry(1, 1000.0, 50.0, LocalDate.of(2024, 1, 1)),
            fullEntry(2, 1500.0, 50.0, LocalDate.of(2024, 1, 15))
        )
        val readings = listOf(
            reading(1, 1800.0, LocalDate.of(2024, 2, 1))  // more recent than last entry
        )

        val result = predictNextFillUp(
            entries = entries,
            odometerReadings = readings,
            tankCapacity = 40.0,
            distanceUnit = DistanceUnit.KM,
            volumeUnit = VolumeUnit.LITERS
        )

        assertNotNull(result)
        // recentMileage = 500/50 = 10, remainingDistance = 40*10 = 400
        assertEquals(400.0, result!!.remainingDistance, 0.001)
    }

    @Test
    fun `predictNextFillUp - gallon unit conversion works`() {
        // Use consistent units: odometer in user's distance unit, fuel in user's volume unit
        // The calculation does: distance / fuelVolume in user units, then remaining = capacity * mileage
        val entries = listOf(
            fullEntry(1, 1000.0, 13.2, LocalDate.of(2024, 1, 1)),   // 1000 mi, 13.2 gal
            fullEntry(2, 1500.0, 13.2, LocalDate.of(2024, 1, 15))   // 1500 mi, 13.2 gal
        )

        val result = predictNextFillUp(
            entries = entries,
            odometerReadings = emptyList(),
            tankCapacity = 10.6,  // 10.6 gal
            distanceUnit = DistanceUnit.MILES,
            volumeUnit = VolumeUnit.GALLONS
        )

        assertNotNull(result)
        // mileage = 500 mi / 13.2 gal = 37.879 mi/gal
        // remaining = 10.6 gal * 37.879 mi/gal = 401.52 mi
        assertEquals(10.6 * (500.0 / 13.2), result!!.remainingDistance, 1.0)
    }

    @Test
    fun `predictNextFillUp - zero volume entries skipped for mileage`() {
        val entries = listOf(
            fullEntry(1, 1000.0, 50.0, LocalDate.of(2024, 1, 1)),
            partialEntry(2, 1500.0, 0.0, LocalDate.of(2024, 1, 8)),  // zero volume
            fullEntry(3, 2000.0, 50.0, LocalDate.of(2024, 1, 15))
        )

        val result = predictNextFillUp(
            entries = entries,
            odometerReadings = emptyList(),
            tankCapacity = 40.0,
            distanceUnit = DistanceUnit.KM,
            volumeUnit = VolumeUnit.LITERS
        )

        assertNotNull(result)
        // fullTankEntries = [entry1, entry3] (entry2 excluded: not full tank)
        // Pair 1-3: distance = 2000-1000 = 1000, fuelVolume = 50 → mileage = 20
        assertEquals(20.0, result!!.recentMileage, 0.001)
    }
}
