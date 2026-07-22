package com.chandanshakya.fuellog.data

import com.chandanshakya.fuellog.util.CurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for CurrencyFormatter.
 */
class CurrencyFormatterTest {

    @Test
    fun testFormatCurrency_USD() {
        val result = CurrencyFormatter.formatCurrency(123.456, "USD")
        assertEquals("$123.46", result)
    }

    @Test
    fun testFormatCurrency_EUR() {
        val result = CurrencyFormatter.formatCurrency(123.456, "EUR")
        assertEquals("\u20AC123.46", result)
    }

    @Test
    fun testFormatCurrency_INR() {
        val result = CurrencyFormatter.formatCurrency(123.456, "INR")
        assertEquals("\u20B9123.46", result)
    }

    @Test
    fun testFormatCurrency_NPR() {
        val result = CurrencyFormatter.formatCurrency(123.456, "NPR")
        // JDK may return "₨" symbol or "NPR" as symbol depending on locale data
        assertTrue(result == "\u20A8123.46" || result == "NPR123.46")
    }

    @Test
    fun testFormatCurrency_Unknown() {
        val result = CurrencyFormatter.formatCurrency(123.456, "XYZ")
        assertEquals("XYZ 123.46", result)
    }

    @Test
    fun testFormatCurrency_CaseInsensitive() {
        val result = CurrencyFormatter.formatCurrency(123.456, "usd")
        assertEquals("$123.46", result)
    }

    @Test
    fun testGetCurrencySymbol_USD() {
        val result = CurrencyFormatter.getCurrencySymbol("USD")
        assertEquals("$", result)
    }

    @Test
    fun testGetCurrencySymbol_EUR() {
        val result = CurrencyFormatter.getCurrencySymbol("EUR")
        assertEquals("\u20AC", result)
    }

    @Test
    fun testGetCurrencySymbol_Unknown() {
        val result = CurrencyFormatter.getCurrencySymbol("XYZ")
        assertEquals("XYZ", result)
    }
}
