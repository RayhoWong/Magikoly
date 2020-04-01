package com.glt.magikoly.function.main

import android.content.Context
import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.LinearLayout
import java.util.*


/**
 * Created by yangguanxiang
 */
class BottomBar constructor(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val mTabs = ArrayList<BottomBarTab>()

    private var mTabLayout: LinearLayout? = null

    private var mTabParams: LinearLayout.LayoutParams? = null

    var currentItemPosition = 0

    private var mListener: OnTabSelectedListener? = null

    init {
        init(context)
    }

    private fun init(context: Context) {
        orientation = LinearLayout.VERTICAL
        mTabLayout = LinearLayout(context)
        mTabLayout!!.setBackgroundColor(Color.WHITE)
        mTabLayout!!.orientation = LinearLayout.HORIZONTAL
        addView(mTabLayout, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT))

        mTabParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
        mTabParams!!.weight = 1f
    }

    fun addItem(tab: BottomBarTab): BottomBar {
        tab.setOnClickListener(OnClickListener {
            if (mListener == null) return@OnClickListener

            val pos = tab.tabPosition
            if (currentItemPosition == pos) {
                mListener!!.onTabReselected(pos)
            } else {
                mListener!!.onTabSelected(pos, currentItemPosition)
                tab.isSelected = true
                mListener!!.onTabUnselected(currentItemPosition)
                mTabs[currentItemPosition].isSelected = false
                currentItemPosition = pos
            }
        })
        tab.tabPosition = mTabLayout!!.childCount
        tab.layoutParams = mTabParams
        mTabLayout!!.addView(tab)
        mTabs.add(tab)
        return this
    }

    fun setOnTabSelectedListener(onTabSelectedListener: OnTabSelectedListener) {
        mListener = onTabSelectedListener
    }

    fun setCurrentItem(position: Int) {
        mTabLayout!!.post { mTabLayout!!.getChildAt(position).performClick() }
    }

    /**
     * 获取 Tab
     */
    fun getItem(index: Int): BottomBarTab? {
        return if (mTabs.size < index) null else mTabs[index]
    }

    interface OnTabSelectedListener {
        fun onTabSelected(position: Int, prePosition: Int)

        fun onTabUnselected(position: Int)

        fun onTabReselected(position: Int)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        return SavedState(superState, currentItemPosition)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)

        if (currentItemPosition != ss.position) {
            mTabLayout!!.getChildAt(currentItemPosition).isSelected = false
            mTabLayout!!.getChildAt(ss.position).isSelected = true
        }
        currentItemPosition = ss.position
    }

    internal class SavedState : View.BaseSavedState {
        var position: Int = 0

        constructor(source: Parcel) : super(source) {
            position = source.readInt()
        }

        constructor(superState: Parcelable, position: Int) : super(superState) {
            this.position = position
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(position)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}
