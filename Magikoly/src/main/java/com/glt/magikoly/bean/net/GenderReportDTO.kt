package com.glt.magikoly.bean.net

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * @desc:变性分析报告的详细内容
 */
class GenderReportDTO : Serializable {

    /**
     * 变性合成图
     */
    @SerializedName("gender_image_url")
    var genderImageUrl: String? = null

    /**
     * 作者图片
     */
    @SerializedName("author_image_url")
    var authorImageUrl: String? = null

    /**
     *  非空，人种,1 白色人种,2 黄色人种,3 西班牙，4 拉丁美洲,5 黑色人种 ,6 其他
     */
    @SerializedName(value = "ethnicity")
    var ethniciy: Int = 0

    /**
     * 可空，原始性别,F-女，M-男
     */
    @SerializedName("original_gender")
    var originalGender: String? = null

    /**
     * 非空，目标性别,F-女，M-男
     */
    @SerializedName("targe_gender")
    var targetGender: String? = null


}