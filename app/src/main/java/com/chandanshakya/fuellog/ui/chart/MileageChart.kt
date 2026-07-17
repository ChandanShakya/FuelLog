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
import androidx.compose.ui.unit.dp
import com.chandanshakya.fuellog.ui.theme.Dimens
import com.chandanshakya.fuellog.viewmodel.ChartDataPoint

@Composable
fun MileageChart(
    dataPoints: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF625B77)
) {
    if (dataPoints.isEmpty()) return

    Canvas(
        modifier = modifier
            .height(Dimens.chartHeight)
            .fillMaxSize()
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        if (dataPoints.size < 2) return@Canvas

        val mileages = dataPoints.map { it.mileage }
        val minMileage = mileages.minOrNull() ?: 0.0
        val maxMileage = mileages.maxOrNull() ?: 1.0
        val mileageRange = maxMileage - minMileage

        val xPositions = dataPoints.indices.map { index ->
            val ratio = index.toFloat() / (dataPoints.size - 1).toFloat()
            ratio * canvasWidth
        }

        val yPositions = dataPoints.map { point ->
            val normalized = if (mileageRange > 0) (point.mileage - minMileage) / mileageRange else 0.5
            canvasHeight - (normalized * canvasHeight).toFloat()
        }

        val path = Path().apply {
            for (i in xPositions.indices) {
                if (i == 0) {
                    moveTo(xPositions[i], yPositions[i])
                } else {
                    lineTo(xPositions[i], yPositions[i])
                }
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2f)
        )

        for (i in xPositions.indices) {
            drawCircle(
                color = lineColor,
                radius = 4f,
                center = Offset(xPositions[i], yPositions[i])
            )
        }

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
