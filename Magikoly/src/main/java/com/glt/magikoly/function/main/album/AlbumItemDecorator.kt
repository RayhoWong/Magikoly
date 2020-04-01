package com.glt.magikoly.function.main.album

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View
import com.glt.magikoly.utils.DrawUtils

/**
 * @desc: 114
 * @auther:duwei
 * @date:2019/2/14
 */
class AlbumItemDecorator : RecyclerView.ItemDecoration() {
    var mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        mPaint.color = Color.parseColor("#1a746d78")
        mPaint.style = Paint.Style.FILL
    }


    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        if (parent.layoutManager == null) {
            return
        }
        c.save()

        draw(c, parent)

        c.restore()
    }

    private fun draw(canvas: Canvas, parent: RecyclerView) {
        val childCount = parent.childCount

        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)


            val layoutParams = child?.layoutParams as RecyclerView.LayoutParams
            val left = child.left
            val right = child.right
            val top = child.bottom + layoutParams.bottomMargin
            val bottom = top + DrawUtils.dip2px(1f)

            canvas.drawRect(
                left.toFloat() + DrawUtils.dip2px(114f),
                top.toFloat(),
                right.toFloat(),
                bottom.toFloat(), mPaint
            )
        }
    }


    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                state: RecyclerView.State?) {
        outRect.set(0, 0, 0, DrawUtils.dip2px(1f))
    }

}