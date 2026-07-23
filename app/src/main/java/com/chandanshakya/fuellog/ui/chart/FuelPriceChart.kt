package com.chandanshakya.fuellog.ui.chart

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    val values = remember(dataPoints) { dataPoints.map { it.pricePerUnit } }
    val dates = remember(dataPoints) { dataPoints.map { it.date } }
    LineChart(
        values = values,
        dates = dates,
        valueLabel = { "$symbol%.2f".format(it) },
        lineColor = lineColor,
        modifier = modifier
    )
}
