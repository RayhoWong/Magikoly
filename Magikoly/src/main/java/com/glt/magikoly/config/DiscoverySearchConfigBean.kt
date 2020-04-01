package com.glt.magikoly.config

import com.glt.magikoly.pref.PrefConst
import org.json.JSONArray

class DiscoverySearchConfigBean : AbsConfigBean() {

    companion object {
        const val SID = 763

        fun isOpenImageSearch(): Boolean {
            val configBean = ConfigManager.getInstance().getConfigBean(SID) as DiscoverySearchConfigBean
            return configBean.isImageSearch
        }
    }


    override fun restoreDefault() {
    }

    private var isImageSearch: Boolean = true

    override fun readConfig(jsonArray: JSONArray?) {
        val jsonObj = jsonArray?.optJSONObject(0) ?: return
        val isOpenSearch = jsonObj.optString("real_time_image_search")
        isImageSearch = if (isOpenSearch == "1") {
            true
        } else if (isOpenSearch == "2") {
            false
        } else {
            true
        }
    }

    override fun getCacheKey(): String {
        return PrefConst.KEY_DISCOVERY_SEARCH_CONFIG_CACHE
    }
}