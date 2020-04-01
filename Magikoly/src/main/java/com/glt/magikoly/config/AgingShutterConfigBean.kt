package com.glt.magikoly.config

import com.glt.magikoly.pref.PrefConst.KEY_AGING_SHUTTER_CONFIG_CACHE
import org.json.JSONArray

class AgingShutterConfigBean : AbsConfigBean() {

    companion object {
        const val SID = 757
        const val TYPE_FACE = "1"
        const val TYPE_BIG_DATA = "2"

        fun getRequestType(): String {
            val configBean = ConfigManager.getInstance().getConfigBean(SID) as AgingShutterConfigBean
            return configBean.requestType
        }
    }

    var requestType: String = TYPE_FACE

    override fun restoreDefault() {
        requestType = TYPE_FACE
    }

    override fun readConfig(jsonArray: JSONArray?) {
        val jsonObj = jsonArray?.optJSONObject(0) ?: return
        requestType = jsonObj.optString("fusion_model")
    }

    override fun getCacheKey(): String {
        return KEY_AGING_SHUTTER_CONFIG_CACHE
    }
}