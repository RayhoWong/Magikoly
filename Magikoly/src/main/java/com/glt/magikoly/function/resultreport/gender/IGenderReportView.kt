package com.glt.magikoly.function.resultreport.gender

import com.glt.magikoly.mvp.IViewInterface

interface IGenderReportView : IViewInterface<GenderReportPresenter> {

    fun onFaceDetectFail(errorCode: Int)
    fun onGenderReportGenerateSuccess(url: String)
    fun onGenderReportGenerateFail(errorCode: Int)
//    fun onPhotoSaved(success: Boolean)
}
