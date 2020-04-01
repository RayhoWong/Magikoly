package com.glt.magikoly.bean

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class LandmarkDTO : Serializable {
    @SerializedName("contour_chin")
    var contourChin: Point? = null
    @SerializedName("contour_left4")
    var contourLeft4: Point? = null
    @SerializedName("contour_right4")
    var contourRight4: Point? = null

    @SerializedName("left_eye_left_corner")
    var leftEyeLeftCorner: Point? = null
    @SerializedName("left_eye_right_corner")
    var leftEyeRightCorner: Point? = null
    @SerializedName("right_eye_left_corner")
    var rightEyeLeftCorner: Point? = null
    @SerializedName("right_eye_right_corner")
    var rightEyeRightCorner: Point? = null

    @SerializedName("left_eyebrow_left_corner")
    var leftEyebrowLeftCorner: Point? = null
    @SerializedName("left_eyebrow_upper_left_quarter")
    var leftEyebrowUpperLeftQuarter: Point? = null
    @SerializedName("left_eyebrow_right_corner")
    var leftEyebrowRightCorner: Point? = null
    @SerializedName("right_eyebrow_left_corner")
    var rightEyebrowLeftCorner: Point? = null
    @SerializedName("right_eyebrow_upper_right_quarter")
    var rightEyebrowUpperRightQuarter: Point? = null
    @SerializedName("right_eyebrow_right_corner")
    var rightEyebrowRightCorner: Point? = null

    @SerializedName("nose_left")
    var noseLeft: Point? = null
    @SerializedName("nose_right")
    var noseRight: Point? = null
    @SerializedName("nose_contour_lower_middle")
    var noseContourLowerMiddle: Point? = null

    @SerializedName("mouth_left_corner")
    var mouthLeftCorner: Point? = null
    @SerializedName("mouth_lower_lip_bottom")
    var mouthLowerLipBottom: Point? = null
    @SerializedName("mouth_right_corner")
    var mouthRightCorner: Point? = null
}