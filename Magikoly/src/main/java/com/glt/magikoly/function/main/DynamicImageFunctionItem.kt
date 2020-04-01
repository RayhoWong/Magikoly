package com.glt.magikoly.function.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable

class DynamicImageFunctionItem(context: Context) : StaticImageFunctionItem(context) {
    var avatar2: Drawable? = null
    private var avatarAlpha = 1.0f
    private var avatarAlpha2 = 0.0f
    //    private val alphaAnimator = ValueAnimator.ofFloat(1.0f, 0.0f, 1.0f)

    private lateinit var alphaAnimator: ValueAnimator

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (avatar != null) {
            avatar2?.bounds = avatar?.bounds
        }
    }

    //    fun startAnim() {
    //        if (alphaAnimator.isRunning) {
    //            alphaAnimator.cancel()
    //        }
    //        alphaAnimator.duration = 3000
    //        alphaAnimator.repeatCount = Animation.INFINITE
    //        alphaAnimator.addUpdateListener {
    //            avatarAlpha = it.animatedValue as Float
    //            avatarAlpha2 = 1.0f - avatarAlpha
    //            invalidate()
    //        }
    //        alphaAnimator.start()
    //    }

    fun startAnim(reverse: Boolean) {
        alphaAnimator = if (reverse) {
            ValueAnimator.ofFloat(1.0f, 0.0f)
        } else {
            ValueAnimator.ofFloat(0.0f, 1.0f)
        }
        alphaAnimator.duration = 1500
        alphaAnimator.startDelay = 2000
        alphaAnimator.addUpdateListener {
            avatarAlpha = it.animatedValue as Float
            avatarAlpha2 = 1.0f - avatarAlpha
            invalidate()
        }
        alphaAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animator: Animator?) {
                post {
                    startAnim(!reverse)
                }
            }
        })
        alphaAnimator.start()
    }

    override fun drawAvatar(canvas: Canvas) {
        avatar?.alpha = (avatarAlpha * 255).toInt()
        avatar?.draw(canvas)
        avatar2?.alpha = (avatarAlpha2 * 255).toInt()
        avatar2?.draw(canvas)
    }
}