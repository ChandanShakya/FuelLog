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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.chandanshakya.fuellog.ui.theme.Dimens
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun LineChart(
    values: List<Double>,
    dates: List<LocalDate>,
    valueLabel: (Double) -> String,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (values.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM") }
    val minVal = remember(values) { values.minOrNull() ?: 0.0 }
    val maxVal = remember(values) { values.maxOrNull() ?: 1.0 }
    val range = remember(minVal, maxVal) { maxVal - minVal }

    val textStyleLabel = MaterialTheme.typography.labelSmall.copy(color = lineColor)
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

        val topMargin = 28.dp.toPx()
        val bottomMargin = 32.dp.toPx()
        val horizontalMargin = 28.dp.toPx()

        val usableWidth = canvasWidth - (horizontalMargin * 2f)
        val usableHeight = canvasHeight - topMargin - bottomMargin

        val xPositions = if (values.size == 1) {
            listOf(canvasWidth / 2f)
        } else {
            values.indices.map { index ->
                val ratio = index.toFloat() / (values.size - 1).toFloat()
                horizontalMargin + ratio * usableWidth
            }
        }

        val yPositions = if (values.size == 1) {
            listOf(topMargin + usableHeight / 2f)
        } else {
            values.map { v ->
                val normalized = if (range > 0) (v - minVal) / range else 0.5
                topMargin + usableHeight - (normalized * usableHeight).toFloat()
            }
        }

        val gridLines = listOf(topMargin, topMargin + usableHeight / 2f, topMargin + usableHeight)
        gridLines.forEach { y ->
            drawLine(
                color = lineColor.copy(alpha = 0.15f),
                start = Offset(horizontalMargin, y),
                end = Offset(canvasWidth - horizontalMargin, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        if (values.size >= 2) {
            val path = Path().apply {
                for (i in xPositions.indices) {
                    if (i == 0) moveTo(xPositions[i], yPositions[i])
                    else lineTo(xPositions[i], yPositions[i])
                }
            }
            drawPath(path = path, color = lineColor, style = Stroke(width = 3.dp.toPx()))
        }

        for (i in xPositions.indices) {
            val x = xPositions[i]
            val y = yPositions[i]

            drawCircle(color = lineColor, radius = 6.dp.toPx(), center = Offset(x, y))
            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(x, y))

            val label = valueLabel(values[i])
            val labelResult = textMeasurer.measure(text = label, style = textStyleLabel)
            drawText(
                textLayoutResult = labelResult,
                topLeft = Offset(x = x - labelResult.size.width / 2f, y = y - labelResult.size.height - 4.dp.toPx())
            )

            val step = when {
                values.size <= 4 -> 1
                values.size <= 8 -> 2
                else -> values.size / 4
            }
            if (i % step == 0 || i == values.lastIndex) {
                val dateText = dates[i].format(dateFormatter)
                val dateResult = textMeasurer.measure(text = dateText, style = textStyleBody)
                drawText(
                    textLayoutResult = dateResult,
                    topLeft = Offset(x = x - dateResult.size.width / 2f, y = canvasHeight - bottomMargin + 8.dp.toPx())
                )
            }
        }
    }
}
