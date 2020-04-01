package com.glt.magikoly.view

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.glt.magikoly.BuyChannelApiProxy
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.ad.inner.InnerAdController
import com.glt.magikoly.ad.inner.InnerAdController.Companion.LOADING_PAGE_BOTTOM_AD_MODULE_ID
import com.glt.magikoly.ad.view.AdEntrance
import com.glt.magikoly.ad.view.AdViewFactory
import com.glt.magikoly.ad.view.IAdViewCallBack
import com.glt.magikoly.apng.PngUtils
import com.glt.magikoly.blur.BlurLayout
import com.glt.magikoly.event.AdLoadEvent
import com.glt.magikoly.event.BaseEvent
import com.glt.magikoly.event.UserStatusEvent
import com.glt.magikoly.ext.postEvent
import com.glt.magikoly.ext.registerEventObserver
import com.glt.magikoly.ext.unregisterEventObserver
import com.glt.magikoly.function.FaceFunctionManager
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.subscribe.SubscribeController
import com.glt.magikoly.subscribe.SubscribeProxy
import com.glt.magikoly.subscribe.SubscribeScene
import com.glt.magikoly.subscribe.billing.BillingStatusManager
import com.glt.magikoly.utils.DrawUtils
import com.glt.magikoly.utils.Logcat
import com.glt.magikoly.view.ProgressBarEvent.Companion.EVENT_CANCEL_BY_USER
import com.google.android.gms.ads.AdListener
import kotlinx.android.synthetic.main.global_progress_bar.view.*
import magikoly.magiccamera.R
import net.ellerton.japng.error.PngException
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException
import kotlin.math.roundToLong

