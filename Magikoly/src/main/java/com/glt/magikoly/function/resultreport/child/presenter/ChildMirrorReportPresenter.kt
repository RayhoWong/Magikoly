package com.glt.magikoly.function.resultreport.child.presenter

import android.graphics.Bitmap
import com.glt.magikoly.constants.ErrorCode.FACE_DETECT_ERROR
import com.glt.magikoly.function.FaceFunctionManager
import com.glt.magikoly.function.resultreport.child.IChildMirrorReportView
import com.glt.magikoly.mvp.AbsPresenter
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.subscribe.SubscribeController
import kotlin.math.roundToLong

class ChildMirrorReportPresenter : AbsPresenter<IChildMirrorReportView>() {

    var isGenerating = false
    fun requestChildReport(src: Bitmap?, useFreeCount: Boolean) {
        if (src == null) {
            view?.onChildReportGenerateFail(FACE_DETECT_ERROR)
        } else {
            isGenerating = true
            val startTime = System.currentTimeMillis()
            FaceFunctionManager.generateChildReport(src,
                    object : FaceFunctionManager.IChildReportListener {
                        override fun onChildReportGenerateSuccess(result: Bitmap) {
                            isGenerating = false
                            BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                                    ((System.currentTimeMillis() - startTime) / 1000f).roundToLong().toString(),
                                    Statistic103Constant.FUNCTION_ACHIEVE,
                                    Statistic103Constant.ENTRANCE_CHILD, "1", "")
                            if (useFreeCount) {
                                SubscribeController.getInstance().subFreeCount()
                            }

                            view?.onChildReportGenerateSuccess(result)
                        }

                        override fun onChildReportGenerateFail(errorCode: String) {
                            isGenerating = false
                            BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                                    ((System.currentTimeMillis() - startTime) / 1000f).roundToLong().toString(),
                                    Statistic103Constant.FUNCTION_ACHIEVE,
                                    Statistic103Constant.ENTRANCE_CHILD, "2",
                                    errorCode)
                            view?.onChildReportGenerateFail(FaceFunctionManager.convertErrorString(errorCode))
                        }
                    })
        }
    }
}