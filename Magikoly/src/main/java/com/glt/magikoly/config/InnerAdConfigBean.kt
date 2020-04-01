package com.glt.magikoly.config

import com.glt.magikoly.pref.PrefConst.KEY_INNER_AD_CONFIG_CACHE
import org.json.JSONArray

class InnerAdConfigBean : AbsConfigBean() {

    companion object {
        const val SID = 804
    }

    var fullScreenAfterLaunching = true
    var loadingPageBottom = false
    var functionPageExit = false
    var switchFunction = false

    override fun restoreDefault() {
        fullScreenAfterLaunching = false
        loadingPageBottom = false
        functionPageExit = false
        switchFunction = false
    }

    override fun readConfig(jsonArray: JSONArray?) {
        val jsonObj = jsonArray?.optJSONObject(0) ?: return
        fullScreenAfterLaunching = jsonObj.optInt("fullscreen_after_launching") == 1
        loadingPageBottom = jsonObj.optInt("loading_page_bottom") == 1
        functionPageExit = jsonObj.optInt("function_page_exit") == 1
        switchFunction = jsonObj.optInt("switch_function") == 1
    }

    override fun getCacheKey(): String {
        return KEY_INNER_AD_CONFIG_CACHE
    }
}