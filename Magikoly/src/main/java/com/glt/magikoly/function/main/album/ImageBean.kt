package com.glt.magikoly.function.main.album

import android.os.Parcel
import android.os.Parcelable

/**
 * @desc:
 * @auther:duwei
 * @date:2019/2/12
 */

data class ImageBean(val mId: Int, val mPath: String):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(mId)
        parcel.writeString(mPath)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ImageBean> {
        override fun createFromParcel(parcel: Parcel): ImageBean {
            return ImageBean(parcel)
        }

        override fun newArray(size: Int): Array<ImageBean?> {
            return arrayOfNulls(size)
        }
    }
}