package com.glt.magikoly.prisma

import android.content.Context
import android.graphics.Bitmap
import com.glt.magikoly.ext.runAsync
import com.glt.magikoly.ext.runMain
import com.glt.magikoly.utils.ZipUtils
import com.prisma.ai.PIEModel
import com.prisma.ai.PIEProcessor
import java.io.File

/**
 *
 * @author rayhahah
 * @blog http://rayhahah.com
 * @time 2019/7/23
 * @tips 这个类是Object的子类
 * @fuction
 */

class PrismaProxy private constructor() {

    private var processor: PIEProcessor? = null

    private val mModelArray: ArrayList<PIEModel> = ArrayList()

    fun release() {
        processor?.release()
        processor = null
    }

    fun releaseModel() {
        mModelArray.forEach {
            it.release()
        }
        mModelArray.clear()
    }

    fun transfer(modelData: ByteArray, bitmap: Bitmap): Bitmap? {
        if (processor == null) {
            processor = PIEProcessor()
        }

        if (processor != null) {
            processor?.apply {
                val pieModel = loadModel(modelData, true)
                mModelArray.add(pieModel)
                val result = styleTransfer(bitmap, pieModel)
                return result
            }
        }
        return null
    }

    fun transferAssets(context: Context, destPath: String, original: Bitmap, onFinished: (Bitmap?) -> Unit) {
        runAsync {
            val inputStream = context.assets.open(destPath)
            val result = transfer(inputStream.readBytes(), original)
            runMain {
                onFinished(result)
            }
        }
    }

    fun transferZip(destPath: String, original: Bitmap, onFinished: (Bitmap?) -> Unit) {
        runAsync {
            val fileList = ZipUtils.unzipFile(destPath, File(destPath).parent)
            if (fileList != null && fileList.size > 0) {
                val result = transfer(fileList[0].readBytes(), original)
                runMain {
                    onFinished(result)
                }
            } else {
                runMain {
                    onFinished(null)
                }
            }
        }
    }

    companion object {
        fun getInstance() = Holder.instance
    }

    object Holder {
        val instance = PrismaProxy()
    }
}