package com.glt.magikoly.function.main.discovery

import android.graphics.Bitmap
import android.graphics.Color
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.glt.magikoly.bean.net.SearchImageDTO
import com.glt.magikoly.utils.BitmapUtils
import com.glt.magikoly.utils.DrawUtils
import com.glt.magikoly.view.RoundedImageView
import magikoly.magiccamera.R

/**
 *
 * @author rayhahah
 * @blog http://rayhahah.com
 * @time 2019/2/15
 * @tips 这个类是Object的子类
 * @fuction
 */
class SearchResultImagesAdapter(private val recyclerView: RecyclerView, val searchImageList: ArrayList<SearchImageDTO>,
                                private val clickListener: OnSearchResultClickListener) : RecyclerView.Adapter<SearchResultImagesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultImagesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search_result, parent, false)
//        val imgView = RoundedImageView(recyclerView.context)
//        imgView.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT,
//                                                         RecyclerView.LayoutParams.WRAP_CONTENT)
        return SearchResultImagesViewHolder(view)
    }

    override fun getItemCount(): Int = searchImageList.size

    override fun onBindViewHolder(holder: SearchResultImagesViewHolder, position: Int) {
        val ivResult = holder.rivSearchResult
//        val ivResult = holder.itemView as RoundedImageView
//        ivResult.layoutParams.height = recyclerView.width / FaceImagesFragment.SPAN_SIZE
//        ivResult.scaleType = ImageView.ScaleType.CENTER_CROP
//        ivResult.cornerRadius = DrawUtils.dip2px(10f).toFloat()

        ivResult.cornerRadius = DrawUtils.dip2px(10f).toFloat()
        val searchImageDTO = searchImageList[position]
        if (holder.tag != searchImageDTO) {
            ivResult.setImageBitmap(null)
            Glide.with(ivResult).clear(ivResult)
            holder.tag = searchImageDTO
//            val options = RequestOptions().centerCrop().override(ivResult.layoutParams.height)
        }

        val placeHolderRes = when (position % 4) {
            1 -> {
                ivResult.setBackgroundColor(Color.parseColor("#f2ecfc"))
                R.drawable.img_placeholder_failed1
            }
            2 -> {
                ivResult.setBackgroundColor(Color.parseColor("#eaeeff"))
                R.drawable.img_placeholder_failed2
            }
            3 -> {
                ivResult.setBackgroundColor(Color.parseColor("#fff3f0"))
                R.drawable.img_placeholder_failed3
            }
            0 -> {
                ivResult.setBackgroundColor(Color.parseColor("#ecfbff"))
                R.drawable.img_placeholder_failed4
            }
            else -> {
                ivResult.setBackgroundColor(Color.parseColor("#f2ecfc"))
                R.drawable.img_placeholder_failed1
            }
        }
        holder.ivSearchError.setImageResource(placeHolderRes)
        holder.ivSearchError.visibility = View.GONE
        val options = RequestOptions().centerCrop()
        val listener = object : RequestListener<Bitmap> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                holder.ivSearchError.visibility = View.VISIBLE
                return false
            }

            override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                holder.ivSearchError.visibility = View.GONE
                return false
            }

        }
        if (BitmapUtils.isBase64Img(searchImageDTO.thumbnail)) {
            Glide.with(ivResult).asBitmap().load(BitmapUtils.getByteForBase64Url(searchImageDTO.thumbnail))
                    .listener(listener)
                    .apply(options).into(ivResult)
        } else {
            Glide.with(ivResult).asBitmap()
                    .listener(listener)
                    .load(searchImageDTO.thumbnail).apply(options).into(ivResult)
        }

        ivResult.setOnClickListener {
            clickListener.onImageItemClick(searchImageDTO)
        }
    }

    fun autoNotify(old: List<SearchImageDTO>, new: List<SearchImageDTO>
                   , compareItemSame: (old: SearchImageDTO, new: SearchImageDTO) -> Boolean
                   , compareContentSame: (old: SearchImageDTO, new: SearchImageDTO) -> Boolean) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return compareItemSame(old[oldItemPosition], new[newItemPosition])
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return compareContentSame(old[oldItemPosition], new[newItemPosition])
            }

            override fun getOldListSize() = old.size

            override fun getNewListSize() = new.size
        })
        this.searchImageList.clear()
        this.searchImageList.addAll(new)

        diff.dispatchUpdatesTo(this)
    }
}

interface OnSearchResultClickListener {
    fun onImageItemClick(searchResult: SearchImageDTO)
}

class SearchResultImagesViewHolder(searchResultView: View) : RecyclerView.ViewHolder(searchResultView) {
    var tag: Any? = null
    val rivSearchResult: RoundedImageView = itemView.findViewById<RoundedImageView>(R.id.riv_search_result)
    val ivSearchError: ImageView = itemView.findViewById<ImageView>(R.id.iv_error_holder)
}