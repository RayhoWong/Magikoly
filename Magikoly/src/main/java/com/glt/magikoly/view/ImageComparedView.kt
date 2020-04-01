package com.glt.magikoly.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.support.annotation.DrawableRes
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.glt.magikoly.utils.DrawUtils
import magikoly.magiccamera.R

/**
 *
 * @author rayhahah
 * @blog http://rayhahah.com
 * @time 2019/8/29
 * @tips 这个类是Object的子类
 * @fuction
 */

class ImageComparedView : View {
    companion object {
        val DEFAULT_LINE_OFFSET = DrawUtils.dip2px(10f)
        val DEFAULT_CENTER_DRAWABLE_WIDTH = DrawUtils.dip2px(10f)
        val DEFAULT_CENTER_DRAWABLE_HEIGHT = DrawUtils.dip2px(10f)
        val DEFAULT_LINE_WIDTH = DrawUtils.dip2px(1f)
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ImageComparedView)
        val lineWidth = typedArray.getDimension(
                R.styleable.ImageComparedView_icv_lineWidth,
                DEFAULT_LINE_WIDTH.toFloat()
        )
        val lineColor = typedArray.getColor(
                R.styleable.ImageComparedView_icv_lineColor,
                Color.WHITE
        )
        val centerDrawable = typedArray.getDrawable(R.styleable.ImageComparedView_icv_centerDrawable)
        val centerDrawableWidth = typedArray.getDimension(
                R.styleable.ImageComparedView_icv_centerWidth,
                DEFAULT_CENTER_DRAWABLE_WIDTH.toFloat()
        )
        val centerDrawableHeight = typedArray.getDimension(
                R.styleable.ImageComparedView_icv_centerHeight,
                DEFAULT_CENTER_DRAWABLE_HEIGHT.toFloat()
        )
        centerDrawable?.apply {
            mCenterBitmap = (this as BitmapDrawable).bitmap
            mCenterBitmap?.let {
                mCenterBitmapWidth = centerDrawableWidth.toInt()
                mCenterBitmapHeight = centerDrawableHeight.toInt()

                mCenterBitmapRect = Rect(0, 0, it.width, it.height)
            }
        }

        mLineWidth = lineWidth.toInt()
        mLineColor = lineColor

