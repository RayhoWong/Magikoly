package com.glt.magikoly.ad.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import magikoly.magiccamera.R

object AdViewFactory {

    fun createAdViewAndBind(context: Context, @AdEntrance entrance: Int, data: Any, adViewCallBack: IAdViewCallBack?): ViewGroup {
        val absAdView = createAdView(context, entrance)
        absAdView.entrance = entrance
        absAdView.adViewCallBack = adViewCallBack
        var wrapAdView = wrapAdViewAndBind(context, absAdView, data, entrance)
        absAdView.attachRootView(wrapAdView)
        return wrapAdView
    }

    private fun createAdView(context: Context, @AdEntrance from: Int): AbsAdView {
        return when (from) {
            AdEntrance.LAUNCHER -> LayoutInflater.from(context).inflate(R.layout.ad_style_one_layout, null) as AdStyleOne
            AdEntrance.LOADING_PAGE -> LayoutInflater.from(context).inflate(R.layout.ad_style_three_layout, null) as AdStyleThree
            else -> LayoutInflater.from(context).inflate(R.layout.ad_style_second_layout, null) as AdStyleThree
        }
    }

    /**
     * 返回的是根view
     */
    private fun wrapAdViewAndBind(context: Context, adView: AbsAdView, data: Any, entrance: Int): ViewGroup {
        var newAdView = adView.bindData(data)
        when (entrance) {
            AdEntrance.LAUNCHER -> {
                val newAdViewContainer = LinearLayout(context)
                newAdViewContainer.orientation = LinearLayout.VERTICAL
                val param = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0)
                param.weight = 1f
                newAdViewContainer.addView(newAdView, param)
                LayoutInflater.from(context).inflate(R.layout.include_ad_about_app, newAdViewContainer)
                newAdView = newAdViewContainer
            }
            AdEntrance.LOADING_PAGE -> {
                val newAdViewContainer = RelativeLayout(context)
                newAdViewContainer.clipChildren = false
                newAdView.id = R.id.admob_native_id_container
                newAdViewContainer.addView(newAdView)
//                val close = ImageView(context)
//                close.id = R.id.admob_native_ad_close
//                close.setOnClickListener {
//                    adView.tryCloseAdView(true)
//                }
//                var drawable = context.resources.getDrawable(R.drawable.ad_icon_close)
//                close.setImageDrawable(drawable)
//                var layoutParams = RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//                layoutParams.addRule(RelativeLayout.ALIGN_RIGHT, newAdView.id)
//                layoutParams.addRule(RelativeLayout.ABOVE, newAdView.id)
//                layoutParams.bottomMargin =  DrawUtils.dip2px(8f)
//                layoutParams.rightMargin = DrawUtils.dip2px(8f)
//                newAdViewContainer.addView(close, layoutParams)
                newAdView = newAdViewContainer
            }
        }
        return newAdView
    }

}