package com.glt.magikoly.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import com.cs.bd.commerce.util.LogUtils
import com.glt.magikoly.FaceAppState
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark
import com.tencent.bugly.crashreport.CrashReport
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import java.io.IOException
import java.io.InputStream


object PersonDetectUtils {

    private val TAG = this.javaClass.name


    private var sTFInterface: TensorFlowInferenceInterface? = null
    private val MODEL_FILE = "mobilefacenet.pb"
    private val INPUT_NAME = "img_inputs"
    private val OUTPUT_NAME = "embeddings"

    private val INPUT_SIZE = 112 // 224 448  672 896 1120


    private val PS_MODEL_FILE = "human_256_input_1_output_0.pb"
    private var sPsTFInterface: TensorFlowInferenceInterface? = null
    private val INPUT_NAME_PS = "input_1"
    private val OUTPUT_NAME_PS = "output_0"
    private val INPUT_SIZE_PS = 256


    @Synchronized
    fun initialize() {
        var input: InputStream? = null
        var psInput: InputStream? = null
        try {
            if (sTFInterface == null) {
                input = FaceAppState.getContext().assets.open(MODEL_FILE)
                sTFInterface = TensorFlowInferenceInterface(input)
                input.close()
            }


            if (sPsTFInterface == null) {
                psInput = FaceAppState.getContext().assets.open(PS_MODEL_FILE)
                sPsTFInterface = TensorFlowInferenceInterface(psInput)
                psInput.close()
            }

        } catch (e: java.lang.Exception) {

            try {
                input?.close()
                psInput?.close()
            } catch (ioException: IOException) {
                Logcat.e(TAG, ioException.message)
            }
            CrashReport.postCatchedException(e)
        }


    }

    fun isInitialize(): Boolean {
        return sTFInterface != null && sPsTFInterface != null
    }


    fun getFaceFeature(src: Bitmap, face: FirebaseVisionFace): FloatArray? {
        try {

            if (!isInitialize()) {
                initialize()
            }

            if (!isInitialize()) {
                return null
            }

            val boundingBox = face.boundingBox
            val landmark = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE)


            val createBitmap = Bitmap.createBitmap(src, boundingBox.left, boundingBox.top, boundingBox.width(), boundingBox.height())
            val position = landmark!!.position!!
            val noseX = position.x - boundingBox.left
            val noseY = position.y - boundingBox.top
            var transX = 0f
            var transY = 0f
            var targetWidth = boundingBox.width()
            var targetHeight = boundingBox.height()


            if (noseX > createBitmap.width / 2) {
                targetWidth = (noseX * 2 + 0.5).toInt()
            } else if (noseX < createBitmap.width / 2) {
                transX = createBitmap.width - noseX * 2
                targetWidth = ((createBitmap.width - noseX) * 2f + 0.5f).toInt()
            }

            if (noseY > createBitmap.height / 2) {
                targetHeight = (noseY * 2 + 0.5).toInt()
            } else if (noseY < createBitmap.height / 2) {
                transY = createBitmap.height - noseY * 2
                targetHeight = ((createBitmap.height - noseY) * 2f + 0.5f).toInt()
            }

            val canvasBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(canvasBitmap)
            canvas.drawBitmap(createBitmap, transX, transY, null)
//            val paint = Paint()
//            paint.color = Color.RED
//            paint.strokeWidth = 20f
//            canvas.drawPoint(transX + noseX, transY + noseY, paint)


            if (!createBitmap.isRecycled) {
                createBitmap.recycle()
            }

            val bitmap = Bitmap.createScaledBitmap(canvasBitmap, INPUT_SIZE, INPUT_SIZE, false)

            if (!canvasBitmap.isRecycled) {
                canvasBitmap.recycle()
            }

            val bW = bitmap!!.width
            val bH = bitmap.height
            val mIntValues = IntArray(INPUT_SIZE * INPUT_SIZE)
            val mFlatIntValues = FloatArray(bW * bH * 3)

            bitmap.getPixels(mIntValues, 0, bW, 0, 0, bW, bH)

            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }

