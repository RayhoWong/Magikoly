package com.glt.magikoly.function.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import com.glt.magikoly.bean.LandmarkDTO
import com.glt.magikoly.bean.Point
import com.glt.magikoly.utils.DrawUtils
import magikoly.magiccamera.R

class ScanView : View {

    var photo: Drawable? = null
        set(value) {
            field = value
//            field?.setBounds(0, 0, width, bottom)
            field?.setBounds(0, 0, field?.intrinsicWidth!!, field?.intrinsicHeight!!)
        }
    private val scanLine: Drawable = context.resources.getDrawable(R.drawable.scan_line)
    private val scanCircle1: Drawable = context.resources.getDrawable(R.drawable.scan_circle1)
    private val scanCircle2: Drawable = context.resources.getDrawable(R.drawable.scan_circle2)
    private var maskBitmap: Bitmap? = null
    private val maskBitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var lineAnimator: ValueAnimator? = null
    private var circleAnimator: ValueAnimator? = null
    private var lineTranslateY = 0f
    private var circleAngle = 0f
    private val markAnimList = ArrayList<MarkLineAnimObj>()
    private val markPointList = ArrayList<MarkPointObj>()
    private var markPointAnimator:ValueAnimator? = null
    private var markLineAnimator:ValueAnimator? = null
    var isFaceDetectAnimationFinish = false

    init {
        maskBitmapPaint.style = Paint.Style.FILL
        maskBitmapPaint.color = Color.BLACK

        maskPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)

        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
//        photo?.setBounds(0, 0, width, height)

