package com.glt.magikoly.function.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class FixedHeightButtonGroup : FrameLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, style: Int) : super(context, attrs, style)

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val lp = child.layoutParams as FrameLayout.LayoutParams
            if (child.height + lp.topMargin + lp.bottomMargin > height) {
                val t = (height - child.height) / 2.0f
                val b = t + child.height
                child.layout(child.left, t.toInt(), child.right, b.toInt())
            }
        }
    }
}