package com.glt.magikoly.function.main

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.RelativeLayout
import magikoly.magiccamera.R


class FaceBackground : RelativeLayout {

    val imgTopLeft = ImageView(context)
    val imgTopRight = ImageView(context)
    val imgBottomLeft = ImageView(context)
    val imgBottomRight = ImageView(context)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ReportBackground)
        var resId = typedArray.getResourceId(R.styleable.ReportBackground_imgTopLeft, 0)
        if (resId > 0) {
            imgTopLeft.setImageResource(resId)
        }

        resId = typedArray.getResourceId(R.styleable.ReportBackground_imgTopRight, 0)
        if (resId > 0) {
            imgTopRight.setImageResource(resId)
        }

        resId = typedArray.getResourceId(R.styleable.ReportBackground_imgBottomLeft, 0)
        if (resId > 0) {
            imgBottomLeft.setImageResource(resId)
        }

        resId = typedArray.getResourceId(R.styleable.ReportBackground_imgBottomRight, 0)
        if (resId > 0) {
            imgBottomRight.setImageResource(resId)
        }
    }

    init {
        var layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        addView(imgTopLeft, layoutParams)
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        addView(imgTopRight, layoutParams)
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        addView(imgBottomLeft, layoutParams)
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        addView(imgBottomRight, layoutParams)

    }
}