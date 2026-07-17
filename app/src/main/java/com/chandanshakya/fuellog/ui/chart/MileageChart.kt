package com.chandanshakya.fuellog.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.chandanshakya.fuellog.ui.theme.Dimens
import com.chandanshakya.fuellog.viewmodel.ChartDataPoint
import kotlin.math.max
import kotlin.math.min

/**
 * Custom mileage trend chart using Compose Canvas.
 * 
 * Draws a line chart showing mileage over time/odometer.
 * Isolated in its own file for easy replacement if requirements grow.
 * 
 * @param dataPoints List of chart data points
 * @param modifier Modifier for the component
 * @param lineColor Color for the mileage line (defaults to primary)
 */
@Composable
fun MileageChart(
    dataPoints: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF625B77)
) {
    if (dataPoints.isEmpty()) {
        return
    }

    Canvas(
        modifier = modifier
            .height(Dimens.chartHeight)
            .fillMaxSize()
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        if (dataPoints.size < 2) return@Canvas
        
        // Calculate min/max mileage for scaling
        val mileages = dataPoints.map { it.mileage }
        val minMileage = mileages.minOrNull() ?: 0.0
        val maxMileage = mileages.maxOrNull() ?: 1.0
        val mileageRange = maxMileage - minMileage
        
        // Calculate x positions for each data point
        val xPositions = dataPoints.indices.map { index ->
            val ratio = index.toFloat() / (dataPoints.size - 1).toFloat()
            ratio * canvasWidth
        }
        
        // Calculate y positions for each data point
        val yPositions = dataPoints.map { point ->
            val normalized = (point.mileage - minMileage) / mileageRange
            canvasHeight - (normalized * canvasHeight)
        }
        
        // Draw the line
        val path = Path().apply {
            for (i in xPositions.indices) {
                val x = xPositions[i]
                val y = yPositions[i]
                if (i == 0) {
                    moveTo(x, y)
                } else {
                    lineTo(x, y)
                }
            }
        }
        
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2f)
        )
        
        // Draw circles at each data point
        for (i in xPositions.indices) {
            drawCircle(
                color = lineColor,
                radius = 4f,
                center = Offset(xPositions[i], yPositions[i])
            )
        }
        
        // Draw horizontal grid lines
        drawLine(
            color = lineColor.copy(alpha = 0.2f),
            start = Offset(0f, canvasHeight / 4),
            end = Offset(canvasWidth, canvasHeight / 4),
            strokeWidth = 1f
        )
        drawLine(
            color = lineColor.copy(alpha = 0.2f),
            start = Offset(0f, canvasHeight / 2),
            end = Offset(canvasWidth, canvasHeight / 2),
            strokeWidth = 1f
        )
        drawLine(
            color = lineColor.copy(alpha = 0.2f),
            start = Offset(0f, canvasHeight * 3 / 4),
            end = Offset(canvasWidth, canvasHeight * 3 / 4),
            strokeWidth = 1f
        )
    }
}
