package com.glt.magikoly.function.main.discovery

import com.glt.magikoly.FaceAppState
import com.glt.magikoly.FaceEnv
import com.glt.magikoly.bean.net.HotWordDTO
import com.glt.magikoly.bean.net.HotwordResponseBean
import com.glt.magikoly.cache.CacheManager
import com.glt.magikoly.cache.utils.RestoreUtil
import com.glt.magikoly.ext.parse
import com.glt.magikoly.ext.toJson
import com.glt.magikoly.pref.PrefConst
import com.glt.magikoly.pref.PrefDelegate

/**
 *
 * @author rayhahah
 * @blog http://rayhahah.com
 * @time 2019/3/7
 * @tips 这个类是Object的子类
 * @fuction
 */
class DiscoveryController private constructor() {


    companion object {
        fun getInstance() = Holder.instance
    }

    object Holder {
        val instance = DiscoveryController()
    }

    private var mCacheManager: CacheManager? = null

    private var mHotWordLines: Int by PrefDelegate(PrefConst.KEY_DISCOVERY_HOTWORD_LINE, 7)


    /**
     * 记录热词行数
     */
    fun recordHotwordLine(line: Int) {
        mHotWordLines = line
    }

    /**
     * 获取热词行数
     */
    fun getHotwordLine(): Int = mHotWordLines

    /**
     * 保存热词数据到本地
     */
    fun saveHotwordOnline(response: HotwordResponseBean) {
        initCacheManager()
        mCacheManager?.saveCacheAsync(PrefConst.KEY_DISCOVERY_HOTWORD_CACHE, response.toJson().toByteArray(), null)
    }

    private fun initCacheManager() {
        if (mCacheManager == null) {
            mCacheManager = CacheManager(RestoreUtil.getCacheImpl(FaceEnv.Path.HOTWORD_CACHE,
                    FaceEnv.InternalPath.getInnerFilePath(FaceAppState.getContext(), FaceEnv.InternalPath.HOTWORD_DIR),
                    PrefConst.KEY_DISCOVERY_HOTWORD_CACHE))
        }
    }

    /**
     * 获取线上下发的热词数据
     */
    fun getHotwordOnline(): List<HotWordDTO> {
        initCacheManager()
        val result = ArrayList<HotWordDTO>()
        mCacheManager?.apply {
            val cacheData = loadCache(PrefConst.KEY_DISCOVERY_HOTWORD_CACHE)
            if (cacheData != null) {
                val responseBean = String(cacheData).parse<HotwordResponseBean>()
                result.addAll(responseBean.hotwords ?: emptyList())
            }
        }
        return result
    }

}