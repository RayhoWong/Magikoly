package com.glt.magikoly.function.resultreport

import android.support.constraint.ConstraintLayout
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.ad.inner.InnerAdController
import com.glt.magikoly.ad.inner.InnerAdController.Companion.ADVIDEO_MODULE_ID
import com.glt.magikoly.ad.reward.WatchVideoAskDialog
import kotlinx.android.synthetic.main.layout_subscribe_entrance.view.*
import magikoly.magiccamera.R

/**
 *
 * @author rayhahah
 * @blog http://rayhahah.com
 * @time 2019/2/21
 * @tips 这个类是Object的子类
 * @fuction
 */
class LayoutPurchaseController private constructor() {

    companion object {
        fun getInstance() = Holder.instance
    }

    object Holder {
        val instance = LayoutPurchaseController()
    }

    /**
     * 初始化付费跳转界面
     * @param clickAdapter:点击回调监听
     */
    fun init(layout: View?, configBean: Bean, clickAdapter: OnClickAdapter) {
        layout?.apply {
            InnerAdController.instance.loadAd(context, ADVIDEO_MODULE_ID)
            setOnTouchListener { _, _ -> true }

            if (configBean.showLockImage) {
                findViewById<ImageView>(R.id.purchase_cover_img).visibility = View.VISIBLE
            } else {
                findViewById<ImageView>(R.id.purchase_cover_img).visibility = View.GONE
            }

            if (configBean.showDescription) {
                findViewById<TextView>(R.id.purchase_content_txt).visibility = View.VISIBLE
            } else {
                findViewById<TextView>(R.id.purchase_content_txt).visibility = View.GONE
            }

            if (configBean.showClose) {
                findViewById<ImageView>(R.id.purchase_img_back).visibility = View.VISIBLE
            } else {
                findViewById<ImageView>(R.id.purchase_img_back).visibility = View.GONE
            }

            findViewById<ImageView>(R.id.purchase_img_back).setOnClickListener {
                clickAdapter.onCloseClick()
            }
            if (configBean.showBackHome) {
                findViewById<ConstraintLayout>(R.id.purchase_back_home).visibility = View.VISIBLE
                findViewById<ConstraintLayout>(R.id.purchase_back_home).setBackgroundResource(configBean.subButtonBgResId)
                findViewById<ConstraintLayout>(R.id.purchase_back_home).setOnClickListener {
                    clickAdapter.onHomeClick()
                }

            } else {
                findViewById<ConstraintLayout>(R.id.purchase_back_home).visibility = View.GONE

                findViewById<ConstraintLayout>(R.id.purchase_watch_ad_video).setBackgroundResource(configBean.subButtonBgResId)
                findViewById<ConstraintLayout>(R.id.purchase_watch_ad_video).setOnClickListener {
                    showWatchVideoDialog(clickAdapter)
                }
            }
        }
    }

    var dialog: WatchVideoAskDialog? = null

    private fun showWatchVideoDialog(clickAdapter: OnClickAdapter) {
        dialog?.apply {
            if (isShowing) {
                return
            }
        }

        FaceAppState.getMainActivity()?.let { activity ->
            if (activity.isFinishing) {
                return
            }
            dialog = WatchVideoAskDialog(activity, ADVIDEO_MODULE_ID)
            dialog?.apply {
                setListener(object : WatchVideoAskDialog.IAskDialogListener() {

                    override fun onVideoPlayStart() {
                        clickAdapter.onWatchVideoStart()
                    }


                    override fun onConfirm() {
                    }

                    override fun onAdFailed() {
                        clickAdapter.onAdFailed()
                    }

                    override fun onAdClicked() {
                        clickAdapter.onAdClick()
                    }

                    override fun onVideoPlayFinish() {
                        clickAdapter.onWatchVideoFinish()
                    }

                    override fun onCancel() {
                        dialog?.dismiss()
                    }

                    override fun onNotClickDismiss(dialog: WatchVideoAskDialog?) {
                        super.onNotClickDismiss(dialog)
                    }
                })
                show()
                showAd()
                clickAdapter.onWatchVideoClick()
            }
        }
    }

    fun show(layout: View?, isTrialCount: Boolean, bgResId: Int = 0, showBackHome: Boolean = false) {
        layout?.apply {
            visibility = View.VISIBLE

            if (bgResId != 0) {
                findViewById<ImageView>(R.id.purchase_img_bg).visibility = View.VISIBLE
                findViewById<ImageView>(R.id.purchase_img_bg).setImageResource(bgResId)
            } else {
                findViewById<ImageView>(R.id.purchase_img_bg).visibility = View.GONE
            }

            if (showBackHome) {
                purchase_back_home.visibility = View.VISIBLE
                purchase_watch_ad_video.visibility = View.GONE
            } else {
                purchase_back_home.visibility = View.GONE

                purchase_watch_ad_video.visibility = View.VISIBLE

                if (isTrialCount) {
                    findViewById<TextView>(R.id.purchase_content_txt).text = context.resources.getString(R.string.layout_purchase_trial_count_text)
                    findViewById<ImageView>(R.id.purchase_cover_img).setImageResource(R.drawable.img_layout_purchase_trial)
                } else {
                    findViewById<TextView>(R.id.purchase_content_txt).text = context.resources.getString(R.string.layout_purchase_upgrade_full_version)
                    findViewById<ImageView>(R.id.purchase_cover_img).setImageResource(R.drawable.img_layout_purchase_lock)
                }
            }
        }
    }

    fun hide(layout: View?) {
        layout?.apply {
            visibility = View.GONE
        }
    }

    open class OnClickAdapter {
        open fun onCloseClick() {

        }

        open fun onWatchVideoFinish() {

        }

        open fun onWatchVideoClick() {

        }

        open fun onWatchVideoStart() {

        }

        open fun onAdClick() {

        }

        open fun onAdFailed() {

        }


        open fun onHomeClick() {

        }
    }


    class Bean(
            var showLockImage: Boolean = true,
            val showDescription: Boolean = true,
            val showClose: Boolean = true,
            val showBackHome: Boolean = false,
            val subButtonBgResId: Int = R.drawable.btn_purple_line_bold
    )

}