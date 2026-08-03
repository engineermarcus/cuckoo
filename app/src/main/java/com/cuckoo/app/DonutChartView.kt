package com.cuckoo.app

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class DonutChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val colors = listOf(
        0xFF4285F4.toInt(), 0xFFEA4335.toInt(), 0xFFFBBC05.toInt(),
        0xFF34A853.toInt(), 0xFFFF6D00.toInt(), 0xFF9C27B0.toInt()
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textAlign = Paint.Align.CENTER
        textSize = 32f
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFAAAAAA.toInt()
        textAlign = Paint.Align.CENTER
        textSize = 24f
    }

    private var data: List<Pair<String, Float>> = emptyList()

    fun setData(data: List<Pair<String, Float>>) {
        this.data = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) return
        val cx = width / 2f
        val cy = height / 2f
        val radius = minOf(cx, cy) * 0.7f
        val strokeWidth = radius * 0.28f
        paint.strokeWidth = strokeWidth
        val oval = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        var startAngle = -90f
        data.forEachIndexed { i, (name, fraction) ->
            val sweep = fraction * 360f
            paint.color = colors[i % colors.size]
            canvas.drawArc(oval, startAngle, sweep, false, paint)

            // label outside ring
            val midAngle = Math.toRadians((startAngle + sweep / 2).toDouble())
            val labelR = radius + strokeWidth
            val lx = cx + (labelR * Math.cos(midAngle)).toFloat()
            val ly = cy + (labelR * Math.sin(midAngle)).toFloat()
            canvas.drawText(name, lx, ly, labelPaint)

            startAngle += sweep
        }
    }
}
