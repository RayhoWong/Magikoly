package com.glt.magikoly.function.views

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.subscribe.SubscribeController
import com.glt.magikoly.utils.DrawUtils
import magikoly.magiccamera.R

class SecondaryMenuAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var menuInfoList: ArrayList<MenuInfo> = ArrayList()
    var listener: SecondaryMenu.MenuItemClickListener? = null


    fun setMenuInfos(menuInfos: ArrayList<MenuInfo>) {
        menuInfoList.clear()
        menuInfoList.addAll(menuInfos)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return menuInfoList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(FaceAppState.getContext())
                .inflate(R.layout.face_report_secondary_menu_item, null)
        itemView.layoutParams = RecyclerView.LayoutParams(DrawUtils.dip2px(80f),
                RecyclerView.LayoutParams.WRAP_CONTENT)
        return MenuItemHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val menuInfo = menuInfoList[position]
        val itemView = holder.itemView.findViewById<SecondaryItemView>(R.id.item_view)
        itemView.setText(menuInfo.title)
        itemView.icon = itemView.context.resources.getDrawable(menuInfo.icon)
        itemView.highLight = itemView.context.resources.getDrawable(menuInfo.highLight)
        itemView.isSelected = menuInfo.isSelected
        val imgLockView = holder.itemView.findViewById<ImageView>(R.id.img_lock)
        if (menuInfo.isLock && !SubscribeController.getInstance().isVIP()) {
            imgLockView.visibility = View.VISIBLE
        } else {
            imgLockView.visibility = View.INVISIBLE
        }
        holder.itemView.setOnClickListener {
            listener?.onMenuItemClick(menuInfo.tabId)
        }
    }

    class MenuItemHolder(view: View) : RecyclerView.ViewHolder(view)

    class MenuInfo(val tabId: Int, val title: Int, val icon: Int, val highLight: Int,
                   var isLock: Boolean,
                   var isSelected: Boolean)
}
