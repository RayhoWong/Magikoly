package com.glt.magikoly.function.splash

import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import com.glt.magikoly.BuyChannelApiProxy
import com.glt.magikoly.ad.inner.InnerAdController
import com.glt.magikoly.ext.l
import com.glt.magikoly.function.main.MainFragment
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.subscribe.SubscribeController
import com.glt.magikoly.subscribe.SubscribeProxy
import com.glt.magikoly.subscribe.SubscribeScene
import com.glt.magikoly.subscribe.billing.BillingOrder
import com.glt.magikoly.subscribe.view.AbsSubscribeView
import com.glt.magikoly.version.VersionController
import com.yqritc.scalablevideoview.ScalableType
import kotlinx.android.synthetic.main.fragment_splash.*
import magikoly.magiccamera.R
import me.yokeyword.fragmentation.SupportFragment
import java.io.IOException

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
 * @time 2019/1/14
 * @tips 这个类是Object的子类
 * @fuction
 */
class SplashFragment : SupportFragment() {

    companion object {
        fun newInstance(): SplashFragment {
            return SplashFragment()
        }
    }

    private var mIsStartMain: Boolean = false
    var isDismiss: Boolean = false

    private var mAnimPreloadTask: ISplashTask = object : AbsSplashTask() {
        override fun preloadKey(): String = SplashTaskManager.PRELOAD_KEY_SPLASH_ANIMATION

        override fun startPreload() {
        }

        override fun onTick(currentMills: Long) {
            super.onTick(currentMills)

            l("current video position=" + vv_splash?.currentPosition)
            if (!mIsPreloadFinished && vv_splash?.currentPosition ?: 0 >= 2000) {
                mIsPreloadFinished = true
            }
            //            tv_count.text = currentMills.toString()
        }

        override fun onCancelPreload(taskAllFinished: Boolean) {
            super.onCancelPreload(taskAllFinished)
            uploadLaunchAnim()
            startLaunchSubscribe()
//            if (vv_splash?.isPlaying == true) {
//                vv_splash?.pause()
//                runMain(1000) {
//                    startLaunchSubscribe(taskAllFinished)
//                    //                            startWithPop(MainFragment.startTakePhoto())
//                }
//            } else {
//                startLaunchSubscribe(taskAllFinished)
//            }
        }
    }

    private val mAdPreloadTask: ISplashTask = object : AbsSplashTask() {
        override fun preloadKey(): String = SplashTaskManager.PRELOAD_KEY_AD

        override fun startPreload() {
            if (_mActivity == null) {
                mIsPreloadFinished = true
            } else {

                InnerAdController.instance.loadAd(_mActivity, InnerAdController.LAUNCHING_AD_MODULE_ID,
                        object : InnerAdController.AdLoadListenerAdapter() {
                            override fun onAdLoadSuccess(
                                    adBean: InnerAdController.AdBean?) {
                                mIsPreloadFinished = true
                            }

                            override fun onAdLoadFail(statusCode: Int) {
                                mIsPreloadFinished = true
                            }
                        })

                InnerAdController.instance.loadAd(_mActivity, InnerAdController.HOME_LAUNCHING_AD_MODULE_ID)

            }
        }
    }

    private fun startLaunchSubscribe() {
        val subscribeListener = object : SubscribeProxy.BaseListener() {
            override fun onDismiss(absSubscribeView: AbsSubscribeView) {
                super.onDismiss(absSubscribeView)
                mIsStartMain = true
                startMain()
            }

            override fun onPurchaseSuccess(billingOrder: BillingOrder) {
                super.onPurchaseSuccess(billingOrder)
                mIsStartMain = true
                startMain()
            }

            override fun onShow(isShow: Boolean, scene: Int) {
                super.onShow(isShow, scene)
                if (!isShow) {
                    mIsStartMain = true
                    startMain()
                }
            }
        }
        SubscribeController.getInstance().launch(_mActivity, SubscribeScene.LAUNCH, subscribeListener)
    }

/*    private val mScene: Scene = object : Scene(SubscribeScene.LAUNCH_LA) {
        override fun getCustomUI(styleId: Int): ICustomUI? {
            return object : ICustomUI {
                override fun show(p0: SubscribeData?, p1: ICustomEvent?) {
                }
            }
        }
    }*/


    @Synchronized
    private fun startMain() {
        if (mIsResume && mIsStartMain && !isDismiss) {
            isDismiss = true
            val adBean = InnerAdController.instance.getPendingAdBean(InnerAdController.LAUNCHING_AD_MODULE_ID)
            if (adBean != null) {
                if (adBean.isInterstitialAd) {
                    val ret = adBean.showInterstitialAd(object :
                            InnerAdController.AdLoadListenerAdapter() {
                        override fun onAdClosed() {
                            doStartMain()
                        }
                    })
                    if (!ret) {
                        doStartMain()
                    }
                } else {
                    doStartMain()
                }
            } else {
                doStartMain()
            }
        }
    }

