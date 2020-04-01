package com.glt.magikoly.function.ethnicity.view

import com.glt.magikoly.bean.net.EthnicityReportDTO
import com.glt.magikoly.function.ethnicity.presenter.EthnicityResultPresenter
import com.glt.magikoly.mvp.IViewInterface

/**
 * @desc:
 * @auther:duwei
 * @date:2019/1/14
 */
interface IEthnicityResultView : IViewInterface<EthnicityResultPresenter> {

//    fun onPhotoSaved(success: Boolean)

//    fun onFaceDetectFailed(errorCode: Int)

    fun onEthnicityAnalyseSuccess(response: EthnicityReportDTO?)

    fun onEthnicityAnalyseFailed(errorMsg: Int)
}