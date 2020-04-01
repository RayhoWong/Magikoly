package com.glt.magikoly.bean.net

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * @desc:热词请求详细内容
 */
class HotWordDTO : Serializable {

    /**
     * 热词
     */
    @SerializedName("word")
    var word: String? = null
}