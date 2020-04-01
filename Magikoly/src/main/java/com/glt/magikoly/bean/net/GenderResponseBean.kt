package com.glt.magikoly.bean.net

import com.google.gson.annotations.SerializedName

/**
 * @desc:变性的分析报告
 *
 * @auther:duwei
 * @date:2019/1/14
 */
class GenderResponseBean : BaseResponseBean() {

    /**
     * 种族报告
     */
    @SerializedName("gender_report")
    var genderReport: GenderReportDTO? = null

}