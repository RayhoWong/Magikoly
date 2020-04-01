package com.glt.magikoly.dialog

import android.app.Activity
import android.app.Dialog
import android.view.*
import android.widget.Button
import android.widget.FrameLayout
import magikoly.magiccamera.R

open class BaseDialog(activity: Activity) : Dialog(activity), IDialog {

    private val contentView: View

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        contentView = LayoutInflater.from(activity)
                .inflate(R.layout.base_dialog_layout, null) as ViewGroup
        setContentView(contentView)
        setCanceledOnTouchOutside(false)
    }

    fun setCustomView(view: View) {
        val container = contentView.findViewById(R.id.custom_view_container) as FrameLayout
        container.addView(view)
    }

    fun setupOKButton(resId: Int, listener: View.OnClickListener) {
        val button = contentView.findViewById(R.id.btn_ok) as Button
        button.visibility = View.VISIBLE
        button.setText(resId)
        button.setOnClickListener(listener)
    }

    fun setupCancelButton(resId: Int, listener: View.OnClickListener) {
        val button = contentView.findViewById(R.id.btn_cancel) as Button
        button.visibility = View.VISIBLE
        button.setText(resId)
        button.setOnClickListener(listener)
    }

    fun setupOKButton(text: String, listener: View.OnClickListener) {
        val button = contentView.findViewById(R.id.btn_ok) as Button
        button.visibility = View.VISIBLE
        button.text = text
        button.setOnClickListener(listener)
    }

    fun setupCancelButton(text: String, listener: View.OnClickListener) {
        val button = contentView.findViewById(R.id.btn_cancel) as Button
        button.visibility = View.VISIBLE
        button.text = text
        button.setOnClickListener(listener)
    }

    override fun show() {
        super.show()
        DialogStatusObserver.onDialogShow(this)
    }

    override fun dismiss() {
        super.dismiss()
        DialogStatusObserver.onDialogDismiss(this)
    }


}