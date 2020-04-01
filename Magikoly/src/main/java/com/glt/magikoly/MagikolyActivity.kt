package com.glt.magikoly

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.glt.magikoly.ad.inner.InnerAdController
import com.glt.magikoly.event.CameraTransitionEvent
import com.glt.magikoly.function.DemoFaceImageInfo
import com.glt.magikoly.function.FaceFunctionBean
import com.glt.magikoly.function.FaceFunctionManager
import com.glt.magikoly.function.FaceFunctionManager.KEY_CURRENT_IMG_PATH
import com.glt.magikoly.function.FaceFunctionManager.KEY_DEMO_FACE_IMAGE_INFO
import com.glt.magikoly.function.FaceFunctionManager.KEY_FACE_BEAN_MAP_KEYS
import com.glt.magikoly.function.main.MainFragment
import com.glt.magikoly.function.splash.SplashFragment
import com.glt.magikoly.permission.PermissionFragmentActivity
import com.glt.magikoly.permission.PermissionHelper
import com.glt.magikoly.statistic.Statistic103Constant.ENTRANCE_MAIN
import com.glt.magikoly.subscribe.SubscribeProxy
import com.glt.magikoly.utils.Duration
import com.glt.magikoly.utils.FileUtils
import com.glt.magikoly.version.VersionController
import com.glt.magikoly.view.GlobalProgressBar
import com.glt.magikoly.view.ProgressBarEvent
import kotlinx.android.synthetic.main.main_container.*
import magikoly.magiccamera.R
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

class MagikolyActivity : PermissionFragmentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        savedInstanceState?.apply {
            FaceFunctionManager.currentFaceImagePath = getString(KEY_CURRENT_IMG_PATH)
            val keyArray = getStringArray(KEY_FACE_BEAN_MAP_KEYS)
            keyArray.forEach { key ->
                val faceBean = getParcelable<FaceFunctionBean>(key)
                FaceFunctionManager.faceBeanMap[key] = faceBean
            }

            getParcelable<DemoFaceImageInfo>(KEY_DEMO_FACE_IMAGE_INFO)?.let {
                FaceFunctionManager.demoFaceImageInfo = it
            }
        }
        super.onCreate(savedInstanceState)
        FaceAppState.setMainActivity(this)
        setContentView(R.layout.main_container)
        findViewById<GlobalProgressBar>(R.id.progress_bar).blurLayout = container

        if (topFragment == null) {
            val fragment = findFragment(SplashFragment::class.java)
            if (fragment == null) {
                loadRootFragment(R.id.container, SplashFragment.newInstance())
            }
//        val fragment = findFragment(MainFragment::class.java)
//        if (fragment == null) {
//            loadRootFragment(R.id.container, MainFragment.startTakePhoto())
//        }
        }


//        StatusBarUtils.statusBarLightMode(this)
        EventBus.getDefault().register(this)
        VersionController.saveEnterCount()
        if (savedInstanceState == null) {
            FileUtils.deleteFile(
                    File(FaceEnv.InternalPath.getInnerFilePath(FaceAppState.getContext(),
                            FaceEnv.InternalPath.BITMAP_CACHE_DIR)))
        }
        Duration.logDuration("Cool Start")
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putString(KEY_CURRENT_IMG_PATH, FaceFunctionManager.currentFaceImagePath)
        val keys = FaceFunctionManager.faceBeanMap.keys
        outState?.putStringArray(KEY_FACE_BEAN_MAP_KEYS, keys.toTypedArray())
        keys.forEach { key ->
            val faceBean = FaceFunctionManager.faceBeanMap[key]
            outState?.putParcelable(key, faceBean)
        }

        FaceFunctionManager.demoFaceImageInfo?.let {
            outState?.putParcelable(KEY_DEMO_FACE_IMAGE_INFO, it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        FileUtils.deleteFile(
                File(FaceEnv.InternalPath.getCacheInnerFilePath(FaceAppState.getContext(),
                        FaceEnv.InternalPath.PHOTO_CROP_DIR)))
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val fragment = findFragment(SplashFragment::class.java)
        fragment?.apply {
            onNewIntent()
        }
        PermissionHelper.resetCurrentRequest()
    }

    override fun onResume() {
        super.onResume()
        BuyChannelApiProxy.onResume()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProgressBarEvent(event: ProgressBarEvent) {
        val progressBar = progress_bar as GlobalProgressBar
        when (event.action) {
            ProgressBarEvent.EVENT_IS_SHOWN -> event.isShown = progressBar.visibility == View.VISIBLE
            ProgressBarEvent.EVENT_CANCEL_BY_USER -> {
                progressBar.hide(true)
                if (event.entrance == ENTRANCE_MAIN) {
                    val adBean = InnerAdController.instance.getPendingAdBean(
                            InnerAdController.FUNCTION_PAGE_EXIT_AD_MODULE_ID)
                    if (adBean != null && adBean.isInterstitialAd) {
                        adBean.showInterstitialAd()
                    }
                }
            }
            ProgressBarEvent.EVENT_HIDE -> progressBar.hide(false)
            ProgressBarEvent.EVENT_SHOW -> progressBar.show(event.entrance, event.showCancel, event.showSubTips)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCameraTransitionEvent(event: CameraTransitionEvent) {
        when (event.action) {
            CameraTransitionEvent.EVENT_SHOW -> {
                if (transition_layout?.visibility != View.VISIBLE) {
                    transition_layout?.visibility = View.VISIBLE
                    transition_layout?.alpha = 1f
                }
            }
            CameraTransitionEvent.EVENT_HIDE -> {
                if (transition_layout?.visibility == View.VISIBLE) {
                    transition_layout?.animate()?.alpha(0f)?.setDuration(300)
                            ?.setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator?) {
                                    transition_layout?.visibility = View.GONE
                                }
                            })?.start()
                }
            }
        }
    }
}