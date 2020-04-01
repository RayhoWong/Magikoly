package com.glt.magikoly.ad.inner

import android.app.AlarmManager
import android.content.Context
import android.util.SparseArray
import com.cs.bd.ad.AdSdkApi
import com.cs.bd.ad.bean.AdModuleInfoBean
import com.cs.bd.ad.http.bean.BaseModuleDataItemBean
import com.cs.bd.ad.manager.AdSdkManager
import com.cs.bd.ad.params.AdSdkParamsBuilder
import com.cs.bd.ad.sdk.bean.SdkAdSourceAdWrapper
import com.glt.magikoly.BuyChannelApiProxy
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.config.ConfigManager
import com.glt.magikoly.config.InnerAdConfigBean
import com.glt.magikoly.pref.PrivatePreference
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.subscribe.SubscribeController
import com.glt.magikoly.utils.Logcat
import com.glt.magikoly.version.VersionController

class InnerAdController {

    private object Holder {
        val INSTANCE = InnerAdController()
    }

    companion object {
        const val TAG = "InnerAdController"

        const val LAUNCHING_AD_MODULE_ID = 8567
        const val HOME_LAUNCHING_AD_MODULE_ID = 8568
        const val LOADING_PAGE_BOTTOM_AD_MODULE_ID = 8566
        const val ADVIDEO_MODULE_ID = 8565
        const val SWITCH_FUNCTION_AD_MODULE_ID = 8564
        const val FUNCTION_PAGE_EXIT_AD_MODULE_ID = 8563




        val instance: InnerAdController by lazy { Holder.INSTANCE }

        fun statistic(abTestId: Int, moduleId: Int, adObj: Any?, obj: String) {
            val entrance = when (moduleId) {
                LAUNCHING_AD_MODULE_ID -> 1
                HOME_LAUNCHING_AD_MODULE_ID -> 10
                LOADING_PAGE_BOTTOM_AD_MODULE_ID -> 4
                FUNCTION_PAGE_EXIT_AD_MODULE_ID -> 5
                SWITCH_FUNCTION_AD_MODULE_ID -> 7
                else -> 0
            }
//            val tab = when (adObj) {
//                is com.google.android.gms.ads.reward.RewardedVideoAd,
//                is com.google.android.gms.ads.formats.NativeAd,
//                is com.google.android.gms.ads.InterstitialAd,
//                is com.google.android.gms.ads.AdView -> "1"
//
//
//                is MoPubInterstitial,
//                is MoPubView -> "2"
//
//                is AppLovinIncentivizedInterstitial -> "3"
//
//                else -> ""
//            }

            val tab = "4"
            BaseSeq103OperationStatistic.uploadSqe103StatisticData(abTestId.toString(),
                    obj, entrance.toString(), tab)
        }
    }

    private var mAdBeanMap: SparseArray<AdBean> = SparseArray()
    private var mAdStateBeanMap: SparseArray<AdStateBean> = SparseArray()

    var needLoadLocalListBannerAd = true

    fun loadAd(context: Context, moduleId: Int, listener: AdLoadListener? = null): Boolean {
        if (SubscribeController.getInstance().isVIP()) {
            return false
        }
        val stateBean = getAdStateBean(moduleId)
        if (stateBean.isLoading) {
            return false
        }
        if (getPendingAdBean(moduleId) != null) {
            return false
        }
        val adConfigBean = ConfigManager.getInstance().getConfigBean(
                InnerAdConfigBean.SID) as InnerAdConfigBean
        val abTestId = adConfigBean.abTestId
        val adControlInterceptor = AdSdkManager.IAdControlInterceptor { itemBean ->
            stateBean.isLoad(adConfigBean, itemBean.adFrequency, itemBean.adsplit * 60L * 1000L)
        }

        val adListener = object : AdSdkManager.IVLoadAdvertDataListener {

            override fun onVideoPlayFinish(p0: Any?) {
                listener?.onVideoPlayFinish(getAdBean(moduleId))
            }

            override fun onAdImageFinish(adModuleInfoBean: AdModuleInfoBean?) {
            }

            override fun onAdInfoFinish(isCache: Boolean, adModuleInfoBean: AdModuleInfoBean?) {
                stateBean.isLoading = false
                var adBean: AdBean? = null
                val sdkAdBean = adModuleInfoBean?.sdkAdSourceAdInfoBean
                sdkAdBean?.let {
                    val wrapperList = it.adViewList
                    if (wrapperList != null && wrapperList.isNotEmpty()) {
                        val wrapper = wrapperList[0]
                        adBean = AdBean(moduleId)
                        adBean?.abTestId = abTestId
                        adBean?.baseModuleDataItemBean = adModuleInfoBean.sdkAdControlInfo
                        adBean?.sdkAdSourceAdWrapper = wrapper
                        mAdBeanMap.put(moduleId, adBean)
                        when (wrapper.adObject) {
                            //TODO 设置view
//                            is com.google.android.gms.ads.formats.NativeAd -> {
//                                //Admob Native
//                                adBean?.admodNatived = wrapper.adObject as com.google.android.gms.ads.formats.NativeAd
//                            }

                        }
                        adBean?.adObj = wrapper?.adObject
                    }
                }
                adBean?.adObj?.let {
                    Logcat.i(TAG, "onAdInfoFinish: $moduleId, adType: ${it::class.java.name}")
                }

                adBean?.onAdFilled()
                listener?.onAdLoadSuccess(getPendingAdBean(moduleId)) //这里统一使用getPendingAdBean返回的adBean，若用户购买了vip这里返回null
            }

            override fun onAdShowed(adViewObj: Any?) { //Native广告被点击后打开浏览器回调; 全屏广告不回调
                Logcat.i(TAG, "onAdShowed: $moduleId")
                listener?.onAdShowed()
            }

            override fun onAdClicked(adViewObj: Any?) {
                Logcat.i(TAG, "onAdClicked: $moduleId")
                getAdBean(moduleId)?.onAdClicked()
                listener?.onAdClicked()
            }

            override fun onAdClosed(adViewObj: Any?) { //Native广告被点击后打开浏览器, 关闭浏览器后回调; 全屏广告不回调
                Logcat.i(TAG, "onAdClosed: $moduleId")
                listener?.onAdClosed()
            }

            override fun onAdFail(statusCode: Int) {
                Logcat.i(TAG, "onAdFail: $moduleId, statusCode: $statusCode")

                stateBean.isLoading = false
                listener?.onAdLoadFail(statusCode)
            }
        }

        stateBean.isLoading = true
        loadSimpleAd(context, moduleId, adControlInterceptor, adListener) { _ ->
        }
        statistic(abTestId, moduleId, null, Statistic103Constant.AD_REQUEST)
        return true
    }

