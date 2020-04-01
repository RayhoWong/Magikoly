package com.glt.magikoly.function.main.multiface

import com.glt.magikoly.function.facesdk.FaceSdkProxy
import com.glt.magikoly.mvp.IViewInterface

interface IMultiFace : IViewInterface<MultiFacePresenter>, FaceSdkProxy.OnDetectResult {
    fun getOriginalPath(): String
}