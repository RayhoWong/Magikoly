package com.glt.magikoly

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Parcel
import android.os.Parcelable
import com.glt.magikoly.utils.BitmapUtils
import java.io.File

class SafelyDrawable : Parcelable {

    private var url: String
    private var drawable: BitmapDrawable? = null

    constructor(drawable: BitmapDrawable) {
        val bitmap = drawable.bitmap
        val cacheFile = File(FaceEnv.InternalPath.getInnerFilePath(FaceAppState.getContext(),
                FaceEnv.InternalPath.BITMAP_CACHE_DIR), bitmap.hashCode().toString())
        this.url = cacheFile.absolutePath
        this.drawable = drawable
    }

    constructor(parcel: Parcel) {
        url = parcel.readString()
        val bitmap = BitmapFactory.decodeFile(url)
        bitmap?.let {
            drawable = BitmapDrawable(FaceAppState.getContext().resources, it)
        }
    }

    fun getDrawable(): BitmapDrawable? = drawable

    fun getIntrinsicWidth(): Int = drawable?.intrinsicWidth ?: 0

    fun getIntrinsicHeight(): Int = drawable?.intrinsicHeight ?: 0

    fun setBounds(rect: Rect) {
        drawable?.bounds = rect
    }

    fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        drawable?.setBounds(left, top, right, bottom)
    }

    fun setAlpha(alpha: Int) {
        drawable?.alpha = alpha
    }

    fun getAlpha(): Int = drawable?.alpha ?: 0

    fun setColorFilter(color: Int, mode: PorterDuff.Mode) {
        drawable?.setColorFilter(color, mode)
    }

    fun setColorFilter(colorFilter: ColorFilter) {
        drawable?.colorFilter = colorFilter
    }

    fun draw(canvas: Canvas) {
        drawable?.draw(canvas)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
        val cache = File(url)
        if (!cache.exists()) {
            drawable?.let {
                BitmapUtils.saveBitmap(it.bitmap, url, Bitmap.CompressFormat.JPEG)
            }
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SafelyDrawable> {
        override fun createFromParcel(parcel: Parcel): SafelyDrawable {
            return SafelyDrawable(parcel)
        }

        override fun newArray(size: Int): Array<SafelyDrawable?> {
            return arrayOfNulls(size)
        }
    }
}