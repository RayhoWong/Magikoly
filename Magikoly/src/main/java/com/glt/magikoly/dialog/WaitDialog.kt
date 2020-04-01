package com.glt.magikoly.dialog

import android.app.Activity
import android.content.DialogInterface
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.utils.ToastUtils
import magikoly.magiccamera.R
import kotlin.math.roundToLong

class WaitDialog(activity: Activity, val entrance: String) : BaseDialog(activity) {

    private val customView = LayoutInflater.from(context).inflate(R.layout.wait_dialog_view, null)
    private var startTime = -1L
    private var mNotCancelRes = -1

    private var subDismissListener: DialogInterface.OnDismissListener? = null
    private var isCancel: Boolean = false

    private val dismissListener = object : DialogInterface.OnDismissListener {
        override fun onDismiss(dialog: DialogInterface?) {
            if (!isCancel) {
                val duration = ((System.currentTimeMillis() - startTime) / 1000.toDouble()).roundToLong()
                BaseSeq103OperationStatistic.uploadSqe103StatisticData(duration.toString(),
                        Statistic103Constant.LOADING_PAGE_ENTER, entrance, "1", "1")
            }

            subDismissListener?.onDismiss(dialog)
        }
    }

    private var subCancelListener: DialogInterface.OnCancelListener? = null
    private val cancelListener = object : DialogInterface.OnCancelListener {
        override fun onCancel(dialog: DialogInterface?) {
            isCancel = true

            val duration = ((System.currentTimeMillis() - startTime) / 1000.toDouble()).roundToLong()
            BaseSeq103OperationStatistic.uploadSqe103StatisticData(duration.toString(),
                    Statistic103Constant.LOADING_PAGE_ENTER, entrance, "2", "1")

            subCancelListener?.onCancel(dialog)
        }

    }


    init {
        setCustomView(customView)

        this.startTime = System.currentTimeMillis()

        setOnDismissListener(dismissListener)
        setOnCancelListener(cancelListener)
    }


    override fun onBackPressed() {
        if (mNotCancelRes != -1) {
            ToastUtils.showToast(mNotCancelRes, Toast.LENGTH_SHORT)
        } else {
            super.onBackPressed()
        }
    }

    fun setNotCancel(notCancelRes: Int) {
        mNotCancelRes = notCancelRes
    }

    fun setContent(contentId: Int) {
        customView.findViewById<TextView>(R.id.loading_text).setText(contentId)
    }

    fun setContent(content: CharSequence) {
        customView.findViewById<TextView>(R.id.loading_text).text = content
    }

    override fun setOnDismissListener(listener: DialogInterface.OnDismissListener?) {
        super.setOnDismissListener(dismissListener)
        if (listener != dismissListener) {
            subDismissListener = listener
        }
    }

    override fun setOnCancelListener(listener: DialogInterface.OnCancelListener?) {
        super.setOnCancelListener(cancelListener)
        if (listener != cancelListener) {
            subCancelListener = listener
        }
    }

}