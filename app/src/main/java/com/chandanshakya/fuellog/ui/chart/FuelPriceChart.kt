package com.chandanshakya.fuellog.ui.chart

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chandanshakya.fuellog.util.CurrencyFormatter
import com.chandanshakya.fuellog.viewmodel.PriceChartDataPoint

@Composable
fun FuelPriceChart(
    dataPoints: List<PriceChartDataPoint>,
    currencyCode: String,
    modifier: Modifier = Modifier,
    lineColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.secondary
) {
    val symbol = CurrencyFormatter.getCurrencySymbol(currencyCode)
    LineChart(
        values = dataPoints.map { it.pricePerUnit },
        dates = dataPoints.map { it.date },
        valueLabel = { "$symbol%.2f".format(it) },
        lineColor = lineColor,
        modifier = modifier
    )
}
