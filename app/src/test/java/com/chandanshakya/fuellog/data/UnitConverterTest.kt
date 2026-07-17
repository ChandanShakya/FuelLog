package com.chandanshakya.fuellog.data

import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.util.UnitConverter
import org.junit.Assert.assertEquals
import org.junit.Test

class UnitConverterTest {

    @Test
    fun testToKilometers_KM() {
        assertEquals(100.0, UnitConverter.toKilometers(100.0, DistanceUnit.KM), 0.001)
    }

    @Test
    fun testToKilometers_MILES() {
        assertEquals(100.0, UnitConverter.toKilometers(62.1371, DistanceUnit.MILES), 0.001)
    }

    @Test
    fun testFromKilometers_KM() {
        assertEquals(100.0, UnitConverter.fromKilometers(100.0, DistanceUnit.KM), 0.001)
    }

    @Test
    fun testFromKilometers_MILES() {
        assertEquals(62.1371, UnitConverter.fromKilometers(100.0, DistanceUnit.MILES), 0.001)
    }

    @Test
    fun testToLiters_LITERS() {
        assertEquals(10.0, UnitConverter.toLiters(10.0, VolumeUnit.LITERS), 0.001)
    }

    @Test
    fun testToLiters_GALLONS() {
        assertEquals(10.0, UnitConverter.toLiters(2.64172, VolumeUnit.GALLONS), 0.001)
    }

    @Test
    fun testFromLiters_LITERS() {
        assertEquals(10.0, UnitConverter.fromLiters(10.0, VolumeUnit.LITERS), 0.001)
    }

    @Test
    fun testFromLiters_GALLONS() {
        assertEquals(2.64172, UnitConverter.fromLiters(10.0, VolumeUnit.GALLONS), 0.001)
    }

    @Test
    fun testCalculateEfficiencyBase_KM_LITERS() {
        val result = UnitConverter.calculateEfficiencyBase(
            distance = 100.0, fuelVolume = 10.0,
            distanceUnit = DistanceUnit.KM, volumeUnit = VolumeUnit.LITERS
        )
        assertEquals(10.0, result, 0.001)
    }

    @Test
    fun testCalculateEfficiencyBase_MILES_GALLONS() {
        val result = UnitConverter.calculateEfficiencyBase(
            distance = 100.0, fuelVolume = 2.64172,
            distanceUnit = DistanceUnit.MILES, volumeUnit = VolumeUnit.GALLONS
        )
        // 100 miles = 160.9344 km, 2.64172 gal = 10.0 L → 16.0934 km/L
        assertEquals(16.0934, result, 0.01)
    }

    @Test
    fun testConvertEfficiency_KM_LITERS_to_MILES_GALLONS() {
        val result = UnitConverter.convertEfficiency(
            efficiencyKmPerLiter = 10.0,
            distanceUnit = DistanceUnit.MILES,
            volumeUnit = VolumeUnit.GALLONS
        )
        // 10 km/L × 3.78541 / 1.609344 = 23.5215 mpg
        assertEquals(23.5215, result, 0.01)
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
