package com.glt.magikoly.ad.view

import android.content.Context
import android.util.AttributeSet

class AdStyleThree @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                             defStyleAttr: Int = 0) : AbsAdView(context, attrs, defStyleAttr) {


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (mChoiceView.bottom > bottom) {
            mBannerView.layoutParams.height = mBannerView.measuredHeight - (mChoiceView.bottom - bottom)
            mBannerView.layoutParams.width = 0
            mBannerView.requestLayout()
        }
    }

}