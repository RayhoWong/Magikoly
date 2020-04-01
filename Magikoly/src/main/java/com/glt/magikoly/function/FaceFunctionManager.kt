package com.glt.magikoly.function

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Base64
import com.android.volley.VolleyError
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.glt.magikoly.amazons3.AmazonS3Manger
import com.glt.magikoly.amazons3.KeyCreator
import com.glt.magikoly.amazons3.UploadImageInfo
import com.glt.magikoly.bean.FaceInfo
import com.glt.magikoly.bean.FaceRectangle
import com.glt.magikoly.bean.S3ImageInfo
import com.glt.magikoly.bean.net.*
import com.glt.magikoly.constants.ErrorCode
import com.glt.magikoly.constants.ErrorCode.NETWORK_ERROR
import com.glt.magikoly.constants.ErrorCode.NETWORK_ERROR_STR
import com.glt.magikoly.ext.registerEventObserver
import com.glt.magikoly.ext.runAsync
import com.glt.magikoly.ext.runMain
import com.glt.magikoly.function.facesdk.FaceSdkProxy
import com.glt.magikoly.net.NetManager
import com.glt.magikoly.net.RequestCallback
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.utils.BitmapUtils
import com.glt.magikoly.view.ProgressBarEvent
import com.glt.magikoly.view.ProgressBarEvent.Companion.EVENT_CANCEL_BY_USER
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import kotlin.math.roundToLong

object FaceFunctionManager {

    const val KEY_DEMO_FACE_IMAGE_INFO = "key_demo_face_image_info"
    const val KEY_CURRENT_IMG_PATH = "key_current_img_path"
    const val KEY_FACE_BEAN_MAP_KEYS = "key_face_bean_map_keys"

    var demoFaceImageInfo:DemoFaceImageInfo? = null
    var faceBeanMap: HashMap<String, FaceFunctionBean> = HashMap()
    var currentFaceImagePath:String? = null

    private var detectImagePath:String? = null

    init {
        registerEventObserver(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProgressBarEvent(event: ProgressBarEvent) {
        if (event.action == EVENT_CANCEL_BY_USER) {
            detectImagePath = null
        }
    }

    fun detectFace(context: Context, imagePath: String, onDetectResult: FaceSdkProxy.OnDetectResult) {
        this.detectImagePath = imagePath
        Glide.with(context).asBitmap().load(imagePath).into(object : SimpleTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                detectFaceAsync(imagePath, resource, onDetectResult)
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                super.onLoadFailed(errorDrawable)
                onDetectResult.onDetectFail(imagePath, ErrorCode.IMAGE_LOAD_FAIL)
            }
        })
    }

    fun detectFace(imagePath: String, faceBitmap: Bitmap, onDetectResult: FaceSdkProxy.OnDetectResult) {
        this.detectImagePath = imagePath
        detectFaceAsync(imagePath, faceBitmap, onDetectResult)
    }

    private fun detectFaceAsync(imagePath: String, faceBitmap: Bitmap, onDetectResult: FaceSdkProxy.OnDetectResult) {
        runAsync {
            FaceSdkProxy.detectForFaceCrop(imagePath, faceBitmap,
                    object : FaceSdkProxy.OnDetectResult {
                        override fun onDetectMultiFaces(originalPath: String, originBitmap: Bitmap,
                                                        faces: List<FirebaseVisionFace>,
                                                        result: FaceSdkProxy.OnDetectResult) {
                            if (detectImagePath == originalPath) {
                                runMain {
                                    onDetectResult.onDetectMultiFaces(originalPath, originBitmap,
                                            faces, result)
                                }
                            }
                        }

                        override fun onDetectSuccess(originalPath: String,
                                                     faceFunctionBean: FaceFunctionBean) {
                            if (detectImagePath == originalPath) {
                                runMain {
                                    onDetectResult.onDetectSuccess(originalPath,
                                            faceFunctionBean)
                                }
                            }
                        }

                        override fun onDetectFail(originalPath: String, errorCode: Int) {
                            if (detectImagePath == originalPath) {
                                runMain {
                                    onDetectResult.onDetectFail(originalPath, errorCode)
                                }
                            }
                        }
                    })
        }
    }

