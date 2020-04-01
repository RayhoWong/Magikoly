package com.glt.magikoly.function.ethnicity.presenter

import com.glt.magikoly.bean.FaceInfo
import com.glt.magikoly.bean.S3ImageInfo
import com.glt.magikoly.bean.net.EthnicityReportDTO
import com.glt.magikoly.constants.ErrorCode
import com.glt.magikoly.function.FaceFunctionManager
import com.glt.magikoly.function.ethnicity.polygon.PolygonChartBean
import com.glt.magikoly.function.ethnicity.view.IEthnicityResultView
import com.glt.magikoly.mvp.AbsPresenter
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.subscribe.SubscribeController
import com.glt.magikoly.utils.Logcat
import magikoly.magiccamera.R
import java.util.*
import kotlin.math.roundToLong

/**
 * @desc:
 * @auther:duwei
 * @date:2019/1/14
 */
class EthnicityResultPresenter : AbsPresenter<IEthnicityResultView>() {
    companion object {
        var isFemale = false
    }

    private var mRetryCount = 0

    fun startEthnicityAnalysis(useFreeCount: Boolean) {
        val startTime = System.currentTimeMillis()
        generateEthnicityReport(
                FaceFunctionManager.faceBeanMap[FaceFunctionManager.currentFaceImagePath]?.imageInfo,
                FaceFunctionManager.faceBeanMap[FaceFunctionManager.currentFaceImagePath]?.faceInfo, startTime, useFreeCount)
    }


    private fun generateEthnicityReport(imageInfo: S3ImageInfo?, faceInfo: FaceInfo?, startTime: Long, useFreeCount: Boolean) {
        val gender = faceInfo?.gender
        EthnicityResultPresenter.isFemale = gender.equals("F")
        if (imageInfo != null) {
            FaceFunctionManager.generateEthnicityReport(gender,
                    imageInfo, object : FaceFunctionManager.IEthnicityReportListener {

                override fun onEthnicityResponse(response: EthnicityReportDTO?) {
                    Logcat.d("wdw", "种族分析---分析报告返回成功")
                    BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                            ((System.currentTimeMillis() - startTime) / 1000f).roundToLong().toString(),
                            Statistic103Constant.FUNCTION_ACHIEVE,
                            Statistic103Constant.ENTRANCE_ETHNICITY, "1", "")
                    if (useFreeCount) {
                        SubscribeController.getInstance().subFreeCount()
                    }

                    view?.onEthnicityAnalyseSuccess(response)
                }

                override fun onEthnicityRequestFailed(errorMsg: String) {
                    Logcat.d("wdw", "种族分析---分析报告返回出错 errorMsg = $errorMsg")
                    if ((errorMsg == ErrorCode.THIRD_PART_PROVIDER_UNAVAILABLE_STR
                                    || errorMsg.startsWith(ErrorCode.NETWORK_ERROR_STR)) && mRetryCount < 3) {
                        Logcat.d("wdw", "种族分析---重试机制---当前次数 = $mRetryCount")
                        generateEthnicityReport(imageInfo, faceInfo, startTime, useFreeCount)
                        mRetryCount++
                    } else {
                        BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                                ((System.currentTimeMillis() - startTime) / 1000f).roundToLong().toString(),
                                Statistic103Constant.FUNCTION_ACHIEVE,
                                Statistic103Constant.ENTRANCE_ETHNICITY, "2", errorMsg)
                        view?.onEthnicityAnalyseFailed(FaceFunctionManager.convertErrorString(errorMsg))
                        mRetryCount = 0
                    }
                }
            })
        }
    }

    fun generateViewData(dto: EthnicityReportDTO): ArrayList<PolygonChartBean> {

        var list = ArrayList<PolygonChartBean>()
        var bean1 = PolygonChartBean(
                if (isFemale)
                    R.drawable.ethnicity_female_asian
                else
                    R.drawable.ethnicity_male_asian,
                R.string.function_ethnicity_asian,
                dto.asianScore!! / 100
        )
        list.add(bean1)
        var bean2 = PolygonChartBean(
                if (isFemale)
                    R.drawable.ethnicity_female_black
                else
                    R.drawable.ethnicity_male_black,
                R.string.function_ethnicity_black,
                dto.blackScore!! / 100
        )
        list.add(bean2)
        var bean3 = PolygonChartBean(
                if (isFemale)
                    R.drawable.ethnicity_female_latino
                else
                    R.drawable.ethnicity_male_latino,
                R.string.function_ethnicity_latino,
                dto.hispanicOrlatinoScore!! / 100
        )
        list.add(bean3)
        var bean4 = PolygonChartBean(
                if (isFemale)
                    R.drawable.ethnicity_female_middle_east
                else
                    R.drawable.ethnicity_male_middle_east,
                R.string.function_ethnicity_spanish,
                dto.middleEasternOrNorthAfrican!! / 100
        )
        list.add(bean4)
        var bean5 = PolygonChartBean(
                if (isFemale)
                    R.drawable.ethnicity_female_other
                else
                    R.drawable.ethnicity_male_other
                ,
                R.string.function_ethnicity_other,
                dto.otherScore!! / 100
        )
        list.add(bean5)
        var bean6 = PolygonChartBean(
                if (isFemale)
                    R.drawable.ethnicity_female_white
                else
                    R.drawable.ethnicity_male_white,
                R.string.function_ethnicity_white,
                dto.caucasianScore!! / 100
        )
        list.add(bean6)
        return list
    }
}