package com.glt.magikoly.function.main

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import com.glt.magikoly.utils.DrawUtils
import com.glt.magikoly.utils.RoundCornerDrawHelper

open class StaticImageFunctionItem(context: Context) : FunctionItem(context) {

    var bg: Drawable? = null
    var avatar: Drawable? = null
    private val maskRectF = RectF()
    private val maskRect = Rect()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (bg != null) {
            when {
                bg!!.intrinsicWidth == measuredWidth -> {
                    bg?.setBounds(0, 0, measuredWidth, measuredHeight)
                }
                bg!!.intrinsicWidth > measuredWidth -> {
                    val left = ((measuredWidth - bg!!.intrinsicWidth) / 2.0f).toInt()
                    val right = left + bg!!.intrinsicWidth
                    bg?.setBounds(left, 0, right, measuredHeight)
                }
                bg!!.intrinsicWidth < measuredWidth -> {
                    val width = bg!!.intrinsicWidth
                    val height = bg!!.intrinsicHeight
                    bg?.setBounds(0, 0, width, height)
                    val widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
                    val heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
                    super.onMeasure(widthSpec, heightSpec)
                }
            }
            avatar?.setBounds(bg!!.bounds.left, bg!!.bounds.top, bg!!.bounds.right,
                bg!!.bounds.top + bg!!.bounds.height() * 341 / 510)
            maskRect.set(0, bg!!.bounds.top + DrawUtils.dip2px(26f), measuredWidth, bg!!.bounds.bottom)
            setShadowBounds(maskRect)
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        drawShadow(canvas)
        maskRectF.set(maskRect)
        val r = DrawUtils.dip2px(7f).toFloat()
        RoundCornerDrawHelper.drawRoundCorner(canvas, maskRectF, r, r) { bg?.draw(canvas) }
        drawAvatar(canvas)
        super.dispatchDraw(canvas)
    }

    open fun drawAvatar(canvas: Canvas) {
        avatar?.draw(canvas)
    }
}