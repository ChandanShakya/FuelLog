package com.chandanshakya.fuellog.ui.chart

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.chandanshakya.fuellog.viewmodel.ChartDataPoint

@Composable
fun MileageChart(
    dataPoints: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF625B77)
) {
    LineChart(
        values = dataPoints.map { it.mileage },
        dates = dataPoints.map { it.date },
        valueLabel = { "%.1f".format(it) },
        lineColor = lineColor,
        modifier = modifier
    )
}
