package com.glt.magikoly.subscribe.view

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.media.MediaPlayer
import android.net.Uri
import android.support.constraint.ConstraintLayout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.animation.AnimatorUtil
import com.glt.magikoly.ext.runMain
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.BaseSeq59OperationStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.subscribe.SubscribeData
import com.glt.magikoly.subscribe.SubscribeProxy
import com.glt.magikoly.utils.AdaptScreenUtils
import com.glt.magikoly.utils.ToastUtils
import com.tencent.bugly.crashreport.CrashReport
import com.yqritc.scalablevideoview.ScalableType
import com.yqritc.scalablevideoview.ScalableVideoView
import magikoly.magiccamera.R

/**
 * ┌───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┐
 * │Esc│ │ F1│ F2│ F3│ F4│ │ F5│ F6│ F7│ F8│ │ F9│F10│F11│F12│ │P/S│S L│P/B│ ┌┐    ┌┐    ┌┐
 * └───┘ └───┴───┴───┴───┘ └───┴───┴───┴───┘ └───┴───┴───┴───┘ └───┴───┴───┘ └┘    └┘    └┘
 * ┌──┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───────┐┌───┬───┬───┐┌───┬───┬───┬───┐
 * │~`│! 1│@ 2│# 3│$ 4│% 5│^ 6│& 7│* 8│( 9│) 0│_ -│+ =│ BacSp ││Ins│Hom│PUp││N L│ / │ * │ - │
 * ├──┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─────┤├───┼───┼───┤├───┼───┼───┼───┤
 * │Tab │ Q │ W │ E │ R │ T │ Y │ U │ I │ O │ P │{ [│} ]│ | \ ││Del│End│PDn││ 7 │ 8 │ 9 │   │
 * ├────┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴─────┤└───┴───┴───┘├───┼───┼───┤ + │
 * │Caps │ A │ S │ D │ F │ G │ H │ J │ K │ L │: ;│" '│ Enter  │             │ 4 │ 5 │ 6 │   │
 * ├─────┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴────────┤    ┌───┐    ├───┼───┼───┼───┤
 * │Shift  │ Z │ X │ C │ V │ B │ N │ M │< ,│> .│? /│  Shift   │    │ ↑ │    │ 1 │ 2 │ 3 │   │
 * ├────┬──┴─┬─┴──┬┴───┴───┴───┴───┴───┴──┬┴───┼───┴┬────┬────┤┌───┼───┼───┐├───┴───┼───┤ E││
 * │Ctrl│Ray │Alt │         Space         │ Alt│code│fuck│Ctrl││ ← │ ↓ │ → ││   0   │ . │←─┘│
 * └────┴────┴────┴───────────────────────┴────┴────┴────┴────┘└───┴───┴───┘└───────┴───┴───┘
 *
 * @author Rayhahah
 * @blog http://rayhahah.com
 * @time 2019/1/9
 * @tips 这个类是Object的子类
 * @fuction
 */

abstract class AbsSubscribeView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                                          defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr), ISubscribeView, View.OnClickListener {

    companion object {

        fun setExtraLineVisual(str: String, titleTextView: TextView, subtitleTextView: TextView) {
            val strArray = if (str.contains("<br>")) {
                str.split("<br>")
            } else if (str.contains("\n")) {
                str.split("\n")
            } else {
                str.split("\n")
            }

            titleTextView.text = strArray[0]
            if (strArray.size > 1 && strArray[1] != null && strArray[1].isNotBlank()) {
                subtitleTextView.visibility = View.VISIBLE
                subtitleTextView.text = strArray[1]
            } else {
                subtitleTextView.visibility = View.GONE
            }
        }
    }

    private val mPrivateURL = "http://magikoly.com/Magikoly_privacy.html"
    private val mUserArgeementURL = "http://magikoly.com/Magikoly_statement.html"

    protected lateinit var mCurrentProductId: String
    protected var mCurrentPosition = 0

    protected var isVisual = false
    protected var isVideoReady = false

    protected lateinit var mSubscribeData: SubscribeData
    protected var mScene: Int = -1

    override fun onFinishInflate() {
        super.onFinishInflate()
        initView()
    }

    override fun bindData() {
    }

    override fun setSubscribeData(subscribeData: SubscribeData) {
        mSubscribeData = subscribeData
        bindData()
    }

    override fun getSubscribeData(): SubscribeData = mSubscribeData

    override fun setScene(scene: Int) {
        mScene = scene
    }

    override fun getScene(): Int = mScene

    override fun onBuyClick() {
        onBuyUpload()
        if (context is Activity) {
            SubscribeProxy.getInstance().pay(context as Activity, mCurrentProductId, this)
        }
    }

    override fun onEventExit() {
    }

    override fun onDismiss() {
        isVisual = false
        SubscribeProxy.getInstance().onDismiss(this)
    }

