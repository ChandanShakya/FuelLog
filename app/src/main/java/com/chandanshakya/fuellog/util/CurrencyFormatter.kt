package com.chandanshakya.fuellog.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyFormatter {
    private val currencySymbols = mapOf(
        "USD" to "$",
        "EUR" to "\u20AC",
        "INR" to "\u20B9",
        "NPR" to "\u20A8"
    )

    private val decimalFormat: DecimalFormat by lazy {
        (DecimalFormat.getInstance(Locale.US) as DecimalFormat).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }
    }

    fun formatCurrency(amount: Double, currencyCode: String): String {
        val symbol = currencySymbols[currencyCode.uppercase()]
        val formattedAmount = decimalFormat.format(amount)

        return if (symbol != null) {
            "$symbol$formattedAmount"
        } else {
            "$currencyCode $formattedAmount"
        }
    }

    fun formatAmount(amount: Double): String = decimalFormat.format(amount)

    fun getCurrencySymbol(currencyCode: String): String =
        currencySymbols[currencyCode.uppercase()] ?: currencyCode
}
