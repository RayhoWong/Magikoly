package com.glt.magikoly.bean.net

import com.glt.magikoly.bean.OldReportDTO
import com.google.gson.annotations.SerializedName

class AgingResponseBean : BaseResponseBean() {
    @SerializedName("old_report")
    var oldReport: OldReportDTO? = null
}