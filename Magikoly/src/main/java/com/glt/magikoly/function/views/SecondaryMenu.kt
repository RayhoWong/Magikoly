package com.glt.magikoly.function.views

import android.content.Context
import android.graphics.Rect
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.glt.magikoly.utils.DrawUtils

class SecondaryMenu(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {

    private var listener: MenuItemClickListener? = null

    init {
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = HORIZONTAL
        this.layoutManager = layoutManager
        addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?,
                                        state: RecyclerView.State?) {
                super.getItemOffsets(outRect, view, parent, state)
                val marginH = DrawUtils.dip2px(4f)
                val marginTop = DrawUtils.dip2px(13.7f)
                val marginBottom = DrawUtils.dip2px(9f)
                outRect?.set(marginH, marginTop, marginH, marginBottom)
            }
        })
        itemAnimator = DefaultItemAnimator()
        adapter = SecondaryMenuAdapter()
    }

    fun setMenuInfos(menuInfos: ArrayList<SecondaryMenuAdapter.MenuInfo>) {
        (adapter as SecondaryMenuAdapter).listener = listener
        (adapter as SecondaryMenuAdapter).setMenuInfos(menuInfos)
    }

    fun switchMenu(tabId: Int) {
        (adapter as SecondaryMenuAdapter).listener?.onMenuItemClick(tabId)
    }

    fun setMenuItemClickListener(listener: MenuItemClickListener) {
        this.listener = listener
    }

    interface MenuItemClickListener {
        fun onMenuItemClick(tabId: Int)
    }
}