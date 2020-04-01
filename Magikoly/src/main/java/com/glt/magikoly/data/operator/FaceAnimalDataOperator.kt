package com.glt.magikoly.data.operator

import android.content.ContentValues
import android.content.Context
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.data.CoreDataOperator
import com.glt.magikoly.data.table.FaceAnimalTable
import com.glt.magikoly.pref.PrefConst
import com.glt.magikoly.pref.PrivatePreference
import java.io.BufferedReader
import java.io.InputStreamReader

class FaceAnimalDataOperator(context: Context?) : CoreDataOperator(context) {

    companion object {
        const val MAX_DATA_VERSION = 2
        const val MAX_DATA_COUNT = 300
    }

    init {
        val pref = PrivatePreference.getPreference(context)
        var curVersion = pref.getInt(PrefConst.KEY_FACE_ANIMAL_DATA_VERSION, 0)
        if (curVersion < MAX_DATA_VERSION) {
            importAnimalData()
            curVersion = MAX_DATA_VERSION
            pref.putInt(PrefConst.KEY_FACE_ANIMAL_DATA_VERSION, curVersion)
            pref.commit()
        }
    }

    fun addFaceAnimalData(bean: FaceAnimalTable.FaceAnimalBean) {
        var count = 0
        val projection = arrayOf("count(*)")
        val cursor = mManager.query(FaceAnimalTable.TABLE_NAME, projection, null, null, null)
        cursor?.use {
            if (it.moveToNext()) {
                count = it.getInt(0)
            }
        }
        if (count >= MAX_DATA_COUNT) {
            val select = "${FaceAnimalTable.ID}=min(${FaceAnimalTable.ID})"
            mManager.delete(FaceAnimalTable.TABLE_NAME, select, null, null)
        }
        val contents = ContentValues()
        val builder = StringBuilder()
        bean.faceFeature.forEachIndexed { index, value ->
            if (index < bean.faceFeature.size - 1) {
                builder.append(value).append(",")
            } else {
                builder.append(value)
            }
        }
        contents.put(FaceAnimalTable.FACE_FEATURE, builder.toString())
        contents.put(FaceAnimalTable.GENDER, bean.gender)
        contents.put(FaceAnimalTable.ETHNICITY, bean.ethnicity)
        contents.put(FaceAnimalTable.ANIMAL, bean.animal)
        mManager.insert(FaceAnimalTable.TABLE_NAME, contents)
    }

    fun getFaceAnimalData(gender: String,
                          ethnicity: Int): ArrayList<FaceAnimalTable.FaceAnimalBean>? {
        val select = "${FaceAnimalTable.GENDER}=? and ${FaceAnimalTable.ETHNICITY}=?"
        val selectArgs = arrayOf(gender, ethnicity.toString())
        val cursor = mManager.query(FaceAnimalTable.TABLE_NAME, null, select, selectArgs, null)
        cursor?.use {
            val faceAnimalData = ArrayList<FaceAnimalTable.FaceAnimalBean>()
            while (it.moveToNext()) {
                val bean = FaceAnimalTable.FaceAnimalBean()
                val vectorStr = it.getString(it.getColumnIndex(FaceAnimalTable.FACE_FEATURE))
                val array = vectorStr.split(",")
                val faceFeature = FloatArray(array.size)
                array.forEachIndexed { index, value ->
                    faceFeature[index] = value.toFloat()
                }
                bean.faceFeature = faceFeature
                bean.gender = it.getString(it.getColumnIndex(FaceAnimalTable.GENDER))
                bean.ethnicity = it.getInt(it.getColumnIndex(FaceAnimalTable.ETHNICITY))
                bean.animal = it.getString(it.getColumnIndex(FaceAnimalTable.ANIMAL))
                faceAnimalData.add(bean)
            }
            return faceAnimalData
        }
        return null
    }

    private fun importAnimalData() {
        val inputStream = FaceAppState.getContext().resources.assets.open("animalData.txt")
        var reader: BufferedReader? = null
        try {
            mManager.setSynchronizeInThread(true)
            mManager.beginTransaction()
            val reader = BufferedReader(InputStreamReader(inputStream))
            do {
                val line = reader.readLine() ?: break
                val columns = line.split("|")
                val projection = arrayOf("count(*)")
                var count = 0
                val select = FaceAnimalTable.FACE_FEATURE + "=?"
                var selectArg = arrayOf(columns[3])
                val cursor = mManager.query(FaceAnimalTable.TABLE_NAME, projection, select,
                        selectArg, null)
                cursor?.use {
                    if (it.moveToNext()) {
                        count = it.getInt(0)
                    }
                }
                if (count == 0) {
                    val contents = ContentValues()
                    contents.put(FaceAnimalTable.FACE_FEATURE, columns[3])
                    contents.put(FaceAnimalTable.GENDER, columns[0])
                    contents.put(FaceAnimalTable.ETHNICITY, columns[1])
                    contents.put(FaceAnimalTable.ANIMAL, columns[2])
                    mManager.insert(FaceAnimalTable.TABLE_NAME, contents)
                }
            } while (true)
            mManager.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            reader?.close()
            mManager.endTransaction()
            mManager.setSynchronizeInThread(false)
        }
    }
}
