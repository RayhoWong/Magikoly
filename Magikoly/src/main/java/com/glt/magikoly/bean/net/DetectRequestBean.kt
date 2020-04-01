package com.glt.magikoly.bean.net

import com.glt.magikoly.bean.S3ImageInfo

class DetectRequestBean : BaseRequestBean() {
    companion object {
        const val MAX_RETRY_COUNT = 3
        const val REQUEST_PATH = "/api/v1/face/detect"
    }
    var image: S3ImageInfo? = null

    @Transient
    var retryCount:Int = 0
}