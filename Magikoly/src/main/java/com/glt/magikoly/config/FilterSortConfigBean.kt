package com.glt.magikoly.config

import com.glt.magikoly.pref.PrefConst
import org.json.JSONArray

class FilterSortConfigBean : AbsConfigBean() {

    companion object {
        const val SID = 823

        fun getFilterSort(): HashMap<String, Int> {
            val configBean = ConfigManager.getInstance().getConfigBean(SID) as FilterSortConfigBean
            val map = configBean.mFilterSort
            if (map.size < 1) {
                configBean.restoreDefault()
                return configBean.mFilterSort
            } else {
                return map
            }

        }
    }

    private val mFilterSort: HashMap<String, Int> = HashMap()

    override fun restoreDefault() {
        localSort()
    }

    private fun localSort() {
        mFilterSort.clear()
        mFilterSort.put("femme", 1)
        mFilterSort.put("alpaca", 2)
        mFilterSort.put("curly_hair", 3)
        mFilterSort.put("dragon", 4)
        mFilterSort.put("picnic", 5)
        mFilterSort.put("marmalade", 6)
        mFilterSort.put("seagull", 7)
        mFilterSort.put("octopus", 8)
        mFilterSort.put("gothic", 9)
        mFilterSort.put("hot_tea", 10)
        mFilterSort.put("clean_line", 11)
        mFilterSort.put("cowboy", 12)
        mFilterSort.put("old_fashioned", 13)
        mFilterSort.put("jellyfish", 14)
        mFilterSort.put("tokyo", 15)
    }

    override fun readConfig(jsonArray: JSONArray?) {
//        localSort()
        jsonArray?.apply {
            mFilterSort.clear()
            if (length() <= 0) {
                localSort()
                return
            }

            for (i in 0..length()) {
                val jsonObj = optJSONObject(i)
                jsonObj?.apply {
                    val tag = optString("identifier")
                    val sort = optString("sort")
                    if (tag.isNotBlank()) {
                        mFilterSort.put(tag, sort.toInt())
                    }
                }
            }
        }
    }

    override fun getCacheKey(): String {
        return PrefConst.KEY_FILTER_SORT_CONFIG_CACHE
    }
}