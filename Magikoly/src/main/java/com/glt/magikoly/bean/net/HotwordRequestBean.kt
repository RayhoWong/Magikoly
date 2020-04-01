package com.glt.magikoly.bean.net

/**
 * @desc: 热词请求bean
 */
class HotwordRequestBean : BaseRequestBean() {

    companion object {
        const val REQUEST_URL = "/api/v1/image/search/hot_words"
    }

}