package com.glt.magikoly.function.main

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.glt.magikoly.utils.DrawUtils
import magikoly.magiccamera.R

/**
 * Created by yangguanxiang
 */
class BottomBarTab(context: Context, icon: Drawable?, iconHighLight: Drawable?, title: String?) : FrameLayout(context) {

    private var mIconDrawable:Drawable? = null
    private var mIconHighLightDrawable:Drawable? = null

    private lateinit var mIcon: ImageView
    private lateinit var mTxtTitle: TextView

    var tabPosition = -1
        set(position) {
            field = position
            if (position == 0) {
                isSelected = true
            }
        }

    init {
        init(context, icon, iconHighLight, title)
    }

    private fun init(context: Context, icon: Drawable?, iconHighLight: Drawable? , title: String?) {
        mIconDrawable = icon
        mIconHighLightDrawable = iconHighLight

        val container = LinearLayout(context)
        container.orientation = LinearLayout.VERTICAL
        container.gravity = Gravity.CENTER
        val paramsContainer = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        paramsContainer.gravity = Gravity.CENTER
        container.layoutParams = paramsContainer

        mIcon = ImageView(context)
        val size = DrawUtils.dip2px(27f)
        val iconParam = LinearLayout.LayoutParams(size, size)
        mIcon.setImageDrawable(icon)
        mIcon.layoutParams = iconParam
        if (mIconHighLightDrawable == null) {
            mIcon.setColorFilter(ContextCompat.getColor(context, R.color.tab_unselect_text_color))
        }
        container.addView(mIcon)

        mTxtTitle = TextView(context)
        mTxtTitle.text = title
        val titleParam = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        titleParam.topMargin = DrawUtils.dip2px(2f)
        mTxtTitle.textSize = 10f
        mTxtTitle.setTextColor(ContextCompat.getColor(context, R.color.tab_unselect_text_color))
        mTxtTitle.layoutParams = titleParam
        container.addView(mTxtTitle)

        addView(container)
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        if (mIconHighLightDrawable != null) {
            if (selected) {
                mIcon.setImageDrawable(mIconHighLightDrawable)
                mTxtTitle.setTextColor(context.resources.getColor(R.color.tab_selected_text_color))
            } else {
                mIcon.setImageDrawable(mIconDrawable)
                mTxtTitle.setTextColor(context.resources.getColor(R.color.tab_unselect_text_color))
            }
        } else {
            if (selected) {
                mIcon.setColorFilter(context.resources.getColor(R.color.tab_selected_text_color))
                mTxtTitle.setTextColor(context.resources.getColor(R.color.tab_selected_text_color))
            } else {
                mIcon.setColorFilter(context.resources.getColor(R.color.tab_unselect_text_color))
                mTxtTitle.setTextColor(context.resources.getColor(R.color.tab_unselect_text_color))
            }
        }
    }
}
