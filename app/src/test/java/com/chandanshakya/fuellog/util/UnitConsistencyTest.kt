package com.chandanshakya.fuellog.util

import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.VolumeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate

class UnitConsistencyTest {

    private fun createEntry(
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

    @Test
    fun `adjacentMileagePairs with MI GAL returns mi per gal`() {
        val entries = listOf(
            createEntry(1, 1000.0, 13.2),
            createEntry(2, 1500.0, 13.2)
        )

        val pairs = entries.adjacentMileagePairs(
            { it.odometer }, { it.fuelVolume },
            DistanceUnit.MILES, VolumeUnit.GALLONS
        )
        assertEquals(1, pairs.size)

        // Returns mileage in user units: 500 mi / 13.2 gal = 37.879 mi/gal
        assertEquals(500.0 / 13.2, pairs[0].mileage, 0.001)
    }

    @Test
    fun `adjacentMileagePairs with KM L returns km per L`() {
        val entries = listOf(
            createEntry(1, 1000.0, 50.0),
            createEntry(2, 1500.0, 50.0)
        )

        val pairs = entries.adjacentMileagePairs(
            { it.odometer }, { it.fuelVolume },
            DistanceUnit.KM, VolumeUnit.LITERS
        )
        assertEquals(1, pairs.size)
        assertEquals(10.0, pairs[0].mileage, 0.001)
    }

    @Test
    fun `MileageCalculator calculateMileage with MI GAL`() {
        val entry1 = createEntry(1, 1000.0, 13.2)
        val entry2 = createEntry(2, 1500.0, 13.2)

        val mileage = MileageCalculator.calculateMileage(
            current = entry2,
            previous = entry1,
            distanceUnit = DistanceUnit.MILES,
            volumeUnit = VolumeUnit.GALLONS
        )

        assertNotNull(mileage)
        // 500 mi / 13.2 gal = 37.879 mi/gal
        assertEquals(500.0 / 13.2, mileage!!, 0.001)
    }

    @Test
    fun `MileageCalculator calculateAverageMileage with MI GAL`() {
        val entries = listOf(
            createEntry(1, 1000.0, 13.2),
            createEntry(2, 1500.0, 13.2),
            createEntry(3, 2000.0, 13.2)
        )

        val averageMileage = MileageCalculator.calculateAverageMileage(
            entries = entries,
            distanceUnit = DistanceUnit.MILES,
            volumeUnit = VolumeUnit.GALLONS
        )

        assertNotNull(averageMileage)
        // 500/13.2 = 37.879 mi/gal
        assertEquals(500.0 / 13.2, averageMileage!!, 0.001)
    }

    @Test
    fun `MileageCalculator calculateTotalFuel with MI GAL`() {
        val entries = listOf(
            createEntry(1, 1000.0, 13.2),
            createEntry(2, 1500.0, 13.2),
            createEntry(3, 2000.0, 13.2)
        )

        val totalFuel = MileageCalculator.calculateTotalFuel(
            entries = entries,
            volumeUnit = VolumeUnit.GALLONS
        )

        // 13.2 * 3 = 39.6 gallons
        assertEquals(39.6, totalFuel, 0.001)
    }

    @Test
    fun `MileageCalculator calculateTotalFuel with KM L`() {
        val entries = listOf(
            createEntry(1, 1000.0, 50.0),
            createEntry(2, 1500.0, 50.0),
            createEntry(3, 2000.0, 50.0)
        )

        val totalFuel = MileageCalculator.calculateTotalFuel(
            entries = entries,
            volumeUnit = VolumeUnit.LITERS
        )

        assertEquals(150.0, totalFuel, 0.001)
    }

    @Test
    fun `TankCapacityLearner should use raw volume as-is when in same unit`() {
        val entries = listOf(
            createEntry(1, 1000.0, 13.2),
            createEntry(2, 1500.0, 13.2),
            createEntry(3, 2000.0, 13.2),
            createEntry(4, 2500.0, 13.2)
        )

        val suggestion = suggestCapacity(entries)

        assertNotNull(suggestion)
        assertEquals(13.2, suggestion!!.learnedCapacity, 0.001)
    }

    @Test
    fun `NextFillUpPredictor with MI GAL`() {
        val entries = listOf(
            createEntry(1, 1000.0, 13.2, LocalDate.of(2024, 1, 1)),
            createEntry(2, 1500.0, 13.2, LocalDate.of(2024, 1, 15)),
            createEntry(3, 2000.0, 13.2, LocalDate.of(2024, 2, 1))
        )

        val result = predictNextFillUp(
            entries = entries,
            odometerReadings = emptyList(),
            tankCapacity = 10.6,
            distanceUnit = DistanceUnit.MILES,
            volumeUnit = VolumeUnit.GALLONS
        )

        assertNotNull(result)
        // mileage = 500/13.2 = 37.879 mi/gal
        // remaining = 10.6 * 37.879 = 401.52 mi
        assertEquals(10.6 * (500.0 / 13.2), result!!.remainingDistance, 1.0)
    }
}
