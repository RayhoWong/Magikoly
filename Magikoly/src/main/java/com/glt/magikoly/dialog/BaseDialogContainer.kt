package com.glt.magikoly.dialog

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.FrameLayout
import com.glt.magikoly.utils.DrawUtils
import com.glt.magikoly.utils.RoundCornerDrawHelper

class BaseDialogContainer(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    override fun draw(canvas: Canvas?) {
        RoundCornerDrawHelper.drawRoundCorner(canvas, DrawUtils.dip2px(8f).toFloat(),
                DrawUtils.dip2px(8f).toFloat()) { super@BaseDialogContainer.draw(canvas) }
    }
}