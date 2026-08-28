package com.racebox.app.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.racebox.app.R

class TrackMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var data: List<HeatPoint> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    private val pathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE63946.toInt()
        style = Paint.Style.STROKE
        strokeWidth = dp(3f)
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val layerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(5f)
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF8B949E.toInt()
        textSize = sp(11f)
    }

    private val path = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data.size < 2) {
            canvas.drawText(
                context.getString(R.string.no_chart_data),
                dp(24f),
                height / 2f,
                textPaint
            )
            return
        }

        val pad = dp(24f)
        val plotWidth = width - 2 * pad
        val plotHeight = height - 2 * pad

        val minLat = data.minOf { it.latitude }
        val maxLat = data.maxOf { it.latitude }
        val minLon = data.minOf { it.longitude }
        val maxLon = data.maxOf { it.longitude }
        val latSpan = (maxLat - minLat).coerceAtLeast(0.000001)
        val lonSpan = (maxLon - minLon).coerceAtLeast(0.000001)
        val maxSpeed = data.maxOf { it.speedKmh }.coerceAtLeast(1.0)

        path.reset()
        data.forEachIndexed { index, point ->
            val x = pad + ((point.longitude - minLon) / lonSpan * plotWidth).toFloat()
            val y = pad + (plotHeight - (point.latitude - minLat) / latSpan * plotHeight).toFloat()
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)

            val normalized = (point.speedKmh / maxSpeed).toFloat().coerceIn(0f, 1f)
            val color = lerpColor(0xFF22D3A5.toInt(), 0xFFE63946.toInt(), normalized)
            dotPaint.color = color
            canvas.drawCircle(x, y, dp(2.5f), dotPaint)
        }
        canvas.drawPath(path, layerPaint)
        canvas.drawPath(path, pathPaint)
    }

    private fun lerpColor(start: Int, end: Int, t: Float): Int {
        val amount = t.coerceIn(0f, 1f)
        val sr = (start shr 16) and 0xFF
        val sg = (start shr 8) and 0xFF
        val sb = start and 0xFF
        val er = (end shr 16) and 0xFF
        val eg = (end shr 8) and 0xFF
        val eb = end and 0xFF
        val r = (sr + (er - sr) * amount).toInt()
        val g = (sg + (eg - sg) * amount).toInt()
        val b = (sb + (eb - sb) * amount).toInt()
        return 0xFF000000.toInt() or (r shl 16) or (g shl 8) or b
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
    private fun sp(value: Float): Float = value * resources.displayMetrics.scaledDensity
}