package com.glt.magikoly.bean.net

import com.glt.magikoly.bean.FaceRectangle
import com.glt.magikoly.bean.S3ImageInfo
import com.google.gson.annotations.SerializedName

class BabyRequestBean : BaseRequestBean() {
    /**
     * gender	String	宝宝性别	非空, F-女，M-男
    ethnicity	Integer	种族id	非空,1 白色人种,2 黄色人种,3 西班牙，4 拉丁美洲,5 黑色人种 ,6 其他
    mother_img	S3ImageInfo 母亲图片s3信息,参考S3ImageInfo类

    非空, key格式: image/baby/report/日期yyyyMMdd/{did}/mother/时间戳+随机数.jpg

    did:用户设备id

    mother_face_rectangle	FaceRectangle	母亲图片人脸位置	非空
    father_img	S3ImageInfo	父亲图片s3信息,参考S3ImageInfo类
    非空, key格式: image/baby/report/日期yyyyMMdd/{did}/father/时间戳+随机数.jpg

    did:用户设备id

    father_face_rectangle	FaceRectangle	父亲图片人脸位置	非空
    time_limit	boolean	服务器是否限制调用次数	默认true, 每人每天限制10次调用
     */
    companion object {
        const val REQUEST_PATH = "/api/v1/baby/report/generate"
    }

    @SerializedName(value = "gender")
    var gender: String? = null

    @SerializedName(value = "ethnicity")
    var ethnicity: Int = 0

    @SerializedName(value = "mother_img")
    var motherImg: S3ImageInfo? = null

    @SerializedName(value = "mother_face_rectangle")
    var motherFaceRectangle: FaceRectangle? = null

    @SerializedName(value = "father_img")
    var fatherImg: S3ImageInfo? = null

    @SerializedName(value = "father_face_rectangle")
    var fatherFaceRectangle: FaceRectangle? = null

    @SerializedName(value = "time_limit")
    var timeLimit: Boolean = true
}