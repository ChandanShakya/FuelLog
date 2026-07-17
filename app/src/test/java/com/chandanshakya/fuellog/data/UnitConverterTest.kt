package com.chandanshakya.fuellog.data

import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.util.UnitConverter
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for UnitConverter.
 */
class UnitConverterTest {

    @Test
    fun testToKilometers_KM() {
        val result = UnitConverter.toKilometers(100.0, DistanceUnit.KM)
        assertEquals(100.0, result, 0.001)
    }

    @Test
    fun testToKilometers_MILES() {
        val result = UnitConverter.toKilometers(62.1371, DistanceUnit.MILES)
        assertEquals(100.0, result, 0.001)
    }

    @Test
    fun testFromKilometers_KM() {
        val result = UnitConverter.fromKilometers(100.0, DistanceUnit.KM)
        assertEquals(100.0, result, 0.001)
    }

    @Test
    fun testFromKilometers_MILES() {
        val result = UnitConverter.fromKilometers(100.0, DistanceUnit.MILES)
        assertEquals(62.1371, result, 0.001)
    }

    @Test
    fun testToLiters_LITERS() {
        val result = UnitConverter.toLiters(10.0, VolumeUnit.LITERS)
        assertEquals(10.0, result, 0.001)
    }

    @Test
    fun testToLiters_GALLONS() {
        val result = UnitConverter.toLiters(2.64172, VolumeUnit.GALLONS)
        assertEquals(10.0, result, 0.001)
    }

    @Test
    fun testFromLiters_LITERS() {
        val result = UnitConverter.fromLiters(10.0, VolumeUnit.LITERS)
        assertEquals(10.0, result, 0.001)
    }

    @Test
    fun testFromLiters_GALLONS() {
        val result = UnitConverter.fromLiters(10.0, VolumeUnit.GALLONS)
        assertEquals(2.64172, result, 0.001)
    }

    @Test
    fun testCalculateEfficiencyBase_KM_LITERS() {
        val result = UnitConverter.calculateEfficiencyBase(
            distance = 100.0,
            fuelVolume = 10.0,
            distanceUnit = DistanceUnit.KM,
            volumeUnit = VolumeUnit.LITERS
        )
        assertEquals(10.0, result, 0.001)
    }

    @Test
    fun testCalculateEfficiencyBase_MILES_GALLONS() {
        val result = UnitConverter.calculateEfficiencyBase(
            distance = 100.0,
            fuelVolume = 2.64172,
            distanceUnit = DistanceUnit.MILES,
            volumeUnit = VolumeUnit.GALLONS
        )
        // 100 miles / 2.64172 gallons = 37.8541 km/L
        assertEquals(37.8541, result, 0.001)
    }

    @Test
    fun testConvertEfficiency_KM_LITERS_to_MILES_GALLONS() {
        val result = UnitConverter.convertEfficiency(
            efficiencyKmPerLiter = 10.0,
            distanceUnit = DistanceUnit.MILES,
            volumeUnit = VolumeUnit.GALLONS
        )
        // 10 km/L = 23.5215 mpg
        assertEquals(23.5215, result, 0.001)
    }

    @Test
    fun testGetDistanceUnitLabel() {
        assertEquals("km", UnitConverter.getDistanceUnitLabel(DistanceUnit.KM))
        assertEquals("mi", UnitConverter.getDistanceUnitLabel(DistanceUnit.MILES))
    }

    @Test
    fun testGetVolumeUnitLabel() {
        assertEquals("L", UnitConverter.getVolumeUnitLabel(VolumeUnit.LITERS))
        assertEquals("gal", UnitConverter.getVolumeUnitLabel(VolumeUnit.GALLONS))
    }

    @Test
    fun testGetEfficiencyLabel() {
        assertEquals("km/L", UnitConverter.getEfficiencyLabel(DistanceUnit.KM, VolumeUnit.LITERS))
        assertEquals("mi/gal", UnitConverter.getEfficiencyLabel(DistanceUnit.MILES, VolumeUnit.GALLONS))
    }
}
