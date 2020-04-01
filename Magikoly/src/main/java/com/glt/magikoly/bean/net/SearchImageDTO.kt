package com.glt.magikoly.bean.net

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 *
 * @author rayhahah
 * @blog http://rayhahah.com
 * @time 2019/2/14
 * @tips 这个类是Object的子类
 * @fuction
 */

/**
 * 图片搜索详细内容
 */
class SearchImageDTO : Serializable {

    /**
     * 缩略图地址
     */
    @SerializedName("thumbnail")
    var thumbnail: String? = null

    /**
     * 原图地址
     */
    @SerializedName("original")
    var original: String? = null
}