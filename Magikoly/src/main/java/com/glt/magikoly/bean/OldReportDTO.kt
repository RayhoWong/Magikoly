package com.glt.magikoly.bean

import com.google.gson.annotations.SerializedName

class OldReportDTO {
    @SerializedName("old_image_url")
    var oldImageUrl: String? = null
    @SerializedName("author_image_url")
    var authorImageUrl: String? = null
    var ethnicity: Int = 0
    var age: Int = 0
    @SerializedName("original_age")
    var originalAge: Int? = 0
    var gender: String? = null
    @SerializedName("author_s3_image_info")
    var authorS3ImageInfo: S3ImageInfo? = null
    @SerializedName("author_face_rectangle")
    var authorFaceRectangle: FaceRectangle? = null
}