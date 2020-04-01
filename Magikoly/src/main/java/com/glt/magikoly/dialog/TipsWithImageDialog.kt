package com.glt.magikoly.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import magikoly.magiccamera.R

class TipsWithImageDialog(activity: Activity) : BaseDialog(activity) {

    private val customView = LayoutInflater.from(context)
            .inflate(R.layout.tips_with_image_dialog_view, null)

    init {
        setCustomView(customView)
    }

    fun setImage(resId: Int) {
        if (resId > 0) {
            customView.findViewById<ImageView>(R.id.img_icon).setImageResource(resId)
        } else {
            customView.findViewById<ImageView>(R.id.img_icon).visibility = View.GONE
        }
    }

    @SuppressLint("ResourceType")
    override fun setTitle(titleId: Int) {
        if (titleId > 0) {
            customView.findViewById<TextView>(R.id.txt_title).setText(titleId)
        } else {
            customView.findViewById<ImageView>(R.id.txt_title).visibility = View.GONE
        }
    }

    fun setTitle(title: String) {
        if (!TextUtils.isEmpty(title)) {
            customView.findViewById<TextView>(R.id.txt_title).text = title
        } else {
            customView.findViewById<TextView>(R.id.txt_title).visibility = View.GONE
        }
    }

    fun setContent(contentId: Int) {
        if (contentId > 0) {
            customView.findViewById<TextView>(R.id.txt_content).setText(contentId)
        } else {
            customView.findViewById<TextView>(R.id.txt_content).visibility = View.GONE
        }
    }

    fun setContent(content: CharSequence) {
        if (!TextUtils.isEmpty(content)) {
            customView.findViewById<TextView>(R.id.txt_content).text = content
        } else {
            customView.findViewById<TextView>(R.id.txt_content).visibility = View.GONE
        }
    }
}