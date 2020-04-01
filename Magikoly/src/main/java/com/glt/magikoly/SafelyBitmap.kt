package com.glt.magikoly

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Parcel
import android.os.Parcelable
import com.glt.magikoly.FaceEnv.InternalPath.BITMAP_CACHE_DIR
import com.glt.magikoly.utils.BitmapUtils
import java.io.File

class SafelyBitmap : Parcelable {

    private var url: String
    private var bitmap: Bitmap?

    constructor(bitmap: Bitmap) {
        val cacheFile = File(
                FaceEnv.InternalPath.getInnerFilePath(FaceAppState.getContext(), BITMAP_CACHE_DIR),
                bitmap.hashCode().toString())
        this.url = cacheFile.absolutePath
        this.bitmap = bitmap
    }

    constructor(parcel: Parcel) {
        url = parcel.readString()
        bitmap = BitmapFactory.decodeFile(url)
    }

    fun getBitmap(): Bitmap? = bitmap

    fun getWidth(): Int = bitmap?.width ?: 0

    fun getHeight(): Int = bitmap?.height ?: 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
        val cache = File(url)
        if (!cache.exists()) {
            bitmap?.let {
                BitmapUtils.saveBitmap(it, url, Bitmap.CompressFormat.JPEG)
            }
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SafelyBitmap> {
        override fun createFromParcel(parcel: Parcel): SafelyBitmap {
            return SafelyBitmap(parcel)
        }

        override fun newArray(size: Int): Array<SafelyBitmap?> {
            return arrayOfNulls(size)
        }
    }
}