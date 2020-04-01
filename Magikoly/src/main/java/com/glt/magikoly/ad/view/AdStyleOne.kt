package com.glt.magikoly.ad.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.glt.magikoly.view.CountDownView
import magikoly.magiccamera.R

class AdStyleOne @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                           defStyleAttr: Int = 0) : AbsAdView(context, attrs, defStyleAttr) {

    private val mCountDownView by lazy { findViewById<CountDownView>(R.id.ad_time_counter) }
    private val mSkip by lazy { findViewById<CountDownView>(R.id.ad_skip) }
    override fun initView() {
        super.initView()
        mSkip.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ad_skip -> {
                mCountDownView.cancelCountDown()
                tryCloseAdView(true)
            }
            else -> super.onClick(v)
        }
    }

    override fun onShow() {
        super.onShow()
        mCountDownView.startCountDown(5) {
            tryCloseAdView(false)
        }
    }

}