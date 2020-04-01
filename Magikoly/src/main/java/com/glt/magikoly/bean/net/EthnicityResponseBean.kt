package com.glt.magikoly.bean.net

import com.google.gson.annotations.SerializedName

/**
 * @desc:种族分析的响应类
 *
 * @auther:duwei
 * @date:2019/1/14
 */
class EthnicityResponseBean : BaseResponseBean() {

    /**
     * 种族报告
     */
    @SerializedName("ethnicity_report")
    var ethnicityReport: EthnicityReportDTO? = null

}