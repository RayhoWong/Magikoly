package com.glt.magikoly.ad.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.facebook.ads.MediaView
import com.glt.magikoly.subscribe.SubscribeController
import com.glt.magikoly.subscribe.SubscribeProxy
import com.glt.magikoly.subscribe.billing.BillingOrder
import com.glt.magikoly.subscribe.view.AbsSubscribeView
import com.google.android.gms.ads.formats.NativeAd
import magikoly.magiccamera.R

abstract class AbsAdView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                                   defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr), IAdView, View.OnClickListener, IAdViewCallBack, View.OnAttachStateChangeListener, SubscribeProxy.Listener {

    lateinit var mBannerView: FrameLayout

    lateinit var mButton: Button

    lateinit var mChoiceView: ImageView

    @AdEntrance
    var entrance: Int = -1

    var adViewCallBack: IAdViewCallBack? = null

    var adRootView: View? = null

    private var closedByUser = false


    /**
     * 有些布局没有icon，需创建不可见view
     */
    lateinit var mIconView: ImageView

    /**
     * 广告title
     */
    lateinit var mTitle: TextView

    /**
     * 广告body
     */
    var mBody: TextView? = null

    /**
     * 广告应用Layout
     */
    var mInstallAppLayout: ViewGroup? = null

    /**
     * 广告评分
     */
    var mRate: TextView? = null

    /**
     * 广告价格
     */
    var mPrice: TextView? = null

    /**
     * 广告商店
     */
    var mStore: TextView? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        initView()
        mBannerView.setOnClickListener(this)
        mButton.setOnClickListener(this)
        mChoiceView.setOnClickListener(this)
        mIconView.setOnClickListener(this)
        mTitle.setOnClickListener(this)
        mBody?.setOnClickListener(this)
        mInstallAppLayout?.setOnClickListener(this)
    }

    override fun initView() {
        mBannerView = findViewById(R.id.ad_banner)
        mButton = findViewById(R.id.ad_button)
        mChoiceView = findViewById(R.id.ad_choice_view)
        mIconView = findViewById(R.id.ad_icon)
        mTitle = findViewById(R.id.ad_title)
        mBody = findViewById(R.id.ad_body)
        mInstallAppLayout = findViewById(R.id.ad_install_app_layout)
        mRate = mInstallAppLayout?.findViewById(R.id.ad_rate)
        mPrice = mInstallAppLayout?.findViewById(R.id.ad_price)
        mStore = mInstallAppLayout?.findViewById(R.id.ad_store)
        SubscribeController.getInstance().addSubscribeListener(this)
    }

    /**
     * 由于Admob 的native广告必须是指定Native的布局，所以包裹了一层，返回admob的native布局
     */
    override fun bindData(data: Any): ViewGroup {
        var adView: ViewGroup = this
        if (data is NativeAd) {
            adView = AdmobNativeHandle.handleAdombNative(context, data, this)
        } else if (data is com.facebook.ads.NativeAd) {
            val mediaView = MediaView(context)
            mBannerView.addView(mediaView)
            mTitle.text = data.adHeadline
            mButton.text = data.adCallToAction
            setBodyContent(data.adBodyText)
            data.registerViewForInteraction(this, mediaView, arrayListOf(mBannerView, mIconView, mButton))
        }
        return adView
    }

    override fun onClick(v: View?) {
    }

    override fun setBodyContent(body: CharSequence) {
        mBody?.visibility = View.VISIBLE
        mBody?.text = body
    }

    override fun setInstallAppContent(rate: Double, price: CharSequence, store: CharSequence) {
        mInstallAppLayout?.visibility = View.VISIBLE
        mRate?.text = rate.toString()
        mPrice?.text = price
        mStore?.text = store
        if ("Google Play" == store) {
            mStore?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_store_gp, 0, 0, 0)
        } else {
            mStore?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_store_default, 0, 0, 0)
        }
    }

    fun attachRootView(wrapAdView: ViewGroup) {
        adRootView?.removeOnAttachStateChangeListener(this)
        adRootView = wrapAdView
        adRootView?.addOnAttachStateChangeListener(this)
    }

    fun tryCloseAdView(closeFromUser: Boolean) {
        adRootView?.let { view ->
            view.parent?.let {
                this.closedByUser = closeFromUser
                (it as ViewGroup).removeView(adRootView)
            }
        }
    }

    override fun onViewAttachedToWindow(v: View?) {
        onShow()
        SubscribeController.getInstance().addSubscribeListener(this)
    }

    override fun onViewDetachedFromWindow(v: View?) {
        onClose(closedByUser)
        SubscribeController.getInstance().removeSubscribeListener(this)
    }

    override fun onClose(closedByUser: Boolean) {
        adViewCallBack?.onClose(closedByUser)
    }

    override fun onShow() {
        adViewCallBack?.onShow()
    }

    override fun onDismiss(absSubscribeView: AbsSubscribeView) {
    }

    override fun onPurchaseSuccess(billingOrder: BillingOrder) {
        tryCloseAdView(false)
    }

    override fun onShow(sShow: Boolean, scene: Int) {
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean = true

}