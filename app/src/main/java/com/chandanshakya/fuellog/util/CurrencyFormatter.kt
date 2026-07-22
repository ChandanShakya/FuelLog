package com.chandanshakya.fuellog.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Currency
import java.util.Locale

object CurrencyFormatter {
    private val decimalFormat: DecimalFormat by lazy {
        (DecimalFormat.getInstance(Locale.US) as DecimalFormat).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }
    }

    fun formatCurrency(amount: Double, currencyCode: String): String {
        val formattedAmount = decimalFormat.format(amount)
        return try {
            val symbol = Currency.getInstance(currencyCode.uppercase()).symbol
            "$symbol$formattedAmount"
        } catch (_: IllegalArgumentException) {
            "$currencyCode $formattedAmount"
        }
    }

    fun getCurrencySymbol(currencyCode: String): String = try {
        Currency.getInstance(currencyCode.uppercase()).symbol
    } catch (_: IllegalArgumentException) {
        currencyCode
    }
}
