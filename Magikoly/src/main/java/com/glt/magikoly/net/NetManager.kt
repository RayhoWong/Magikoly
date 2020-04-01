package com.glt.magikoly.net

import com.android.volley.Request.Method.POST
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.bean.Device
import com.glt.magikoly.bean.net.*
import com.glt.magikoly.config.AgingShutterConfigBean
import com.glt.magikoly.encrypt.FaceDesUtils
import com.glt.magikoly.encrypt.FaceDesUtils.FACE_DES_KEY
import com.glt.magikoly.encrypt.FaceDesUtils.VISION_DES_KEY
import com.glt.magikoly.utils.AppUtils
import com.glt.magikoly.utils.CompanyApiUtils
import com.glt.magikoly.utils.CompanyApiUtils.AUTHORIZATION
import com.glt.magikoly.utils.CompanyApiUtils.FACE_PRIVATE_SECRET_KEY
import com.glt.magikoly.utils.CompanyApiUtils.VISION_API_KEY
import com.glt.magikoly.utils.CompanyApiUtils.VISION_PRIVATE_SECRET_KEY
import com.glt.magikoly.utils.CompanyApiUtils.X_ENCRYPT_DEVICE
import com.glt.magikoly.utils.CompanyApiUtils.X_SIGNATURE
import com.glt.magikoly.utils.CompanyApiUtils.obtainRequestUrl
import com.glt.magikoly.utils.Logcat
import com.glt.magikoly.utils.Machine
import com.google.gson.GsonBuilder
import java.util.*

object NetManager {

    private const val FACE_HOST = "http://faccore.magikoly.com" //正式服
//    private const val HOST = "http://faccore.3g.net.cn" //测试服

    private const val VISION_HOST = "http://vision.magikoly.com" //正式服
//    private const val VISION_HOST = "http://svisnhime01.dc.gomo.com:5000" //测试服

    private val device = Device()

    init {
        val context = FaceAppState.getContext()
//        device.pkgname = context.packageName
        device.pkgname = "faceapp.magikoly.exploreandhavefun"
        device.cversion = AppUtils.getVersionCodeByPkgName(context, context.packageName)
        device.country = Machine.getSimCountryIso(context)
        device.zoneId = TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT)
        device.lang = Machine.getLanguageWithLocale(context)
        device.did = Machine.getAndroidId(context)
    }

    fun performDetect(request: DetectRequestBean, callback: RequestCallback<DetectResponseBean>) {
        performFaceRequest(DetectRequestBean.REQUEST_PATH, request, DetectResponseBean::class.java, callback)
    }

    fun performAgingShutter(request: AgingRequestBean,
                            callback: RequestCallback<AgingResponseBean>) {
        val requestUrl = when (AgingShutterConfigBean.getRequestType()) {
            AgingShutterConfigBean.TYPE_FACE -> AgingRequestBean.FACE_REQUEST_PATH
            AgingShutterConfigBean.TYPE_BIG_DATA -> AgingRequestBean.BIG_DATA_REQUEST_PATH
            else -> AgingRequestBean.FACE_REQUEST_PATH
        }
        performFaceRequest(requestUrl, request, AgingResponseBean::class.java, callback)
    }

    fun performEthnicityRequest(request: EthnicityRequestBean, callback: RequestCallback<EthnicityResponseBean>) {
        performFaceRequest(EthnicityRequestBean.REQUEST_URL, request, EthnicityResponseBean::class.java, callback)
    }


    fun performGenderRequest(request: GenderRequestBean, callback: RequestCallback<GenderResponseBean>) {
        performFaceRequest(GenderRequestBean.REQUEST_URL, request, GenderResponseBean::class.java, callback)
    }

    fun performHotwordRequest(request: HotwordRequestBean, callback: RequestCallback<HotwordResponseBean>) {
        performFaceRequest(HotwordRequestBean.REQUEST_URL, request, HotwordResponseBean::class.java, callback)
    }

    fun performSearchImageRequest(request: SearchImageRequestBean, callback: RequestCallback<SearchImageResponseBean>) {
        performFaceRequest(SearchImageRequestBean.REQUEST_URL, request, SearchImageResponseBean::class.java, callback)
    }

    fun performBabyPrediction(request: BabyRequestBean, callback: RequestCallback<BabyResponseBean>) {
        performFaceRequest(BabyRequestBean.REQUEST_PATH, request, BabyResponseBean::class.java, callback)
    }

    fun performVisionDetect(request:VisionDetectRequestBean, callback:RequestCallback<VisionDetectResponseBean>) {
        performVisionRequest(VisionDetectRequestBean.REQUEST_URL, request, VisionDetectResponseBean::class.java, callback)
    }

    fun performVisionChildRequest(request:VisionChildRequestBean, callback:RequestCallback<VisionChildResponseBean>) {
        performVisionRequest(VisionChildRequestBean.REQUEST_URL, request, VisionChildResponseBean::class.java, callback)
    }

    private fun <T : BaseResponseBean> performFaceRequest(requestUri: String,
                                                          requestBean: BaseRequestBean,
                                                          responseBeanClass: Class<T>,
                                                          callback: RequestCallback<T>) {
        requestBean.device = device
        val gson = GsonBuilder().disableHtmlEscaping().create()
        val payload = gson.toJson(requestBean)
        Logcat.i("GsonRequest_payload", "$payload")
        val body = FaceDesUtils.encryptToBase64URLSafeString(payload, FACE_DES_KEY)
        val signature = CompanyApiUtils.obtainSignature(FACE_PRIVATE_SECRET_KEY, "POST", requestUri,
                "", payload)
        val url = obtainRequestUrl(FACE_HOST, requestUri, "")
        GsonPostRequest.Builder<T>()
                .method(POST)
                .url(url)
                .addHeader(X_SIGNATURE, signature)
                .requestBody(body)
                .targetObject(responseBeanClass)
                .decoder { json ->
                    FaceDesUtils.decryptToString(json, FACE_DES_KEY)
                }
                .callback(callback)
                .build().execute()
    }