        var radius = width.toFloat()
        var left = (width - radius) / 2.0f
        var top = (height - radius) / 2.0f
        var right = left + radius
        var bottom = top + radius
        scanCircle1.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())

        radius = width * 233f / 320f
        left = (width - radius) / 2.0f
        top = (height - radius) / 2.0f
        right = left + radius
        bottom = top + radius
        scanCircle2.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())

        scanLine.setBounds(0, 0, width, scanLine.intrinsicHeight)
    }

    override fun onDraw(canvas: Canvas) {
        if (photo != null) {
            drawPhoto(canvas)
        }
        drawCircle(canvas)
        drawScanLine(canvas)
    }

    private fun drawPhoto(canvas: Canvas) {
        if (maskBitmap == null) {
            maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(maskBitmap)
            canvas.drawCircle(maskBitmap?.width!! / 2.0f, maskBitmap?.height!! / 2.0f,
                    maskBitmap?.width!! / 2.0f, maskBitmapPaint)

        }
        val save1 = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null,
                Canvas.ALL_SAVE_FLAG)
        val save2 = canvas.save()
        val scale = 1.0f * width / photo!!.intrinsicWidth
        canvas.scale(scale, scale)
        photo?.draw(canvas)
        drawMarks(canvas)
        canvas.restoreToCount(save2)
        canvas.drawBitmap(maskBitmap, 0f, 0f, maskPaint)
        canvas.restoreToCount(save1)
    }

    private fun drawCircle(canvas: Canvas) {
        canvas.save()
        canvas.rotate(circleAngle, scanCircle1.bounds.centerX().toFloat(),
                scanCircle1.bounds.centerY().toFloat())
        scanCircle1.draw(canvas)
        canvas.restore()
        canvas.save()
        canvas.rotate(-circleAngle, scanCircle2.bounds.centerX().toFloat(),
                scanCircle2.bounds.centerY().toFloat())
        scanCircle2.draw(canvas)
        canvas.restore()
    }

    private fun drawScanLine(canvas: Canvas) {
        val save = canvas.save()
        canvas.translate(0f, lineTranslateY)
        scanLine.draw(canvas)
        canvas.restoreToCount(save)
    }

    private fun drawMarks(canvas: Canvas) {
        markAnimList.forEach { markAnim ->
            markAnim.markList.forEach {
                if (it.start != null && it.current != null) {
                    canvas.drawLine(it.start!!.x.toFloat(), it.start!!.y.toFloat(),
                            it.current!!.x.toFloat(), it.current!!.y.toFloat(), paint)
                }
            }
        }
        paint.style = Paint.Style.FILL
        markPointList.forEach {
            paint.alpha = it.alpha
            if (it.alpha > 0) {
                canvas.drawCircle(it.point.x.toFloat(), it.point.y.toFloat(),
                        DrawUtils.dip2px(1.15f).toFloat(), paint)
            }
        }
        paint.style = Paint.Style.STROKE
    }

    fun startScanAnimation() {
        lineAnimator = ValueAnimator.ofFloat(0f, 1f)
        lineAnimator?.duration = 1000
        lineAnimator?.repeatCount = Animation.INFINITE
        lineAnimator?.interpolator = LinearInterpolator()
        lineAnimator?.addUpdateListener {
            val v: Float = it.animatedValue as Float
            lineTranslateY = height * v
            invalidate()
        }
        lineAnimator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                lineTranslateY = 0f
                invalidate()
            }
        })
        lineAnimator?.start()

        circleAnimator = ValueAnimator.ofFloat(0f, 360f)
        circleAnimator?.duration = 1000
        circleAnimator?.repeatCount = Animation.INFINITE
        circleAnimator?.interpolator = LinearInterpolator()
        circleAnimator?.addUpdateListener {
            circleAngle = it.animatedValue as Float
            invalidate()
        }
        circleAnimator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                circleAngle = 0f
                invalidate()
            }
        })
        circleAnimator?.start()
    }

    fun stopScanAnimation() {
        if (lineAnimator != null && lineAnimator!!.isRunning) {
            lineAnimator?.cancel()
        }
        if (circleAnimator != null && circleAnimator!!.isRunning) {
            circleAnimator?.cancel()
        }
    }

    fun startMarkAnimation(landmarkDTO: LandmarkDTO) {
        initMarkPointAnimation(landmarkDTO)
        initMarkLineAnimation(landmarkDTO)
        startMarkPointAnimation()
    }

    private fun initMarkPointAnimation(landmarkDTO: LandmarkDTO) {
        markPointList.clear()
        if (landmarkDTO.contourChin != null) {
            markPointList.add(MarkPointObj(landmarkDTO.contourChin!!, 0.4f))
        }
        if (landmarkDTO.contourLeft4 != null) {
            markPointList.add(
                MarkPointObj(
                    landmarkDTO.contourLeft4!!,
                    0.3f
                )
            )
        }
        if (landmarkDTO.contourRight4 != null) {
            markPointList.add(
                MarkPointObj(
                    landmarkDTO.contourRight4!!,
                    0.3f
                )
            )
        }
        if (landmarkDTO.leftEyeLeftCorner != null) {
            markPointList.add(
                MarkPointObj(
                    landmarkDTO.leftEyeLeftCorner!!,
                    0.2f
                )
            )
        }
        if (landmarkDTO.leftEyeRightCorner != null) {
            markPointList.add(
                MarkPointObj(
                    landmarkDTO.leftEyeRightCorner!!,
                    0.6f
                )
            )
        }
        if (landmarkDTO.rightEyeLeftCorner != null) {
            markPointList.add(
                MarkPointObj(
                    landmarkDTO.rightEyeLeftCorner!!,
                    0.6f
                )
            )
        }
        if (landmarkDTO.rightEyeRightCorner != null) {
            markPointList.add(
                MarkPointObj(
                    landmarkDTO.rightEyeRightCorner!!,
                    0.2f
                )
            )
        }
        if (landmarkDTO.leftEyebrowLeftCorner != null) {
            markPointList.add(
                MarkPointObj(
                    landmarkDTO.leftEyebrowLeftCorner!!,
                    0.3f
                )
            )
        }
        if (landmarkDTO.leftEyebrowUpperLeftQuarter != null) {
            markPointList.add(
                MarkPointObj(
                    landmarkDTO.leftEyebrowUpperLeftQuarter!!,
                    0f
                )
            )
        }
        if (landmarkDTO.leftEyebrowRightCorner != null) {
            markPointList.add(
                MarkPointObj(
                    landmarkDTO.leftEyebrowRightCorner!!,
                    0.5f
                )
            )
        }
        if (landmarkDTO.rightEyebrowLeftCorner != null) {
            markPointList.add(
                MarkPointObj(
                    landmarkDTO.rightEyebrowLeftCorner!!,
                    0.5f
                )
            )
        }
        if (landmarkDTO.rightEyebrowUpperRightQuarter != null) {
            markPointList.add(
                MarkPointObj(
                    landmarkDTO.rightEyebrowUpperRightQuarter!!,
                    0f
                )
            )
        }
        if (landmarkDTO.rightEyebrowRightCorner != null) {
            markPointList.add(
                MarkPointObj(
                    landmarkDTO.rightEyebrowRightCorner!!,
                    0.1f
                )
            )
        }
        if (landmarkDTO.noseContourLowerMiddle != null) {
            markPointList.add(
                MarkPointObj(
                    landmarkDTO.noseContourLowerMiddle!!,
                    0.8f
                )
            )
        }
        if (landmarkDTO.noseLeft != null) {
            markPointList.add(MarkPointObj(landmarkDTO.noseLeft!!, 0.7f))
        }
        if (landmarkDTO.noseRight != null) {
            markPointList.add(MarkPointObj(landmarkDTO.noseRight!!, 0.7f))
        }
        if (landmarkDTO.mouthLeftCorner != null) {
            markPointList.add(
                MarkPointObj(
                    landmarkDTO.mouthLeftCorner!!,
                    0.9f
                )
            )
        }
        if (landmarkDTO.mouthRightCorner != null) {
            markPointList.add(
                MarkPointObj(
                    landmarkDTO.mouthRightCorner!!,
                    0.9f
                )
            )
        }
        if (landmarkDTO.mouthLowerLipBottom != null) {
            markPointList.add(
                MarkPointObj(
                    landmarkDTO.mouthLowerLipBottom!!,
                    1f
                )
            )
        }
    }

    private fun startMarkPointAnimation() {
        var isCanceled = false
        markPointAnimator = ValueAnimator.ofFloat(0f, 1.1f)
        markPointAnimator?.duration = 2000
        markPointAnimator?.addUpdateListener { it ->
            val v = it.animatedValue as Float
            markPointList.forEach {
                if (v >= it.animStart && v <= it.animEnd) {
                    it.alpha = ((v - it.animStart) / (it.animEnd - it.animStart) * 255).toInt()
                }
            }
        }
        markPointAnimator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator?) {
                isCanceled = true
            }

            override fun onAnimationEnd(animation: Animator?) {
                if (!isCanceled) {
                    markPointList.forEach {
                        it.alpha = 255
                    }
                    startMarkLineAnimation(markAnimList, 0)
                }
            }
        })
        markPointAnimator?.start()
    }

    private fun initMarkLineAnimation(landmarkDTO: LandmarkDTO) {
        markAnimList.clear()
        var mark1: MarkLineObj?
        var mark2: MarkLineObj?

        mark1 = MarkLineObj()
        mark1.start = landmarkDTO.leftEyebrowUpperLeftQuarter
        mark1.end = landmarkDTO.leftEyebrowLeftCorner
        markAnimList.add(MarkLineAnimObj(mark1))

        mark1 = MarkLineObj()
        mark1.start = landmarkDTO.leftEyebrowLeftCorner
        mark1.end = landmarkDTO.leftEyeLeftCorner
        markAnimList.add(MarkLineAnimObj(mark1))

        mark1 = MarkLineObj()
        mark1.start = landmarkDTO.leftEyeLeftCorner
        mark1.end = landmarkDTO.contourLeft4
        markAnimList.add(MarkLineAnimObj(mark1))

        mark1 = MarkLineObj()
        mark1.start = landmarkDTO.contourLeft4
        mark1.end = landmarkDTO.contourChin
        markAnimList.add(MarkLineAnimObj(mark1))

        mark1 = MarkLineObj()
        mark1.start = landmarkDTO.contourChin
        mark1.end = landmarkDTO.contourRight4
        markAnimList.add(MarkLineAnimObj(mark1))

        mark1 = MarkLineObj()
        mark1.start = landmarkDTO.contourRight4
        mark1.end = landmarkDTO.rightEyeRightCorner
        markAnimList.add(MarkLineAnimObj(mark1))

        mark1 = MarkLineObj()
        mark1.start = landmarkDTO.rightEyeRightCorner
        mark1.end = landmarkDTO.rightEyebrowRightCorner
        mark2 = MarkLineObj()
        mark2.start = landmarkDTO.rightEyeRightCorner
        mark2.end = landmarkDTO.rightEyeLeftCorner
        markAnimList.add(MarkLineAnimObj(mark1, mark2))

        mark1 = MarkLineObj()
        mark1.start = landmarkDTO.rightEyebrowRightCorner
        mark1.end = landmarkDTO.rightEyebrowUpperRightQuarter
        mark2 = MarkLineObj()
        mark2.start = landmarkDTO.rightEyeLeftCorner
        mark2.end = landmarkDTO.noseRight
        markAnimList.add(MarkLineAnimObj(mark1, mark2))

        mark1 = MarkLineObj()
        mark1.start = landmarkDTO.rightEyebrowUpperRightQuarter
        mark1.end = landmarkDTO.rightEyebrowLeftCorner
        mark2 = MarkLineObj()
        mark2.start = landmarkDTO.noseRight
        mark2.end = landmarkDTO.noseContourLowerMiddle
        markAnimList.add(MarkLineAnimObj(mark1, mark2))

        mark1 = MarkLineObj()
        mark1.start = landmarkDTO.rightEyebrowLeftCorner
        mark1.end = landmarkDTO.noseContourLowerMiddle
        mark2 = MarkLineObj()
        mark2.start = landmarkDTO.noseContourLowerMiddle
        mark2.end = landmarkDTO.mouthRightCorner
        markAnimList.add(MarkLineAnimObj(mark1, mark2))

        mark1 = MarkLineObj()
        mark1.start = landmarkDTO.noseContourLowerMiddle
        mark1.end = landmarkDTO.leftEyebrowRightCorner
        mark2 = MarkLineObj()
        mark2.start = landmarkDTO.mouthRightCorner
        mark2.end = landmarkDTO.mouthLowerLipBottom
        markAnimList.add(MarkLineAnimObj(mark1, mark2))

        mark1 = MarkLineObj()
        mark1.start = landmarkDTO.leftEyebrowRightCorner
        mark1.end = landmarkDTO.leftEyebrowUpperLeftQuarter
        mark2 = MarkLineObj()
        mark2.start = landmarkDTO.mouthLowerLipBottom
        mark2.end = landmarkDTO.mouthLeftCorner
        markAnimList.add(MarkLineAnimObj(mark1, mark2))


        mark2 = MarkLineObj()
        mark2.start = landmarkDTO.mouthLeftCorner
        mark2.end = landmarkDTO.noseContourLowerMiddle
        markAnimList.add(MarkLineAnimObj(mark2))

        mark2 = MarkLineObj()
        mark2.start = landmarkDTO.noseContourLowerMiddle
        mark2.end = landmarkDTO.noseLeft
        markAnimList.add(MarkLineAnimObj(mark2))

        mark2 = MarkLineObj()
        mark2.start = landmarkDTO.noseLeft
        mark2.end = landmarkDTO.leftEyeRightCorner
        markAnimList.add(MarkLineAnimObj(mark2))

        mark2 = MarkLineObj()
        mark2.start = landmarkDTO.leftEyeRightCorner
        mark2.end = landmarkDTO.leftEyeLeftCorner
        markAnimList.add(MarkLineAnimObj(mark2))
    }

    private fun startMarkLineAnimation(markAnimList: ArrayList<MarkLineAnimObj>, index: Int) {
        var isCanceled = false
        val markAnim: MarkLineAnimObj = markAnimList[index]
        markLineAnimator = ValueAnimator.ofFloat(0f, 1f)
        markLineAnimator?.duration = (250 - ((250 - 100) / (markAnimList.size - 1) * index)).toLong()
        markLineAnimator?.addUpdateListener { animator ->
            val v = animator.animatedValue as Float
            markAnim.markList.forEach {
                if (it.start != null && it.end != null) {
                    it.current = Point()
                    it.current?.x = (it.start!!.x + (it.end!!.x - it.start!!.x) * v).toInt()
                    it.current?.y = (it.start!!.y + (it.end!!.y - it.start!!.y) * v).toInt()
                }
            }
        }
        markLineAnimator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator?) {
                isCanceled = true
            }

            override fun onAnimationEnd(animation: Animator?) {
                if (!isCanceled && index + 1 < markAnimList.size) {
                    startMarkLineAnimation(markAnimList, index + 1)
                } else {
                    isFaceDetectAnimationFinish = true
                }
            }
        })
        markLineAnimator?.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancelAllAnimation()
    }

    private fun cancelAllAnimation() {
        stopScanAnimation()
        if (markPointAnimator != null && markPointAnimator!!.isRunning) {
            markPointAnimator?.cancel()
        }
        if (markLineAnimator != null && markLineAnimator!!.isRunning) {
            markLineAnimator?.cancel()
        }
    }

    private class MarkPointObj {
        var point: Point
        var alpha = 0
        var animStart = 0f
        var animEnd = 0f

        constructor(point: Point, animStart: Float) {
            this.point = point
            this.animStart = animStart
            this.animEnd = animStart + 0.1f
        }
    }

    private class MarkLineObj {
        var start: Point? = null
        var end: Point? = null
        var current: Point? = null
    }

    private class MarkLineAnimObj {
        val markList = ArrayList<MarkLineObj>()

        constructor(vararg marks: MarkLineObj) {
            markList.addAll(marks)
        }
    }
}