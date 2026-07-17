package com.chandanshakya.fuellog.viewmodel

import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.util.MileageCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for MileageCalculator.
 */
class MileageCalculatorTest {

    @Test
    fun testCalculateMileage_WithPrevious() {
        val previous = FuelEntry(
            id = 1,
            vehicleId = 1,
            date = LocalDate.now().minusDays(1),
            odometer = 1000.0,
            fuelVolume = 50.0,
            fuelCost = 100.0
        )
        
        val current = FuelEntry(
            id = 2,
            vehicleId = 1,
            date = LocalDate.now(),
            odometer = 1100.0,
            fuelVolume = 40.0,
            fuelCost = 80.0
        )
        
        val result = MileageCalculator.calculateMileage(
            current = current,
            previous = previous,
            distanceUnit = DistanceUnit.KM,
            volumeUnit = VolumeUnit.LITERS
        )
        
        // (1100 - 1000) / 40 = 25 km/L
        assertEquals(25.0, result, 0.001)
    }

    @Test
    fun testCalculateMileage_WithoutPrevious() {
        val current = FuelEntry(
            id = 1,
            vehicleId = 1,
            date = LocalDate.now(),
            odometer = 1000.0,
            fuelVolume = 50.0,
            fuelCost = 100.0
        )
        
        val result = MileageCalculator.calculateMileage(
            current = current,
            previous = null,
            distanceUnit = DistanceUnit.KM,
            volumeUnit = VolumeUnit.LITERS
        )
        
        assertNull(result)
    }

    @Test
    fun testCalculateMileageBase() {
        val previous = FuelEntry(
            id = 1,
            vehicleId = 1,
            date = LocalDate.now().minusDays(1),
            odometer = 1000.0,
            fuelVolume = 50.0,
            fuelCost = 100.0
        )
        
        val current = FuelEntry(
            id = 2,
            vehicleId = 1,
            date = LocalDate.now(),
            odometer = 1100.0,
            fuelVolume = 40.0,
            fuelCost = 80.0
        )
        
        val result = MileageCalculator.calculateMileageBase(
            current = current,
            previous = previous
        )
        
        // (1100 - 1000) / 40 = 25 km/L
        assertEquals(25.0, result, 0.001)
    }

    @Test
    fun testCalculateAverageMileage() {
        val entries = listOf(
            FuelEntry(1, 1, LocalDate.now().minusDays(2), 1000.0, 50.0, 100.0),
            FuelEntry(2, 1, LocalDate.now().minusDays(1), 1100.0, 40.0, 80.0),
            FuelEntry(3, 1, LocalDate.now(), 1200.0, 30.0, 60.0)
        )
        
        val result = MileageCalculator.calculateAverageMileage(
            entries = entries,
            distanceUnit = DistanceUnit.KM,
            volumeUnit = VolumeUnit.LITERS
        )
        
        // Mileage 1: (1100-1000)/40 = 25 km/L
        // Mileage 2: (1200-1100)/30 = 33.333 km/L
        // Average: (25 + 33.333) / 2 = 29.1665 km/L
        assertEquals(29.1665, result, 0.001)
    }

    @Test
    fun testCalculateTotalDistance() {
        val entries = listOf(
            FuelEntry(1, 1, LocalDate.now().minusDays(2), 1000.0, 50.0, 100.0),
            FuelEntry(2, 1, LocalDate.now().minusDays(1), 1100.0, 40.0, 80.0),
            FuelEntry(3, 1, LocalDate.now(), 1200.0, 30.0, 60.0)
        )
        
        val result = MileageCalculator.calculateTotalDistance(
            entries = entries,
            distanceUnit = DistanceUnit.KM
        )
        
        // 1200 - 1000 = 200 km
        assertEquals(200.0, result, 0.001)
    }

    @Test
    fun testCalculateTotalFuel() {
        val entries = listOf(
            FuelEntry(1, 1, LocalDate.now().minusDays(2), 1000.0, 50.0, 100.0),
            FuelEntry(2, 1, LocalDate.now().minusDays(1), 1100.0, 40.0, 80.0),
            FuelEntry(3, 1, LocalDate.now(), 1200.0, 30.0, 60.0)
        )
        
        val result = MileageCalculator.calculateTotalFuel(
            entries = entries,
            volumeUnit = VolumeUnit.LITERS
        )
        
        // 50 + 40 + 30 = 120 L
        assertEquals(120.0, result, 0.001)
    }

    @Test
    fun testCalculateTotalCost() {
        val entries = listOf(
            FuelEntry(1, 1, LocalDate.now().minusDays(2), 1000.0, 50.0, 100.0),
            FuelEntry(2, 1, LocalDate.now().minusDays(1), 1100.0, 40.0, 80.0),
            FuelEntry(3, 1, LocalDate.now(), 1200.0, 30.0, 60.0)
        )
        
        val result = MileageCalculator.calculateTotalCost(entries)
        
        // 100 + 80 + 60 = 240
        assertEquals(240.0, result, 0.001)
    }

    @Test
    fun testCalculateCostPerDistance() {
        val entries = listOf(
            FuelEntry(1, 1, LocalDate.now().minusDays(2), 1000.0, 50.0, 100.0),
            FuelEntry(2, 1, LocalDate.now().minusDays(1), 1100.0, 40.0, 80.0),
            FuelEntry(3, 1, LocalDate.now(), 1200.0, 30.0, 60.0)
        )
        
        val result = MileageCalculator.calculateCostPerDistance(
            entries = entries,
            distanceUnit = DistanceUnit.KM,
            volumeUnit = VolumeUnit.LITERS
        )
        
        // Total cost: 240, Total distance: 200 km
        // Cost per km: 240 / 200 = 1.2
        assertEquals(1.2, result, 0.001)
    }
}