        typedArray.recycle()
    }

    private var mTopBitmap: Bitmap? = null
    private var mTopBitmapRect: Rect? = null
    private var mTopBitmapRectDest: Rect? = null

    private var mBottomBitmap: Bitmap? = null
    private var mBottomBitmapRect: Rect? = null
    private var mBottomBitmapRectDest: Rect? = null

    private var mLineWidth: Int = DEFAULT_LINE_WIDTH
    private var mLineColor: Int = Color.WHITE
    private var mLinePaint: Paint? = null

    private var mCenterBitmap: Bitmap? = null
    private var mCenterBitmapWidth: Int = 0
    private var mCenterBitmapHeight: Int = 0
    private var mCenterBitmapRect: Rect? = null
    private var mCenterBitmapRectDest: Rect? = null

    private var mWidth: Int = 0
    private var mHeight: Int = 0

    private var mCurrentX: Int = 0

    private var mIsSizeChanged: Boolean = false

    private var mIsSetBitmap: Boolean = false

    private var mIsInit: Boolean = false

    private var mIsTouching: Boolean = false

    private var mTouchEnable: Boolean = true

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
        mCurrentX = mWidth / 2
        mLinePaint = Paint()
        mLinePaint?.apply {
            strokeWidth = mLineWidth.toFloat()
            color = mLineColor
        }
        mIsSizeChanged = true
        initBitmapRect()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!mIsInit) {
            return
        }
        mBottomBitmap?.apply {
            canvas.drawBitmap(mBottomBitmap, mBottomBitmapRect, mBottomBitmapRectDest, null)
            drawTop(canvas)
            drawLine(canvas)
            drawCenterBitmap(canvas)
        }
    }

    private fun drawTop(canvas: Canvas) {
        mTopBitmapRectDest?.apply {
            left = 0
            top = 0
            right = mCurrentX
            bottom = mHeight
        }
        mTopBitmapRect?.apply {
            left = 0
            top = 0
            right = mCurrentX
            bottom = mHeight
        }
        canvas.drawBitmap(mTopBitmap, mTopBitmapRect, mTopBitmapRectDest, null)
    }

    private fun drawLine(canvas: Canvas) {
        canvas.drawLine(
                mCurrentX.toFloat(), 0f,
                mCurrentX.toFloat(), mHeight.toFloat(),
                mLinePaint
        )
    }

    private fun drawCenterBitmap(canvas: Canvas) {
        mCenterBitmap?.apply {
            mCenterBitmapRectDest?.apply {
                left = mCurrentX - mCenterBitmapWidth / 2
                top = mHeight / 2 - mCenterBitmapHeight / 2
                right = mCurrentX + mCenterBitmapWidth / 2
                bottom = mHeight / 2 + mCenterBitmapHeight / 2
            }
            canvas.drawBitmap(this, mCenterBitmapRect, mCenterBitmapRectDest, null)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!mTouchEnable) {
            return true
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (touchDownInLine(event) || touchDownInCenterBitmap(event)) {
                    mIsTouching = true
                    parent.requestDisallowInterceptTouchEvent(true)
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (mIsTouching) {
                    mCurrentX = event.x.toInt()
                    invalidate()
                    parent.requestDisallowInterceptTouchEvent(true)
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                mIsTouching = false
                parent.requestDisallowInterceptTouchEvent(false)
                return true
            }
            else -> {
            }
        }
        return super.onTouchEvent(event)
    }

    private fun touchDownInCenterBitmap(event: MotionEvent): Boolean {
        mCenterBitmapRectDest?.let {
            if ((event.x >= it.left - DEFAULT_LINE_OFFSET
                            && event.x <= it.right + DEFAULT_LINE_OFFSET
                            && event.y >= it.top - DEFAULT_LINE_OFFSET
                            && event.y <= it.bottom + DEFAULT_LINE_OFFSET)
            ) {
                return true
            }
        }
        return false
    }

    private fun touchDownInLine(event: MotionEvent): Boolean {
        if (event.x >= mCurrentX - mLineWidth - DEFAULT_LINE_OFFSET
                && event.x <= mCurrentX + mLineWidth + DEFAULT_LINE_OFFSET
        ) {
            return true
        }
        return false
    }

    private fun initBitmapRect() {
        if (mIsSizeChanged && mIsSetBitmap) {
            if (mTopBitmap != null && mBottomBitmap != null) {
                mTopBitmapRect = Rect(0, 0, mCurrentX, mTopBitmap?.height!!)
                mTopBitmapRectDest = Rect(0, 0, mCurrentX, mHeight)

                mBottomBitmapRect = Rect(0, 0, mBottomBitmap?.width!!, mBottomBitmap?.height!!)
                mBottomBitmapRectDest = Rect(0, 0, mWidth, mHeight)

                mCenterBitmap?.apply {
                    mCenterBitmapRectDest = Rect(
                            mCurrentX - mCenterBitmapWidth / 2,
                            mHeight / 2 - mCenterBitmapHeight / 2,
                            mCurrentX + mCenterBitmapWidth / 2,
                            mHeight / 2 + mCenterBitmapHeight / 2
                    )
                }
                mIsInit = true
            }
        }
    }

    fun setBitmapRes(
            context: Context,
            @DrawableRes topBitmap: Int,
            @DrawableRes bottomBitmap: Int
    ) {
        setBitmap(
                (context.resources.getDrawable(topBitmap) as BitmapDrawable).bitmap,
                (context.resources.getDrawable(bottomBitmap) as BitmapDrawable).bitmap
        )
    }

    fun setBitmap(
            topBitmap: Bitmap,
            bottomBitmap: Bitmap
    ) {
        mIsInit = false
        mTopBitmap = topBitmap
        mBottomBitmap = bottomBitmap
        mIsSetBitmap = true
        initBitmapRect()
        invalidate()
    }

    /**
     * 设置是否可滑动
     */
    fun setTouchEnable(enable: Boolean) {
        mTouchEnable = enable
    }

    /**
     * 设置当前分割线的位置
     * @param percent: 0.0-1.0f
     */
    fun setCurrentLine(percent: Float) {
        mCurrentX = (mWidth * 1f * percent).toInt()
        invalidate()
    }

}