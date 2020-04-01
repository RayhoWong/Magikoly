package com.glt.magikoly.bean.net

import com.glt.magikoly.bean.FaceInfo
import com.google.gson.annotations.SerializedName

class DetectResponseBean : BaseResponseBean() {
    @SerializedName("face_info")
    var faceInfos: List<FaceInfo>? = null
}