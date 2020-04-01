package com.glt.magikoly.bean.net

import com.google.gson.annotations.SerializedName

class StatusResult {
    @SerializedName("status_code")
    var statusCode: String? = null
    var message: String? = null
}