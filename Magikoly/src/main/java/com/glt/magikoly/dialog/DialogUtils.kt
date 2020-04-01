package com.glt.magikoly.dialog

import android.app.Activity
import android.content.DialogInterface
import android.view.View
import com.glt.magikoly.constants.ErrorInfoFactory
import magikoly.magiccamera.R

class DialogUtils {

    companion object {
        fun showErrorDialog(activity: Activity?, errorCode: Int, onOKCallback: Runnable? = null,
                            onDismissListener: DialogInterface.OnDismissListener? = null) {
            if (activity != null && !activity.isFinishing) {
                val errorInfo = ErrorInfoFactory.getErrorInfo(errorCode)
                val dialog = TipsWithImageDialog(activity)
                dialog.setImage(errorInfo.imageId)
                dialog.setTitle(errorInfo.titleId)
                dialog.setContent(errorInfo.descId)
                dialog.setupOKButton(R.string.ok, View.OnClickListener {
                    onOKCallback?.run()
                    dialog.dismiss()
                })
                dialog.setOnDismissListener(onDismissListener)
                dialog.show()
            }
        }

        fun showTipsWithImageDialog(activity: Activity?, imageId: Int, titleId: Int, descId: Int,
                                    onOKCallback: Runnable? = null,
                                    onCancelCallback: Runnable? = null,
                                    onDismissListener: DialogInterface.OnDismissListener? = null) {
            if (activity != null && !activity.isFinishing) {
                val dialog = TipsWithImageDialog(activity)
                dialog.setImage(imageId)
                dialog.setTitle(titleId)
                dialog.setContent(descId)
                onOKCallback?.apply {
                    dialog.setupOKButton(R.string.ok, View.OnClickListener {
                        run()
                        dialog.dismiss()
                    })
                }

                onCancelCallback?.apply {
                    dialog.setupCancelButton(R.string.cancel, View.OnClickListener {
                        run()
                        dialog.dismiss()
                    })
                }
                dialog.setOnDismissListener(onDismissListener)
                dialog.show()
            }
        }

        fun showTipsDialog(activity: Activity?, contentId: Int, onOKCallback: Runnable? = null,
                           onCancelCallback: Runnable? = null,
                           onDismissListener: DialogInterface.OnDismissListener? = null) {
            if (activity != null && !activity.isFinishing) {
                val dialog = TipsDialog(activity)
                dialog.setContent(contentId)
                onOKCallback?.apply {
                    dialog.setupOKButton(R.string.ok, View.OnClickListener {
                        run()
                        dialog.dismiss()
                    })
                }

                onCancelCallback?.apply {
                    dialog.setupCancelButton(R.string.cancel, View.OnClickListener {
                        run()
                        dialog.dismiss()
                    })
                }
                dialog.setOnDismissListener(onDismissListener)
                dialog.show()
            }
        }


        fun showTipsDialog(activity: Activity?, contentId: Int, okId: Int, cancelId: Int, onOKCallback: Runnable? = null,
                           onCancelCallback: Runnable? = null,
                           onDismissListener: DialogInterface.OnDismissListener? = null) {
            if (activity != null && !activity.isFinishing) {
                val dialog = TipsDialog(activity)
                dialog.setContent(contentId)
                onOKCallback?.apply {
                    dialog.setupOKButton(okId, View.OnClickListener {
                        run()
                        dialog.dismiss()
                    })
                }

                onCancelCallback?.apply {
                    dialog.setupCancelButton(cancelId, View.OnClickListener {
                        run()
                        dialog.dismiss()
                    })
                }
                dialog.setOnDismissListener(onDismissListener)
                dialog.show()
            }
        }
    }
}