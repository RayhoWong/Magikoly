package com.glt.magikoly.bean.net

import com.glt.magikoly.bean.S3ImageInfo
import com.google.gson.annotations.SerializedName

/**
 * @desc: 种族预测请求bean
 *      METHOD : POST ,
 *      Content-Type : application/json
 *      接口时间可能比较长，请求超时时间请设置60秒以上
 * @auther:duwei
 * @date:2019/1/14
 */
class EthnicityRequestBean : BaseRequestBean() {

    companion object {
        const val REQUEST_URL = "/api/v1//ethnicity/report/generate"
    }

    /**
     * image	S3ImageInfo	人脸图片信息，参考S3ImageInfo类
     */
    @SerializedName("image")
    var image: S3ImageInfo? = null
    /**
     * gender	String	用户性别	非空, F-女，M-男
     */
    @SerializedName("gender")
    var gender: String? = null
    /**
     * time_limit	boolean	服务器是否限制调用次数
     */
    @SerializedName("time_limit")
    var timeLimit: Boolean = false

}