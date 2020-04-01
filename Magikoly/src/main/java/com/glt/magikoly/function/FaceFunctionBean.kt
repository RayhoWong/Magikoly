package com.glt.magikoly.function

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Parcel
import android.os.Parcelable
import com.glt.magikoly.SafelyBitmap
import com.glt.magikoly.bean.FaceInfo
import com.glt.magikoly.bean.S3ImageInfo

class FaceFunctionBean : Parcelable {

    lateinit var photo: SafelyBitmap
    lateinit var face: SafelyBitmap
    lateinit var facePath: String
    lateinit var faceRect: Rect
    lateinit var imageInfo: S3ImageInfo
    lateinit var faceInfo: FaceInfo
    var category: String = ""

    constructor()
    constructor(photo: Bitmap, face: Bitmap, facePath: String,
                faceRect: Rect, imageInfo: S3ImageInfo, faceInfo: FaceInfo) {
        this.photo = SafelyBitmap(photo)
        this.face = SafelyBitmap(face)
        this.facePath = facePath
        this.faceRect = faceRect
        this.imageInfo = imageInfo
        this.faceInfo = faceInfo
    }

    constructor(source: Parcel?) : super() {
        source?.apply {
            photo = readParcelable(SafelyBitmap::class.java.classLoader)
            face = readParcelable(SafelyBitmap::class.java.classLoader)
            facePath = readString()
            faceRect = readParcelable(Rect::class.java.classLoader)
            imageInfo = readSerializable() as S3ImageInfo
            faceInfo = readSerializable() as FaceInfo
            category = readString()
        }
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.apply {
            writeParcelable(photo, 0)
            writeParcelable(face, 0)
            writeString(facePath)
            writeParcelable(faceRect, 0)
            writeSerializable(imageInfo)
            writeSerializable(faceInfo)
            writeString(category)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FaceFunctionBean> {

        override fun createFromParcel(parcel: Parcel): FaceFunctionBean {
            return FaceFunctionBean(parcel)
        }

        override fun newArray(size: Int): Array<FaceFunctionBean?> {
            return arrayOfNulls(size)
        }
    }
}