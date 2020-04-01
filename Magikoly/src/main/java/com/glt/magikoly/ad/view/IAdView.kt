package com.glt.magikoly.ad.view

import android.view.View

interface IAdView {
    fun initView()
    fun bindData(data: Any):View
    fun setBodyContent(body: CharSequence)
    fun setInstallAppContent(rate: Double, price: CharSequence, store: CharSequence)
}