    private fun doStartMain() {
        vv_splash?.visibility = View.GONE
        startWithPop(MainFragment.newInstance())
    }

    private var mIsResume: Boolean = false

    override fun onResume() {
        super.onResume()
        mIsResume = true
        startMain()
    }

    override fun onPause() {
        super.onPause()
        mIsResume = false
    }

    override fun onStop() {
        super.onStop()
        mIsResume = false
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_splash, null)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSplashVideo()
        initSplashPreloadTask()
        //        btn_pass.setOnClickListener {
        //            SplashTaskManager.getInstance().cancel()
        //        }
    }

    /**
     * 初始化启动页视频
     */
    private fun initSplashVideo() {

        vv_splash.visibility = View.VISIBLE
//        val url = "android.resource://" + context?.packageName + "/" + R.raw.splash_video
//        initVideoBanner(vv_splash, url)
        try {
            vv_splash.releaseSafety()
            vv_splash.setRawData(R.raw.splash_video)
            vv_splash.setVolume(0f, 0f)
            vv_splash.setScalableType(ScalableType.CENTER_CROP)
            vv_splash.isLooping = false
            vv_splash.prepare {
                it.setOnInfoListener { mp, what, extra ->
                    return@setOnInfoListener true
                }
                vv_splash?.start()
            }
        } catch (ioe: IOException) {
            //ignore
        }
        //        val uri = "android.resource://" + context?.packageName + "/" + vv_splash.setBackgroundColor(Color.WHITE)
        //        vv_splash.setVideoURI(Uri.parse(uri))
        //        vv_splash.setOnPreparedListener(object : MediaPlayer.OnPreparedListener {
        //            override fun onPrepared(mp: MediaPlayer) {
        //                mp.isLooping = false
        //                mp.setOnInfoListener(MediaPlayer.OnInfoListener { mp, what, extra ->
        //                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
        //                        // video 视屏播放的时候把背景设置为透明
        //                        vv_splash?.setBackgroundColor(Color.TRANSPARENT)
        //                        return@OnInfoListener true
        //                    }
        //                    false
        //                })
        //                mp.start()
        //            }
        //        })

    }


    protected fun initVideoBanner(videoView: VideoView, url: String) {
//        videoView.setBackgroundColor(Color.TRANSPARENT)
        videoView.setBackgroundResource(R.drawable.splash_bg)
        videoView.setVideoURI(Uri.parse(url))
        videoView.setOnErrorListener { mp, what, extra ->
            videoView.setVideoURI(Uri.parse(url))
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
        mp.isLooping = false
        mp.setOnInfoListener(MediaPlayer.OnInfoListener { mp, what, extra ->
            if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                // video 视屏播放的时候把背景设置为透明
                videoView.setBackgroundColor(Color.TRANSPARENT)
                return@OnInfoListener true
            }

            false
        })
        mp.start()
    }


    /**
     * 初始化预加载任务
     */
    private fun initSplashPreloadTask() {
        SplashTaskManager.getInstance().clear()
        SplashTaskManager.getInstance().addSplashTask(mAnimPreloadTask)
        SplashTaskManager.getInstance().addSplashTask(mBuychannelPreloadTask)
        if (!SubscribeController.getInstance().isVIP()) {
//        SplashTaskManager.getInstance().addSplashTask(SubscribeVideoSplashTask(subscribeDataSplashTask))
//            SplashTaskManager.getInstance().addSplashTask(SubscribeBannerSplashTask(subscribeDataSplashTask))
//            if (!SubscribeController.getInstance().isNonForceSubscribe() && !SubscribeController.getInstance().isForceSubscribe()) {
            SplashTaskManager.getInstance().addSplashTask(mAdPreloadTask)
//            }
        }

        SplashTaskManager.getInstance().start()
    }

    fun onNewIntent() {
        mIsResume = false
        mIsStartMain = false
        initSplashVideo()
        initSplashPreloadTask()
    }

    private fun uploadLaunchAnim() {
        val videoSecond = ((vv_splash?.currentPosition ?: 0) / 1000)
        BaseSeq103OperationStatistic.uploadSqe103StatisticData(videoSecond.toString(),
                Statistic103Constant.LAUNCHINGANIMATION_SHOW, VersionController.getAppEnterCount().toString(), "", "", BuyChannelApiProxy.getCampaign())
    }

    private val mBuychannelPreloadTask: ISplashTask = object : AbsSplashTask() {
        override fun preloadKey(): String = SplashTaskManager.PRELOAD_KEY_BUYCHANNEL

        override fun startPreload() {
            if (BuyChannelApiProxy.isBuyChannelFetched()) {
                mIsPreloadFinished = true
            }
        }

        override fun onTick(currentMills: Long) {
            super.onTick(currentMills)
            if (BuyChannelApiProxy.isBuyChannelFetched()) {
                mIsPreloadFinished = true
            }
        }
    }

}