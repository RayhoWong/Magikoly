package com.glt.magikoly.function.main.discovery

import com.glt.magikoly.bean.net.SearchImageDTO
import com.glt.magikoly.function.main.discovery.presenter.DiscoveryPresenter
import com.glt.magikoly.mvp.IViewInterface

interface IDiscoveryView : IViewInterface<DiscoveryPresenter> {
    fun obtainHotWordSuccess(hotword: List<String>)
    fun obtainHotWordFailure(msg: String)

    fun obtainSearchContentSuccess(content: List<SearchImageDTO>,
        page: Int)
    fun obtainSearchContentFailure(msg: String, page: Int)
}
