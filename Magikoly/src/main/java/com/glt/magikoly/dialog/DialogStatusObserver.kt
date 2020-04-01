package com.glt.magikoly.dialog

/**
 * Dialog状态观察者，用于Home键事件处理中
 * @author yangguanxiang
 */
object DialogStatusObserver {

    private var mDialog: IDialog? = null

    fun isDialogShowing(): Boolean {
        return mDialog != null && mDialog!!.isShowing()
    }

    fun onDialogShow(dialog: IDialog) {
        mDialog = dialog
    }

    fun onDialogDismiss(dialog: IDialog) {
        if (mDialog === dialog) {
            mDialog = null
        }
    }

    fun dismissDialog() {
        if (mDialog != null && mDialog!!.isShowing()) {
            try {
                mDialog!!.dismiss()
            } catch (e: Exception) {
            }
            mDialog = null
        }
    }
}
