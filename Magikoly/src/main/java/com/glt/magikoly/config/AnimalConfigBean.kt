package com.glt.magikoly.config

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.util.SparseArray
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.FaceEnv
import com.glt.magikoly.download.DownloadListener
import com.glt.magikoly.download.DownloadManager
import com.glt.magikoly.download.DownloadTask
import com.glt.magikoly.pref.PrefConst.KEY_ANIMAL_CONFIG_CACHE
import com.glt.magikoly.utils.FileUtils
import com.glt.magikoly.utils.Logcat
import com.glt.magikoly.utils.ZipUtils
import org.json.JSONArray
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class AnimalConfigBean : AbsConfigBean() {

    companion object {
        const val SID = 837
    }


    private val animalConfigs = SparseArray<ArrayList<AnimalConfig>>()

    override fun restoreDefault() {
    }

    override fun readConfig(jsonArray: JSONArray?) {
        jsonArray?.let { array ->
            animalConfigs.clear()
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i)
                val gender = obj.optInt("gender")
                val animal = obj.optString("animal")
                val zipUrl = obj.optString("zip_url")
                var configList = animalConfigs.get(gender)
                if (configList == null) {
                    configList = ArrayList()
                    animalConfigs.put(gender, configList)
                }
                val animalConfig = AnimalConfig(animal, zipUrl)
                configList.add(animalConfig)
            }
        }
    }

    override fun getCacheKey(): String {
        return KEY_ANIMAL_CONFIG_CACHE
    }

    fun isConfigLoaded(): Boolean {
        return animalConfigs.size() > 0
    }

    fun getRandomAnimalConfig(gender: Int): AnimalConfig? {
        val configs = animalConfigs.get(gender)
        if (configs != null && configs.isNotEmpty()) {
            val index = Random().nextInt(configs.size)
            Logcat.i("animal", "Get animal(${configs[index].animal}) successfully")
            return configs[index]
        }
        Logcat.i("animal", "Get random animal failed")
        return null
    }

    fun getAnimalConfig(gender: Int, animal: String): AnimalConfig? {
        val configs = animalConfigs.get(gender)
        if (configs != null && configs.isNotEmpty()) {
            configs.forEach {
                if (it.animal == animal) {
                    Logcat.i("animal", "Get animal($animal) successfully")
                    return it
                }
            }
        }
        Logcat.i("animal", "Get animal($animal) failed")
        return null
    }

    class AnimalConfig(val animal: String, val zipUrl: String) {

        companion object {
            const val LEFT_EYEBROW_TOP = "LEFT_EYEBROW_TOP"
            const val RIGHT_EYEBROW_TOP = "RIGHT_EYEBROW_TOP"
            const val LEFT_EYE = "LEFT_EYE"
            const val RIGHT_EYE = "RIGHT_EYE"
            const val NOSE_BRIDGE = "NOSE_BRIDGE"
            const val NOSE_BOTTOM = "NOSE_BOTTOM"
            const val UPPER_LIP_BOTTOM = "UPPER_LIP_BOTTOM"
            const val FACE_OVAL = "FACE_OVAL"

            val EYE_BROW_FILTER_INDEXES = intArrayOf(1)
            val EYE_FILTER_INDEXES = intArrayOf(3, 5, 7, 9, 12, 14)
            val FACE_FILTER_INDEXES = intArrayOf(3, 5, 8, 13, 15, 17, 19, 21, 23, 28, 31)
            val KEY_ARRAY = arrayOf(
                    LEFT_EYEBROW_TOP,
                    RIGHT_EYEBROW_TOP,
                    LEFT_EYE,
                    RIGHT_EYE,
                    NOSE_BRIDGE,
                    NOSE_BOTTOM,
                    UPPER_LIP_BOTTOM,
                    FACE_OVAL)
        }

        private val animalDir: File = File(FaceEnv.InternalPath.getInnerFilePath(FaceAppState.getContext(),
                FaceEnv.InternalPath.ANIMAL_DIR + animal))
        private val animalImage: File = File(animalDir, "$animal.png")
        private val animalLandmark: File = File(animalDir, "$animal.landmark")

        var isDownloading = false

        fun isCacheValid(): Boolean {
            return animalDir.exists() && animalImage.exists() && animalLandmark.exists()
        }

        fun download(listener: AnimalDownloadListener?) {
            val listenerProxy = object : DownloadListener {
                override fun pending(task: DownloadTask) {
                }

                override fun taskStart(task: DownloadTask) {
                }

                override fun connectStart(task: DownloadTask) {
                }

                override fun progress(task: DownloadTask) {
                }

                override fun completed(task: DownloadTask) {
                    if (DownloadManager.instance.getDownloadTaskId(zipUrl, animalDir.parent,
                                    animalDir.name + ".zip") == task.id) {
                        isDownloading = false
                        if (unzipFile()) {
                            listener?.onCompleted()
                        } else {
                            listener?.onFailed()
                        }
                    }
                }

                override fun paused(task: DownloadTask) {
                }

                override fun error(task: DownloadTask) {
                    if (DownloadManager.instance.getDownloadTaskId(zipUrl, animalDir.parent,
                                    animalDir.name + ".zip") == task.id) {
                        isDownloading = false
                        listener?.onFailed()
                    }
                }
            }
            if (isDownloading) {
                DownloadManager.instance.setDownloadListener(zipUrl,
                        animalDir.parent, animalDir.name + ".zip", listenerProxy)
            } else {
                isDownloading = true
                DownloadManager.instance.startDownload(zipUrl,
                        animalDir.parent, animalDir.name + ".zip", listenerProxy)
            }
        }

        private fun unzipFile(): Boolean {
            return try {
                if (animalDir.exists()) {
                    FileUtils.deleteFile(animalDir)
                }
                val zipFile = File(animalDir.parentFile, animalDir.name + ".zip")
                ZipUtils.unzipFile(zipFile, animalDir)
                zipFile.delete()
                true
            } catch (e: IOException) {
                false
            }
        }

        fun extractLandmark(xOffset: Int, yOffset: Int,
                            scale: Float): HashMap<String, List<Point>> {
            val properties = Properties()
            properties.load(FileInputStream(animalLandmark))
            val landmark = HashMap<String, List<Point>>()
            properties.stringPropertyNames().forEach { key ->
                val points = ArrayList<Point>()
                landmark[key] = points
                val value = properties.getProperty(key)
                value.split(";").forEach { str ->
                    val pointStr = str.split("(", ",", ")").filter { it.isNotEmpty() }
                    val x = (pointStr[0].toInt() - xOffset) * scale
                    val y = -(pointStr[1].toInt() - yOffset) * scale
                    val point = Point(x.toInt(), y.toInt())
                    points.add(point)
                }
            }
            return landmark
        }

        fun getAnimalImage(): Bitmap {
            return BitmapFactory.decodeFile(animalImage.absolutePath)
        }
    }

    interface AnimalDownloadListener {
        fun onCompleted()
        fun onFailed()
    }
}