    fun cancelLoad(moduleId: Int) {
        val stateBean = getAdStateBean(moduleId)
        stateBean.isLoading = false
    }

    fun getPendingAdBean(moduleId: Int): AdBean? {
        val adBean = getAdBean(moduleId)
        if (adBean != null && (SubscribeController.getInstance().isVIP() || adBean.isOutDate || adBean.isShown)) {
            mAdBeanMap.remove(moduleId)
            return null
        }
        return adBean
    }

    private fun getAdBean(moduleId: Int): AdBean? {
        return mAdBeanMap[moduleId]
    }

    private fun getAdStateBean(moduleId: Int): AdStateBean {
        var stateBean = mAdStateBeanMap[moduleId]
        if (stateBean == null) {
            stateBean = AdStateBean(moduleId)
            mAdStateBeanMap.put(moduleId, stateBean)
        }
        return stateBean
    }

    private fun loadSimpleAd(context: Context, moduleId: Int,
                             adControlInterceptor: AdSdkManager.IAdControlInterceptor,
                             listener: AdSdkManager.ILoadAdvertDataListener, preLoad: (AdSdkParamsBuilder.Builder) -> Unit) {
        val builder = AdSdkParamsBuilder.Builder(context, moduleId, null, listener)
        preLoad(builder)
        builder.adControlInterceptor(adControlInterceptor)
        val adBuilder = builder.returnAdCount(1)
                .isNeedDownloadIcon(true)
                .isNeedDownloadBanner(true)
                .isNeedPreResolve(true)
                .isRequestData(false)
                .buyuserchannel(BuyChannelApiProxy.getBuyChannel())
//                .touTiaoAdCfg(TouTiaoAdCfg())
                .cdays(VersionController.getCdays()).build()
        AdSdkApi.loadAdBean(adBuilder)
    }


    class AdBean {
        companion object {
            private const val VALID_DATE = AlarmManager.INTERVAL_HOUR
        }

        var moduleId: Int = 0
        private val createTime: Long = System.currentTimeMillis()
        var isInterstitialAd = false
        var abTestId: Int = -1
        var adObj: Any? = null
        /**
         * 是否展示过
         */
        var isShown: Boolean = false
        var baseModuleDataItemBean: BaseModuleDataItemBean? = null
        var sdkAdSourceAdWrapper: SdkAdSourceAdWrapper? = null
        var adListener: AdLoadListener? = null


        /**
         * TODO 控件
         * fackbook native AD
        var fbNativeAd: com.facebook.ads.NativeAd? = null
         */

        constructor(moduleId: Int) {
            this.moduleId = moduleId
        }

        /**
         * 是否失效
         *
         * @return
         */
        val isOutDate: Boolean
            get() = System.currentTimeMillis() - createTime >= VALID_DATE

        fun showInterstitialAd(listener: AdLoadListener? = null): Boolean {
            if (!isInterstitialAd) {
                return false
            }
            adListener = listener
            var ret = false
            //todo 显示全屏广告
//            admobInterstitialAd?.let {
//                it.adListener = object : AdListener() {
//
//                    override fun onAdLeftApplication() {
//                        this@AdBean.onAdClicked()
//                    }
//
//                    override fun onAdClosed() {
//                        this@AdBean.onAdClosed()
//                    }
//
//                    override fun onAdOpened() {
//                        this@AdBean.onAdShowed()
//                    }
//                }
//                it.show()
//                ret = true
//            }

            return ret
        }


