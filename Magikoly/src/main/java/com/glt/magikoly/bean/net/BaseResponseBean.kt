package com.glt.magikoly.bean.net

import com.google.gson.annotations.SerializedName

open class BaseResponseBean {
    @SerializedName("status_result")
    var statusResult: StatusResult? = null
}