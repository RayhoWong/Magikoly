package com.glt.magikoly.function.main.faceimages

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.glt.magikoly.view.RoundedDrawable
import com.glt.magikoly.view.RoundedImageView
import magikoly.magiccamera.R

class FaceImageView(context: Context) : RoundedImageView(context) {

    private val loadingBg = RoundedDrawable.fromDrawable(
            ColorDrawable(Color.parseColor("#F7F3FE")))
    private val demo = context.resources.getDrawable(R.drawable.logo_demo)
    private var isDemoVisible = false

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        loadingBg.setBounds(0, 0, width, height)
    }

    override fun updateDrawableAttrs() {
        updateAttrs(loadingBg)
        updateAttrs(mDrawable)
        updateAttrs(demo)
    }

    override fun draw(canvas: Canvas?) {
        loadingBg.draw(canvas)
        super.draw(canvas)
        if (isDemoVisible) {
            val left = width - demo.intrinsicWidth
            val top = 0
            val right = width
            val bottom = demo.intrinsicHeight
            demo.setBounds(left, top, right, bottom)
            demo.draw(canvas)
        }
    }

    fun setDemoVisible(visible: Boolean) {
        isDemoVisible = visible
    }
}