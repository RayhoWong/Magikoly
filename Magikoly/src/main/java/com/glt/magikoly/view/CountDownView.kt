package com.glt.magikoly.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.TextView
import com.glt.magikoly.utils.DrawUtils


class CountDownView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                              defStyleAttr: Int = 0) : TextView(context, attrs, defStyleAttr) {

    private val mCirclePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mPercent = 0f
    private val mOval = RectF()
    private val mCirclePath = Path()
    private var timeCount: ValueAnimator? = null

    init {
        mCirclePaint.color = Color.WHITE
        mCirclePaint.strokeJoin = Paint.Join.ROUND
        mCirclePaint.strokeCap = Paint.Cap.ROUND
        mCirclePaint.isDither = true
        mCirclePaint.strokeWidth = DrawUtils.dip2px(4f).toFloat()
        mCirclePaint.style = Paint.Style.STROKE
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mOval.set(mCirclePaint.strokeWidth * 0.5f, mCirclePaint.strokeWidth * 0.5f,
                w.toFloat() - mCirclePaint.strokeWidth * 0.5f,
                h.toFloat() - mCirclePaint.strokeWidth * 0.5f)
        mCirclePath.reset()
        mCirclePath.addCircle(w * 0.5f, h * 0.5f, Math.min(w * 0.5f, h * 0.5f), Path.Direction.CW)
    }

    override fun draw(canvas: Canvas?) {
        canvas?.clipPath(mCirclePath)
        canvas?.drawFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        super.draw(canvas)
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawArc(mOval, -90f, 360 * mPercent, false, mCirclePaint)
    }

    fun startCountDown(secondTime: Int, timeUp: () -> Unit) {
        timeCount?.run {
            this.cancel()
        }
        timeCount = ValueAnimator.ofFloat(1f, 0f)
        timeCount?.interpolator = LinearInterpolator()
        timeCount?.duration = (secondTime * 1000).toLong()
        timeCount?.addUpdateListener {
            mPercent = it.animatedValue as Float
            text = (secondTime * mPercent + 1).toInt().toString()
            invalidate()
        }
        timeCount?.addListener(object: AnimatorListenerAdapter(){
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                timeUp.invoke()
            }
        })
        timeCount?.start()
    }

    fun cancelCountDown() {
        timeCount?.run {
            this.cancel()
        }
    }
}