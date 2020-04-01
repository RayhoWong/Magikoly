package com.glt.magikoly.bean.net

import com.google.gson.annotations.SerializedName

/**
 * @desc: 图片搜索请求bean
 */
class SearchImageRequestBean : BaseRequestBean() {

    companion object {
        const val REQUEST_URL = "/api/v1/image/search"
    }

    /**
     * 	搜索内容	非空
     */
    @SerializedName("content")
    var content: String? = null

    /**
     * 搜索模式	1:热词点击, 2:手动输入
     */
    @SerializedName("mode")
    var mode: Int? = null

    /**
     * 搜索页	可空, 默认为1, 每页20张图
     */
    @SerializedName("page")
    var page: Int? = null

}