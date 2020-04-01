package com.glt.magikoly.function.resultreport.animal.presenter

import android.graphics.Bitmap
import android.graphics.Point
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.config.AbsConfigBean
import com.glt.magikoly.config.AnimalConfigBean
import com.glt.magikoly.config.ConfigManager
import com.glt.magikoly.constants.ErrorCode.*
import com.glt.magikoly.data.operator.FaceAnimalDataOperator
import com.glt.magikoly.data.table.FaceAnimalTable
import com.glt.magikoly.ext.runAsync
import com.glt.magikoly.ext.runMain
import com.glt.magikoly.function.DemoFaceImageInfo
import com.glt.magikoly.function.FaceFunctionManager
import com.glt.magikoly.function.facesdk.FaceSdkProxy
import com.glt.magikoly.function.resultreport.animal.IAnimalView
import com.glt.magikoly.mvp.AbsPresenter
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.utils.BitmapUtils
import com.glt.magikoly.utils.PersonDetectUtils
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToLong


class AnimalPresenter : AbsPresenter<IAnimalView>() {

    private val dataOperator = FaceAnimalDataOperator(FaceAppState.getContext())

    var isLoading = false

    fun detectFace(image: Bitmap) {
        isLoading = true
        val startTime = System.currentTimeMillis()
        FaceSdkProxy.detectImage(false, false, image, 0,
                { faces ->
                    if (faces != null && faces.isNotEmpty()) {
                        val face = faces[0]
                        runAsync {
                            val landmark = HashMap<String, List<Point>>()
                            val xOffset = (image.width / 2f).toInt()
                            val yOffset = (image.height / 2f).toInt()
                            extractAllFaceLandmark(face, landmark, xOffset, yOffset)
                            val feature = PersonDetectUtils.getFaceFeature(image, face)
                            runMain {
                                view?.onFaceDetectSuccess(landmark, feature, startTime)
                            }
                        }
                    } else {
                        isLoading = false
                        view?.onFaceDetectError(FACE_NOT_FOUND)
                        BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                                ((System.currentTimeMillis() - startTime) / 1000f).roundToLong().toString(),
                                Statistic103Constant.FUNCTION_ACHIEVE,
                                Statistic103Constant.ENTRANCE_ANIMAL, "2", "")
                    }
                }, {
            isLoading = false
            view?.onFaceDetectError(FACE_DETECT_ERROR)
            BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                    ((System.currentTimeMillis() - startTime) / 1000f).roundToLong().toString(),
                    Statistic103Constant.FUNCTION_ACHIEVE,
                    Statistic103Constant.ENTRANCE_ANIMAL, "2", "")
        })
    }

    private fun extractAllFaceLandmark(face: FirebaseVisionFace,
                                       landmark: HashMap<String, List<Point>>, xOffset: Int,
                                       yOffset: Int) {
        extractFaceLandmark(face, landmark, xOffset, yOffset,
                FirebaseVisionFaceContour.LEFT_EYEBROW_TOP,
                AnimalConfigBean.AnimalConfig.EYE_BROW_FILTER_INDEXES,
                AnimalConfigBean.AnimalConfig.LEFT_EYEBROW_TOP)
        extractFaceLandmark(face, landmark, xOffset, yOffset,
                FirebaseVisionFaceContour.RIGHT_EYEBROW_TOP,
                AnimalConfigBean.AnimalConfig.EYE_BROW_FILTER_INDEXES,
                AnimalConfigBean.AnimalConfig.RIGHT_EYEBROW_TOP)
        extractFaceLandmark(face, landmark, xOffset, yOffset,
                FirebaseVisionFaceContour.LEFT_EYE,
                AnimalConfigBean.AnimalConfig.EYE_FILTER_INDEXES,
                AnimalConfigBean.AnimalConfig.LEFT_EYE)
        extractFaceLandmark(face, landmark, xOffset, yOffset,
                FirebaseVisionFaceContour.RIGHT_EYE,
                AnimalConfigBean.AnimalConfig.EYE_FILTER_INDEXES,
                AnimalConfigBean.AnimalConfig.RIGHT_EYE)
        extractFaceLandmark(face, landmark, xOffset, yOffset,
                FirebaseVisionFaceContour.NOSE_BRIDGE, null,
                AnimalConfigBean.AnimalConfig.NOSE_BRIDGE)
        extractFaceLandmark(face, landmark, xOffset, yOffset,
                FirebaseVisionFaceContour.NOSE_BOTTOM, null,
                AnimalConfigBean.AnimalConfig.NOSE_BOTTOM)
        extractFaceLandmark(face, landmark, xOffset, yOffset,
                FirebaseVisionFaceContour.UPPER_LIP_BOTTOM, null,
                AnimalConfigBean.AnimalConfig.UPPER_LIP_BOTTOM)
        extractFaceLandmark(face, landmark, xOffset, yOffset,
                FirebaseVisionFaceContour.FACE,
                AnimalConfigBean.AnimalConfig.FACE_FILTER_INDEXES,
                AnimalConfigBean.AnimalConfig.FACE_OVAL)
    }

    private fun extractFaceLandmark(face: FirebaseVisionFace,
                                    landmark: HashMap<String, List<Point>>, xOffset: Int,
                                    yOffset: Int, firebaseContourType: Int,
                                    filterIndexArray: IntArray?, landmarkKey: String) {
        val contour = face.getContour(firebaseContourType)
        var points = contour.points
        filterIndexArray?.let { filterIndexes ->
            points = contour.points.filterIndexed { index, _ ->
                !filterIndexes.contains(index)
            }
        }
        val targetPoints = ArrayList<Point>(points.size)
        points.forEach {
            val x = it.x - xOffset
            val y = -(it.y - yOffset)
            val point = Point(x.toInt(), y.toInt())
            targetPoints.add(point)
        }
        landmark[landmarkKey] = targetPoints
    }

    fun loadDemoAnimal(demoFaceImageInfo: DemoFaceImageInfo, xOffset: Int, yOffset: Int, scale: Float) {
        runAsync {
            val startTime = System.currentTimeMillis()
            val properties = Properties()
            properties.load(FaceAppState.getContext().resources.assets.open(
                    "animal/${demoFaceImageInfo.landmark}"))
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
            runMain {
                view?.onFaceDetectSuccess(landmark, null, startTime)
            }
        }
    }

    fun loadAnimal(viewWidth: Float, viewHeight: Float, humanFeature: FloatArray?, startTime: Long) {
        val gender = if (FaceFunctionManager.demoFaceImageInfo != null)
            FaceFunctionManager.demoFaceImageInfo?.faceInfo?.gender
        else FaceFunctionManager.faceBeanMap[FaceFunctionManager.currentFaceImagePath]?.faceInfo?.gender
        val ethnicity = if (FaceFunctionManager.demoFaceImageInfo != null)
            FaceFunctionManager.demoFaceImageInfo?.faceInfo?.ethnicity
        else FaceFunctionManager.faceBeanMap[FaceFunctionManager.currentFaceImagePath]?.faceInfo?.ethnicity
        runAsync {
            var matchedAnimal: String? = null
            humanFeature?.let {
                val dataList = dataOperator.getFaceAnimalData(gender!!, ethnicity!!)
                dataList?.forEach { bean ->
                    if (PersonDetectUtils.characterMatch(humanFeature, bean.faceFeature)) {
                        matchedAnimal = bean.animal
                        return@forEach
                    }
                }
            }
            obtainAndHandleAnimalConfig(viewWidth, viewHeight, humanFeature, gender!!, ethnicity!!,
                    matchedAnimal, startTime)
        }
    }

    fun loadAnimal(viewWidth: Float, viewHeight: Float, gender: String, matchedAnimal: String, startTime:Long) {
        isLoading = true
        val genderInt = if (gender == "F") 1 else 0
        val animalConfigBean = ConfigManager.getInstance().getConfigBean(
                AnimalConfigBean.SID) as AnimalConfigBean
        var animalConfig = animalConfigBean.getAnimalConfig(genderInt, matchedAnimal)
        if (animalConfig == null) {
            if (animalConfigBean.isConfigLoaded()) {
                view?.onAnimalLoadFailed(NETWORK_ERROR)
                BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                        ((System.currentTimeMillis() - startTime) / 1000f).roundToLong().toString(),
                        Statistic103Constant.FUNCTION_ACHIEVE,
                        Statistic103Constant.ENTRANCE_ANIMAL, "2", "")
                return
            }
            ConfigManager.getInstance()
                    .requestConfig(FaceAppState.getContext(), AnimalConfigBean.SID, object :
                            ConfigManager.HttpCallback {
                        override fun success(configBean: AbsConfigBean?) {
                            if (configBean != null) {
                                animalConfig = animalConfigBean.getAnimalConfig(genderInt, matchedAnimal)
                                if (animalConfig != null) {
                                    handleAnimalConfig(animalConfig!!, viewWidth, viewHeight, startTime)
                                } else {
                                    error()
                                }
                            } else {
                                error()
                            }
                        }

                        override fun error() {
                            isLoading = false
                            view?.onAnimalLoadFailed(NETWORK_ERROR)
                            BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                                    ((System.currentTimeMillis() - startTime) / 1000f).roundToLong().toString(),
                                    Statistic103Constant.FUNCTION_ACHIEVE,
                                    Statistic103Constant.ENTRANCE_ANIMAL, "2", "")
                        }
                    })
        } else {
            handleAnimalConfig(animalConfig!!, viewWidth, viewHeight, startTime)
        }
    }

    private fun obtainAndHandleAnimalConfig(viewWidth: Float, viewHeight: Float,
                                            humanFeature: FloatArray?, gender: String,
                                            ethnicity: Int, matchedAnimal: String?, startTime: Long) {
        val genderInt = if (gender == "F") 1 else 0
        val animalConfigBean = ConfigManager.getInstance().getConfigBean(
                AnimalConfigBean.SID) as AnimalConfigBean
        var animalConfig = if (matchedAnimal == null) {
            animalConfigBean.getRandomAnimalConfig(genderInt)
        } else {
            animalConfigBean.getAnimalConfig(genderInt, matchedAnimal)
        }

        if (animalConfig == null) {
            if (animalConfigBean.isConfigLoaded()) {
                view?.onAnimalLoadFailed(NETWORK_ERROR)
                BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                        ((System.currentTimeMillis() - startTime) / 1000f).roundToLong().toString(),
                        Statistic103Constant.FUNCTION_ACHIEVE,
                        Statistic103Constant.ENTRANCE_ANIMAL, "2", "")
                return
            }
            ConfigManager.getInstance()
                    .requestConfig(FaceAppState.getContext(), AnimalConfigBean.SID, object :
                            ConfigManager.HttpCallback {
                        override fun success(configBean: AbsConfigBean?) {
                            if (configBean != null) {
                                animalConfig = if (matchedAnimal == null) {
                                    animalConfigBean.getRandomAnimalConfig(genderInt)
                                } else {
                                    animalConfigBean.getAnimalConfig(genderInt, matchedAnimal)
                                }
                                if (animalConfig != null) {
                                    if (matchedAnimal == null) {
                                        humanFeature?.let { faceFeature ->
                                            animalConfig?.let { config ->
                                                val faceAnimalBean = FaceAnimalTable.FaceAnimalBean()
                                                faceAnimalBean.gender = gender
                                                faceAnimalBean.ethnicity = ethnicity
                                                faceAnimalBean.faceFeature = faceFeature
                                                faceAnimalBean.animal = config.animal
                                                dataOperator.addFaceAnimalData(faceAnimalBean)
                                            }
                                        }
                                    }
                                    handleAnimalConfig(animalConfig!!, viewWidth, viewHeight, startTime)
                                } else {
                                    error()
                                }
                            } else {
                                error()
                            }
                        }

                        override fun error() {
                            isLoading = false
                            view?.onAnimalLoadFailed(NETWORK_ERROR)
                            BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                                    ((System.currentTimeMillis() - startTime) / 1000f).roundToLong().toString(),
                                    Statistic103Constant.FUNCTION_ACHIEVE,
                                    Statistic103Constant.ENTRANCE_ANIMAL, "2", "")
                        }
                    })
        } else {
            if (matchedAnimal == null) {
                humanFeature?.let { faceFeature ->
                    animalConfig?.let { config ->
                        val faceAnimalBean = FaceAnimalTable.FaceAnimalBean()
                        faceAnimalBean.gender = gender
                        faceAnimalBean.ethnicity = ethnicity
                        faceAnimalBean.faceFeature = faceFeature
                        faceAnimalBean.animal = config.animal
                        dataOperator.addFaceAnimalData(faceAnimalBean)
                    }
                }
            }
            handleAnimalConfig(animalConfig!!, viewWidth, viewHeight, startTime)
        }
    }

    private fun handleAnimalConfig(animalConfig: AnimalConfigBean.AnimalConfig, viewWidth: Float,
                                   viewHeight: Float, startTime:Long) {
        if (animalConfig.isCacheValid()) {
            postHandlingAnimalConfig(animalConfig, viewWidth, viewHeight, startTime)
        } else {
            animalConfig.download(object : AnimalConfigBean.AnimalDownloadListener {
                override fun onCompleted() {
                    postHandlingAnimalConfig(animalConfig, viewWidth, viewHeight, startTime)
                }

                override fun onFailed() {
                    isLoading = false
                    view?.onAnimalLoadFailed(NETWORK_ERROR)
                    BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                            ((System.currentTimeMillis() - startTime) / 1000f).roundToLong().toString(),
                            Statistic103Constant.FUNCTION_ACHIEVE,
                            Statistic103Constant.ENTRANCE_ANIMAL, "2", "")
                }
            })
        }
    }

    private fun postHandlingAnimalConfig(animalConfig: AnimalConfigBean.AnimalConfig,
                                         viewWidth: Float, viewHeight: Float, startTime: Long) {
        runAsync {
            val animalImage = animalConfig.getAnimalImage()
            val xOffset = (animalImage.width / 2f).toInt()
            val yOffset = (animalImage.height / 2f).toInt()
            val scaledImage = BitmapUtils.centerScaleBitmapForViewSize(animalImage, viewWidth,
                    viewHeight)
            val scale = 1f * scaledImage.width / animalImage.width
            animalImage.recycle()
            val landmark = animalConfig.extractLandmark(xOffset, yOffset, scale)
            runMain {
                isLoading = false
                view?.onAnimalLoadCompleted(scaledImage, landmark)
                BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                        ((System.currentTimeMillis() - startTime) / 1000f).roundToLong().toString(),
                        Statistic103Constant.FUNCTION_ACHIEVE,
                        Statistic103Constant.ENTRANCE_ANIMAL, "1", "")
            }
        }
    }
}