package com.glt.magikoly.function.resultreport.child

import android.graphics.Bitmap
import com.glt.magikoly.function.resultreport.child.presenter.ChildMirrorReportPresenter
import com.glt.magikoly.mvp.IViewInterface

interface IChildMirrorReportView : IViewInterface<ChildMirrorReportPresenter> {

    fun onChildReportGenerateSuccess(result: Bitmap)
    fun onChildReportGenerateFail(errorCode: Int)
}
