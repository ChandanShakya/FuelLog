package com.chandanshakya.fuellog.ui.chart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.chandanshakya.fuellog.viewmodel.ChartDataPoint

@Composable
fun MileageChart(
    dataPoints: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF625B77)
) {
    val values = remember(dataPoints) { dataPoints.map { it.mileage } }
    val dates = remember(dataPoints) { dataPoints.map { it.date } }
    LineChart(
        values = values,
        dates = dates,
        valueLabel = { "%.2f".format(it) },
        lineColor = lineColor,
        modifier = modifier
    )
}
