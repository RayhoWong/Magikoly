package com.glt.magikoly.function.resultreport.baby

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class BabyReportDTO : Serializable {
    /**
     * 1 baby_image_url String	非空	宝宝合成图
    2	father_image_url	String	非空	父亲图片
    3	mother_image_url	String	非空	母亲图片
    4	ethnicity	Integer	非空	宝宝人种,1 白色人种,2 黄色人种,3 西班牙，4 拉丁美洲,5 黑色人种 ,6 其他
    5	gender	String	非空	F-女，M-男
     */
    @SerializedName(value = "baby_image_url")
    var babyImageUrl: String? = null

    @SerializedName(value = "father_image_url")
    var fatherImageUrl: String? = null

    @SerializedName(value = "mother_image_url")
    var motherImageUrl: String? = null

    @SerializedName(value = "ethnicity")
    var ethniciy: Int = 0

    @SerializedName(value = "gender")
    var gender: String? = null
}