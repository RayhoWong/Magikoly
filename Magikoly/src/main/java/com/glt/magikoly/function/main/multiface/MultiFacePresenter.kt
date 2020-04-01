package com.glt.magikoly.function.main.multiface

import android.graphics.Bitmap
import android.graphics.Rect
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.FaceEnv
import com.glt.magikoly.amazons3.KeyCreator
import com.glt.magikoly.bean.S3ImageInfo
import com.glt.magikoly.bean.net.DetectResponseBean
import com.glt.magikoly.constants.ErrorCode.AMAZON_UPLOAD_FAIL_FILE_NO_EXISTS
import com.glt.magikoly.ext.registerEventObserver
import com.glt.magikoly.ext.unregisterEventObserver
import com.glt.magikoly.function.FaceFunctionBean
import com.glt.magikoly.function.FaceFunctionManager
import com.glt.magikoly.function.facesdk.SharedUtil
import com.glt.magikoly.function.views.MultiFaceImageView
import com.glt.magikoly.mvp.AbsPresenter
import com.glt.magikoly.utils.BitmapUtils
import com.glt.magikoly.view.GlobalProgressBar
import com.glt.magikoly.view.ProgressBarEvent
import com.glt.magikoly.view.ProgressBarEvent.Companion.EVENT_CANCEL_BY_USER
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

class MultiFacePresenter : AbsPresenter<IMultiFace>(), MultiFaceImageView.OnFaceSelectedListener {
    private var mCurrentDetectPath: String? = null
    override fun onSelected(entrance:String, image: Bitmap, selectedRec: Rect) {
        GlobalProgressBar.show(entrance)
        val rectWithHeadNew = SharedUtil.adjustRect(image, selectedRec)
        val thumbnail = BitmapUtils.clipBitmap(image, rectWithHeadNew)
        val absolutePath = FaceEnv.InternalPath.getCacheInnerFilePath(
                FaceAppState.getContext(), FaceEnv.InternalPath.PHOTO_CROP_DIR + System.currentTimeMillis() + ""
        )
        val saveBitmap = BitmapUtils.saveBitmap(thumbnail, absolutePath, Bitmap.CompressFormat.JPEG)
        if (saveBitmap) {
            mCurrentDetectPath = absolutePath
            FaceFunctionManager.detectFace(KeyCreator.TYPE_FACE_DETECT, File(absolutePath), rectWithHeadNew.width(), rectWithHeadNew.height(), object : FaceFunctionManager.IFaceDetectListener {
                override fun onDetectSuccess(imageInfo: S3ImageInfo, detectBean: DetectResponseBean) {
                    if (mCurrentDetectPath == absolutePath) {
                        view?.onDetectSuccess(view.getOriginalPath(), FaceFunctionBean(image, thumbnail, absolutePath, rectWithHeadNew, imageInfo, detectBean.faceInfos!![0]))
                    }

                }

                override fun onDetectFail(errorCode: String) {
                    if (mCurrentDetectPath == absolutePath) {
                        view?.onDetectFail(view.getOriginalPath(),
                                FaceFunctionManager.convertErrorString(errorCode))
                    }
                }
            })

        } else {
            view?.onDetectFail(view.getOriginalPath(), AMAZON_UPLOAD_FAIL_FILE_NO_EXISTS)
        }
    }

    override fun detachView() {
        super.detachView()
        unregisterEventObserver(this)
    }

    override fun attachView(view: IMultiFace?) {
        super.attachView(view)
        registerEventObserver(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProgressBarEvent(event: ProgressBarEvent) {
        if (event.action == EVENT_CANCEL_BY_USER) {
            mCurrentDetectPath = null
        }
    }
}