//    private fun <T : BaseResponseBean> performFaceRequest(
//        requestUri: String,
//        requestBean: BaseRequestBean,
//        responseBeanClass: Class<T>,
//        callback: RequestCallback<T>
//    ) {
//        requestBean.device = device
//        val gson = Gson()
//        val payload = gson.toJson(requestBean)
//        Logcat.i("GsonRequest_payload", "$payload")
//        val signature = CompanyApiUtils.obtainSignature("POST", requestUri, "", payload)
//        GsonPostRequest.Builder<T>()
//                .method(POST)
//                .url(obtainRequestUrl(HOST, requestUri, ""))
//                .addHeader(X_SIGNATURE, signature)
//                .requestBody(DesUtils.encrypt(payload, DesUtils.FACE_DES_KEY))
//                .targetObject(responseBeanClass)
//                .decoder { json ->
//                    DesUtils.decrypt(json, DesUtils.FACE_DES_KEY)
//                }
//                .callback(callback)
//                .build().execute()
//    }

    private fun <T : VisionBaseResponseBean> performVisionRequest(requestUri: String,
                                                                  requestBean: VisionBaseRequestBean,
                                                                  responseBeanClass: Class<T>,
                                                                  callback: RequestCallback<T>) {
        val gson = GsonBuilder().disableHtmlEscaping().create()
        val payload = gson.toJson(requestBean)
        Logcat.i("GsonRequest_payload", "$payload")
        val body = FaceDesUtils.encryptToBase64URLSafeString(payload, VISION_DES_KEY)
        val queryBuilder = StringBuilder()
        queryBuilder.append("api_key=").append(VISION_API_KEY).append("&")
                .append("timestamp=").append(System.currentTimeMillis()).append("&")
                .append("device=")
                .append(CompanyApiUtils.visionDeviceBase64(FaceAppState.getContext()))
        val query = queryBuilder.toString()
        val url = obtainRequestUrl(VISION_HOST, requestUri, query)
        val signature = CompanyApiUtils.obtainSignature(VISION_PRIVATE_SECRET_KEY, "POST",
                requestUri, query, body)
        GsonPostRequest.Builder<T>()
                .method(POST)
                .url(url)
                .addHeader(X_ENCRYPT_DEVICE, "1")
                .addHeader(AUTHORIZATION, signature)
                .requestBody(body)
                .targetObject(responseBeanClass)
                .callback(callback)
                .build().execute()
    }
}