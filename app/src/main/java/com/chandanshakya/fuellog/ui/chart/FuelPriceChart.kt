package com.chandanshakya.fuellog.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.chandanshakya.fuellog.ui.theme.Dimens
import com.chandanshakya.fuellog.util.CurrencyFormatter
import com.chandanshakya.fuellog.viewmodel.PriceChartDataPoint
import java.time.format.DateTimeFormatter

@Composable
fun FuelPriceChart(
    dataPoints: List<PriceChartDataPoint>,
    currencyCode: String,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.secondary
) {
    if (dataPoints.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM") }
    
    val textStyleLabel = MaterialTheme.typography.labelSmall.copy(
        color = MaterialTheme.colorScheme.secondary
    )
    val textStyleBody = MaterialTheme.typography.bodySmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Canvas(
        modifier = modifier
            .height(Dimens.chartHeight)
            .fillMaxWidth()
            .padding(horizontal = Dimens.spacingSm)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Define padding inside the canvas to prevent text or dots from clipping
        val topMargin = 28.dp.toPx()
        val bottomMargin = 32.dp.toPx()
        val horizontalMargin = 28.dp.toPx()

        val usableWidth = canvasWidth - (horizontalMargin * 2f)
        val usableHeight = canvasHeight - topMargin - bottomMargin

        val xPositions = if (dataPoints.size == 1) {
            listOf(canvasWidth / 2f)
        } else {
            dataPoints.indices.map { index ->
                val ratio = index.toFloat() / (dataPoints.size - 1).toFloat()
                horizontalMargin + ratio * usableWidth
            }
        }

        val yPositions = if (dataPoints.size == 1) {
            listOf(topMargin + usableHeight / 2f)
        } else {
            val prices = dataPoints.map { it.pricePerUnit }
            val minPrice = prices.minOrNull() ?: 0.0
            val maxPrice = prices.maxOrNull() ?: 1.0
            val priceRange = maxPrice - minPrice
            dataPoints.map { point ->
                val normalized = if (priceRange > 0) (point.pricePerUnit - minPrice) / priceRange else 0.5
                topMargin + usableHeight - (normalized * usableHeight).toFloat()
            }
        }

        // 1. Draw horizontal guide lines
        val gridLines = listOf(
            topMargin,
            topMargin + usableHeight / 2f,
            topMargin + usableHeight
        )
        gridLines.forEach { y ->
            drawLine(
                color = lineColor.copy(alpha = 0.15f),
                start = Offset(horizontalMargin, y),
                end = Offset(canvasWidth - horizontalMargin, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // 2. Draw line/path connecting points (if there are >= 2 points)
        if (dataPoints.size >= 2) {
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
                style = Stroke(width = 3.dp.toPx())
            )
        }

        // 3. Draw circles, value labels, and date labels
        val symbol = CurrencyFormatter.getCurrencySymbol(currencyCode)
        for (i in xPositions.indices) {
            val point = dataPoints[i]
            val x = xPositions[i]
            val y = yPositions[i]

            // Draw data point circle
            drawCircle(
                color = lineColor,
                radius = 6.dp.toPx(),
                center = Offset(x, y)
            )

            // Draw small inner circle for styled punchiness
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = Offset(x, y)
            )

            // Draw price value above circle
            val priceText = "$symbol%.2f".format(point.pricePerUnit)
            val priceLayoutResult = textMeasurer.measure(
                text = priceText,
                style = textStyleLabel
            )
            drawText(
                textLayoutResult = priceLayoutResult,
                topLeft = Offset(
                    x = x - priceLayoutResult.size.width / 2f,
                    y = y - priceLayoutResult.size.height - 4.dp.toPx()
                )
            )

            // Draw date text below chart, preventing overcrowding
            val step = when {
                dataPoints.size <= 4 -> 1
                dataPoints.size <= 8 -> 2
                else -> dataPoints.size / 4
            }
            if (i % step == 0 || i == dataPoints.lastIndex) {
                val dateText = point.date.format(dateFormatter)
                val dateLayoutResult = textMeasurer.measure(
                    text = dateText,
                    style = textStyleBody
                )
                drawText(
                    textLayoutResult = dateLayoutResult,
                    topLeft = Offset(
                        x = x - dateLayoutResult.size.width / 2f,
                        y = canvasHeight - bottomMargin + 8.dp.toPx()
                    )
                )
            }
        }
    }
}
