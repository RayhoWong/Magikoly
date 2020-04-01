package com.glt.magikoly.bean.net

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * @desc:种族分析报告的详细内容
 * @auther:duwei
 * @date:2019/1/14
 */
class EthnicityReportDTO : Serializable {

    /**
     * 作者图片
     */
    @SerializedName("author_image_url")
    var authorImageUrl: String? = null
    /**
     * 白种人分数
     */
    @SerializedName("caucasian_score")
    var caucasianScore: Double? = null
    /**
     * 黃种人分数
     */
    @SerializedName("asian_score")
    var asianScore: Double? = null
    /**
     * 黑种人分数
     */
    @SerializedName("black_score")
    var blackScore: Double? = null
    /**
     * 西班牙人 或 拉丁美洲人 分数
     */
    @SerializedName("hispanic_or_latino_score")
    var hispanicOrlatinoScore: Double? = null

    /**
     * 中东或北非分数
     */
    @SerializedName("middle_eastern_or_north_african")
    var middleEasternOrNorthAfrican: Double? = null
    /**
     * 其他分数
     */
    @SerializedName("other_score")
    var otherScore: Double? = null

}