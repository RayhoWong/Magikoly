package com.glt.magikoly.function.resultreport.gender

import com.glt.magikoly.bean.FaceRectangle
import com.glt.magikoly.bean.net.GenderReportDTO
import com.glt.magikoly.constants.ErrorCode
import com.glt.magikoly.ext.log
import com.glt.magikoly.function.FaceFunctionManager
import com.glt.magikoly.mvp.AbsPresenter
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.subscribe.billing.BillingStatusManager
import com.glt.magikoly.utils.Logcat
import kotlin.math.roundToLong

/**
 *
 * @author rayhahah
 * @blog http://rayhahah.com
 * @time 2019/2/18
 * @tips 这个类是Object的子类
 * @fuction
 */
class GenderReportPresenter : AbsPresenter<IGenderReportView>() {

    private var mRetryCount: Int = 0
    var isGenerating: Boolean = false

    fun generateGenderReport(useFreeCount: Boolean) {
        mRetryCount = 0
        isGenerating = true
        val startTime = System.currentTimeMillis()
        performGender(startTime, useFreeCount)
    }

    private fun performGender(startTime: Long, useFreeCount: Boolean) {
        val faceBean = FaceFunctionManager.faceBeanMap[FaceFunctionManager.currentFaceImagePath]
        if(faceBean == null || faceBean.faceInfo == null){
            return
        }
        val faceInfo = faceBean.faceInfo
        val faceRectangle = FaceRectangle()
        faceRectangle.left = faceInfo.left
        faceRectangle.top = faceInfo.top
        faceRectangle.width = faceInfo.width
        faceRectangle.height = faceInfo.height
        FaceFunctionManager.generateGenderReport(faceInfo.gender, faceInfo.ethnicity,
                faceRectangle, faceBean.imageInfo, false,
                object : FaceFunctionManager.IGenderReportListener {
                    override fun onGenderResponse(response: GenderReportDTO?) {
                        log("变性分析---分析报告返回成功", "lzh")
                        BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                                ((System.currentTimeMillis() - startTime) / 1000f).roundToLong().toString(),
                                Statistic103Constant.FUNCTION_ACHIEVE,
                                Statistic103Constant.ENTRANCE_GENDER, "1", "")
                        if (useFreeCount) {
                            BillingStatusManager.getInstance().subFreeCount()
                        }

                        isGenerating = false
                        view?.onGenderReportGenerateSuccess(response?.genderImageUrl ?: "")
                    }

                    override fun onGenderRequestFailed(errorCode: String) {
                        log("变性分析---分析报告返回出错 errorMsg = $errorCode", "lzh")
                        if (view != null) {
                            if ((errorCode == ErrorCode.THIRD_PART_PROVIDER_UNAVAILABLE_STR
                                            || errorCode.startsWith(ErrorCode.NETWORK_ERROR_STR)) && mRetryCount < 3) {
                                Logcat.d("wdw", "变性分析---重试机制---当前次数 = $mRetryCount")
                                performGender(startTime,useFreeCount)
                                mRetryCount++
                            } else {
                                BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                                        ((System.currentTimeMillis() - startTime) / 1000f).roundToLong().toString(),
                                        Statistic103Constant.FUNCTION_ACHIEVE,
                                        Statistic103Constant.ENTRANCE_GENDER, "2", errorCode)
                                view.onGenderReportGenerateFail(
                                        FaceFunctionManager.convertErrorString(errorCode))
                                mRetryCount = 0
                                isGenerating = false
                            }
                        } else {
                            isGenerating = false
                        }
                    }
                })
    }
}