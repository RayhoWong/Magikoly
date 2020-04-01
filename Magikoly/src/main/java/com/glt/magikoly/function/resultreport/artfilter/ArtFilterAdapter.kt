package com.glt.magikoly.function.resultreport.artfilter

import android.graphics.Bitmap
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.glt.magikoly.bean.ArtFilterBean
import com.glt.magikoly.download.DownloadListener
import com.glt.magikoly.download.DownloadManager
import com.glt.magikoly.download.DownloadTask
import magikoly.magiccamera.R


/**
 *
 * @author rayhahah
 * @blog http://rayhahah.com
 * @time 2019/2/15
 * @tips 这个类是Object的子类
 * @fuction
 */
class ArtFilterAdapter(private val recyclerView: RecyclerView, val data: ArrayList<ArtFilterBean>,
                       private val clickListener: OnClickListener, val original: Bitmap?, val presenter: ArtFilterPresenter) : RecyclerView.Adapter<ArtFilterViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtFilterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_art_filter, parent, false)
//        val imgView = RoundedImageView(recyclerView.context)
//        imgView.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT,
//                                                         RecyclerView.LayoutParams.WRAP_CONTENT)
        return ArtFilterViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ArtFilterViewHolder, position: Int) {
        val artFilterBean = data[position]

        holder.bean = artFilterBean

        val cover = holder.ivFilterCover

        if (holder.tag != artFilterBean) {
            cover.setImageBitmap(null)
            Glide.with(cover).clear(cover)
            holder.tag = artFilterBean
//            val options = RequestOptions().centerCrop().override(ivResult.layoutParams.height)
        }
//        val options = RequestOptions().centerCrop()
//        Glide.with(cover).asBitmap().load(artFilterBean.cover)
//                .listener(listener)
//                .apply(options).into(cover)

        if (artFilterBean.tag == "original") {
            cover.setImageBitmap(original)
        } else {
            cover.setImageResource(artFilterBean.cover)
        }

        holder.tvName.text = artFilterBean.name


        artFilterBean.downloadStatus = presenter.obtainDownloadStatus(artFilterBean)
        holder.setViewStatus(artFilterBean.isCheck, artFilterBean.downloadStatus, artFilterBean.isLock)

        holder.cardFilter.setOnClickListener {
            clickListener.onItemClick(artFilterBean, holder, position)
        }
    }
}

interface OnClickListener {
    fun onItemClick(artFilterBean: ArtFilterBean, holder: ArtFilterViewHolder, position: Int)
}

class ArtFilterViewHolder(artFiltertView: View) : RecyclerView.ViewHolder(artFiltertView) {
    var tag: Any? = null

    var bean: ArtFilterBean? = null

    val ivFilterCover: ImageView = itemView.findViewById<ImageView>(R.id.iv_filter_cover)
    val cardFilter: CardView = itemView.findViewById<CardView>(R.id.card_filter)
    val tvName: TextView = itemView.findViewById<TextView>(R.id.tv_name)
    val ivDownload: ImageView = itemView.findViewById<ImageView>(R.id.iv_download)
    val ivLock: ImageView = itemView.findViewById<ImageView>(R.id.iv_lock)

    val viewMask: View = itemView.findViewById<View>(R.id.view_mask)
    val ivTick: ImageView = itemView.findViewById<ImageView>(R.id.iv_tick)
    val progress: ProgressBar = itemView.findViewById<ProgressBar>(R.id.progress)

