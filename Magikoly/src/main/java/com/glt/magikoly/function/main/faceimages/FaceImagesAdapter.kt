package com.glt.magikoly.function.main.faceimages

import android.animation.ValueAnimator
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.glt.magikoly.function.FaceImageInfo
import com.glt.magikoly.utils.DrawUtils
import magikoly.magiccamera.R

class FaceImagesAdapter(context: Context, faceImageList: ArrayList<FaceImageInfo>,
                        listener: OnImageItemClickListener) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_CAMERA = 1
        const val VIEW_TYPE_IMAGE = 2
        const val VIEW_TYPE_LOADING = 3

        private val imageHeight = DrawUtils.dip2px(108f)
    }

    private val context = context
    private var faceImageList: ArrayList<FaceImageInfo> = faceImageList
    private val listener: OnImageItemClickListener = listener
    var isLoadingViewVisible = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CAMERA -> {
                val view = LayoutInflater.from(context).inflate(R.layout.main_camera_view, null)
                CameraViewHolder(view)
            }
            VIEW_TYPE_IMAGE -> {
                val imgView = FaceImageView(context)
                imgView.layoutParams = RecyclerView.LayoutParams(imageHeight, imageHeight)
                FaceImagesViewHolder(imgView)
            }
            VIEW_TYPE_LOADING -> {
                val loadingView = LayoutInflater.from(context)
                        .inflate(R.layout.main_face_images_item_loading_view, null)
                loadingView.layoutParams = RecyclerView.LayoutParams(imageHeight, imageHeight)
                LoadingViewHolder(loadingView)
            }
            else -> FaceImagesViewHolder(null)
        }
    }

    override fun getItemCount(): Int {
        return if (faceImageList.isEmpty()) {
            1
        } else {
            if (isLoadingViewVisible) {
                faceImageList.size + 2
            } else {
                faceImageList.size + 1
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoadingViewVisible) {
            when (position) {
                0 -> VIEW_TYPE_CAMERA
                faceImageList.size + 1 -> VIEW_TYPE_LOADING
                else -> VIEW_TYPE_IMAGE
            }
        } else {
            when (position) {
                0 -> VIEW_TYPE_CAMERA
                else -> VIEW_TYPE_IMAGE
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            VIEW_TYPE_CAMERA -> {
                val view = holder.itemView
                view.setOnClickListener {
                    listener.onCameraClick(view.findViewById(R.id.img_camera))
                }
            }
            VIEW_TYPE_IMAGE -> {
                val imgHolder = holder as FaceImagesViewHolder
                val imgView = imgHolder.itemView as FaceImageView
                imgView.scaleType = ImageView.ScaleType.CENTER_CROP
                imgView.cornerRadius = DrawUtils.dip2px(10f).toFloat()
                val index = position - 1
                val faceImageInfo = faceImageList[index]
                if (holder.tag != faceImageInfo) {
                    imgView.setImageBitmap(null)
                    Glide.with(imgView).clear(imgView)
                    holder.tag = faceImageInfo
                    if (faceImageInfo.isDemo()) {
                        imgView.setDemoVisible(true)
                        imgView.setImageResource(faceImageInfo.imgId)
                    } else {
                        imgView.setDemoVisible(false)
                        val options = RequestOptions().override(imageHeight)
                        Glide.with(imgView).asBitmap().load(faceImageInfo.imagePath).apply(options)
                                .into(imgView)
                    }
                }
                imgView.setOnClickListener {
                    listener.onImageItemClick(faceImageInfo)
                }
            }
        }
    }

    fun setFaceImageList(faceImageList: ArrayList<FaceImageInfo>) {
        this.faceImageList = faceImageList
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
    }
}

interface OnImageItemClickListener {
    fun onCameraClick(cameraView: View?)
    fun onImageItemClick(faceImageInfo: FaceImageInfo)
}

class FaceImagesViewHolder(imgView: ImageView?) : RecyclerView.ViewHolder(imgView) {
    var tag: Any? = null
}

class CameraViewHolder(view: View) : RecyclerView.ViewHolder(view)

class LoadingViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private var loadingAnimation = ValueAnimator.ofFloat(0f, 360f)

    fun startRotateAnimation() {
        if (!loadingAnimation.isRunning) {
            val progressBar = itemView.findViewById<ImageView>(R.id.img_progressbar)
            loadingAnimation.apply {
                duration = 1000
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
                addUpdateListener {
                    progressBar.rotation = it.animatedValue as Float
                    progressBar.invalidate()
                }
                start()
            }
        }
    }
}