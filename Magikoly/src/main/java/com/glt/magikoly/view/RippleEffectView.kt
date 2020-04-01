package com.glt.magikoly.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import magikoly.magiccamera.R

/**
 *
 * @author rayhahah
 * @blog http://rayhahah.com
 * @time 2019/9/5
 * @tips 这个类是Object的子类
 * @fuction
 */

class RippleEffectView : FrameLayout {
    companion object {
        val DEFAULT_RIPPLE_DURATION = 15000
        val DEFAULT_RIPPLE_COLOR = Color.parseColor("#26ffffff")
        val DEFAULT_RIPPLE_X = 0.5f
        val DEFAULT_RIPPLE_Y = 0.5f
        val DEFAULT_IS_ROUND = true
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RippleEffectView)
        val rippleColor = typedArray.getColor(
                R.styleable.RippleEffectView_rev_rippleColor,
                DEFAULT_RIPPLE_COLOR
        )
        val rippleDuration = typedArray.getInteger(
                R.styleable.RippleEffectView_rev_rippleDuration,
                DEFAULT_RIPPLE_DURATION
        )
        val ripplePercentX = typedArray.getFloat(
                R.styleable.RippleEffectView_rev_ripplePercentX,
                DEFAULT_RIPPLE_X
        )
        val ripplePercentY = typedArray.getFloat(
                R.styleable.RippleEffectView_rev_ripplePercentY,
                DEFAULT_RIPPLE_Y
        )
        val isRound = typedArray.getBoolean(
                R.styleable.RippleEffectView_rev_isRound,
                DEFAULT_IS_ROUND
        )

        mRippleColor = rippleColor
        mAnimDuration = rippleDuration
        mRipplePercentX = ripplePercentX
        mRipplePercentY = ripplePercentY

        mBgIsRound = isRound

        typedArray.recycle()
    }

    private var mBgIsRound: Boolean = DEFAULT_IS_ROUND

    private var mRipplePercentX: Float = DEFAULT_RIPPLE_X

    private var mRipplePercentY: Float = DEFAULT_RIPPLE_Y

    private var mRippleColor: Int = Color.parseColor("#26ffffff")
    private var mRipplePaint: Paint? = null

    private var mWidth: Int = 0
    private var mHeight: Int = 0

    private var mBgPath: Path = Path()

    private var mIsSizeChanged: Boolean = false

    private var mRippleX: Float = 0f
    private var mRippleY: Float = 0f
    private var mRippleRadius: Float = 0f
    private var mBgRadius: Float = 0f

    private var mValueAnimator: ValueAnimator? = null
    private var mCurrentProgress: Float = 0f
    private var mAnimDuration: Int = 1000

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
        mRipplePaint = Paint()
        mRipplePaint?.apply {
            strokeWidth = 10f
            color = mRippleColor
        }

        mRippleX = mWidth * mRipplePercentX
        mRippleY = mHeight * mRipplePercentY

        mRippleRadius = if (mWidth >= mHeight) {
            mWidth.toFloat()
        } else {
            mHeight.toFloat()
        }

        mBgRadius = if (mWidth >= mHeight) {
            mHeight.toFloat() / 2
        } else {
            mWidth.toFloat() / 2
        }

        mBgPath.addRoundRect(
                RectF(0f, 0f, mWidth.toFloat(), mHeight.toFloat())
                , mBgRadius, mBgRadius, Path.Direction.CW
        )
        mIsSizeChanged = true
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        canvas?.let { c ->
            c.save()
            if (mBgIsRound) {
                canvas.clipPath(mBgPath)
            }
            c.drawCircle(mRippleX, mRippleY, mRippleRadius * mCurrentProgress, mRipplePaint)
            c.restore()
        }
    }


    fun start() {
        if (mValueAnimator == null) {
            mValueAnimator = ValueAnimator.ofFloat(0f, 1f)
            mValueAnimator?.let {
                it.duration = mAnimDuration.toLong()
                it.interpolator = LinearInterpolator()
                it.repeatMode = ValueAnimator.RESTART
                it.repeatCount = ValueAnimator.INFINITE
                it.addUpdateListener {
                    mCurrentProgress = it.animatedValue as Float
                    invalidate()
                }
            }
        }
        mValueAnimator?.apply {
            if (!isRunning) {
                start()
            }
        }
    }

    fun stop() {
        mValueAnimator?.apply {
            if (isRunning) {
                stop()
            }
        }
    }

}