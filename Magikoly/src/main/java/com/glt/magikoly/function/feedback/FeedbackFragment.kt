package com.glt.magikoly.function.feedback

import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.cs.bd.commerce.util.Machine
import com.glt.magikoly.function.feedback.presenter.FeedbackPresenter
import com.glt.magikoly.mvp.BaseSupportFragment
import com.glt.magikoly.utils.ToastUtils
import magikoly.magiccamera.R
import kotlinx.android.synthetic.main.feek_back_main_layout.*
import kotlinx.android.synthetic.main.include_face_common_toolbar.*

class FeedbackFragment : BaseSupportFragment<FeedbackPresenter>(), IFeedBack {
    private lateinit var mSubmit: Button
    override fun onFeedbackStatus(success: Boolean) {
        var inflate = LayoutInflater.from(context).inflate(R.layout.feedback_submit_toast, null)
        var imageView = inflate.findViewById<ImageView>(R.id.img_feedback_toast_image)
        var textView = inflate.findViewById<TextView>(R.id.tv_feedback_toast_text)
        if (success) {
            imageView.setImageResource(R.drawable.feedback_toast_ok)
            textView.setText(R.string.feedback_submit_success)
        } else {
            imageView.setImageResource(R.drawable.feedback_toast_opps)
            textView.setText(R.string.feedback_submit_fail)
        }

        val toast = Toast(context)
        toast.view = inflate
        toast.setGravity(Gravity.START or Gravity.END or Gravity.BOTTOM, 0, 0)
        toast.duration = Toast.LENGTH_SHORT
        toast.show()
        post {
            if (success) {
                pop()
            } else {
                mSubmit.isEnabled = true
                mSubmit.setText(R.string.feedback_submit)
            }
        }
    }


    companion object {
        fun newInstance(): FeedbackFragment {
            return FeedbackFragment()
        }
    }

    override fun createPresenter(): FeedbackPresenter {
        return FeedbackPresenter()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.feek_back_main_layout, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        face_common_toolbar.setBackDrawable(R.drawable.icon_back_black_selector)
        face_common_toolbar.setTitle(
            resources.getString(R.string.setting_item_feedback)
        )
        face_common_toolbar.setTitleColor(resources.getColor(R.color.toolbar_title_dark_color))
        face_common_toolbar.setTitleGravity(Gravity.START)
        face_common_toolbar.setOnTitleClickListener { view, back ->
            if (view.id == R.id.img_back) {
                pop()
            }
        }
        mSubmit = view.findViewById(R.id.btn_submit)
        mSubmit.setOnClickListener {
            var email = et_feedback_email.text.toString()
            if (TextUtils.isEmpty(email)) {
                ToastUtils.showToast(R.string.feedback_email_empty, Toast.LENGTH_LONG)
                return@setOnClickListener
            }
            var emailFormat = emailFormat(email)
            if (!emailFormat) {
                ToastUtils.showToast(R.string.feedback_email_error, Toast.LENGTH_LONG)
                return@setOnClickListener
            }

            var detail = et_feedback_detail.text.toString()
            if (TextUtils.isEmpty(detail)) {
                ToastUtils.showToast(R.string.feedback_email_detail_empty_error, Toast.LENGTH_LONG)
                return@setOnClickListener
            }

            mPresenter.doSendFeedback(et_feedback_detail.text.toString(), email)
            post {
                mSubmit.setText(R.string.feedback_uploading)
                mSubmit.isEnabled = false
            }
        }
    }

    fun emailFormat(email: String): Boolean {
        return Machine.compileEmailAddress().matcher(email).matches()
    }

}