package com.glt.magikoly.function.takephoto

import android.app.Activity
import android.graphics.Bitmap
import android.support.v4.app.FragmentActivity
import android.view.View
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.dialog.DialogUtils
import com.glt.magikoly.dialog.TipsDialog
import com.glt.magikoly.event.ImageDetectEvent
import com.glt.magikoly.ext.runMain
import com.glt.magikoly.function.FaceFunctionBean
import com.glt.magikoly.function.FaceFunctionManager
import com.glt.magikoly.function.facesdk.FaceSdkProxy
import com.glt.magikoly.function.main.multiface.MultiFaceFragment
import com.glt.magikoly.mvp.AbsPresenter
import com.glt.magikoly.permission.OnPermissionResult
import com.glt.magikoly.permission.PermissionHelper
import com.glt.magikoly.permission.PermissionSettingPage
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.view.GlobalProgressBar
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import magikoly.magiccamera.R
import org.greenrobot.eventbus.EventBus

class TakePhotoPresenter : AbsPresenter<ITakePhoto>() {

    fun onOkCrop(startTag: String, bitmap: Bitmap, path: String) {
        FaceFunctionManager.detectFace(path, bitmap, object : FaceSdkProxy.OnDetectResult {
            override fun onDetectSuccess(originalPath: String, faceFunctionBean: FaceFunctionBean) {
                runMain {
                    faceFunctionBean.category = Statistic103Constant.CATEGORY_CAMERA
                    view?.hideSelf()
                    EventBus.getDefault()
                            .post(ImageDetectEvent(startTag, originalPath, faceFunctionBean))
                }
            }

            override fun onDetectMultiFaces(originalPath: String, originBitmap: Bitmap,
                                            faces: List<FirebaseVisionFace>,
                                            onDetectResult: FaceSdkProxy.OnDetectResult) {
                FaceAppState.getMainActivity()
                        ?.start(MultiFaceFragment.newInstance(startTag, originalPath, originBitmap,
                                faces, onDetectResult))
            }

            override fun onDetectFail(originalPath: String, errorCode: Int) {
                runMain {
                    GlobalProgressBar.hide()
                    FaceAppState.getMainActivity()?.run {
                        DialogUtils.showErrorDialog(this, errorCode)
                    }
                }
            }
        })
    }

    fun requestPermission(activity: FragmentActivity, onPermissionResult: OnPermissionResult) {
        PermissionHelper.requestCameraPermission(activity, object : OnPermissionResult {
            override fun onPermissionGrant(permission: String) {
                onPermissionResult.onPermissionGrant(permission)
                BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                        Statistic103Constant.PHOTO_PERMISSION_OBTAINED, "")
            }

            override fun onPermissionDeny(permission: String, never: Boolean) {
                if (!never) {
                    //权限说明弹窗
                    showPermissionTipDialog(activity, onPermissionResult)
                } else {
                    onPermissionResult.onPermissionDeny(permission, never)
                    //权限引导弹窗
                    showPermissionDenyNeverDialog(activity)
                }
            }
        }, -1)
        BaseSeq103OperationStatistic.uploadSqe103StatisticData(Statistic103Constant.PHOTO_PERMISSION_REQUEST, "")

//        if (PermissionHelper.isPermissionGroupDeny(activity, Permissions.CAMERA)) {
//            //权限引导弹窗
//            showPermissionDenyNeverDialog(activity)
//        } else {
//            //权限说明弹窗
//            showPermissionTipDialog(activity, onPermissionResult)
//        }
    }


    fun showPermissionTipDialog(activity: Activity, onPermissionResult: OnPermissionResult?) {
        val dialog = TipsDialog(activity)
        dialog.setContent(R.string.permission_tip_camera)
        dialog.setupOKButton(R.string.ok, View.OnClickListener {
            dialog.dismiss()
            PermissionHelper.requestCameraPermission(activity, object : OnPermissionResult {
                override fun onPermissionGrant(permission: String) {
                    onPermissionResult?.onPermissionGrant(permission)
                }

                override fun onPermissionDeny(permission: String, never: Boolean) {
                    dialog.dismiss()
                    onPermissionResult?.onPermissionDeny(permission, never)
                }
            }, -1)
        })
        dialog.setupCancelButton(R.string.cancel,
                View.OnClickListener { dialog.dismiss() })

        dialog.show()
    }

    private fun showPermissionDenyNeverDialog(activity: Activity) {
        val dialog = TipsDialog(activity)
        dialog.setContent(R.string.permission_tip_camera_never)
        dialog.setupOKButton(R.string.ok, View.OnClickListener {
            dialog.dismiss()
            PermissionSettingPage.start(activity, false)
        })

        dialog.setupCancelButton(R.string.cancel, View.OnClickListener { dialog.dismiss() })
        dialog.show()
    }

    fun uploadEnterStatistic(entrance:String) {
        BaseSeq103OperationStatistic
                .uploadSqe103StatisticData(Statistic103Constant.CAMERA_ENTER, entrance)
    }

    fun uploadTakePhotoClick(facing: Int) {
        BaseSeq103OperationStatistic
                .uploadSqe103StatisticData("", Statistic103Constant.CAMERA_CLICK, "",
                        facing.toString())
    }

}
