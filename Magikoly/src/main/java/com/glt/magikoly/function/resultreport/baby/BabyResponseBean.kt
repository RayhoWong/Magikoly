package com.glt.magikoly.bean.net

import com.glt.magikoly.bean.net.BaseResponseBean
import com.glt.magikoly.function.resultreport.baby.BabyReportDTO
import com.google.gson.annotations.SerializedName

class BabyResponseBean : BaseResponseBean() {

    @SerializedName(value = "baby_report")
    var babyReport: BabyReportDTO? = null
}