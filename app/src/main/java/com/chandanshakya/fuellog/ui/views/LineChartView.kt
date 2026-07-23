package com.chandanshakya.fuellog.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LineChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var values: List<Double> = emptyList()
    private var dates: List<LocalDate> = emptyList()
    private var valueLabel: (Double) -> String = { "%.2f".format(it) }
    private var lineColor: Int = Color.parseColor("#625B77")

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f * resources.displayMetrics.density
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val dotCenterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f * resources.displayMetrics.density
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 10f * resources.displayMetrics.density
        textAlign = Paint.Align.CENTER
    }

    private val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 10f * resources.displayMetrics.density
        textAlign = Paint.Align.CENTER
        color = Color.parseColor("#49454F")
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM")
    private val path = Path()

    fun setData(
        values: List<Double>,
        dates: List<LocalDate>,
        valueLabel: (Double) -> String,
        lineColor: Int
    ) {
        this.values = values
        this.dates = dates
        this.valueLabel = valueLabel
        this.lineColor = lineColor
        linePaint.color = lineColor
        dotPaint.color = lineColor
        gridPaint.color = Color.argb(38, Color.red(lineColor), Color.green(lineColor), Color.blue(lineColor))
        labelPaint.color = lineColor
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (values.isEmpty()) return

        val canvasWidth = width.toFloat()
        val canvasHeight = height.toFloat()

        val density = resources.displayMetrics.density
        val topMargin = 28f * density
        val bottomMargin = 32f * density
        val horizontalMargin = 28f * density

        val usableWidth = canvasWidth - (horizontalMargin * 2f)
        val usableHeight = canvasHeight - topMargin - bottomMargin

        val minVal = values.minOrNull() ?: 0.0
        val maxVal = values.maxOrNull() ?: 1.0
        val range = maxVal - minVal

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

        // Grid lines
        val gridLines = listOf(topMargin, topMargin + usableHeight / 2f, topMargin + usableHeight)
        gridLines.forEach { y ->
            canvas.drawLine(horizontalMargin, y, canvasWidth - horizontalMargin, y, gridPaint)
        }

        // Line path
        if (values.size >= 2) {
            path.reset()
            for (i in xPositions.indices) {
                if (i == 0) path.moveTo(xPositions[i], yPositions[i])
                else path.lineTo(xPositions[i], yPositions[i])
            }
            canvas.drawPath(path, linePaint)
        }

        // Dots and labels
        val dotRadius = 6f * density
        val dotCenterRadius = 2f * density
        val labelOffset = 4f * density
        val dateOffset = 8f * density

        for (i in xPositions.indices) {
            val x = xPositions[i]
            val y = yPositions[i]

            canvas.drawCircle(x, y, dotRadius, dotPaint)
            canvas.drawCircle(x, y, dotCenterRadius, dotCenterPaint)

            // Value label above dot
            val label = valueLabel(values[i])
            canvas.drawText(label, x, y - labelOffset, labelPaint)

            // Date label below
            val step = when {
                values.size <= 4 -> 1
                values.size <= 8 -> 2
                else -> values.size / 4
            }
            if (i % step == 0 || i == values.lastIndex) {
                val dateText = dates[i].format(dateFormatter)
                canvas.drawText(dateText, x, canvasHeight - bottomMargin + dateOffset + density * 8, datePaint)
            }
        }
    }
}
