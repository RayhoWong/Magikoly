package com.glt.magikoly.config

import com.glt.magikoly.pref.PrefConst
import org.json.JSONArray

class MainBannerConfigBean : AbsConfigBean() {

    companion object {
        const val SID = 834

        fun isShowMainBanner(): Boolean {
            val configBean = ConfigManager.getInstance().getConfigBean(SID) as MainBannerConfigBean
            return configBean.mShowHomeBanner
        }

        fun isShowCloseButton(): Boolean {
            val configBean = ConfigManager.getInstance().getConfigBean(SID) as MainBannerConfigBean
            return configBean.mShowCloseButton
        }
    }

    private var mShowHomeBanner: Boolean = false
    private var mShowCloseButton: Boolean = false

    override fun restoreDefault() {
        mShowCloseButton = false
        mShowHomeBanner = false
    }

    override fun readConfig(jsonArray: JSONArray?) {
//        localSort()
        jsonArray?.apply {
            for (i in 0..length()) {
                val jsonObj = optJSONObject(i)
                jsonObj?.apply {
                    val showHomeBanner = optString("home_banner")
                    val showCloseButton = optString("close_button")

                    mShowHomeBanner = if (showHomeBanner == "1") {
                        true
                    } else {
                        false
                    }

                    mShowCloseButton = if (showCloseButton == "1") {
                        true
                    } else {
                        false
                    }
                }
            }
        }
    }

    override fun getCacheKey(): String {
        return PrefConst.KEY_MAIN_BANNER_CONFIG_CACHE
    }
}