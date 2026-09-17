package com.chandanshakya.fuellog.util

import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Currency
import java.util.Locale

/**
 * Money helpers. Amounts are stored as [Double] for Room/JSON compatibility,
 * but all display and aggregation go through 2-decimal rounding so totals do
 * not drift on long histories.
 */
object Money {
    /** Round to 2 decimal places (cents / paisa). */
    fun roundToCents(amount: Double): Double =
        Math.floor(amount * 100.0 + 0.5) / 100.0

    fun roundToCentsOrNull(amount: Double?): Double? =
        amount?.let { roundToCents(it) }

    fun sumCents(amounts: Iterable<Double>): Double =
        roundToCents(amounts.fold(0.0) { acc, v -> acc + v })

    fun rate(volume: Double, cost: Double): Double? =
        if (volume > 0) roundToCents(cost / volume) else null

    fun cost(volume: Double, rate: Double): Double =
        roundToCents(volume * rate)
}

/**
 * Formats currency amounts. Uses a per-thread [DecimalFormat] so concurrent
 * Flow collectors (Default dispatcher) and UI formatting cannot corrupt state.
 */
object CurrencyFormatter {
    private val threadLocalFormat = object : ThreadLocal<DecimalFormat>() {
        override fun initialValue(): DecimalFormat =
            (DecimalFormat.getInstance(Locale.US) as DecimalFormat).apply {
                maximumFractionDigits = 2
                minimumFractionDigits = 2
                roundingMode = RoundingMode.HALF_UP
            }
    }

    fun formatCurrency(amount: Double, currencyCode: String): String {
        val formattedAmount = threadLocalFormat.get()!!.format(Money.roundToCents(amount))
        return try {
            val symbol = Currency.getInstance(currencyCode.uppercase()).symbol
            "$symbol$formattedAmount"
        } catch (_: IllegalArgumentException) {
            "$currencyCode $formattedAmount"
        }
    }
}
