package com.glt.magikoly.function.main

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.RelativeLayout
import com.glt.magikoly.utils.DrawUtils
import magikoly.magiccamera.R
import kotlinx.android.synthetic.main.function_item.view.*

open class FunctionItem(context: Context) : RelativeLayout(context) {

    private var shadow: Drawable = resources.getDrawable(R.drawable.function_item_shadow_mask)

    fun setItemTitle(title: String) {
        txt_title.text = title.toUpperCase()
    }

    fun setItemDesc(desc: String) {
        txt_desc.text = desc
    }

    fun setVipFunction(isVipFunction: Boolean) {
        if (isVipFunction) {
            img_vip.visibility = View.VISIBLE
        } else {
            img_vip.visibility = View.GONE
        }
    }

    protected fun drawShadow(canvas: Canvas) {
        shadow.draw(canvas)
    }

    protected fun setShadowBounds(baseBounds: Rect) {
        val shadowLeft = baseBounds.left - DrawUtils.dip2px(6f)
        val shadowTop = baseBounds.top - DrawUtils.dip2px(3f)
        val shadowRight = baseBounds.right + DrawUtils.dip2px(6f)
        val shadowBottom = baseBounds.bottom + DrawUtils.dip2px(8f)
        shadow.setBounds(shadowLeft, shadowTop, shadowRight, shadowBottom)
    }
}