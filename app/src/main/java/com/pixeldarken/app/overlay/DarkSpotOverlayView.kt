package com.pixeldarken.app.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.pixeldarken.app.DarkSpot

class DarkSpotOverlayView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    var spots: List<DarkSpot> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (spot in spots) {
            paint.color = Color.argb(spot.alpha.coerceIn(0, 255), 0, 0, 0)
            canvas.drawCircle(spot.xPx.toFloat(), spot.yPx.toFloat(), spot.radiusPx.toFloat(), paint)
        }
    }
}
