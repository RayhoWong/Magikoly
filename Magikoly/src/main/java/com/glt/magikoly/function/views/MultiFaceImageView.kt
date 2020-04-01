package com.glt.magikoly.function.views

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ImageView
import com.glt.magikoly.utils.DrawUtils
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import magikoly.magiccamera.R

class MultiFaceImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {


    private val rectFDst = RectF()
    private val rectFSrc = RectF()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mFaces: List<FirebaseVisionFace>? = null
    private val gestureListener: GestureDetector.SimpleOnGestureListener
    private val gestureDetector: GestureDetector
    private val selectedPoint = PointF()
    private var selectRect = RectF()
    private var onFaceSelectedListener: OnFaceSelectedListener? = null
    var entrance: String = ""

    private val faceRect: NinePatch  by lazy {
        val decodeResource = BitmapFactory.decodeResource(context.resources,
                R.drawable.face_rect_patch)
        NinePatch(decodeResource, decodeResource.ninePatchChunk, null)
    }
    fun setOnFaceSelectedListener(onFaceSelectedListener: OnFaceSelectedListener?) {
        this.onFaceSelectedListener  = onFaceSelectedListener
    }

    init {
        paint.color = Color.parseColor("#66f2fcff")
        paint.style = Paint.Style.FILL
        gestureListener = object : GestureDetector.SimpleOnGestureListener() {

            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                e?.run {
                    selectedPoint.x = this.x
                    selectedPoint.y = this.y
                    mFaces?.forEach {
                        rectFSrc.set(it.boundingBox)
                        rectFDst.setEmpty()
                        imageMatrix.mapRect(rectFDst, rectFSrc)
                        if (rectFDst.contains(selectedPoint.x, selectedPoint.y)) {
                            selectRect.set(it.boundingBox)
                            post { onFaceSelected() }
                            return@forEach
                        }
                    }
                    invalidate()
                }
                return true
            }
        }
        gestureDetector = GestureDetector(getContext(), gestureListener)
        setBackgroundColor(Color.WHITE)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }


    fun setImageWithFaces(image: Bitmap, faces: List<FirebaseVisionFace>) {
        mFaces = faces
        setImageBitmap(image)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        mFaces?.let {
            if (!selectRect.isEmpty) {
                rectFSrc.set(selectRect)
                rectFDst.setEmpty()
                imageMatrix.mapRect(rectFDst, rectFSrc)
                canvas?.drawRoundRect(rectFDst, DrawUtils.dip2px(10f).toFloat(), DrawUtils.dip2px(10f).toFloat(), paint)
            }
        }
        mFaces?.forEach {
            rectFSrc.set(it.boundingBox)
            rectFDst.setEmpty()
            imageMatrix.mapRect(rectFDst, rectFSrc)
            faceRect.draw(canvas, rectFDst)
        }

    }

    private fun onFaceSelected() {
        onFaceSelectedListener?.onSelected(entrance,
            (drawable as BitmapDrawable).bitmap, Rect(
                selectRect.left.toInt(), selectRect.top.toInt(), selectRect.right.toInt(), selectRect.bottom.toInt()
            )
        )
    }

    interface OnFaceSelectedListener {
        fun onSelected(entrance: String, image: Bitmap, selectedRec: Rect)
    }
}