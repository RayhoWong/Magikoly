package com.glt.magikoly.function.main.album

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.glt.magikoly.FaceAppState
import magikoly.magiccamera.R
import kotlinx.android.synthetic.main.album_recycler_item.view.*
import java.io.File

/**
 * @desc:
 * @auther:duwei
 * @date:2019/2/12
 */
class AlbumAdapter(private val folders: List<ImageFolderBean>,
                   private val itemClick: (ImageFolderBean) -> Unit) :
    RecyclerView.Adapter<AlbumAdapter.AlbumHolder>() {

    companion object {
        private var photoString: String = FaceAppState.getContext().resources.getString(R.string.string_photos)
        private const val TYPE_HEADER = 0
        private const val TYPE_NORMAL = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_NORMAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumHolder {
        val inflater = LayoutInflater.from(parent.context)
        var itemView = if (viewType == TYPE_HEADER)
            inflater.inflate(R.layout.album_recycler_header, parent, false)
        else
            inflater.inflate(R.layout.album_recycler_item, parent, false)
        return AlbumHolder(itemView)
    }

    override fun getItemCount(): Int = folders.size +1


    override fun onBindViewHolder(holder: AlbumHolder, position: Int) {
        if (getItemViewType(position) == TYPE_HEADER) return
        holder.bindFolder(folders[position-1], itemClick)
    }


    class AlbumHolder(convertView: View) : RecyclerView.ViewHolder(convertView) {

        fun bindFolder(folder: ImageFolderBean, itemClick: (ImageFolderBean) -> Unit) = with(folder) {
            itemView.album_item_tv.text = folder.mFolderName
            itemView.album_item_num.text = folder.mImageList?.size.toString() + " " + AlbumAdapter.photoString
            Glide.with(FaceAppState.getContext())
                .load(File(folder.mImageList!![0].mPath))
                .into(itemView.album_item_image)
            itemView.setOnClickListener { itemClick(this) }
        }

    }
}