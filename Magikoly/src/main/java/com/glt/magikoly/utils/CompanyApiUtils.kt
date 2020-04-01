package com.glt.magikoly.utils

import android.content.Context
import android.text.TextUtils
import com.glt.magikoly.FaceEnv
import com.glt.magikoly.encrypt.FaceDesUtils.VISION_DES_KEY
import com.glt.magikoly.encrypt.HmacUtils
import com.gomo.commons.security.Base64
import com.gomo.commons.security.DESUtils
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

/**
 * Created by yangjiacheng on 2018/4/10.
 * ...
 */
object CompanyApiUtils {

    //Face接口
    const val FACE_PRIVATE_SECRET_KEY = "539A228B0C1F1BC1"
    const val X_SIGNATURE = "X-Signature"


    //Vision接口
    const val VISION_API_KEY = "PfjuEIOoInZZXvyVVakrSinMe"
    const val VISION_PRIVATE_SECRET_KEY = "mfzOmzczDZZOyaQthAePrvdwvRRxFjiF"
    const val X_ENCRYPT_DEVICE = "X-Encrypt-Device"
    const val AUTHORIZATION = "Authorization"

    //    public static String obtainBase64Header(JSONObject jsonObject) {
    //        try {
    //            return Base64.encode(jsonObject.toString().getBytes("utf-8"));
    //        } catch (UnsupportedEncodingException e) {
    //            e.printStackTrace();
    //        }
    //        return "";
    //    }

    fun obtainRequestUrl(hostName: String, requestUri: String, queryString: String): String {
        return if (TextUtils.isEmpty(queryString)) {
            hostName + requestUri
        } else "$hostName$requestUri?$queryString"
    }


    //获取 Signature header
    fun obtainSignature(signatureKey: String, method: String, requestUrl: String,
                        queryString: String, payload: String): String {
        val valueToDigest = StringBuilder()
        valueToDigest.append(method).append("\n").append(requestUrl).append("\n")
                .append(queryString).append("\n").append(payload)
        return createSignature(signatureKey, valueToDigest.toString())
    }

    private fun createSignature(signatureKey: String, valueToDigest: String): String {
        val digest = HmacUtils.hmacSha256(signatureKey, valueToDigest)
        return Base64.encodeBase64URLSafeString(digest)
    }

    fun visionDeviceBase64(context: Context): String? {
        return try {
            val deviceJson = visionDevice(context)
            if (deviceJson == null)
                null
            else
//                DesUtils.encrypt(deviceJson.toString(), DesUtils.VISION_DES_KEY)
                DESUtils.encryptToBase64URLSafeString(deviceJson.toString(), VISION_DES_KEY)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun visionDevice(context: Context): JSONObject? {
        return try {
            JSONObject().apply {
                put("dtype", "1")   //安卓为1，iOS为2
                put("did", Machine.getAndroidId(context))
                put("country", Machine.getCountry(context))
                put("channel", FaceEnv.sChannelId)
                put("app_version_number",
                        AppUtils.getVersionCodeByPkgName(context, context.packageName))   //版本号
                put("system_version_name", Machine.getAndroidSDKVersion())
                put("lang", Machine.getLanguage(context))
            }
        } catch (e: JSONException) {
            null
        }
    }
}
