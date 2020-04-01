package com.glt.magikoly.function.poster

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.event.PredictionResultSaveEvent
import com.glt.magikoly.ext.getAppContext
import com.glt.magikoly.ext.postEvent
import com.glt.magikoly.ext.runTask
import com.glt.magikoly.function.resultreport.TabInfo.CREATOR.SUB_TAB_ETHNICITY
import com.glt.magikoly.thread.FaceThreadExecutorProxy
import com.glt.magikoly.utils.BitmapUtils
import magikoly.magiccamera.R
import java.io.File

interface ISavePhoto {
    fun savePoster(tabId: Int, posterTitle: String, posterDesc: String, originalImage: Bitmap?, resultImage: Bitmap?, background: Bitmap) {
        val context = FaceAppState.getContext()
        val posterView = LayoutInflater.from(context)
                .inflate(R.layout.face_poster_container_layout, null) as FacePosterContainer
        posterView.setGPColor(getGPColor())
        posterView.setPosterBackground(background)
        posterView.setPosterTitle(posterTitle)
        posterView.setPosterDesc(posterDesc)
        val options = BitmapFactory.Options()
        options.inScaled = false
        posterView.setOriginalImage(BitmapUtils.getShadowBitmap(originalImage,
                BitmapFactory.decodeResource(getAppContext().resources, R.drawable.poster_circle_mask, options),
                BitmapFactory.decodeResource(getAppContext().resources, R.drawable.poster_circle_shadow, options)))
        posterView.setResultImage(
                BitmapUtils.getShadowBitmap(resultImage,
                        BitmapFactory.decodeResource(getAppContext().resources, R.drawable.poster_square_mask, options),
                        BitmapFactory.decodeResource(getAppContext().resources, R.drawable.poster_square, options),
                        if (tabId == SUB_TAB_ETHNICITY) ImageView.ScaleType.CENTER_INSIDE else ImageView.ScaleType.CENTER_CROP)
        )
        val widthSpec = View.MeasureSpec.makeMeasureSpec(context.resources.getDimensionPixelSize(R.dimen.face_report_poster_width), View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(context.resources.getDimensionPixelSize(R.dimen.face_report_poster_height), View.MeasureSpec.EXACTLY)
        posterView.measure(widthSpec, heightSpec)
        posterView.layout(0, 0, posterView.measuredWidth, posterView.measuredHeight)
        FaceThreadExecutorProxy.execute {
            val poster = BitmapUtils.createBitmap(posterView, 1f)
            val basePath = Environment.getExternalStorageDirectory().absolutePath + File.separator + Environment.DIRECTORY_PICTURES
            val path = basePath + File.separator + getFilePrefix() + System.currentTimeMillis() + ".jpg"
            val success = BitmapUtils.saveBitmap(poster, path, Bitmap.CompressFormat.JPEG)
            FaceThreadExecutorProxy.runOnMainThread {
                if (success) {
                    MediaScannerConnection.scanFile(context, arrayOf(path), null, null)
                }
                postEvent(PredictionResultSaveEvent(tabId, success))
            }
        }
//        if (tabId == SUB_TAB_AGING ||  tabId == SUB_TAB_CHILD || tabId == SUB_TAB_GENDER) {
//            resultImage?.let {
//                saveResultImage(tabId, it)
//            }
//        }
    }

    fun saveResultImage(tabId: Int, result: Bitmap, onFinish: (success: Boolean) -> Unit = {}) {
        val height = result.height
        val width = result.width
        val context = getAppContext()
        val viewContainer = LayoutInflater.from(context).inflate(R.layout.result_image_water_poster_layout, null)
        viewContainer.findViewById<ImageView>(R.id.img_result).setImageBitmap(result)
        val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        viewContainer.measure(widthSpec, heightSpec)
        viewContainer.layout(0, 0, viewContainer.measuredWidth, viewContainer.measuredHeight)
        runTask({
            val createBitmap = BitmapUtils.createBitmap(viewContainer, 1.0f)
            val basePath = Environment.getExternalStorageDirectory().absolutePath + File.separator + Environment.DIRECTORY_PICTURES
            val path = basePath + File.separator + getFilePrefix() + System.currentTimeMillis() + ".jpg"
            val success = BitmapUtils.saveBitmap(createBitmap, path, Bitmap.CompressFormat.JPEG)
            return@runTask object {
                var savePath = path
                var saveSuccess = success
            }
        }, {
            if (it.saveSuccess) {
                MediaScannerConnection.scanFile(FaceAppState.getContext(), arrayOf(it.savePath), null
                ) { path, uri ->
                    postEvent(PredictionResultSaveEvent(tabId, true))
                    onFinish(true)
                }
            } else {
                postEvent(PredictionResultSaveEvent(tabId, false))
                onFinish(false)
            }
        })
    }

    fun getFilePrefix(): String

    fun getGPColor(): Int
}