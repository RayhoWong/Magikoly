package com.glt.magikoly.bean.net

import com.glt.magikoly.bean.FaceRectangle
import com.glt.magikoly.bean.S3ImageInfo
import com.google.gson.annotations.SerializedName

class AgingRequestBean : BaseRequestBean() {
    companion object {
        const val FACE_TEMPLATE_REQUEST_PATH = "/api/v1/old/template/report/generate"
        const val FACE_REQUEST_PATH = "/api/v1/old/report/generate"
        const val BIG_DATA_REQUEST_PATH = "/api/v1/old/bigdata/report/generate"
    }

    var image: S3ImageInfo? = null
    @SerializedName("face_rectangle")
    var faceRectangle: FaceRectangle? = null
    var gender:String? = null
    var age:Int = 0
    @SerializedName("original_age")
    var originalAge: Int = 0
    var ethnicity: Int = 0
    @SerializedName("time_limit")
    var timeLimit: Boolean = false
    @SerializedName("save_report")
    var saveReport: Boolean = false
}