package com.glt.magikoly.function.resultreport.aging.presenter

import com.glt.magikoly.constants.ErrorCode
import com.glt.magikoly.ext.runMain
import com.glt.magikoly.function.FaceFunctionManager
import com.glt.magikoly.function.resultreport.aging.IAgingShutterReportView
import com.glt.magikoly.mvp.AbsPresenter
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.subscribe.SubscribeController
import kotlin.math.roundToLong

class AgingShutterReportPresenter : AbsPresenter<IAgingShutterReportView>() {

    companion object {
        const val AGE = 70
    }

    private var retryCount: Int = 0
    var isGenerating = false

    fun generateAgingReport(useFreeCount: Boolean) {
        retryCount = 0
        isGenerating = true
        val startTime = System.currentTimeMillis()
        doGenerateAgingReport(startTime, useFreeCount)
    }

    fun doGenerateAgingReport(startTime: Long, useFreeCount: Boolean) {
        val faceBean = FaceFunctionManager.faceBeanMap[FaceFunctionManager.currentFaceImagePath]
        faceBean?.let {
            FaceFunctionManager.generateAgingReport(AGE, it.faceInfo, it.imageInfo,
                    object : FaceFunctionManager.IAgingReportListener {
                        override fun onAgingReportGenerateSuccess(imageUrl: String) {
                            BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                                    ((System.currentTimeMillis() - startTime) / 1000f).roundToLong().toString(),
                                    Statistic103Constant.FUNCTION_ACHIEVE,
                                    Statistic103Constant.ENTRANCE_AGING, "1", "")
                            if (useFreeCount) {
                                SubscribeController.getInstance().subFreeCount()
                            }

                            isGenerating = false
                            view?.onAgingReportGenerateSuccess(imageUrl)
                        }

                        override fun onAgingReportGenerateFail(errorCode: String) {
                            if (view != null) {
                                if ((errorCode == ErrorCode.THIRD_PART_PROVIDER_UNAVAILABLE_STR
                                                || errorCode.startsWith(
                                                ErrorCode.NETWORK_ERROR_STR)) && retryCount < 3) {
                                    retryCount++
                                    runMain(500) { doGenerateAgingReport(startTime, useFreeCount) }
                                } else {
                                    BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                                            ((System.currentTimeMillis() - startTime) / 1000f).roundToLong().toString(),
                                            Statistic103Constant.FUNCTION_ACHIEVE,
                                            Statistic103Constant.ENTRANCE_AGING, "2",
                                            errorCode)
                                    view?.onAgingReportGenerateFail(
                                            FaceFunctionManager.convertErrorString(errorCode))
                                    isGenerating = false
                                }
                            } else {
                                isGenerating = false
                            }
                        }
                    })
        }
    }
}