package com.glt.magikoly.bean.net

import com.google.gson.annotations.SerializedName

class VisionChildRequestBean : VisionBaseRequestBean() {

    companion object {
        const val REQUEST_URL = "/api/public/v2/face/child" //正式服
//        const val REQUEST_URL = "/child" //测试服
    }

    @JvmField
    @SerializedName("img_base64")
    var imgBase64: String? = null
}