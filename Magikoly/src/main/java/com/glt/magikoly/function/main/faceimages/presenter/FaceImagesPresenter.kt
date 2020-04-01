package com.glt.magikoly.function.main.faceimages.presenter

import android.content.Context
import android.graphics.Bitmap
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.ext.getAppContext
import com.glt.magikoly.function.*
import com.glt.magikoly.function.facesdk.FaceSdkProxy
import com.glt.magikoly.function.main.faceimages.IFaceImagesView
import com.glt.magikoly.function.main.multiface.MultiFaceFragment
import com.glt.magikoly.mvp.AbsPresenter
import com.glt.magikoly.permission.OnPermissionResult
import com.glt.magikoly.permission.PermissionHelper
import com.glt.magikoly.pref.PrefConst
import com.glt.magikoly.pref.PrivatePreference
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.statistic.Statistic103Constant.ENTRANCE_MAIN
import com.google.firebase.ml.vision.face.FirebaseVisionFace


class FaceImagesPresenter : AbsPresenter<IFaceImagesView>(), IScanListener {

    override fun attachView(view: IFaceImagesView?) {
        super.attachView(view)
        ImageScanner.registerScanListener(this)
    }

    override fun detachView() {
        super.detachView()
        ImageScanner.unregisterScanListener(this)
    }

    override fun onFaceImagesLoadStart(faceImageList: ArrayList<FaceImageInfo>) {
        view?.onLoadDataStart(faceImageList)
    }

    override fun onFaceImageFound(position: Int, faceImageInfo: FaceImageInfo, isIncrement: Boolean) {
        view?.addFaceImage(position, faceImageInfo)
        if (!isIncrement) {
            view?.hideProgressBar()
        }
    }

    override fun onFaceImageRemove(position: Int) {
        view?.removeFaceImage(position)
    }

    override fun onAllFaceImagesLoadFinish(faceImageList: ArrayList<FaceImageInfo>,
                                           forceRefresh: Boolean) {
        if (forceRefresh) {
            view?.refresh(faceImageList)
        }
        view?.hideProgressBar()
        view?.onLoadDataFinish()
    }

    fun loadFaceImages(reload: Boolean) {
        FaceAppState.getMainActivity()?.apply {
            if (PermissionHelper.hasReadStoragePermission(this)) {
                if (reload) {
                    view?.hideEmptyView()
                    if (hasNoFaceImage(ImageScanner.faceImageList)) {
                        view?.showProgressBar()
                    }
                    ImageScanner.startScanFaceImages(FaceAppState.getContext(), true)
                } else {
                    if (ImageScanner.isFaceImagesScanFinish) {
                        view?.refresh(ImageScanner.faceImageList)
                    } else if (!ImageScanner.isFaceImagesScanStarted) {
                        view?.hideEmptyView()
                        if (hasNoFaceImage(ImageScanner.faceImageList)) {
                            view?.showProgressBar()
                        }
                        ImageScanner.startScanFaceImages(FaceAppState.getContext(), false)
                    } else {
                        view?.hideEmptyView()
                        if (hasNoFaceImage(ImageScanner.faceImageList)) {
                            view?.showProgressBar()
                        }
                        view?.onLoadDataStart(ImageScanner.faceImageList)
                    }
                }
            } else {
                view?.refresh(ImageScanner.faceImageList)
            }
        }
    }

    fun loadIncrementFaceImages() {
        FaceAppState.getMainActivity()?.apply {
            if (PermissionHelper.hasReadStoragePermission(this)) {
                ImageScanner.startScanIncrementFaceImages(this)
            }
        }
    }

    fun handlePermission():Boolean {
        val privatePreference = PrivatePreference.getPreference(getAppContext())
        if (privatePreference.getBoolean(PrefConst.KEY_FIRST_ENTER_VIEW, true)) {
            FaceAppState.getMainActivity()?.run {
                PermissionHelper.requestWritePermission(this, object : OnPermissionResult {
                    override fun onPermissionDeny(permission: String?, never: Boolean) {
                        view?.refresh(ImageScanner.faceImageList)
                    }

                    override fun onPermissionGrant(permission: String?) {
                        loadFaceImages(false)
                        BaseSeq103OperationStatistic.uploadSqe103StatisticData(Statistic103Constant.PHOTO_PERMISSION_OBTAINED,
                                Statistic103Constant.ENTRANCE_PHOTO_FIRST_ENTER)
                    }
                }, -1)
                BaseSeq103OperationStatistic.uploadSqe103StatisticData(Statistic103Constant.PHOTO_PERMISSION_REQUEST,
                        Statistic103Constant.ENTRANCE_PHOTO_FIRST_ENTER)
            }
            privatePreference.putBoolean(PrefConst.KEY_FIRST_ENTER_VIEW, false)
            privatePreference.commit()
            return true
        }
        return false
    }

    fun getFaceImageList(): ArrayList<FaceImageInfo> {
        return ImageScanner.faceImageList
    }

    fun isLoadStart() = ImageScanner.isFaceImagesScanStarted

    fun isLoadFinish() = ImageScanner.isFaceImagesScanFinish

    fun hasNoFaceImage(
            faceImageList: ArrayList<FaceImageInfo>?) = faceImageList == null || faceImageList.size <= ImageScanner.demoListSize

    fun detectFaces(context: Context, faceImageInfo: FaceImageInfo) {
        FaceFunctionManager.detectFace(context, faceImageInfo.imagePath,
                object : FaceSdkProxy.OnDetectResult {
                    override fun onDetectMultiFaces(originalPath: String, originBitmap: Bitmap,
                                                    faces: List<FirebaseVisionFace>,
                                                    onDetectResult: FaceSdkProxy.OnDetectResult) {
                        FaceAppState.getMainActivity()
                                ?.start(MultiFaceFragment.newInstance(ENTRANCE_MAIN, originalPath,
                                        originBitmap, faces, onDetectResult))
                    }

                    override fun onDetectSuccess(originalPath: String,
                                                 faceFunctionBean: FaceFunctionBean) {
                        view?.onFaceDetectSuccess(originalPath, faceFunctionBean)
                    }

                    override fun onDetectFail(originalPath: String, errorCode: Int) {
                        view?.onFaceDetectFail(originalPath, errorCode, faceImageInfo)
                    }
                })
    }
}