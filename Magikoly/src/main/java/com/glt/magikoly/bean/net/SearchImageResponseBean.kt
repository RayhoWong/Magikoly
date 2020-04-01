package com.glt.magikoly.bean.net

import com.google.gson.annotations.SerializedName

/**
 * @desc:热词请求返回数据
 */
class SearchImageResponseBean : BaseResponseBean() {

    /**
     * 图片列表	参见SearchImageDTO数据项
     */
    @SerializedName("images")
    var images: List<SearchImageDTO>? = null

}