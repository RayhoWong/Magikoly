package com.glt.magikoly.function.feedback.presenter

import android.content.Context
import android.content.pm.PackageManager
import com.android.volley.Request
import com.android.volley.VolleyError
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.encrypt.FaceDesUtils
import com.glt.magikoly.encrypt.FaceDesUtils.FEEDBACK_DES_KEY
import com.glt.magikoly.ext.getAppContext
import com.glt.magikoly.function.feedback.IFeedBack
import com.glt.magikoly.mvp.AbsPresenter
import com.glt.magikoly.net.RequestCallback
import com.glt.magikoly.net.StringRequest
import com.glt.magikoly.utils.Machine
import java.util.*

class FeedbackPresenter : AbsPresenter<IFeedBack>() {

    companion object {
        const val PID = "233"
        const val ENCRIPY_KEY = "K8N9X68T"
        const val URL = "http://fb.magikoly.com/userfeedback/interface/clientfeedbackdes.jsp"
    }

    private var mIsUpLoad = false
    fun doSendFeedback(detail: String, email: String) {
        if (mIsUpLoad) {
            return
        }
        try {
            // 联系方式
//            var contact = DesUtils.encrypt(email, FEEDBACK_DES_KEY)
            var contact = FaceDesUtils.encryptToBase64URLSafeString(email, FEEDBACK_DES_KEY)
            // 1 问题反馈 2 功能建议 3 操作疑问 4 其他，get(必填) 请查看 分类数据信息单元
            val type = "1"
            sendHttpFeedback(detail, contact, type, URL)
            mIsUpLoad = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendHttpFeedback(detail: String, contact: String, type: String, urls: String) {
        var builder = StringRequest.Builder().url(URL)
        builder.method(Request.Method.POST)
        val mParamMap = HashMap<String, String>()
        mParamMap["pid"] = PID
        mParamMap["contact"] = contact
        var packageInfo = FaceAppState.getContext().packageManager.getPackageInfo(
            getAppContext().packageName,
            PackageManager.GET_CONFIGURATIONS
        )
        mParamMap["versionname"] = packageInfo.versionName
        mParamMap["versioncode"] = packageInfo.versionCode.toString()
        mParamMap["type"] = type
        mParamMap["adatas"] = urls
        mParamMap["detail"] = detail
        try {
//            mParamMap["devinfo"] = DesUtils.encrypt(getDeviceInfo(FaceAppState.getContext()), FEEDBACK_DES_KEY)
            mParamMap["devinfo"] = FaceDesUtils.encryptToBase64URLSafeString(getDeviceInfo(
                    FaceAppState.getContext()), FEEDBACK_DES_KEY)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // TODO 常见问题
        builder.callback(object : RequestCallback<String> {
            override fun onErrorResponse(error: VolleyError?) {
                view?.onFeedbackStatus(false)
                mIsUpLoad = false
            }

            override fun onResponse(response: String?) {
                val result = response?.trim()
                if ("1" == result) {
                    view?.onFeedbackStatus(true)
                } else {
                    view?.onFeedbackStatus(false)
                }
                mIsUpLoad = false
            }

        })
        builder.params(mParamMap)
        var request = builder.addHeader("Content-Type", "application/x-www-form-urlencoded").build()
        request.execute()
    }

    private fun getDeviceInfo(context: Context): String {
        val body = StringBuffer()
        body.append("\nProduct=" + android.os.Build.PRODUCT)
        body.append("\nPhoneModel=" + android.os.Build.MODEL)
        body.append("\nROM=" + android.os.Build.DISPLAY)
        body.append("\nBoard=" + android.os.Build.BOARD)
        body.append("\nDevice=" + android.os.Build.DEVICE)
        body.append("\nDensity=" + context.resources.displayMetrics.density.toString())
        body.append("\nPackageName=" + context.packageName)
        body.append("\nAndroidVersion=" + android.os.Build.VERSION.RELEASE)
        body.append("\nTotalMemSize=" + Machine.getTotalInternalMemorySize() / 1024 / 1024 + "MB")
        body.append("\nFreeMemSize=" + Machine.getAvailableInternalMemorySize() / 1024 / 1024 + "MB")
        body.append(
            "\nRom App Heap Size=" + Integer.toString((Runtime.getRuntime().maxMemory() / 1024L / 1024L).toInt()) + "MB"
        )
        body.append("\nCountry=" + Machine.getlocal(context).toLowerCase())
        return body.toString()
    }
}