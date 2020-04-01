package com.glt.magikoly.function.main.album

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.glt.magikoly.utils.DrawUtils
import com.glt.magikoly.view.RoundedImageView
import magikoly.magiccamera.R

/**
 * @desc:
 * @auther:duwei
 * @date:2019/2/14
 */
class AlbumDetailAdapter(private val mData: List<ImageBean>, private var listener: ItemClickListener?) :
    RecyclerView.Adapter<AlbumDetailAdapter.AlbumDetailHolder>() {

    companion object {
        private val imageHeight = DrawUtils.dip2px(108f)
        val SPACE = DrawUtils.dip2px(3f)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumDetailHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.album_detail_item, null)
        return AlbumDetailHolder(view)
    }

    override fun getItemCount(): Int = mData.size

    override fun onBindViewHolder(holder: AlbumDetailHolder, position: Int) {
        val imageBean = mData[position]
        val imgView = holder.itemView as RoundedImageView
        if (holder.tag != imageBean) {
            imgView.setImageBitmap(null)
            Glide.with(imgView).clear(imgView)
            holder.tag = imageBean
            val options = RequestOptions()
                .centerCrop().override(imageHeight)
            Glide.with(imgView).asBitmap().load(imageBean.mPath).apply(options)
                .into(imgView)
            imgView.setOnClickListener {
                listener?.onItemClick(imageBean.mPath)
            }
        }
    }

    class AlbumDetailHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tag: Any? = null
    }

    interface ItemClickListener {
        fun onItemClick(imgPath : String)
    }
}