        fun onAdFilled() {
            statistic(abTestId, moduleId, adObj, Statistic103Constant.AD_FILLED)
        }

        fun onAdShowed() {
            isShown = true
            val stateBean = InnerAdController.instance.getAdStateBean(moduleId)
            if (System.currentTimeMillis() - stateBean.getAdLastDisplayTime() > AlarmManager.INTERVAL_DAY) {
                stateBean.resetAdDisplayCount()
            }
            stateBean.updateAdDisplayCount()
            stateBean.updateAdLastDisplayTime()
            AdSdkApi.sdkAdShowStatistic(FaceAppState.getContext(), baseModuleDataItemBean,
                    sdkAdSourceAdWrapper, "")
            statistic(abTestId, moduleId, adObj, Statistic103Constant.AD_SHOW)
            adListener?.onAdShowed()
        }

        fun onAdClicked() {
            AdSdkApi.sdkAdClickStatistic(FaceAppState.getContext(), baseModuleDataItemBean,
                    sdkAdSourceAdWrapper, "")
            statistic(abTestId, moduleId, adObj, Statistic103Constant.AD_CLICK)
            adListener?.onAdClicked()
        }

        fun onAdClosed() {
            statistic(abTestId, moduleId, adObj, Statistic103Constant.AD_CLOSE)
            adListener?.onAdClosed()
        }
    }

    private class AdStateBean {
        companion object {
            const val KEY_DISPLAY_COUNT_PREFIX = "key_display_count_"
            const val KEY_LAST_DISPLAY_TIME_PREFIX = "key_last_display_time_"
        }

        var moduleId: Int = 0
        var isLoading = false

        constructor(moduleId: Int) {
            this.moduleId = moduleId
        }

        fun isLoad(adConfigBean: InnerAdConfigBean, adFrequency: Int, adSplit: Long): Boolean {
            if (moduleId == ADVIDEO_MODULE_ID) {
                return true
            }
            val isSwitchOpen = when (moduleId) {
                LAUNCHING_AD_MODULE_ID -> adConfigBean.fullScreenAfterLaunching
                HOME_LAUNCHING_AD_MODULE_ID -> false
                LOADING_PAGE_BOTTOM_AD_MODULE_ID -> adConfigBean.loadingPageBottom
                FUNCTION_PAGE_EXIT_AD_MODULE_ID -> adConfigBean.functionPageExit
                SWITCH_FUNCTION_AD_MODULE_ID -> adConfigBean.switchFunction
                else -> true
            }
            Logcat.i(TAG, "moduleId: $moduleId switch: $isSwitchOpen adFrequency: $adFrequency adSplit: $adSplit")

            if (!isSwitchOpen) {
                return false
            }
            val displayInterval = System.currentTimeMillis() - getAdLastDisplayTime()
            if (adSplit > 0 && displayInterval < adSplit) {
                return false
            }
            if (displayInterval < AlarmManager.INTERVAL_DAY && adFrequency > 0 && getAdDisplayCount() >= adFrequency) {
                return false
            }
            return true
        }

        fun updateAdDisplayCount() {
            val pref = PrivatePreference.getPreference(FaceAppState.getContext())
            val key = "$KEY_DISPLAY_COUNT_PREFIX$moduleId"
            pref.putInt(key, pref.getInt(key, 0) + 1)
            pref.commit()
        }

        fun resetAdDisplayCount() {
            val pref = PrivatePreference.getPreference(FaceAppState.getContext())
            val key = "$KEY_DISPLAY_COUNT_PREFIX$moduleId"
            pref.putInt(key, 0)
            pref.commit()
        }

        fun updateAdLastDisplayTime() {
            val pref = PrivatePreference.getPreference(FaceAppState.getContext())
            val key = "$KEY_LAST_DISPLAY_TIME_PREFIX$moduleId"
            pref.putLong(key, System.currentTimeMillis())
            pref.commit()
        }

        private fun getAdDisplayCount(): Int {
            val pref = PrivatePreference.getPreference(FaceAppState.getContext())
            return pref.getInt("$KEY_DISPLAY_COUNT_PREFIX$moduleId", 0)
        }

        fun getAdLastDisplayTime(): Long {
            val pref = PrivatePreference.getPreference(FaceAppState.getContext())
            return pref.getLong("$KEY_LAST_DISPLAY_TIME_PREFIX$moduleId", 0L)
        }
    }

    interface AdLoadListener {
        fun onAdLoadSuccess(adBean: AdBean?) //当加载广告过程中用户购买了vip，广告虽然加载成功但这里返回null
        fun onAdLoadFail(statusCode: Int)
        fun onAdShowed()
        fun onAdClicked()
        fun onAdClosed()
        fun onVideoPlayFinish(adBean: AdBean?)
    }

    open class AdLoadListenerAdapter : AdLoadListener {
        override fun onAdLoadSuccess(adBean: AdBean?) {}
        override fun onAdLoadFail(statusCode: Int) {}
        override fun onAdShowed() {}
        override fun onAdClicked() {}
        override fun onAdClosed() {}
        override fun onVideoPlayFinish(adBean: AdBean?) {}
    }
}