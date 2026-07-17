package com.chandanshakya.fuellog.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Utility class for currency formatting.
 * 
 * Provides real symbol mapping for USD, EUR, INR, NPR with fallback to code + amount.
 */
object CurrencyFormatter {
    private val currencySymbols = mapOf(
        "USD" to "$",
        "EUR" to "\u20AC",
        "INR" to "\u20B9",
        "NPR" to "\u20A8"
    )

    private val decimalFormat: DecimalFormat by lazy {
        DecimalFormat.getInstance(Locale.US).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }
    }

    /**
     * Format a monetary amount with currency symbol.
     * 
     * @param amount The amount to format
     * @param currencyCode ISO 4217 currency code
     * @return Formatted string with currency symbol and amount
     */
    fun formatCurrency(amount: Double, currencyCode: String): String {
        val symbol = currencySymbols[currencyCode.uppercase()]
        val formattedAmount = decimalFormat.format(amount)
        
        return if (symbol != null) {
            "$symbol$formattedAmount"
        } else {
            "$currencyCode $formattedAmount"
        }
    }

    /**
     * Format a monetary amount without symbol (just the number).
     */
    fun formatAmount(amount: Double): String = decimalFormat.format(amount)

    /**
     * Get the currency symbol for a given code.
     */
    fun getCurrencySymbol(currencyCode: String): String = 
        currencySymbols[currencyCode.uppercase()] ?: currencyCode
}
