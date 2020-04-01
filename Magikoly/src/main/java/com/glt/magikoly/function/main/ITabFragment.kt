package com.glt.magikoly.function.main

import android.graphics.drawable.Drawable
import me.yokeyword.fragmentation.ISupportFragment

interface ITabFragment : ISupportFragment {
    var fromClick: Boolean
    fun getTabId(): Int
    fun getToolBarTitle(): String
    fun getToolBarBackDrawable(): Drawable?
    fun getToolBarMenuDrawable(): Drawable?
    fun getToolBarBackCallback(): ToolBarCallback?
    fun getToolBarMenuCallback(): ToolBarCallback?
    fun getToolBarSelfCallback(): ToolBarCallback?
    fun getToolBarItemColor(): Int?
    fun getBottomBarTitle(): String
    fun getBottomBarIcon(): Array<Drawable>?
    fun getTabLock(): Boolean
    fun reload()
    fun onTabFragmentVisible()
    fun onTabFragmentInvisible()

    fun getStatus(): Int
    fun setStatus(status: Int)

    companion object {
        const val STATUS_OPEN = 0
        const val STATUS_ERROR = 1
        const val STATUS_PURCHASE = 2
    }
}

interface INewTabFragment{
    fun onExit()
}

interface ToolBarCallback {
    fun invoke(): Boolean
}

interface ISubscribe {
    var watchAdFinish: Boolean
}