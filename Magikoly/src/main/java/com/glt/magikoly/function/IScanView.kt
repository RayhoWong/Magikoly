package com.glt.magikoly.function

import com.glt.magikoly.bean.LandmarkDTO
import com.glt.magikoly.mvp.AbsPresenter
import com.glt.magikoly.mvp.IViewInterface

open interface IScanView<T : AbsPresenter<out IScanView<T>>> : IViewInterface<T> {
    fun startMarkAnimation(scanViewId:Int, landmarkDTO: LandmarkDTO?)
}