package com.glt.magikoly.bean.net

import com.google.gson.annotations.SerializedName

class VisionDetectRequestBean : VisionBaseRequestBean() {

    companion object {
        const val REQUEST_URL = "/api/public/v2/face/face_detection"
    }

    @JvmField
    @SerializedName("img_base64")
    var imgBase64: String? = null
}