package com.glt.magikoly.data.operator

import android.content.ContentValues
import android.content.Context
import com.glt.magikoly.data.CoreDataOperator
import com.glt.magikoly.data.table.ImageInfoTable
import com.glt.magikoly.function.FaceImageInfo

class ImageInfoDataOperator(context: Context) : CoreDataOperator(context) {

    fun addImageInfo(faceImageInfo: FaceImageInfo) {
        mManager.insert(ImageInfoTable.TABLE_NAME, faceImageInfo.toContentValues())
    }

    fun getImageInfo(imgId: Int, path: String): FaceImageInfo? {
        val select = "${ImageInfoTable.IMG_ID}=?"
        val selectArgs = arrayOf("$imgId")
        val cursor = mManager.query(ImageInfoTable.TABLE_NAME, null, select, selectArgs, null)
        cursor?.use {
            if (it.moveToNext()) {
                val imgId = it.getInt(it.getColumnIndex(ImageInfoTable.IMG_ID))
                val faceCount = it.getInt(it.getColumnIndex(ImageInfoTable.FACE_COUNT))
                val facePercent = it.getFloat(it.getColumnIndex(ImageInfoTable.FACE_AREA_PERCENT))
                return FaceImageInfo(imgId, path, faceCount, facePercent)
            }
        }
        return null
    }

    fun updateAllImageInvalid() {
        val contentValues = ContentValues()
        contentValues.put(ImageInfoTable.IS_VALID, "0")
        mManager.update(ImageInfoTable.TABLE_NAME, contentValues, null, null)
    }

    fun updateImageValid(imgId: Int, valid: Boolean) {
        val contentValues = ContentValues()
        contentValues.put(ImageInfoTable.IS_VALID, valid)
        val select = "${ImageInfoTable.IMG_ID}=?"
        val selectArgs = arrayOf("$imgId")
        mManager.update(ImageInfoTable.TABLE_NAME, contentValues, select, selectArgs)
    }

    fun deleteAllInvalidImages() {
        val select = "${ImageInfoTable.IS_VALID}=?"
        val selectArgs = arrayOf("0")
        mManager.delete(ImageInfoTable.TABLE_NAME, select, selectArgs)
    }
}