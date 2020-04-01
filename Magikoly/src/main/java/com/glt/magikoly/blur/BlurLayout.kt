package com.glt.magikoly.blur

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewParent
import android.widget.FrameLayout

class BlurLayout(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    companion object {
        const val DEFAULT_BLUR_RADIUS = 12
        const val DEFAULT_DOWNSCALE = 0.25f
    }

    private var isBlurStatus = false
    private var maskDrawable: ColorDrawable? = null
    private val blurKit = BlurKit()
    private var blurCache: Bitmap? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var invalidateBlur = false
    private var pendingBlur = false
    private var blurRadius = DEFAULT_BLUR_RADIUS
    private var downScale = DEFAULT_DOWNSCALE

    fun startBlur() {
        isBlurStatus = true
        pendingBlur = true
        invalidate()
//        if (width == 0 || height == 0) {
//            pendingBlur = true
//        } else {
//            buildBlurCache()
//            invalidate()
//        }
    }

    fun stopBlur() {
        isBlurStatus = false
        invalidateBlur = false
        pendingBlur = false
        if (BlurKit.isBlurAvailable()) {
            if (blurCache != null) {
                blurCache?.recycle()
                blurCache = null
            }
        } else {
            maskDrawable = null
        }
        invalidate()
    }

    fun invalidateBlur() {
        if (blurCache != null) {
            invalidateBlur = true
            invalidate()
        }
    }

    override fun onDescendantInvalidated(child: View?, target: View?) {
        super.onDescendantInvalidated(child, target)
        if (isBlurStatus && blurCache != null) {
            invalidateBlur = true
        }
    }

    override fun invalidateChildInParent(location: IntArray?, dirty: Rect?): ViewParent? {
        val ret = super.invalidateChildInParent(location, dirty)
        if (isBlurStatus && blurCache != null) {
            invalidateBlur = true
        }
        return ret
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (invalidateBlur || pendingBlur) {
            invalidateBlur = false
            pendingBlur = false
            buildBlurCache()
        }
        if (blurCache != null) {
            val scale = 1.0f * width / blurCache!!.width
            canvas.save()
            canvas.scale(scale, scale)
            canvas.drawBitmap(blurCache!!, 0f, 0f, paint)
            canvas.restore()
        } else {
            super.dispatchDraw(canvas)
            maskDrawable?.draw(canvas)
        }
    }

    private fun buildBlurCache() {
        if (BlurKit.isBlurAvailable()) {
            blurCache?.recycle()
            blurCache = null
            blurCache = blurKit.fastBlur(this, blurRadius, downScale)
        } else {
            maskDrawable = ColorDrawable(Color.parseColor("#D9000000"))
            maskDrawable?.setBounds(0, 0, width, height)
        }
    }

    fun isBlur(): Boolean {
        return blurCache != null || maskDrawable != null
    }
}