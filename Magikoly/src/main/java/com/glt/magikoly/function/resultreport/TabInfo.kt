package com.glt.magikoly.function.resultreport

import android.os.Parcel
import android.os.Parcelable

class TabInfo(var tabId: Int = TAB_EFFECTS, var curSubTabId: Int = SUB_TAB_AGING) : Parcelable {

    var preSubTabId: Int = -1

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt()) {
        preSubTabId = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(tabId)
        parcel.writeInt(curSubTabId)
        parcel.writeInt(preSubTabId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TabInfo> {
        const val TAB_EFFECTS = 0 //特效
        const val TAB_BEAUTY = 1 //美颜
        const val TAB_ANALYSIS = 2 //分析

        //sub tab
        const val SUB_TAB_AGING = 0 //变老
        const val SUB_TAB_CHILD = 1 //童颜
        const val SUB_TAB_BABY = 2 //宝宝
        const val SUB_TAB_GENDER = 3 //变性
        const val SUB_TAB_TUNING = 4 //美颜
        const val SUB_TAB_ETHNICITY = 5 //种族
        const val SUB_TAB_FILTER = 6 //美颜滤镜入口
        const val SUB_TAB_ART_FILTER = 7 //艺术滤镜
        const val SUB_TAB_ANIMAL = 8 //动物

        override fun createFromParcel(parcel: Parcel): TabInfo {
            return TabInfo(parcel)
        }

        override fun newArray(size: Int): Array<TabInfo?> {
            return arrayOfNulls(size)
        }
    }
}