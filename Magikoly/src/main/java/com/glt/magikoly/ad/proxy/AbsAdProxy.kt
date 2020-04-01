package com.glt.magikoly.ad.proxy


import com.cs.bd.ad.params.OuterAdLoader
import com.cs.bd.ad.sdk.bean.SdkAdSourceAdInfoBean
import java.util.*

abstract class AbsAdProxy<T>(var mAdUnit: String) {

    companion object {
        const val ERROR_NULL_AD_LOAD = 1000
        const val ERROR_FLURRY_NOT_INIT = 1001
    }

    var mListener: OuterAdLoader.OuterSdkAdSourceListener? = null

    abstract fun load()

    open fun getTimeout():Long {
        return 6000
    }

    fun setAdListener(listener: OuterAdLoader.OuterSdkAdSourceListener?) {
        mListener = listener
    }

    fun onAdLoad(ad: T) {
        mListener?.let {
            val sdkAdSourceAdInfoBean = SdkAdSourceAdInfoBean()
            val sdkAdSourceAdWrappers = ArrayList<Any>()
            sdkAdSourceAdWrappers.add(ad as Any)
            sdkAdSourceAdInfoBean.addAdViewList(mAdUnit, sdkAdSourceAdWrappers)
            it.onFinish(sdkAdSourceAdInfoBean)
        }
    }

    fun onAdShow(ad: T) {
        mListener?.onAdShowed(ad)
    }

    fun onAdClose(ad: T) {
        mListener?.onAdClosed(ad)
    }

    fun onAdClick(ad: T) {
        mListener?.onAdClicked(ad)
    }

    fun onAdError(error: Int) {
        mListener?.onException(error)
    }


}
