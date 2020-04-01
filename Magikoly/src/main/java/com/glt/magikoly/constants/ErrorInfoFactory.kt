package com.glt.magikoly.constants

import com.glt.magikoly.constants.ErrorCode.*
import magikoly.magiccamera.R

object ErrorInfoFactory {

    fun getErrorInfo(errorCode: Int?): ErrorInfo {
        val errorInfo = ErrorInfo()
        when (errorCode) {
            FACE_DETECT_ERROR -> {
                errorInfo.imageId = R.drawable.img_dialogs_failed_recognize
                errorInfo.titleId = R.string.face_detect_fail
                errorInfo.descId = R.string.face_detect_fail_tips
            }
            BAD_FACE -> {
                errorInfo.imageId = R.drawable.img_dialogs_failed_recognize
                errorInfo.titleId = R.string.unclear_face
                errorInfo.descId = R.string.face_detect_fail_tips
            }
            NETWORK_ERROR -> {
                errorInfo.imageId = R.drawable.img_dialogs_network_error
                errorInfo.titleId = R.string.network_error
                errorInfo.descId = R.string.network_error_tips
            }

            THIRD_PART_PROVIDER_UNAVAILABLE -> {
                errorInfo.imageId = R.drawable.img_dialogs_unknownerror
                errorInfo.titleId = R.string.identification_error
                errorInfo.descId = R.string.error_code_1
            }

            TEMPLATE_NOT_FOUND -> {
                errorInfo.imageId = R.drawable.img_dialogs_unknownerror
                errorInfo.titleId = R.string.identification_error
                errorInfo.descId = R.string.error_code_2
            }

            IMAGE_LOAD_FAIL -> {
                errorInfo.imageId = R.drawable.img_dialogs_unknownerror
                errorInfo.titleId = R.string.identification_error
                errorInfo.descId = R.string.error_code_3
            }

            FACE_NOT_FOUND, VISION_FACE_NOT_FOUND, VISION_FACE_POINTS_OUTBOUNDS -> {
                errorInfo.imageId = R.drawable.img_dialogs_unknownerror
                errorInfo.titleId = R.string.face_detect_fail
                errorInfo.descId = R.string.face_detect_fail_tips
            }

            IMAGE_NOT_FOUND -> {
                errorInfo.imageId = R.drawable.img_dialogs_unknownerror
                errorInfo.titleId = R.string.image_not_found_error
                errorInfo.descId = R.string.image_not_found_error_tips
            }

            FATHER_GENDER_FAIL  -> {
                errorInfo.imageId = R.drawable.img_dialogs_unknownerror
                errorInfo.titleId = R.string.baby_father_gender_fail
                errorInfo.descId = R.string.baby_father_gender_fail_desc
            }
            MOTHER_GENDER_FAIL -> {
                errorInfo.imageId = R.drawable.img_dialogs_unknownerror
                errorInfo.titleId = R.string.baby_mother_gender_fail
                errorInfo.descId = R.string.baby_mother_gender_fail_desc
            }
            VISION_BASE_64_DECODE_ERROR, VISION_REQUEST_FACE_POINTS_ERROR -> {
                errorInfo.imageId = R.drawable.img_dialogs_unknownerror
                errorInfo.titleId = R.string.identification_error
                errorInfo.descId = R.string.face_detect_fail_tips
            }
            VISION_SERVER_BUSY -> {
                errorInfo.imageId = R.drawable.img_dialogs_network_error
                errorInfo.titleId = R.string.server_busy
                errorInfo.descId = R.string.server_busy_tips
            }
            else -> {
                errorInfo.imageId = R.drawable.img_dialogs_unknownerror
                errorInfo.titleId = R.string.unknown_error
                errorInfo.descId = R.string.unknown_error_tips
            }
        }
        return errorInfo
    }
}

class ErrorInfo {
    var imageId = 0
    var titleId = 0
    var descId = 0
}