    override fun onVisual() {
        isVisual = true
        BaseSeq103OperationStatistic.uploadSqe103StatisticData(mCurrentProductId, Statistic103Constant.SUBSCRIBE_VISUAL, mScene.toString(), getStyleId())
        BaseSeq59OperationStatistic.uploadPurchaseVisual(mCurrentProductId, mScene, getStyleId())
    }

    override fun onBuyUpload() {
        //统计
        BaseSeq59OperationStatistic.uploadPurchaseClick(mCurrentProductId, mScene, getStyleId())
    }

    override fun onClick(v: View) {
        when (v.id) {
//            R.id.iv_close_left, R.id.iv_close_right, R.id.tv_close_bottom, R.id.tv_close_bottom_long -> {
//                onEventExit()
//                onDismiss()
//            }
//            R.id.btn_apply -> onBuyClick()
//            else -> {
//            }
        }
    }

    protected fun turnToBrower(url: String) {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.data = Uri.parse(url)
        intent.action = Intent.ACTION_VIEW
        try {
            FaceAppState.getContext().startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            ToastUtils.showToast("No browser available", Toast.LENGTH_SHORT)
        }
    }

    protected fun transStrLine(str: String): String {
        return str.replace("<br>", "\n")
    }

    protected fun setTextVisible(view: TextView, msg: String?) {
        if (msg != null && "" != msg && "null" != msg) {
            view.text = transStrLine(msg)
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }


    protected fun obtainUserPrivateSpan(str: String): SpannableStringBuilder {
        val indexOfLastUserAgreement = str.indexOfLast {
            return@indexOfLast it.equals('^')
        }
        val indexOfFirstAgreement = str.indexOfFirst {
            return@indexOfFirst it.equals('^')
        }
        val indexOfLast = str.indexOfLast {
            return@indexOfLast it.equals('#')
        }
        val indexOfFirst = str.indexOfFirst {
            return@indexOfFirst it.equals('#')
        }

        val builder = SpannableStringBuilder(str.replace("^", "").replace("#", ""))
        val clickableSpanUserAgreement = object : ClickableSpan() {
            override fun onClick(widget: View?) {
                turnToBrower(mUserArgeementURL)
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color = Color.parseColor("#a7a7a7")
                ds.isUnderlineText = true
            }
        }
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View?) {
                turnToBrower(mPrivateURL)
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color = Color.parseColor("#a7a7a7")
                ds.isUnderlineText = true
            }
        }
        if (indexOfFirstAgreement < indexOfFirst) {
            if (indexOfFirstAgreement > 0) {
                builder.setSpan(clickableSpanUserAgreement, indexOfFirstAgreement - 1, indexOfLastUserAgreement - 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            if (indexOfFirst > 0) {
                builder.setSpan(clickableSpan, indexOfFirst - 3, indexOfLast - 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

        } else {
            if (indexOfFirstAgreement > 0) {
                builder.setSpan(clickableSpanUserAgreement, indexOfFirstAgreement - 3, indexOfLastUserAgreement - 3,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            if (indexOfFirst > 0) {
                builder.setSpan(clickableSpan, indexOfFirst - 1, indexOfLast - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        return builder
    }

    protected fun initClosePosition(closePosition: Int, left: ImageView, right: ImageView, bottom: TextView?,
                                    bottomLong: TextView?) {
        when (closePosition) {
            1 -> {
                left.visibility = View.GONE
                right.visibility = View.GONE
                bottom?.visibility = View.GONE
                bottomLong?.visibility = View.GONE
            }
            2 -> {
                left.visibility = View.GONE
                right.visibility = View.GONE
                bottom?.visibility = View.GONE
                runMain(5000) {
                    left.setImageResource(R.drawable.btn_close_dark)
                    left.visibility = View.VISIBLE
                }
                bottomLong?.visibility = View.GONE
            }
            3 -> {
                left.visibility = View.GONE
                right.setImageResource(R.drawable.btn_close_dark)
                right.visibility = View.VISIBLE
                bottom?.visibility = View.GONE
                bottomLong?.visibility = View.GONE
            }
            4 -> {
                left.visibility = View.VISIBLE
                left.setImageResource(R.drawable.btn_close_dark)
                right.visibility = View.GONE
                bottom?.visibility = View.GONE
                bottomLong?.visibility = View.GONE
            }
            5 -> {
                left.visibility = View.GONE
                right.visibility = View.GONE
                bottom?.visibility = View.VISIBLE
                val lp = bottom?.layoutParams as ConstraintLayout.LayoutParams
                lp.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
                bottom.layoutParams = lp
                bottomLong?.visibility = View.GONE
            }
            6, 8 -> {
                right.visibility = View.GONE
                left.visibility = View.VISIBLE
                left.setImageResource(R.drawable.btn_close_clear)
                bottom?.visibility = View.GONE
                bottomLong?.visibility = View.GONE
            }
            7 -> {
                right.visibility = View.GONE
                left.visibility = View.GONE
                bottom?.visibility = View.GONE
                bottomLong?.visibility = View.VISIBLE
            }
            else -> {
            }
        }
    }

    private var mButtonAnimator: ObjectAnimator? = null

    protected fun animButton(button: View) {
        mButtonAnimator = AnimatorUtil.animTransX(button, 300, 0f, AdaptScreenUtils.pt2Px(5f) * 1f,
                -AdaptScreenUtils.pt2Px(5f) * 1f, 0f)
        mButtonAnimator?.repeatCount = ValueAnimator.INFINITE
        mButtonAnimator?.repeatMode = ValueAnimator.RESTART
        mButtonAnimator?.start()
    }

    fun startButtonAnim() {
        mButtonAnimator?.apply {
            if (!isRunning) {
                start()
            }
        }
    }

    fun stopButtonAnim() {
        mButtonAnimator?.apply {
            if (isRunning) {
                cancel()
            }
        }
    }

    protected fun initDefaultVideoBanner(videoView: VideoView, resId: Int) {
        isVideoReady = false
        val uri = "android.resource://" + context?.packageName + "/" + resId
        initVideoBanner(videoView, uri)
    }

    protected fun initLocalVideoBanner(videoView: VideoView, path: String) {
        isVideoReady = false
        initVideoBanner(videoView, path)
    }

    protected fun initVideoBanner(videoView: VideoView, url: String) {
        isVideoReady = false
        //        videoView.setDataSource(url)
        //        videoView.setOnErrorListener { mp, what, extra ->
        //            videoView.setDataSource(url)
        //            videoView.prepareAsync {
        //                onVideoPrepare(it, videoView)
        //            }
        //            return@setOnErrorListener true
        //        }
        //        videoPlay(videoView)

        videoView.setBackgroundColor(Color.WHITE)
        videoView.setVideoURI(Uri.parse(url))
        videoView.setOnErrorListener { mp, what, extra ->
            try {
                videoView.setVideoURI(Uri.parse(url))
            } catch (e: Exception) {
                CrashReport.postCatchedException(e)
            }
            videoView.setOnPreparedListener { mp ->
                onPrepareVideoView(mp, videoView)
            }
            return@setOnErrorListener true
        }
        videoView.setOnPreparedListener { mp ->
            onPrepareVideoView(mp, videoView)
        }
    }

    private fun onPrepareVideoView(mp: MediaPlayer, videoView: VideoView) {
        mp.isLooping = true
        isVideoReady = true
        mp.setOnInfoListener(MediaPlayer.OnInfoListener { mp, what, extra ->
            if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                // video 视屏播放的时候把背景设置为透明
                videoView.setBackgroundColor(Color.TRANSPARENT)
//                SubscribeProxy.getInstance().recordVideoStartTime()
                return@OnInfoListener true
            }

            false
        })
        if (isVisual) {
            mp.start()
        }
    }

    private fun onVideoPrepare(it: MediaPlayer, videoView: ScalableVideoView) {
        isVideoReady = true
        it.isLooping = true
        it.setOnInfoListener { mp, what, extra ->
            if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                // video 视屏播放的时候把背景设置为透明
                videoView.setBackgroundColor(Color.TRANSPARENT)
//                SubscribeProxy.getInstance().recordVideoStartTime()
                return@setOnInfoListener true
            }
            return@setOnInfoListener false
        }

        if (isVisual) {
            videoView.start()
        }
    }

    private fun videoPlay(videoView: ScalableVideoView) {
        videoView.setBackgroundColor(Color.WHITE)
        videoView.setVolume(0f, 0f)
        videoView.setScalableType(ScalableType.FIT_XY)
        videoView.isLooping = true
        videoView.prepareAsync {
            isVideoReady = true
            it.isLooping = true
            it.setOnInfoListener { _, what, _ ->
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    // video 视屏播放的时候把背景设置为透明
                    videoView.setBackgroundColor(Color.TRANSPARENT)
//                    SubscribeProxy.getInstance().recordVideoStartTime()
                    return@setOnInfoListener true
                }
                return@setOnInfoListener false
            }

            if (isVisual) {
                videoView.start()
            }
        }
    }

    protected fun setLinearGradient(textView: TextView, startColor: Int, endColor: Int) {
        textView.post {
            val linearGradient =
                    LinearGradient(0f, textView.height * 1f / 2, textView.width * 1f, textView.height * 1f / 2,
                            intArrayOf(startColor, endColor), null,
                            Shader.TileMode.CLAMP)
            textView.paint.shader = linearGradient
            textView.postInvalidate()
        }
    }


}