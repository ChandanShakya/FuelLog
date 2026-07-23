package com.chandanshakya.fuellog.data

import com.chandanshakya.fuellog.util.Validation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for Validation utilities.
 */
class ValidationTest {

    @Test
    fun testValidateVehicleName_Valid() {
        assertTrue(Validation.validateVehicleName("My Car"))
        assertTrue(Validation.validateVehicleName("Toyota Camry"))
        assertTrue(Validation.validateVehicleName("A"))
    }

    @Test
    fun testValidateVehicleName_Invalid() {
        assertFalse(Validation.validateVehicleName(""))
        assertFalse(Validation.validateVehicleName("   "))
        assertFalse(Validation.validateVehicleName("A".repeat(101)))
    }

    @Test
    fun testValidateOdometer_Valid() {
        assertTrue(Validation.validateOdometer(0.0))
        assertTrue(Validation.validateOdometer(10000.0))
        assertTrue(Validation.validateOdometer(9999999.99))
    }

    @Test
    fun testValidateOdometer_Invalid() {
        assertFalse(Validation.validateOdometer(-1.0))
        assertFalse(Validation.validateOdometer(10000000.0))
    }

    @Test
    fun testValidateFuelVolume_Valid() {
        assertTrue(Validation.validateFuelVolume(0.1))
        assertTrue(Validation.validateFuelVolume(50.0))
        assertTrue(Validation.validateFuelVolume(9999.99))
    }

    @Test
    fun testValidateFuelVolume_Invalid() {
        assertFalse(Validation.validateFuelVolume(0.0))
        assertFalse(Validation.validateFuelVolume(-1.0))
        assertFalse(Validation.validateFuelVolume(10000.0))
    }

    @Test
    fun testValidateFuelCost_Valid() {
        assertTrue(Validation.validateFuelCost(0.0))
        assertTrue(Validation.validateFuelCost(50.0))
        assertTrue(Validation.validateFuelCost(999999.99))
    }

    @Test
    fun testValidateFuelCost_Invalid() {
        assertFalse(Validation.validateFuelCost(-1.0))
        assertFalse(Validation.validateFuelCost(1000000.0))
    }

    @Test
    fun testValidateCurrencyCode_Valid() {
        assertTrue(Validation.validateCurrencyCode("USD"))
        assertTrue(Validation.validateCurrencyCode("EUR"))
        assertTrue(Validation.validateCurrencyCode("INR"))
        assertTrue(Validation.validateCurrencyCode("NPR"))
    }

    @Test
    fun testValidateCurrencyCode_Invalid() {
        assertFalse(Validation.validateCurrencyCode("US"))
        assertFalse(Validation.validateCurrencyCode("USDD"))
        assertFalse(Validation.validateCurrencyCode("123"))
        assertFalse(Validation.validateCurrencyCode(""))
    }

    @Test
    fun testValidateFuelEntry_Valid() {
        assertTrue(Validation.validateFuelEntry(1000.0, 50.0, 100.0))
    }

    @Test
    fun testValidateFuelEntry_Invalid() {
        assertFalse(Validation.validateFuelEntry(-1.0, 50.0, 100.0))
        assertFalse(Validation.validateFuelEntry(1000.0, 0.0, 100.0))
        assertFalse(Validation.validateFuelEntry(1000.0, 50.0, -1.0))
    }

    @Test
    fun testGetVehicleNameError() {
        assertEquals("Vehicle name cannot be empty", Validation.getVehicleNameError(""))
        assertEquals("Vehicle name must be 100 characters or less", Validation.getVehicleNameError("A".repeat(101)))
        assertEquals(null, Validation.getVehicleNameError("Valid Name"))
    }

    @Test
    fun testGetOdometerError() {
        assertEquals("Odometer cannot be negative", Validation.getOdometerError(-1.0))
        assertEquals("Odometer value too large", Validation.getOdometerError(10000000.0))
        assertEquals(null, Validation.getOdometerError(1000.0))
    }

    @Test
    fun testGetFuelVolumeError() {
        assertEquals("Fuel volume must be greater than 0", Validation.getFuelVolumeError(0.0))
        assertEquals("Fuel volume too large", Validation.getFuelVolumeError(10000.0))
        assertEquals(null, Validation.getFuelVolumeError(50.0))
    }

    @Test
    fun testGetFuelCostError() {
        assertEquals("Cost cannot be negative", Validation.getFuelCostError(-1.0))
        assertEquals("Cost value too large", Validation.getFuelCostError(1000000.0))
        assertEquals(null, Validation.getFuelCostError(100.0))
    }
}
