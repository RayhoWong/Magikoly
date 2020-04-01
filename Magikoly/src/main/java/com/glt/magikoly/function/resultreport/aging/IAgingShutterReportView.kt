package com.glt.magikoly.function.resultreport.aging

import com.glt.magikoly.function.resultreport.aging.presenter.AgingShutterReportPresenter
import com.glt.magikoly.mvp.IViewInterface

interface IAgingShutterReportView : IViewInterface<AgingShutterReportPresenter> {

//    fun onFaceDetectFail(errorCode: Int)
    fun onAgingReportGenerateSuccess(url: String)
    fun onAgingReportGenerateFail(errorCode: Int)
//    fun onPhotoSaved(success: Boolean)
}
