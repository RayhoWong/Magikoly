package com.glt.magikoly.function.main.faceimages

import android.animation.ValueAnimator
import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import com.glt.magikoly.event.ImageScanStateEvent
import com.glt.magikoly.ext.postEvent
import kotlinx.android.synthetic.main.main_face_images_item_loading_view.view.*

class LoadingView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private var loadingAnimation = ValueAnimator.ofFloat(0f, 360f)

    private val mHandler = Handler()

    private fun startRotateAnimation() {
        if (!loadingAnimation.isRunning) {
            loadingAnimation.apply {
                duration = 1000
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
                addUpdateListener {
                    img_progressbar.rotation = it.animatedValue as Float
                    img_progressbar.invalidate()
                }
                start()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startRotateAnimation()
        mHandler.removeCallbacksAndMessages(null)
        mHandler.postDelayed({
            postEvent(ImageScanStateEvent(true))
        }, 600L)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        loadingAnimation.cancel()
        mHandler.removeCallbacksAndMessages(null)
        mHandler.postDelayed({
            postEvent(ImageScanStateEvent(false))
        }, 600L)
    }
}