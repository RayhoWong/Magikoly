package com.glt.magikoly.function.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect

import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import magikoly.magiccamera.R

class FaceAutoFlowView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mScanImage: Drawable
    private var mImageWidth = 0
    private var mImageHeight = 0
    private var mBottomRect: Rect
    private var mTopRect: Rect
    private var mCurrentTranX = 0
    private var mValueAnimator: ValueAnimator

    init {
        val obtainStyledAttributes = context.obtainStyledAttributes(attrs, R.styleable.FaceAutoFlowView)
        mScanImage = obtainStyledAttributes.getDrawable(R.styleable.FaceAutoFlowView_flow_image)
        mImageWidth = mScanImage.intrinsicWidth
        mImageHeight = mScanImage.intrinsicHeight
        mBottomRect = Rect(0, 0, mImageWidth, mImageHeight)
        mTopRect = Rect(0, -mImageHeight, mImageWidth, 0)
        mValueAnimator = ValueAnimator.ofInt(0, mImageHeight)
        mValueAnimator.addUpdateListener {
            mCurrentTranX = it.animatedValue as Int
            invalidate()
        }
        mValueAnimator.interpolator = LinearInterpolator()
        mValueAnimator.repeatMode = ValueAnimator.RESTART
        mValueAnimator.repeatCount = ValueAnimator.INFINITE
        mValueAnimator.duration = 1500
        obtainStyledAttributes?.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
        if (widthMode == View.MeasureSpec.AT_MOST && heightMode == View.MeasureSpec.AT_MOST) {
            setMeasuredDimension(mImageWidth, mImageHeight)
        } else if (widthMode == View.MeasureSpec.AT_MOST) {
            setMeasuredDimension(mImageWidth, heightSize)
        } else if (heightMode == View.MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSize, mImageHeight)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null) {
            canvas.save()
            canvas.translate(0F, mCurrentTranX.toFloat())
            mScanImage.bounds = mTopRect
            mScanImage.draw(canvas)
            mScanImage.bounds = mBottomRect
            mScanImage.draw(canvas)
            canvas.restore()
        }
    }

    fun startScanAnim() {
        mValueAnimator.start()
    }

    fun stopScanAnim() {
        if (mValueAnimator.isRunning) {
            mValueAnimator.cancel()
        }
    }

}