    fun setViewStatus(isCheck: Boolean, downloadStatus: Int, isLock: Boolean) {
        when (downloadStatus) {
            ArtFilterBean.DOWNLOAD_STATUS_WAIT -> {
                if (isCheck) {
                    viewMask.visibility = View.VISIBLE

                    if (isLock) {
                        progress.visibility = View.GONE
                        ivTick.visibility = View.VISIBLE
                    } else {
                        progress.visibility = View.VISIBLE
                        ivTick.visibility = View.GONE
                    }

                } else {
                    viewMask.visibility = View.GONE
                    progress.visibility = View.GONE
                    ivTick.visibility = View.GONE
                }

                ivDownload.visibility = View.VISIBLE
                ivLock.visibility = if (isLock) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                bean?.apply {
                    DownloadManager.instance.setDownloadListener(zipUrl,
                            DownloadManager.sBaseFilePath,
                            DownloadManager.instance.getRealFileName(tag, zipUrl), downloadListener, DownloadManager.DOWNLOAD_GROUP_FILTER)
                }
            }
            ArtFilterBean.DOWNLOAD_STATUS_DOWNLOADED -> {
                if (isCheck) {
                    viewMask.visibility = View.VISIBLE
                    progress.visibility = View.GONE
                    ivTick.visibility = View.VISIBLE
                } else {
                    viewMask.visibility = View.GONE
                    progress.visibility = View.GONE
                    ivTick.visibility = View.GONE
                }

                ivDownload.visibility = View.GONE
                ivLock.visibility = if (isLock) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            }
            ArtFilterBean.DOWNLOAD_STATUS_DOWNLOADING -> {
                if (isCheck) {
                    viewMask.visibility = View.VISIBLE
                    progress.visibility = View.VISIBLE
                    ivTick.visibility = View.GONE
                } else {
                    viewMask.visibility = View.VISIBLE
                    progress.visibility = View.VISIBLE
                    ivTick.visibility = View.GONE
                }

                ivDownload.visibility = View.VISIBLE
                ivLock.visibility = if (isLock) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                bean?.apply {
                    DownloadManager.instance.setDownloadListener(zipUrl,
                            DownloadManager.sBaseFilePath,
                            DownloadManager.instance.getRealFileName(tag, zipUrl),
                            downloadListener, DownloadManager.DOWNLOAD_GROUP_FILTER)
                }
            }
            else -> {
            }
        }


    }

    val downloadListener = object : DownloadListener {
        override fun pending(task: DownloadTask) {
        }

        override fun taskStart(task: DownloadTask) {
            bean?.apply {
                if (DownloadManager.instance.getDownloadTaskId(zipUrl, DownloadManager.sBaseFilePath, DownloadManager.instance.getRealFileName(tag, zipUrl)) == task.id) {
                    setViewStatus(isCheck, ArtFilterBean.DOWNLOAD_STATUS_DOWNLOADING, isLock)
                    downloadStatus = ArtFilterBean.DOWNLOAD_STATUS_DOWNLOADING
                }
            }
        }

        override fun connectStart(task: DownloadTask) {
        }

        override fun progress(task: DownloadTask) {
            bean?.apply {
                if (DownloadManager.instance.getDownloadTaskId(zipUrl,
                                DownloadManager.sBaseFilePath,
                                DownloadManager.instance.getRealFileName(tag, zipUrl)) == task.id) {
//                    setViewStatus(isCheck, ArtFilterBean.DOWNLOAD_STATUS_DOWNLOADING, isLock)
                    progress.progress = (task.currentProgress * 100f / task.totalLength).toInt()
                    downloadStatus = ArtFilterBean.DOWNLOAD_STATUS_DOWNLOADING
                }
            }
        }

        override fun completed(task: DownloadTask) {
            bean?.apply {
                if (DownloadManager.instance.getDownloadTaskId(zipUrl,
                                DownloadManager.sBaseFilePath,
                                DownloadManager.instance.getRealFileName(tag, zipUrl)) == task.id) {
                    setViewStatus(isCheck, ArtFilterBean.DOWNLOAD_STATUS_DOWNLOADED, isLock)
                    downloadStatus = ArtFilterBean.DOWNLOAD_STATUS_DOWNLOADED
                }
            }
        }

        override fun paused(task: DownloadTask) {
        }

        override fun error(task: DownloadTask) {
        }

    }
}