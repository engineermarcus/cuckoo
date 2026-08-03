package com.cuckoo.app

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class DonutChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val colors = listOf(
        0xFF4285F4.toInt(), 0xFFEA4335.toInt(), 0xFFFBBC05.toInt(),
        0xFF34A853.toInt(), 0xFFFF6D00.toInt(), 0xFF9C27B0.toInt()
    )

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = 0x66AAAAAA
    }
    private val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.LEFT
        textSize = 26f
    }

    private var data: List<Pair<String, Float>> = emptyList()
    private var labelColor: Int = 0xFFAAAAAA.toInt()

    fun setData(data: List<Pair<String, Float>>) {
        this.data = data
        invalidate()
    }

    /** Lets callers (e.g. the fragment) pass a theme-correct label color. */
    fun setLabelColor(color: Int) {
        labelColor = color
        invalidate()
    }

    private data class Slice(val name: String, val fraction: Float, val midAngleDeg: Float, val color: Int)

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) return
        labelPaint.color = labelColor

        val cx = width / 2f
        val cy = height / 2f

        // Reserve horizontal room on both sides for labels + leader lines.
        val labelAreaWidth = min(width * 0.28f, 130f)
        val availableForRing = min(width - labelAreaWidth * 2f, height.toFloat())
        val radius = (availableForRing / 2f) * 0.72f
        val strokeWidth = radius * 0.28f
        arcPaint.strokeWidth = strokeWidth
        val oval = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        var startAngle = -90f
        val slices = data.mapIndexed { i, (name, fraction) ->
            val sweep = fraction * 360f
            val mid = startAngle + sweep / 2f
            val slice = Slice(name, fraction, mid, colors[i % colors.size])
            arcPaint.color = slice.color
            canvas.drawArc(oval, startAngle, sweep, false, arcPaint)
            startAngle += sweep
            slice
        }

        // Split into left-half / right-half slices so labels stack in two columns
        // instead of all fighting for space around the same ring edge.
        val rightSide = slices.filter { cos(Math.toRadians(it.midAngleDeg.toDouble())) >= 0 }
            .sortedBy { it.midAngleDeg }
        val leftSide = slices.filter { cos(Math.toRadians(it.midAngleDeg.toDouble())) < 0 }
            .sortedBy { it.midAngleDeg }

        drawLabelColumn(canvas, rightSide, cx, cy, radius, strokeWidth, isRight = true)
        drawLabelColumn(canvas, leftSide, cx, cy, radius, strokeWidth, isRight = false)
    }

    private fun drawLabelColumn(
        canvas: Canvas,
        slices: List<Slice>,
        cx: Float, cy: Float,
        radius: Float, strokeWidth: Float,
        isRight: Boolean
    ) {
        if (slices.isEmpty()) return

        val lineHeight = labelPaint.textSize + 14f
        val anchorR = radius + strokeWidth * 0.5f
        val labelX = if (isRight) width - min(width * 0.28f, 130f) + 8f else 8f
        val maxLabelWidth = min(width * 0.24f, 110f)

        // Evenly distribute this column's labels top-to-bottom so they never overlap,
        // even if several slices' natural angles land close together.
        val totalHeight = lineHeight * slices.size
        var y = cy - totalHeight / 2f + lineHeight * 0.7f

        for (slice in slices) {
            val midAngle = Math.toRadians(slice.midAngleDeg.toDouble())
            val startX = cx + (anchorR * cos(midAngle)).toFloat()
            val startY = cy + (anchorR * sin(midAngle)).toFloat()

            val bendX = if (isRight) labelX - 14f else labelX + maxLabelWidth + 14f
            linePaint.color = (slice.color and 0x00FFFFFF) or 0x66000000
            canvas.drawLine(startX, startY, bendX, y, linePaint)
            canvas.drawLine(bendX, y, if (isRight) labelX - 4f else labelX + maxLabelWidth + 4f, y, linePaint)

            val truncated = TextUtils.ellipsize(slice.name, labelPaint, maxLabelWidth, TextUtils.TruncateAt.END)
            if (isRight) {
                labelPaint.textAlign = Paint.Align.LEFT
                canvas.drawText(truncated, 0, truncated.length, labelX, y + labelPaint.textSize * 0.35f, labelPaint)
            } else {
                labelPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText(truncated, 0, truncated.length, labelX + maxLabelWidth, y + labelPaint.textSize * 0.35f, labelPaint)
            }

            y += lineHeight
        }
    }
}
