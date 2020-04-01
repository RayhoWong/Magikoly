package com.glt.magikoly.bean.net

import com.google.gson.annotations.SerializedName

/**
 * @desc:热词请求返回数据
 */
class HotwordResponseBean : BaseResponseBean() {

    /**
     * 热词列表
     */
    @SerializedName("hot_words")
    var hotwords: List<HotWordDTO>? = null

}