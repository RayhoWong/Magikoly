package com.glt.magikoly.function.resultreport.animal

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.FaceEnv
import com.glt.magikoly.ext.runAsync
import com.glt.magikoly.function.FaceFunctionBean
import com.glt.magikoly.function.FaceFunctionManager
import com.glt.magikoly.function.facesdk.FaceSdkProxy
import com.glt.magikoly.utils.FileUtils
import com.glt.magikoly.utils.PersonDetectUtils
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import java.io.File
import java.util.*

object AnimalDataTool {

    fun createAnimalData() {
        val p = Properties()
        p.load(FaceAppState.getContext().resources?.assets?.open("animal/animal.properties"))
        val contentBuilder = StringBuilder()
        val keys = LinkedList<String>()
        p.keys.forEach {
            keys.add(it.toString())
        }
        val nextKey = keys.pollLast()
        val nextAnimal = p.getProperty(nextKey)
        doCreateAnimalData(nextKey, nextAnimal, contentBuilder, keys, p)
    }

    private fun doCreateAnimalData(key:String, animal:String, contentBuilder:StringBuilder, keys: LinkedList<String>, p: Properties) {
        val image = BitmapFactory.decodeStream(
                FaceAppState.getContext().resources?.assets?.open("animal/$key"))
        FaceFunctionManager.detectFaceBy(key, image, object : FaceSdkProxy.OnDetectResult {
            override fun onDetectSuccess(originalPath: String?,
                                         faceBean: FaceFunctionBean?) {
                FaceSdkProxy.detectImage(false, false, image, 0,
                        { faces ->
                            if (faces != null && faces.isNotEmpty()) {
                                val face = faces[0]
                                runAsync {
                                    val faceFeature = PersonDetectUtils.getFaceFeature(
                                            image, face)
                                    val featureBuilder = StringBuilder()
                                    faceFeature?.forEachIndexed { index, value ->
                                        if (index < faceFeature.size - 1) {
                                            featureBuilder.append(value).append(",")
                                        } else {
                                            featureBuilder.append(value)
                                        }
                                    }
                                    contentBuilder.append(faceBean?.faceInfo?.gender)
                                            .append("|")
                                            .append(faceBean?.faceInfo?.ethnicity!!)
                                            .append("|")
                                            .append(animal).append("|")
                                            .append(featureBuilder.toString()).append("\n")
                                    Log.i("createAnimalData", "Import done: $key")
                                    createNextAnimalData(keys, p, contentBuilder)
                                }
                            } else {
                                Log.i("createAnimalData", "Import failed 1: $key")
                                createNextAnimalData(keys, p, contentBuilder)
                            }
                        },
                        {
                            Log.i("createAnimalData", "Import failed 2: $key")
                            createNextAnimalData(keys, p, contentBuilder)
                        })
            }

            override fun onDetectMultiFaces(originalPath: String, originBitmap: Bitmap,
                                            faces: MutableList<FirebaseVisionFace>,
                                            onDetectResult: FaceSdkProxy.OnDetectResult) {
                Log.i("createAnimalData", "Import failed 3: $key")
                createNextAnimalData(keys, p, contentBuilder)
            }

            override fun onDetectFail(originalPath: String?, errorCode: Int) {
                Log.i("createAnimalData", "Import failed 4: $key, Error: $errorCode")
                createNextAnimalData(keys, p, contentBuilder)
            }

        })
    }

    private fun createNextAnimalData(keys: LinkedList<String>, p: Properties,
                                     contentBuilder: StringBuilder) {
        val nextKey = keys.pollLast()
        if (nextKey != null) {
            val nextAnimal = p.getProperty(nextKey)
            doCreateAnimalData(nextKey, nextAnimal, contentBuilder, keys, p)
        } else {
            val dataFile = File(
                    FaceEnv.InternalPath.getInnerFilePath(FaceAppState.getContext(),
                            "animal") + "/animalData.txt")
            dataFile.parentFile.mkdirs()
            FileUtils.appendString(dataFile, contentBuilder.toString())
        }
    }
}