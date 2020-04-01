package com.glt.magikoly.function.main

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import magikoly.magiccamera.R

class DrawerItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        val obtainStyledAttributes = context.obtainStyledAttributes(attrs, R.styleable.DrawerItemView)
        // 显示图片
        val imageDrawable = obtainStyledAttributes.getDrawable(R.styleable.DrawerItemView_setting_image)
        val imageDrawableShow = obtainStyledAttributes.getBoolean(R.styleable.DrawerItemView_setting_image_show, true)

        // 标题内容
        val titleText = obtainStyledAttributes.getText(R.styleable.DrawerItemView_setting_title)

        // 标题颜色
        val titleTextColor = obtainStyledAttributes.getColor(
            R.styleable.DrawerItemView_setting_title_color, resources.getColor(R.color.toolbar_title_dark_color)
        )
        val titleTextSize = obtainStyledAttributes.getDimension(
            R.styleable.DrawerItemView_setting_title_size, resources.getDimension(R.dimen.setting_item_title_size)
        )
        val showBottomLine =
            obtainStyledAttributes.getBoolean(R.styleable.DrawerItemView_setting_show_bottom_line, true)
        obtainStyledAttributes?.recycle()
        val rootView = LayoutInflater.from(context).inflate(R.layout.setting_item_layout, this, true)
        val imageView = rootView.findViewById<ImageView>(R.id.setting_item_image)
        if (!imageDrawableShow || imageDrawable == null) {
            imageView.visibility = View.GONE
        } else {
            imageView.setImageDrawable(imageDrawable)
        }
        val textTitle = rootView.findViewById<TextView>(R.id.setting_item_title)
        textTitle.text = titleText
        textTitle.setTextColor(titleTextColor)
        textTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize)
        val lineView = rootView.findViewById<View>(R.id.setting_item_line)
        if (showBottomLine) {
            lineView.visibility = View.VISIBLE
        } else {
            lineView.visibility = View.GONE
        }

    }

}