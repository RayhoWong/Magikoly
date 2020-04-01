package com.glt.magikoly.ad.view

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import com.google.android.gms.ads.formats.*

class AdmobNativeHandle {
    companion object {
        fun handleAdombNative(context: Context, native: NativeAd, adView: AbsAdView): ViewGroup {
            var newAdView: ViewGroup = adView
            val mediaView = MediaView(context)
            var nativeAdView: NativeAdView? = null
            when (native) {
                is NativeContentAd -> {
                    val title = native.headline
                    val body = native.body
                    nativeAdView = NativeContentAdView(context)
                    var bannerView = adView.mBannerView
                    bannerView.addView(mediaView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                    nativeAdView.addView(adView)
                    nativeAdView.headlineView = adView.mTitle
                    nativeAdView.bodyView = adView.mBody
                    nativeAdView.logoView = adView.mIconView
                    nativeAdView.callToActionView = adView.mButton
                    nativeAdView.mediaView = mediaView
                    nativeAdView.setNativeAd(native)
                    adView.mButton.text = native.callToAction
                    adView.mTitle.text = title
                    adView.setBodyContent(body)
                    newAdView = nativeAdView
                }
                is NativeAppInstallAd -> {
                    val title = native.headline
                    val iconImage = native.icon?.drawable
                    nativeAdView = NativeAppInstallAdView(context)
                    var bannerView = adView.mBannerView
                    bannerView.addView(mediaView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                    nativeAdView.addView(adView)
                    nativeAdView.headlineView = adView.mTitle
                    nativeAdView.bodyView = adView.mBody
                    nativeAdView.iconView = adView.mIconView
                    nativeAdView.callToActionView = adView.mButton
                    nativeAdView.starRatingView = adView.mRate
                    nativeAdView.priceView = adView.mPrice
                    nativeAdView.storeView = adView.mStore
                    nativeAdView.mediaView = mediaView
                    nativeAdView.setNativeAd(native)
                    adView.mButton.text = native.callToAction
                    adView.mTitle.text = title
                    adView.setInstallAppContent(native.starRating, native.price, native.store)
                    iconImage?.let { icon ->
                        adView.mIconView.apply {
                            visibility = View.VISIBLE
                            setImageDrawable(icon)
                        }
                    }
                    newAdView = nativeAdView
                }
            }
            adView.mChoiceView.visibility = View.INVISIBLE
//            adjustAdChoicePosition(nativeAdView, adView)
//            mediaView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
//                override fun onGlobalLayout() {
//                    try {
//                        var childAt = mediaView.getChildAt(0)
//                        val layoutParams = adView.mBannerView.layoutParams as ConstraintLayout.LayoutParams
//                        layoutParams.height = 0
//                        layoutParams.width = ConstraintLayout.LayoutParams.MATCH_PARENT
//                        layoutParams.dimensionRatio = "235:164"
//                        if (childAt is ImageView) {
//                            if (childAt.drawable.intrinsicWidth != 0 && childAt.drawable.intrinsicHeight != 0) {
//                                layoutParams.height = 0
//                                layoutParams.dimensionRatio = childAt.drawable.intrinsicWidth.toString() + ":" + childAt.drawable.intrinsicHeight.toString()
//                            }
//                            if (adView.entrance == AdEntrance.LAUNCHER) {
//                                adView.ad_background.setImageDrawable(childAt.drawable)
//                            }
//                        }
//
//                        adView.mBannerView.requestLayout()
//                        mediaView.viewTreeObserver.removeOnGlobalLayoutListener(this)
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
//
//            })
            return newAdView
        }

        /**
         * 不同通过方式设置，而是通过加载广告时设置
         *  val admobAdConfig = AdmobAdConfig(null)
        val builder = NativeAdOptions.Builder()
        builder.setAdChoicesPlacement（）
         */
        private fun adjustAdChoicePosition(nativeAdView: NativeAdView?, adView: AbsAdView) {
            nativeAdView?.let {
                it.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        try {
                            if (nativeAdView.childCount != 0) {
                                var childAt = nativeAdView.getChildAt(nativeAdView.childCount - 1) as ViewGroup
                                var choicesContainer = (childAt.getChildAt(0) as ViewGroup).getChildAt(0)
                                if (choicesContainer.y != adView.mChoiceView.y || choicesContainer.x != adView.mChoiceView.x) {
                                    choicesContainer.y = adView.mChoiceView.y
                                    choicesContainer.x = adView.mChoiceView.x
                                    adView.mChoiceView.visibility = View.INVISIBLE
                                } else {
                                    it.viewTreeObserver.removeOnGlobalLayoutListener(this)
                                }
                            }


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                })
            }
        }

    }

}