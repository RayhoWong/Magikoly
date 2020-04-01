package com.glt.magikoly.bean

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class FaceInfo : Serializable {
    var top: Int = 0
    var left: Int = 0
    var width: Int = 0
    var height: Int = 0
    var gender: String? = null
    var age: Int = 0
    var ethnicity: Int = 0
    var landmark: LandmarkDTO? = null
    @SerializedName("glass_flag")
    var glassFlag: Boolean? = null
//    var emotion: EmotionDTO? = null
}