package com.racebox.app.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.racebox.app.R
import java.util.Locale
import kotlin.math.max

class SpeedChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var data: List<SpeedPoint> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE63946.toInt()
        style = Paint.Style.STROKE
        strokeWidth = dp(2.5f)
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x33E63946
        style = Paint.Style.FILL
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF30363D.toInt()
        strokeWidth = dp(1f)
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF8B949E.toInt()
        textSize = sp(11f)
    }

    private val linePath = Path()
    private val fillPath = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()
        val pad = dp(14f)

        canvas.drawLine(pad, height - pad, width - pad, height - pad, gridPaint)
        canvas.drawLine(pad, pad, pad, height - pad, gridPaint)

        if (data.size < 2) {
            val label = context.getString(R.string.no_chart_data)
            canvas.drawText(label, pad + dp(6f), height / 2f, textPaint)
            return
        }

        val maxSpeed = max(data.maxOf { it.speedKmh }, 1.0)
        val maxTime = max(data.last().offsetMillis, 1L)
        val plotWidth = width - 2 * pad
        val plotHeight = height - 2 * pad

        linePath.reset()
        fillPath.reset()
        var first = true
        data.forEach { point ->
            val x = pad + (point.offsetMillis.toFloat() / maxTime) * plotWidth
            val y = pad + plotHeight - (point.speedKmh.toFloat() / maxSpeed) * plotHeight
            if (first) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, y)
                first = false
            } else {
                linePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo(pad + plotWidth, height - pad)
        fillPath.lineTo(pad, height - pad)
        fillPath.close()
        canvas.drawPath(fillPath, fillPaint)
        canvas.drawPath(linePath, linePaint)

        val label = String.format(Locale.US, "%.0f", maxSpeed) + " " + context.getString(R.string.kmh_unit)
        canvas.drawText(label, pad + dp(4f), pad + dp(14f), textPaint)
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
    private fun sp(value: Float): Float = value * resources.displayMetrics.scaledDensity
}