    fun detectFaceBy(imagePath: String, faceBitmap: Bitmap, onDetectResult: FaceSdkProxy.OnDetectResult) {
        runAsync {
            FaceSdkProxy.detectForFaceCrop(imagePath, faceBitmap,
                    object : FaceSdkProxy.OnDetectResult {
                        override fun onDetectMultiFaces(originalPath: String, originBitmap: Bitmap,
                                                        faces: List<FirebaseVisionFace>,
                                                        result: FaceSdkProxy.OnDetectResult) {
                            if (imagePath == originalPath) {
                                runMain {
                                    onDetectResult.onDetectMultiFaces(originalPath, originBitmap,
                                            faces, result)
                                }
                            }
                        }

                        override fun onDetectSuccess(originalPath: String,
                                                     faceFunctionBean: FaceFunctionBean) {
                            if (imagePath == originalPath) {
                                runMain {
                                    onDetectResult.onDetectSuccess(originalPath,
                                            faceFunctionBean)
                                }
                            }
                        }

                        override fun onDetectFail(originalPath: String, errorCode: Int) {
                            if (imagePath == originalPath) {
                                runMain {
                                    onDetectResult.onDetectFail(originalPath, errorCode)
                                }
                            }
                        }
                    })
        }
    }


    /**
     * 人脸识别接口
     * type: KeyCreator下的key类型
     */
    fun detectFace(type: Int, imageFile: File, width: Int, height: Int, listener: IFaceDetectListener) {
        val uploadImageInfo = UploadImageInfo()
        uploadImageInfo.type = type
        uploadImageInfo.file = imageFile
        uploadImageInfo.width = width
        uploadImageInfo.height = height

        val entrance = when (type) {
            KeyCreator.TYPE_OLD_REPORT -> Statistic103Constant.ENTRANCE_AGING
            KeyCreator.TYPE_ETHNICITY_REPORT -> Statistic103Constant.ENTRANCE_ETHNICITY
            KeyCreator.TYPE_GENDER_REPORT -> Statistic103Constant.ENTRANCE_GENDER
            else -> ""
        }
        AmazonS3Manger.uploadImage(uploadImageInfo, object : AmazonS3Manger.UploadListener {
            override fun onUploadCompleted(imageInfo: S3ImageInfo, imageUrl: String) {
                val requestBean = DetectRequestBean()
                requestBean.image = imageInfo
                val startTime = System.currentTimeMillis()
                NetManager.performDetect(requestBean, object : RequestCallback<DetectResponseBean> {
                    override fun onResponse(response: DetectResponseBean?) {
                        if (response != null) {
                            when (response.statusResult?.statusCode) {
                                "SUCCESS" -> {
                                    BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                                            ((System.currentTimeMillis() - startTime) / 1000f).roundToLong().toString(),
                                            Statistic103Constant.FACE_DETECT, "", "1", "")
                                    listener.onDetectSuccess(imageInfo, response)
                                }
                                ErrorCode.THIRD_PART_PROVIDER_UNAVAILABLE_STR -> {
                                    checkAndRetryFaceDetect(requestBean, imageInfo, listener,
                                            response.statusResult?.statusCode!!, entrance,
                                            startTime)
                                }
                                else -> {
                                    BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                                            ((System.currentTimeMillis() - startTime) / 1000f).roundToLong().toString(),
                                            Statistic103Constant.FACE_DETECT, "", "2",
                                            response.statusResult?.statusCode!!)
                                    listener.onDetectFail(response.statusResult?.statusCode!!)
                                }
                            }
                        } else {
                            checkAndRetryFaceDetect(requestBean, imageInfo, listener,
                                    getNetErrorStatisticsValue(null), entrance, startTime)
                        }
                    }

                    override fun onErrorResponse(error: VolleyError?) {
                        error?.printStackTrace()
                        checkAndRetryFaceDetect(requestBean, imageInfo, listener,
                                getNetErrorStatisticsValue(error?.networkResponse?.statusCode.toString()),
                                entrance, startTime)
                    }
                })
            }

            override fun onUploadProgress(percent: Int) {
            }

            override fun onUploadError(errorCode: Int) {
                listener.onDetectFail(getNetErrorStatisticsValue(errorCode.toString()))
            }
        })
    }

    private fun checkAndRetryFaceDetect(requestBean: DetectRequestBean, imageInfo: S3ImageInfo,
        listener: IFaceDetectListener, errorCode: String, entrance: String, startTime: Long) {
        if (requestBean.retryCount < DetectRequestBean.MAX_RETRY_COUNT) {
            requestBean.retryCount++
            runMain(500) {
                NetManager.performDetect(requestBean, object :
                        RequestCallback<DetectResponseBean> {
                    override fun onResponse(response: DetectResponseBean?) {
                        if (response != null) {
                            when (response.statusResult?.statusCode) {
                                "SUCCESS" -> {
                                    BaseSeq103OperationStatistic.uploadSqe103StatisticData(Math.round(
                                            (System.currentTimeMillis() - startTime) / 1000f).toString(),
                                            Statistic103Constant.FACE_DETECT, "", "1", "")
                                    listener.onDetectSuccess(imageInfo, response)
                                }
                                ErrorCode.THIRD_PART_PROVIDER_UNAVAILABLE_STR -> {
                                    checkAndRetryFaceDetect(requestBean, imageInfo, listener,
                                            response.statusResult?.statusCode!!, entrance,
                                            startTime)
                                }
                                else -> {
                                    BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                                            ((System.currentTimeMillis() - startTime) / 1000f).roundToLong().toString(),
                                            Statistic103Constant.FACE_DETECT, "", "2",
                                            response.statusResult?.statusCode!!)
                                    listener.onDetectFail(response.statusResult?.statusCode!!)
                                }
                            }
                        } else {
                            checkAndRetryFaceDetect(requestBean, imageInfo, listener,
                                    getNetErrorStatisticsValue(null), entrance, startTime)
                        }
                    }

                    override fun onErrorResponse(error: VolleyError?) {
                        error?.printStackTrace()
                        checkAndRetryFaceDetect(requestBean, imageInfo, listener,
                                getNetErrorStatisticsValue(
                                        error?.networkResponse?.statusCode.toString()), entrance,
                                startTime)
                    }
                })
            }
        } else {
            BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                    ((System.currentTimeMillis() - startTime) / 1000f).roundToLong().toString(),
                    Statistic103Constant.FACE_DETECT, "", "2", errorCode)
            listener.onDetectFail(errorCode)
        }
    }

    fun generateAgingReport(age: Int, faceInfo: FaceInfo, imageInfo: S3ImageInfo,
                            listener: IAgingReportListener) {
        val request = AgingRequestBean()
        request.age = age
        request.image = imageInfo
        request.ethnicity = faceInfo.ethnicity
        request.gender = faceInfo.gender
        request.originalAge = faceInfo.age
        request.faceRectangle = FaceRectangle()
        request.faceRectangle?.left = faceInfo.left
        request.faceRectangle?.top = faceInfo.top
        request.faceRectangle?.width = faceInfo.width
        request.faceRectangle?.height = faceInfo.height
        NetManager.performAgingShutter(request, object : RequestCallback<AgingResponseBean> {
            override fun onResponse(response: AgingResponseBean?) {
                if (response?.oldReport != null && response.oldReport!!.oldImageUrl != null) {
                    listener.onAgingReportGenerateSuccess(response.oldReport!!.oldImageUrl!!)
                } else {
                    listener.onAgingReportGenerateFail(response?.statusResult?.statusCode!!)
                }
            }

            override fun onErrorResponse(error: VolleyError?) {
                listener.onAgingReportGenerateFail(
                        getNetErrorStatisticsValue(error?.networkResponse?.statusCode.toString()))
            }
        })
    }

    fun generateEthnicityReport(gender: String?, imageInfo: S3ImageInfo, callback: IEthnicityReportListener) {
        var requestBean = EthnicityRequestBean()
        requestBean.gender = gender
        requestBean.image = imageInfo

        NetManager.performEthnicityRequest(requestBean, object : RequestCallback<EthnicityResponseBean> {
            override fun onErrorResponse(error: VolleyError?) {
                callback.onEthnicityRequestFailed(
                        getNetErrorStatisticsValue(error?.networkResponse?.statusCode.toString()))
            }

            override fun onResponse(response: EthnicityResponseBean?) {
                if (response?.ethnicityReport != null) {
                    callback.onEthnicityResponse(response.ethnicityReport)
                } else {
                    callback.onEthnicityRequestFailed(response?.statusResult?.statusCode!!)
                }
            }

        })
    }

    fun generateGenderReport(gender: String?, ethnicity: Int, faceRect: FaceRectangle, imageInfo: S3ImageInfo,
        limit: Boolean, callback: IGenderReportListener) {
        val requestBean = GenderRequestBean()
        requestBean.gender = gender
        requestBean.faceRectangle = faceRect
        requestBean.image = imageInfo
        requestBean.ethnicity = ethnicity
        requestBean.timeLimit = limit
        NetManager.performGenderRequest(requestBean, object : RequestCallback<GenderResponseBean> {
            override fun onErrorResponse(error: VolleyError?) {
                callback.onGenderRequestFailed(
                        getNetErrorStatisticsValue(error?.networkResponse?.statusCode.toString()))
            }

            override fun onResponse(response: GenderResponseBean?) {
                if (response?.genderReport != null) {
                    callback.onGenderResponse(response.genderReport)
                } else {
                    callback.onGenderRequestFailed(response?.statusResult?.statusCode!!)
                }
            }

        })
    }

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
    fun babyPrediction(gender: String,
                       ethnicity: Int,
                       motherInfo: S3ImageInfo,
                       motherFaceRect: FaceRectangle,
                       father: S3ImageInfo,
                       fatherFaceRect: FaceRectangle,
                       limit: Boolean,
                       callback: RequestCallback<BabyResponseBean>) {
        var babyRequestBean = BabyRequestBean()
        babyRequestBean.gender = gender
        babyRequestBean.ethnicity = ethnicity
        babyRequestBean.motherImg = motherInfo
        babyRequestBean.motherFaceRectangle = motherFaceRect
        babyRequestBean.fatherImg = father
        babyRequestBean.fatherFaceRectangle = fatherFaceRect
        babyRequestBean.timeLimit = limit
        NetManager.performBabyPrediction(babyRequestBean, callback)
    }

    fun generateChildReport(src:Bitmap, callback:IChildReportListener){
        val requestBean = VisionChildRequestBean()
        requestBean.imgBase64 = Base64.encodeToString(BitmapUtils.toByteArray(src), Base64.NO_WRAP)
        NetManager.performVisionChildRequest(requestBean, object :
                RequestCallback<VisionChildResponseBean> {
            override fun onResponse(response: VisionChildResponseBean?) {
                if (response != null) {
                    if (response.errcode == 0 && response.data != null) {
                        val bytes = Base64.decode(response.data, Base64.NO_WRAP)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        callback.onChildReportGenerateSuccess(bitmap)
                    } else {
                        callback.onChildReportGenerateFail(response.errcode.toString())
                    }
                } else {
                    callback.onChildReportGenerateFail(getNetErrorStatisticsValue(null))
                }
            }

            override fun onErrorResponse(error: VolleyError?) {
                error?.printStackTrace()
                callback.onChildReportGenerateFail(
                        getNetErrorStatisticsValue(error?.networkResponse?.statusCode.toString()))
            }
        })
    }

    fun convertErrorString(statusCode: String?): Int {
        return when (statusCode) {
            ErrorCode.THIRD_PART_PROVIDER_UNAVAILABLE_STR -> ErrorCode.THIRD_PART_PROVIDER_UNAVAILABLE
            ErrorCode.FACE_NOT_FOUND_STR -> ErrorCode.FACE_NOT_FOUND
            ErrorCode.BAD_FACE_STR -> ErrorCode.BAD_FACE
            ErrorCode.TEMPLATE_NOT_FOUND_STR -> ErrorCode.TEMPLATE_NOT_FOUND
            else -> {
                NETWORK_ERROR
            }
        }
    }

    /**
     * 请求获取热词数据
     */
    fun requestHotword(callback: RequestCallback<HotwordResponseBean>) {
        NetManager.performHotwordRequest(HotwordRequestBean(), callback)
    }

    private fun getNetErrorStatisticsValue(statusCode: String?):String {
        return "${NETWORK_ERROR_STR}_${statusCode?:"UNKNOWN"}"
    }

    /**
     * 搜索图片请求
     */
    fun requestSearchImage(content: String, mode: Int,page:Int, callback: RequestCallback<SearchImageResponseBean>) {
        val searchImageRequestBean = SearchImageRequestBean()
        searchImageRequestBean.content = content
        searchImageRequestBean.mode = mode
        searchImageRequestBean.page=page
        NetManager.performSearchImageRequest(searchImageRequestBean, callback)
    }

    open interface IFaceDetectListener {
        fun onDetectSuccess(imageInfo: S3ImageInfo, detectBean: DetectResponseBean)
        fun onDetectFail(errorCode: String)
    }

    open interface IAgingReportListener {
        fun onAgingReportGenerateSuccess(imageUrl: String)
        fun onAgingReportGenerateFail(errorCode: String)
    }

    open interface IEthnicityReportListener {
        fun onEthnicityResponse(response: EthnicityReportDTO?)
        fun onEthnicityRequestFailed(errorCode: String)
    }

    open interface IGenderReportListener {
        fun onGenderResponse(response: GenderReportDTO?)
        fun onGenderRequestFailed(errorCode: String)
    }

    open interface IVisionDetectListener {
        fun onDetectSuccess()
        fun onDetectFail(errorCode: Int)
    }

    open interface IChildReportListener {
        fun onChildReportGenerateSuccess(result:Bitmap)
        fun onChildReportGenerateFail(errorCode: String)
    }
}