package com.glt.magikoly.bean.net

import com.glt.magikoly.bean.FaceRectangle
import com.glt.magikoly.bean.S3ImageInfo
import com.google.gson.annotations.SerializedName

/**
 * @desc: 变性预测请求bean
 */
class GenderRequestBean : BaseRequestBean() {

    companion object {
        const val REQUEST_URL = "/api/v1/gender/report/generate"
    }

    /**
     * image	S3ImageInfo	人脸图片信息，参考S3ImageInfo类
     */
    @SerializedName("image")
    var image: S3ImageInfo? = null

    @SerializedName("face_rectangle")
    var faceRectangle: FaceRectangle? = null

    /**
     * gender	String	用户性别	非空, F-女，M-男
     */
    @SerializedName("gender")
    var gender: String? = null


    /**
     * 种族id 非空,1 白色人种,2 黄色人种,3 西班牙，4 拉丁美洲,5 黑色人种 ,6 其他
     */
    @SerializedName(value = "ethnicity")
    var ethnicity: Int = 0

    /**
     * 标签id 非空
     */
    @SerializedName(value = "tag")
    var tag: Int = 1

    /**
     * time_limit	boolean	服务器是否限制调用次数
     */
    @SerializedName("time_limit")
    var timeLimit: Boolean = true

}