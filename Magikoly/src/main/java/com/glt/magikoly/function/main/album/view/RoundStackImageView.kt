package com.glt.magikoly.function.main.album.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import com.glt.magikoly.utils.DrawUtils
import magikoly.magiccamera.R

/**
 * @desc: 四个圆角自定义，边缘有层叠效果的ImageView
 * @auther:duwei
 * @date:2019/2/13
 */
class RoundStackImageView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    companion object {
        const val DEFAULT_RADIUS = 0
    }

    var mRadius = DEFAULT_RADIUS
    private var mRadiusTopLeft: Int
    private var mRadiusTopRight: Int
    private var mRadiusBottomLeft: Int
    private var mRadiusBottomRight: Int
    private var mPath = Path()

    private var mRadiusWidthTotal: Int = 0
    private var mRadiusHeightTotal: Int = 0


    init {
        var typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundStackImageView)
        mRadius = typedArray.getDimensionPixelOffset(R.styleable.RoundStackImageView_radius_all, DEFAULT_RADIUS)
        mRadiusTopLeft =
            typedArray.getDimensionPixelOffset(R.styleable.RoundStackImageView_radius_leftTop, DEFAULT_RADIUS)
        mRadiusTopRight =
            typedArray.getDimensionPixelOffset(R.styleable.RoundStackImageView_radius_rightTop, DEFAULT_RADIUS)
        mRadiusBottomLeft =
            typedArray.getDimensionPixelOffset(R.styleable.RoundStackImageView_radius_leftBottom, DEFAULT_RADIUS)
        mRadiusBottomRight =
            typedArray.getDimensionPixelOffset(R.styleable.RoundStackImageView_radius_rightBottom, DEFAULT_RADIUS)

        if (mRadiusTopLeft == DEFAULT_RADIUS) {
            mRadiusTopLeft = mRadius
        }
        if (mRadiusTopRight == DEFAULT_RADIUS) {
            mRadiusTopRight = mRadius
        }
        if (mRadiusBottomLeft == DEFAULT_RADIUS) {
            mRadiusBottomLeft = mRadius
        }
        if (mRadiusBottomRight == DEFAULT_RADIUS) {
            mRadiusBottomRight = mRadius
        }
        typedArray.recycle()

        var maxLeftWidth = Math.max(mRadiusTopLeft, mRadiusBottomLeft)
        var maxRightWidth = Math.max(mRadiusBottomRight, mRadiusTopRight)
        mRadiusWidthTotal = maxLeftWidth + maxRightWidth

        var maxTopHeight = Math.max(mRadiusTopLeft, mRadiusTopRight)
        var maxBottomHeight = Math.max(mRadiusBottomRight, mRadiusBottomLeft)
        mRadiusHeightTotal = maxTopHeight + maxBottomHeight
    }

    override fun onDraw(canvas: Canvas?) {
        if (drawable == null) return
        if (measuredWidth >= mRadiusWidthTotal && measuredHeight > mRadiusHeightTotal) {
            mPath.reset()

            mPath.moveTo(mRadiusTopLeft.toFloat(), 0f)
            mPath.lineTo((measuredWidth - mRadiusTopRight).toFloat(), 0f)
            mPath.quadTo(measuredWidth.toFloat(), 0f, measuredWidth.toFloat(), mRadiusTopRight.toFloat())

            mPath.lineTo(measuredWidth.toFloat(), (measuredHeight - mRadiusBottomRight).toFloat())
            mPath.quadTo(measuredWidth.toFloat(), measuredHeight.toFloat(),
                (measuredWidth - mRadiusBottomRight).toFloat(), measuredHeight.toFloat())

            mPath.lineTo(mRadiusBottomLeft.toFloat(), measuredHeight.toFloat())
            mPath.quadTo(0f, measuredHeight.toFloat(), 0f, (measuredHeight - mRadiusBottomLeft).toFloat())

            mPath.lineTo(0f, mRadiusTopLeft.toFloat())
            mPath.quadTo(0f, 0f, mRadiusTopLeft.toFloat(), 0f)

            canvas?.clipPath(mPath)
        }

//        super.onDraw(canvas)
        canvas?.drawColor(Color.TRANSPARENT)

        canvas?.save()
        canvas?.scale(1f,0.65f, (measuredWidth/2).toFloat(), (measuredHeight/2).toFloat())
        drawable.alpha = 100
        super.onDraw(canvas)
        canvas?.restore()

        canvas?.save()
        canvas?.translate(-DrawUtils.dip2px(3f).toFloat(),0f)
        canvas?.scale(1f,0.85f, (measuredWidth/2).toFloat(), (measuredHeight/2).toFloat())
        drawable.alpha = 175
        super.onDraw(canvas)
        canvas?.restore()

        canvas?.save()
        canvas?.translate(-DrawUtils.dip2px(6f).toFloat(),0f)
        drawable.alpha = 255
        super.onDraw(canvas)
        canvas?.restore()

    }

}