            for (i in mIntValues.indices) {
                val value = mIntValues[i]
                mFlatIntValues[i * 3] = (value shr 16 and 0xFF) / 127.5f - 1  //r
                mFlatIntValues[i * 3 + 1] = (value shr 8 and 0xFF) / 127.5f - 1 //g
                mFlatIntValues[i * 3 + 2] = (value and 0xFF) / 127.5f - 1 //b
            }

            val resultValues = FloatArray(192)

            synchronized(this) {
                sTFInterface?.feed(INPUT_NAME, mFlatIntValues, 1L, bW.toLong(), bH.toLong(), 3L)

                sTFInterface?.run(arrayOf(OUTPUT_NAME))

                sTFInterface?.fetch(OUTPUT_NAME, resultValues)

            }



            LogUtils.e(resultValues.toString())

            return resultValues
        } catch (e: Exception) {
            Logcat.e(TAG, e.message)
            CrashReport.postCatchedException(e)
            return null
        }


    }

    fun portraitSeparation(src: Bitmap): Bitmap? {

        try {

            if (!isInitialize()) {
                initialize()
            }

            if (!isInitialize()) {
                return null
            }


            val srcW = src.width
            val srcH = src.height
            val bitmap = Bitmap.createScaledBitmap(src, INPUT_SIZE_PS, INPUT_SIZE_PS, false)
            val bW = bitmap!!.width
            val bH = bitmap.height
            val intValues = IntArray(INPUT_SIZE_PS * INPUT_SIZE_PS)

            val mFlatIntValues = FloatArray(bW * bH * 3)

            bitmap.getPixels(intValues, 0, bW, 0, 0, bW, bH)

            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }

            var pixel = 0
            var value: Int
            for (i in 0 until INPUT_SIZE_PS) {
                for (j in 0 until INPUT_SIZE_PS) {
                    value = intValues[pixel++]

                    val offset = i * INPUT_SIZE_PS * 3 + j * 3
                    mFlatIntValues[offset] = ((value and 0xff0000 shr 16) - 127.5f) / 127.5f
                    mFlatIntValues[offset + 1] = ((value and 0x00ff00 shr 8) - 127.5f) / 127.5f
                    mFlatIntValues[offset + 2] = ((value and 0x0000ff) - 127.5f) / 127.5f
                }
            }


            val outputArray = FloatArray(INPUT_SIZE_PS * INPUT_SIZE_PS)


            synchronized(this) {
                sPsTFInterface?.feed(INPUT_NAME_PS, mFlatIntValues, 1L, bW.toLong(), bH.toLong(), 3L)

                sPsTFInterface?.run(arrayOf(OUTPUT_NAME_PS))

                sPsTFInterface?.fetch(OUTPUT_NAME_PS, outputArray)
            }


            for (i in intValues.indices) {
                value = intValues[i]

                intValues[i] = Color.argb((outputArray[i] * 255).toInt(), value and 0xff0000 shr 16, value and 0x00ff00 shr 8, value and 0x0000ff)
            }


            val mask = Bitmap.createBitmap(intValues, bW, bH, Bitmap.Config.ARGB_8888)

            val createScaledBitmap = Bitmap.createScaledBitmap(mask, srcW, srcH, true)

            if (!mask.isRecycled) {
                mask.recycle()
            }


            return createScaledBitmap

        } catch (e: Exception) {
            e.printStackTrace()
            CrashReport.postCatchedException(e)
            return null
        }

    }


    fun characterMatch(floatArray: FloatArray, twoArray: FloatArray): Boolean {

        if (floatArray.size != 192 || twoArray.size != 192) {
            return false
        }

        var similarity = 0.0f

        for (i in 0..191) {

            similarity += floatArray[i] * twoArray[i]

        }

        LogUtils.e(TAG, similarity.toString())
        return similarity >= 0.7


    }


}