class GlobalProgressBar(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    companion object {
        fun isShown(): Boolean {
            val event = ProgressBarEvent(ProgressBarEvent.EVENT_IS_SHOWN, "-1")
            postEvent(event)
            return event.isShown
        }

        fun show(entrance: String, showCancel: Boolean = true, showSubTips: Boolean = false) {
            postEvent(ProgressBarEvent(ProgressBarEvent.EVENT_SHOW, entrance, showCancel, showSubTips))
        }

        fun hide() {
            postEvent(ProgressBarEvent(ProgressBarEvent.EVENT_HIDE, "-1"))
        }
    }

    var blurLayout: BlurLayout? = null

    private var entrance = "-1"
    private var showSubTips = false
    private var startTime = -1L
    private var pngDrawable: Drawable? = null
    private var adClose: View? = null
    private var needShowSubscribeAfterAdClosed = false
    private var adBean: InnerAdController.AdBean? = null

    private val progressBar: PointProgressBar by lazy {
        findViewById<PointProgressBar>(R.id.global_progress_bar)
    }

    private val imageView: ImageView by lazy {
        findViewById<ImageView>(R.id.global_progress_view)
    }

    private val loadingTextView: TextView by lazy {
        findViewById<TextView>(R.id.loading_text)
    }

    private val loadingTextSecondView: TextView by lazy {
        findViewById<TextView>(R.id.loading_text_under)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        img_back.setOnClickListener {
            postEvent(ProgressBarEvent(EVENT_CANCEL_BY_USER, entrance))
        }
        setOnTouchListener { _, event ->
            adClose?.let {
                val intArray = IntArray(2)
                it.getLocationInWindow(intArray)
                val rect = Rect(intArray[0], intArray[1], intArray[0] + it.width,
                        intArray[1] + it.height)
                val result = rect.contains(event.x.toInt(), event.y.toInt())
                if (result && event.action == MotionEvent.ACTION_UP) {
                    it.performClick()
                }
            }
            true
        }
    }

    override fun onVisibilityChanged(changedView: View?, visibility: Int) {
        if (changedView == this) {
            if (visibility == View.VISIBLE) {
                blurLayout?.startBlur()
                post {
                    when (this.entrance) {
                        Statistic103Constant.ENTRANCE_MAIN, Statistic103Constant.ENTRANCE_ETHNICITY,
                        Statistic103Constant.ENTRANCE_AGING, Statistic103Constant.ENTRANCE_CHILD,
                        Statistic103Constant.ENTRANCE_GENDER, Statistic103Constant.ENTRANCE_BABY,
                        Statistic103Constant.ENTRANCE_ART_FILTER, Statistic103Constant.ENTRANCE_TURNING,
                        Statistic103Constant.ENTRANCE_ANIMAL -> {
                            try {
                                progressBar.visibility = View.INVISIBLE
                                imageView.visibility = View.VISIBLE
                                pngDrawable?.apply {
                                    if (this is AnimationDrawable) {
                                        (pngDrawable as AnimationDrawable).stop()
                                    }
                                }
                                var ent = entrance
                                when (ent) {
                                    Statistic103Constant.ENTRANCE_MAIN -> {
                                        val campaign = BuyChannelApiProxy.getCampaign()
                                        if (campaign != null) {
                                            ent = when {
                                                campaign.contains("child", true) -> {
                                                    Statistic103Constant.ENTRANCE_CHILD
                                                }
                                                campaign.contains("baby", true) -> {
                                                    Statistic103Constant.ENTRANCE_BABY
                                                }
                                                campaign.contains("gen", true) -> {
                                                    Statistic103Constant.ENTRANCE_GENDER
                                                }
                                                campaign.contains("cart", true) -> {
                                                    Statistic103Constant.ENTRANCE_ART_FILTER
                                                }
                                                campaign.contains("animal", true) -> {
                                                    Statistic103Constant.ENTRANCE_ANIMAL
                                                }
                                                else -> {
                                                    Statistic103Constant.ENTRANCE_AGING
                                                }
                                            }
                                        } else {
                                            ent = Statistic103Constant.ENTRANCE_AGING
                                        }
                                    }
                                }
                                when (ent) {
                                    Statistic103Constant.ENTRANCE_AGING -> {
                                        pngDrawable = PngUtils.readDrawable(context,
                                                context.assets.open("apng/loading_aging.png"))
                                        loadingTextView.setText(R.string.loading_for_aging_tip)

                                        if (showSubTips) {
                                            loadingTextSecondView.visibility = View.VISIBLE
                                        } else {
                                            loadingTextSecondView.visibility = View.GONE
                                        }
                                    }
                                    Statistic103Constant.ENTRANCE_CHILD -> {
                                        pngDrawable = PngUtils.readDrawable(context,
                                                context.assets.open("apng/loading_child.png"))
                                        loadingTextView.setText(R.string.loading_for_child_tip)
                                        loadingTextSecondView.visibility = View.GONE
                                    }
                                    Statistic103Constant.ENTRANCE_ETHNICITY -> {
                                        pngDrawable = PngUtils.readDrawable(context,
                                                context.assets.open("apng/loading_ethnicity.png"))
                                        loadingTextView.setText(R.string.loading_for_ethnicity_tip)
                                        if (showSubTips) {
                                            loadingTextSecondView.visibility = View.VISIBLE
                                        } else {
                                            loadingTextSecondView.visibility = View.GONE
                                        }
                                    }
                                    Statistic103Constant.ENTRANCE_GENDER -> {
                                        pngDrawable = PngUtils.readDrawable(context,
                                                context.assets.open("apng/loading_gender.png"))
                                        loadingTextView.setText(R.string.loading_for_gender_tip)
                                        if (showSubTips) {
                                            loadingTextSecondView.visibility = View.VISIBLE
                                        } else {
                                            loadingTextSecondView.visibility = View.GONE
                                        }
                                    }
                                    Statistic103Constant.ENTRANCE_BABY -> {
                                        pngDrawable = PngUtils.readDrawable(context,
                                                context.assets.open("apng/loading_baby.png"))
                                        loadingTextView.setText(R.string.loading_for_baby_tip)
                                        if (showSubTips) {
                                            loadingTextSecondView.visibility = View.VISIBLE
                                        } else {
                                            loadingTextSecondView.visibility = View.GONE
                                        }
                                    }
                                    Statistic103Constant.ENTRANCE_TURNING -> {
                                        pngDrawable = PngUtils.readDrawable(context,
                                                context.assets.open("apng/loading_beauty.png"))
                                        loadingTextView.setText(R.string.loading_for_beauty_tip)
                                        if (showSubTips) {
                                            loadingTextSecondView.visibility = View.VISIBLE
                                        } else {
                                            loadingTextSecondView.visibility = View.GONE
                                        }
                                    }
                                    Statistic103Constant.ENTRANCE_ART_FILTER -> {
                                        pngDrawable = PngUtils.readDrawable(context,
                                                context.assets.open("apng/loading_art_filter.png"))
                                        loadingTextView.setText(R.string.loading_for_art_filter_tip)
                                        if (showSubTips) {
                                            loadingTextSecondView.visibility = View.VISIBLE
                                        } else {
                                            loadingTextSecondView.visibility = View.GONE
                                        }
                                    }
                                    Statistic103Constant.ENTRANCE_ANIMAL -> {
                                        pngDrawable = PngUtils.readDrawable(context,
                                                context.assets.open("apng/loading_animal.png"))
                                        loadingTextView.setText(R.string.loading_for_animal_tip)
                                        if (showSubTips) {
                                            loadingTextSecondView.visibility = View.VISIBLE
                                        } else {
                                            loadingTextSecondView.visibility = View.GONE
                                        }
                                    }
                                    Statistic103Constant.ENTRANCE_SAVE_VIDEO -> {
                                        pngDrawable = PngUtils.readDrawable(context,
                                                context.assets.open("apng/loading_animal.png"))
                                        loadingTextView.setText(R.string.exporting_video)
                                        if (showSubTips) {
                                            loadingTextSecondView.visibility = View.VISIBLE
                                        } else {
                                            loadingTextSecondView.visibility = View.GONE
                                        }
                                    }
                                    Statistic103Constant.ENTRANCE_SAVE_GIF -> {
                                        pngDrawable = PngUtils.readDrawable(context,
                                                context.assets.open("apng/loading_animal.png"))
                                        loadingTextView.setText(R.string.exporting_gif)
                                        if (showSubTips) {
                                            loadingTextSecondView.visibility = View.VISIBLE
                                        } else {
                                            loadingTextSecondView.visibility = View.GONE
                                        }
                                    }
                                    else -> {
                                        pngDrawable = PngUtils.readDrawable(context,
                                                context.assets.open("apng/loading_aging.png"))
                                        loadingTextView.setText(R.string.loading_for_aging_tip)
                                        loadingTextSecondView.visibility = View.GONE
                                    }
                                }

                                imageView.setImageDrawable(pngDrawable)
                                if (pngDrawable is AnimationDrawable) {
                                    (pngDrawable as AnimationDrawable).start()
                                }
                            } catch (pE: PngException) {
                                pE.printStackTrace()
                            } catch (pE: IOException) {
                                pE.printStackTrace()
                            }
                        }
                        else -> {
                            progressBar.visibility = View.VISIBLE
                            imageView.visibility = View.INVISIBLE
                            loadingTextView.setText(R.string.loading)
                            progressBar.startAnim()
                        }
                    }

                }
            } else {
                blurLayout?.stopBlur()
                post {
                    progressBar.stopAnim()
                    if (pngDrawable is AnimationDrawable) {
                        (pngDrawable as AnimationDrawable).stop()
                        pngDrawable = null
                    }
                }
            }
        }
    }

    fun show(entrance: String, showCancel: Boolean, showSubTips: Boolean) {
        if (this.entrance != "-1" && this.entrance != entrance && this.startTime != -1L) {
            //如果加载页已经展示，且与上次的事务不同，则先对上次的事务进行统计
            val duration = ((System.currentTimeMillis() - startTime) / 1000.toDouble()).roundToLong()
            BaseSeq103OperationStatistic.uploadSqe103StatisticData(duration.toString(),
                    Statistic103Constant.LOADING_PAGE_ENTER, this.entrance, "1", "1")
        }
        if (visibility != View.VISIBLE) {
            registerEventObserver(this)
            visibility = View.VISIBLE
        }
        img_back.visibility = if (showCancel) {
            View.VISIBLE
        } else {
            View.GONE
        }
        this.entrance = entrance
        this.showSubTips = showSubTips
        this.startTime = System.currentTimeMillis()

        adClose = null
        if (FaceFunctionManager.demoFaceImageInfo == null) {
            InnerAdController.instance.getPendingAdBean(LOADING_PAGE_BOTTOM_AD_MODULE_ID)?.let {
                addAdView(it)
            }

        }
    }

    fun hide(canceledByUser: Boolean) {
        unregisterEventObserver(this)
        visibility = View.INVISIBLE
        if (this.entrance != "-1" && startTime != -1L) {
            val duration = ((System.currentTimeMillis() - startTime) / 1000.toDouble()).roundToLong()
            BaseSeq103OperationStatistic.uploadSqe103StatisticData(duration.toString(),
                    Statistic103Constant.LOADING_PAGE_ENTER, this.entrance,
                    if (canceledByUser) "2" else "1", "1")
        }
        this.entrance = "-1"
        this.showSubTips = false
        startTime = -1L
        removeAdView()
    }

    private fun addAdView(adBean: InnerAdController.AdBean) {
//        needShowSubscribeAfterAdClosed = true
//        this.adBean = adBean
//        var adNative: Any? = null
//        if (adBean.admodNatived != null) {
//            adNative = adBean.admodNatived
//        } else if (adBean.fbNativeAd != null) {
//            adNative = adBean.fbNativeAd
//        }
//        adNative?.let { native ->
//            banner_ad_container?.let {
//                it.visibility = View.VISIBLE
//                it.translationY = DrawUtils.dip2px(10f).toFloat()
//                it.alpha = 0f
//                it.animate().translationY(0f).alpha(1f).setDuration(300).start()
//            }
//            banner_ad_content_container?.let { adContentContainer ->
//                adContentContainer.removeAllViews()
//                val adView = AdViewFactory.createAdViewAndBind(adContentContainer.context,
//                        AdEntrance.LOADING_PAGE, native, object : IAdViewCallBack {
//                    override fun onShow() {
//                        adBean.onAdShowed()
//                    }
//
//                    override fun onClose(closedByUser: Boolean) {
//                        adContentContainer.removeAllViews()
//                        banner_ad_container?.visibility = View.GONE
//                        adBean.onAdClosed()
//                        if (closedByUser && needShowSubscribeAfterAdClosed &&
//                                !SubscribeController.getInstance().isVIP() &&
//                                SubscribeProxy.getInstance().hasAutoShownSubscribe()) {
//                            FaceAppState.getMainActivity()?.let { activity ->
//                                SubscribeController.getInstance()
//                                        .launch(activity, SubscribeScene.FREE_AD,
//                                                object : SubscribeProxy.BaseListener() {})
//                            }
//                        }
//                    }
//                })
//                adContentContainer.addView(adView,
//                        FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
//                                FrameLayout.LayoutParams.WRAP_CONTENT))
//            }
//        }
//
//        adBean.moPubViewWrapper?.let { adView ->
//            banner_ad_container?.let {
//                it.visibility = View.VISIBLE
//                it.translationY = DrawUtils.dip2px(10f).toFloat()
//                it.alpha = 0f
//                it.animate().translationY(0f).alpha(1f).setDuration(300).start()
//            }
//            banner_ad_content_container?.let { adContentContainer ->
//                adContentContainer.removeAllViews()
//                adContentContainer.addView(adView,
//                        FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
//                                FrameLayout.LayoutParams.WRAP_CONTENT))
//                adBean.adListener = object : InnerAdController.AdLoadListenerAdapter() {
//                    override fun onAdClicked() {
//                        adView.forceRefresh()
//                    }
//                }
//                adView.setAutoRefreshAvailable(true)
//                adClose = banner_ad_btn_close
//                banner_ad_btn_close?.setOnClickListener {
//                    adClose = null
//                    adContentContainer.removeAllViews()
//                    banner_ad_container?.visibility = View.GONE
//                    adBean.onAdClosed()
//                    if (needShowSubscribeAfterAdClosed && !SubscribeController.getInstance().isVIP()
//                            && SubscribeProxy.getInstance().hasAutoShownSubscribe()) {
//                        FaceAppState.getMainActivity()?.let { activity ->
//                            SubscribeController.getInstance()
//                                    .launch(activity, SubscribeScene.FREE_AD,
//                                            object : SubscribeProxy.BaseListener() {})
//                        }
//                    }
//                    Logcat.i("InnerAdController", "MoPubView closed")
//                }
//                adBean.onAdShowed()
//                Logcat.i("InnerAdController", "MoPubView showed")
//            }
//        }
//
//        adBean.admobAdView?.let { adView ->
//            banner_ad_container?.let {
//                it.visibility = View.VISIBLE
//                it.translationY = DrawUtils.dip2px(10f).toFloat()
//                it.alpha = 0f
//                it.animate().translationY(0f).alpha(1f).setDuration(300).start()
//            }
//            banner_ad_content_container?.let { adContentContainer ->
//                adContentContainer.removeAllViews()
//                adContentContainer.addView(adView,
//                        FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
//                                FrameLayout.LayoutParams.WRAP_CONTENT))
//                adView.adListener = object : AdListener() {
//                    override fun onAdLeftApplication() {
//                        adBean.onAdClicked()
//                        Logcat.i("InnerAdController", "admobAdView clicked")
//                    }
//                }
//                adBean.adListener = object : InnerAdController.AdLoadListenerAdapter() {
//                    override fun onAdClicked() {
//                        adContentContainer.removeAllViews()
//                        banner_ad_container?.visibility = View.GONE
//                        adBean.onAdClosed()
//                    }
//                }
//                adClose = banner_ad_btn_close
//                banner_ad_btn_close?.setOnClickListener {
//                    adClose = null
//                    adContentContainer.removeAllViews()
//                    banner_ad_container?.visibility = View.GONE
//                    adBean.onAdClosed()
//                    Logcat.i("InnerAdController", "admobAdView closed")
//                    if (needShowSubscribeAfterAdClosed && !SubscribeController.getInstance().isVIP()
//                            && SubscribeProxy.getInstance().hasAutoShownSubscribe()) {
//                        FaceAppState.getMainActivity()?.let { activity ->
//                            SubscribeController.getInstance()
//                                    .launch(activity, SubscribeScene.FREE_AD,
//                                            object : SubscribeProxy.BaseListener() {})
//                        }
//                    }
//                }
//                adBean.onAdShowed()
//                Logcat.i("InnerAdController", "admobAdView showed")
//            }
//        }
    }

    private fun removeAdView() {
        needShowSubscribeAfterAdClosed = false
        if (adClose == null) {
            banner_ad_content_container.removeAllViews()
            banner_ad_container?.visibility = View.GONE
            adBean?.onAdClosed()
        } else {
            adClose?.performClick()
        }
        adBean = null
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAdLoadEvent(event: AdLoadEvent) {
        if (event.adBean.moduleId == LOADING_PAGE_BOTTOM_AD_MODULE_ID)
            if (!SubscribeController.getInstance().isVIP()) {
                if (FaceFunctionManager.demoFaceImageInfo == null) {
                    event.adBean.let {
                        addAdView(it)
                    }
                }
            }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUserStatusEvent(event: UserStatusEvent) {
        if (event.status == BillingStatusManager.STATUS_VIP) {
            removeAdView()
        }
    }
}

class ProgressBarEvent(val action: Int, var entrance: String, var showCancel: Boolean = true, var showSubTips: Boolean = false) : BaseEvent() {

    companion object {
        const val EVENT_IS_SHOWN = 0
        const val EVENT_SHOW = 1
        const val EVENT_HIDE = 2
        const val EVENT_CANCEL_BY_USER = 3
    }

    var isShown = false
}