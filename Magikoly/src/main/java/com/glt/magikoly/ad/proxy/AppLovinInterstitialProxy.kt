package com.glt.magikoly.ad.proxy

import com.applovin.adview.AppLovinIncentivizedInterstitial
import com.applovin.sdk.AppLovinAd
import com.applovin.sdk.AppLovinAdLoadListener
import com.applovin.sdk.AppLovinSdk
import com.cs.bd.ad.AdSdkContants
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.ext.runMain
import com.glt.magikoly.utils.Logcat

class AppLovinInterstitialProxy(adUnit: String) :
        AbsAdProxy<AppLovinIncentivizedInterstitial>(adUnit) {

    companion object {
        const val WAIT_TIME: Long = 500
        var isInitialized = false
        var isInitializing = false
        fun initialize() {
            if (!isInitialized && !isInitializing) {
                isInitializing = true
                val startTime = System.currentTimeMillis()
                AppLovinSdk.initializeSdk(FaceAppState.getContext()) {
                    isInitializing = false
                    isInitialized = true
                    Logcat.i("AppLovin", "init time: ${System.currentTimeMillis() - startTime}")
                }
            }
        }
    }

    private var retryCount = 0
    private var maxRetryCount = 0
    init {
        maxRetryCount = (getTimeout() / WAIT_TIME).toInt()
    }

    override fun getTimeout(): Long {
        return 20000
    }

    override fun load() {
        if (isInitialized) {
            val startTime = System.currentTimeMillis()
            val interstitial = AppLovinIncentivizedInterstitial.create(
                    FaceAppState.getContext())
            interstitial.preload(object : AppLovinAdLoadListener {
                override fun adReceived(appLovinAd: AppLovinAd) {
                    Logcat.d("AppLovin", "AppLovin onAdLoad success: ${System.currentTimeMillis() - startTime}")
                    onAdLoad(interstitial)
                }

                override fun failedToReceiveAd(errorCode: Int) {
                    Logcat.d("AppLovin", "AppLovin failedToReceiveAd: $errorCode")
                    onAdError(errorCode)
                }
            })
        } else {
            if (retryCount < maxRetryCount) {
                runMain(WAIT_TIME) {
                    load()
                }
            } else {
                Logcat.d("AppLovin", "AppLovin timeout")
                onAdError(AdSdkContants.REQUEST_AD_STATUS_CODE_REQUEST_ERROR)
            }
        }
    }
}