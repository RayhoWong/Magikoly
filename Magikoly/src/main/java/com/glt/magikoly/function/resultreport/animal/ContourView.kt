package com.glt.magikoly.function.resultreport.animal

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.util.AttributeSet
import android.view.View
import com.glt.magikoly.utils.DrawUtils

class ContourView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    var landmark: HashMap<String, List<Point>>? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        paint.color = Color.RED
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        landmark?.keys?.forEach { key ->
            val points = landmark!![key]
            points?.forEach { point ->
                val x = width / 2f + point.x
                val y = height / 2f - point.y
                canvas.drawCircle(x, y, DrawUtils.dip2px(3f).toFloat(), paint)
            }
        }
    }
}