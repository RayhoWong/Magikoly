package com.glt.magikoly.dialog

import android.app.Activity
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.glt.magikoly.function.main.MainFragment
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.utils.AppUtils
import magikoly.magiccamera.R

class TipsDialog(activity: Activity) : BaseDialog(activity) {

    private val customView = LayoutInflater.from(context).inflate(R.layout.tips_dialog_view, null)

    init {
        setCustomView(customView)
    }

    fun setContent(contentId: Int) {
        customView.findViewById<TextView>(R.id.txt_content).setText(contentId)
    }

    fun setContent(content: CharSequence) {
        customView.findViewById<TextView>(R.id.txt_content).text = content
    }

    fun setPrivacyPolicyVisible(entrance: String) {
        val findViewById = customView.findViewById<TextView>(R.id.txt_privacy_policy)
        findViewById.visibility = View.VISIBLE
        findViewById.paint.flags = Paint.UNDERLINE_TEXT_FLAG
        findViewById.setOnClickListener {
            AppUtils.openBrowser(it.context, MainFragment.PRIVACY_POLICY)

            BaseSeq103OperationStatistic
                    .uploadSqe103StatisticData(Statistic103Constant.PRIVACYAGREEMENT_CLICK, entrance)
        }

        customView.requestLayout()
    }
}