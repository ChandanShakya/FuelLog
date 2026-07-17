package com.chandanshakya.fuellog.viewmodel

import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.util.MileageCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class MileageCalculatorTest {

    @Test
    fun testCalculateMileage_WithPrevious() {
        val previous = FuelEntry(id = 1, vehicleId = 1, date = LocalDate.now().minusDays(1), odometer = 1000.0, fuelVolume = 50.0, fuelCost = 100.0)
        val current = FuelEntry(id = 2, vehicleId = 1, date = LocalDate.now(), odometer = 1100.0, fuelVolume = 40.0, fuelCost = 80.0)

        val result = MileageCalculator.calculateMileage(current, previous, DistanceUnit.KM, VolumeUnit.LITERS)

        // (1100 - 1000) / 40 = 2.5 km/L
        assertEquals(2.5, result!!, 0.001)
    }

    @Test
    fun testCalculateMileage_WithoutPrevious() {
        val current = FuelEntry(id = 1, vehicleId = 1, date = LocalDate.now(), odometer = 1000.0, fuelVolume = 50.0, fuelCost = 100.0)
        assertNull(MileageCalculator.calculateMileage(current, null, DistanceUnit.KM, VolumeUnit.LITERS))
    }

    @Test
    fun testCalculateMileageBase() {
        val previous = FuelEntry(id = 1, vehicleId = 1, date = LocalDate.now().minusDays(1), odometer = 1000.0, fuelVolume = 50.0, fuelCost = 100.0)
        val current = FuelEntry(id = 2, vehicleId = 1, date = LocalDate.now(), odometer = 1100.0, fuelVolume = 40.0, fuelCost = 80.0)

        // (1100 - 1000) / 40 = 2.5 km/L
        assertEquals(2.5, MileageCalculator.calculateMileageBase(current, previous)!!, 0.001)
    }

    @Test
    fun testCalculateAverageMileage() {
        val entries = listOf(
            FuelEntry(1, 1, LocalDate.now().minusDays(2), 1000.0, 50.0, 100.0),
            FuelEntry(2, 1, LocalDate.now().minusDays(1), 1100.0, 40.0, 80.0),
            FuelEntry(3, 1, LocalDate.now(), 1200.0, 30.0, 60.0)
        )

        val result = MileageCalculator.calculateAverageMileage(entries, DistanceUnit.KM, VolumeUnit.LITERS)

        // Mileage 1: (1100-1000)/40 = 2.5, Mileage 2: (1200-1100)/30 = 3.333
        // Average: (2.5 + 3.333) / 2 = 2.9165
        assertEquals(2.9165, result!!, 0.01)
    }

    @Test
    fun testCalculateTotalDistance() {
        val entries = listOf(
            FuelEntry(1, 1, LocalDate.now().minusDays(2), 1000.0, 50.0, 100.0),
            FuelEntry(2, 1, LocalDate.now().minusDays(1), 1100.0, 40.0, 80.0),
            FuelEntry(3, 1, LocalDate.now(), 1200.0, 30.0, 60.0)
        )
        assertEquals(200.0, MileageCalculator.calculateTotalDistance(entries, DistanceUnit.KM), 0.001)
    }

    @Test
    fun testCalculateTotalFuel() {
        val entries = listOf(
            FuelEntry(1, 1, LocalDate.now().minusDays(2), 1000.0, 50.0, 100.0),
            FuelEntry(2, 1, LocalDate.now().minusDays(1), 1100.0, 40.0, 80.0),
            FuelEntry(3, 1, LocalDate.now(), 1200.0, 30.0, 60.0)
        )
        assertEquals(120.0, MileageCalculator.calculateTotalFuel(entries, VolumeUnit.LITERS), 0.001)
    }

    @Test
    fun testCalculateTotalCost() {
        val entries = listOf(
            FuelEntry(1, 1, LocalDate.now().minusDays(2), 1000.0, 50.0, 100.0),
            FuelEntry(2, 1, LocalDate.now().minusDays(1), 1100.0, 40.0, 80.0),
            FuelEntry(3, 1, LocalDate.now(), 1200.0, 30.0, 60.0)
        )
        assertEquals(240.0, MileageCalculator.calculateTotalCost(entries), 0.001)
    }

    @Test
    fun testCalculateCostPerDistance() {
        val entries = listOf(
            FuelEntry(1, 1, LocalDate.now().minusDays(2), 1000.0, 50.0, 100.0),
            FuelEntry(2, 1, LocalDate.now().minusDays(1), 1100.0, 40.0, 80.0),
            FuelEntry(3, 1, LocalDate.now(), 1200.0, 30.0, 60.0)
        )
        // Total cost: 240, Total distance: 200 km → 1.2 per km
        assertEquals(1.2, MileageCalculator.calculateCostPerDistance(entries, DistanceUnit.KM, VolumeUnit.LITERS)!!, 0.001)
    }
}
