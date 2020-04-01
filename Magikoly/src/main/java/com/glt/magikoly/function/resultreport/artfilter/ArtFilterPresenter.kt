package com.glt.magikoly.function.resultreport.artfilter

import android.content.Context
import com.glt.magikoly.bean.ArtFilterBean
import com.glt.magikoly.bean.ArtFilterData
import com.glt.magikoly.bean.FilterBean
import com.glt.magikoly.config.FilterSortConfigBean
import com.glt.magikoly.download.DownloadListener
import com.glt.magikoly.download.DownloadManager
import com.glt.magikoly.download.DownloadStatus
import com.glt.magikoly.download.DownloadTask
import com.glt.magikoly.ext.runAsync
import com.glt.magikoly.ext.runMain
import com.glt.magikoly.mvp.AbsPresenter
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.subscribe.SubscribeController
import com.glt.magikoly.utils.JsonHelper
import magikoly.magiccamera.R

/**
 *
 * @author rayhahah
 * @blog http://rayhahah.com
 * @time 2019/2/18
 * @tips 这个类是Object的子类
 * @fuction
 */
class ArtFilterPresenter : AbsPresenter<IArtFilterView>() {

    private val mFilterCover: HashMap<String, Int> = HashMap()
    private val mFilterBlurCover: HashMap<String, Int> = HashMap()

    init {
        mFilterCover.clear()
        mFilterCover.put("femme", R.drawable.cover_femme)
        mFilterCover.put("alpaca", R.drawable.cover_alpaca)
        mFilterCover.put("curly_hair", R.drawable.cover_curly_hair)
        mFilterCover.put("dragon", R.drawable.cover_dragon)
        mFilterCover.put("picnic", R.drawable.cover_picnic)
        mFilterCover.put("marmalade", R.drawable.cover_marmalade)
        mFilterCover.put("seagull", R.drawable.cover_seagull)
        mFilterCover.put("octopus", R.drawable.cover_octopus)
        mFilterCover.put("gothic", R.drawable.cover_gothic)
        mFilterCover.put("hot_tea", R.drawable.cover_hot_tea)
        mFilterCover.put("clean_line", R.drawable.cover_clena_line)
        mFilterCover.put("cowboy", R.drawable.cover_cowboy)
        mFilterCover.put("old_fashioned", R.drawable.cover_old_fashion)
        mFilterCover.put("jellyfish", R.drawable.cover_jellyfish)
        mFilterCover.put("tokyo", R.drawable.cover_tokyo)

        mFilterBlurCover.clear()
        mFilterBlurCover.put("femme", R.drawable.cover_blur_femme)
        mFilterBlurCover.put("alpaca", R.drawable.cover_blur_alpaca)
        mFilterBlurCover.put("curly_hair", R.drawable.cover_blur_curlyhair)
        mFilterBlurCover.put("dragon", R.drawable.cover_blur_dragon)
        mFilterBlurCover.put("picnic", R.drawable.cover_blur_picnic)
        mFilterBlurCover.put("marmalade", R.drawable.cover_blur_marmalade)
        mFilterBlurCover.put("seagull", R.drawable.cover_blur_seagull)
        mFilterBlurCover.put("octopus", R.drawable.cover_blur_octopus)
        mFilterBlurCover.put("gothic", R.drawable.cover_blur_gothic)
        mFilterBlurCover.put("hot_tea", R.drawable.cover_blur_hottea)
        mFilterBlurCover.put("clean_line", R.drawable.cover_blur_clenaline)
        mFilterBlurCover.put("cowboy", R.drawable.cover_blur_cowboy)
        mFilterBlurCover.put("old_fashioned", R.drawable.cover_blur_oldfashion)
        mFilterBlurCover.put("jellyfish", R.drawable.cover_blur_jellyfish)
        mFilterBlurCover.put("tokyo", R.drawable.cover_blur_tokyo)

    }

    fun loadFilterData(context: Context, onFinished: (ArrayList<ArtFilterBean>) -> Unit) {
        runAsync {
            val inputStream = context.assets.open("PrismaFilterData.json")
            val artFilterData = JsonHelper.getInstance().gson().fromJson<ArtFilterData>(inputStream.reader(), ArtFilterData::class.java)
            val filterSort = FilterSortConfigBean.getFilterSort()
            val result = ArrayList<ArtFilterBean>()
            result.clear()
            result.add(ArtFilterBean(R.drawable.cover_tokyo,
                    "original", "Original", "", "",
                    false, true, 0, R.drawable.cover_blur_tokyo, true, ArtFilterBean.DOWNLOAD_STATUS_DOWNLOADED, false))
            artFilterData?.apply {
                for (entry in filterSort) {
                    for (filterData in artFilterData.data) {
                        if (filterData.tag == entry.key) {
                            result.add(assemArtFilterBean(filterData, entry.value))
                        }
                    }
                }
            }
            result.sortBy {
                it.sort
            }
            runMain {
                onFinished(result)
            }
        }
    }

    private fun assemArtFilterBean(filterData: FilterBean, sort: Int): ArtFilterBean {
        return if (filterData.isLocal) {
            val artFilterBean = ArtFilterBean(mFilterCover.get(filterData.tag)!!,
                    filterData.tag, filterData.name, filterData.url,
                    filterData.url, filterData.isVIP, filterData.isLocal, sort,
                    mFilterBlurCover.get(filterData.tag)!!, false, ArtFilterBean.DOWNLOAD_STATUS_WAIT,
                    !SubscribeController.getInstance().isVIP(SubscribeController.PERMISSION_WITH_TRIAL) && filterData.isVIP)
            artFilterBean.downloadStatus = obtainDownloadStatus(artFilterBean)
            obtainDestPath(artFilterBean)
            artFilterBean
        } else {
            val artFilterBean = ArtFilterBean(mFilterCover.get(filterData.tag)!!,
                    filterData.tag, filterData.name, filterData.url,
                    "", filterData.isVIP, filterData.isLocal, sort,
                    mFilterBlurCover.get(filterData.tag)!!, false, ArtFilterBean.DOWNLOAD_STATUS_WAIT,
                    !SubscribeController.getInstance().isVIP(SubscribeController.PERMISSION_WITH_TRIAL) && filterData.isVIP)
            artFilterBean.downloadStatus = obtainDownloadStatus(artFilterBean)
            obtainDestPath(artFilterBean)
            artFilterBean
        }
    }

    fun obtainDestPath(artFilterBean: ArtFilterBean) {
        if (artFilterBean.downloadStatus == ArtFilterBean.DOWNLOAD_STATUS_DOWNLOADED) {
            if (artFilterBean.isLocal) {
                artFilterBean.destPath = artFilterBean.zipUrl
            } else {
                artFilterBean.destPath = DownloadManager.instance.getDestPath(DownloadManager.sBaseFilePath,
                        DownloadManager.instance.getRealFileName(artFilterBean.tag, artFilterBean.zipUrl))
            }
        }
    }

    /**
     * 开启下载
     */
    fun doDownload(tag: String, zipUrl: String, isVip: Boolean) {
        DownloadManager.instance.startDownload(zipUrl, DownloadManager.sBaseFilePath,
                DownloadManager.instance.getRealFileName(tag, zipUrl),
                null, ArtFilterStatisticListener(tag, isVip),
                DownloadManager.DOWNLOAD_GROUP_FILTER)
    }


    fun obtainDownloadStatus(artFilterBean: ArtFilterBean): Int {
        if (artFilterBean.isLocal) {
            return ArtFilterBean.DOWNLOAD_STATUS_DOWNLOADED
        }

        val downloadStatus = DownloadManager.instance.getDownloadStatus(artFilterBean.zipUrl,
                DownloadManager.sBaseFilePath,
                DownloadManager.instance.getRealFileName(artFilterBean.tag, artFilterBean.zipUrl))
        return when (downloadStatus) {
            DownloadStatus.STATUS_COMPLETED -> {
                ArtFilterBean.DOWNLOAD_STATUS_DOWNLOADED
            }
            DownloadStatus.STATUS_DOWNLOADING -> {
                ArtFilterBean.DOWNLOAD_STATUS_DOWNLOADING
            }
            DownloadStatus.STATUS_ERROR,
            DownloadStatus.STATUS_PAUSED,
            DownloadStatus.STATUS_NONE -> {
                ArtFilterBean.DOWNLOAD_STATUS_WAIT
            }
            else -> {
                ArtFilterBean.DOWNLOAD_STATUS_WAIT
            }
        }
    }

    fun obtainFilterBeanDefault(artFilterBean: ArtFilterBean) {
        artFilterBean.downloadStatus = ArtFilterBean.DOWNLOAD_STATUS_WAIT
        artFilterBean.destPath = ""
    }

    class ArtFilterStatisticListener(val tag: String, val isVip: Boolean) : DownloadListener {
        override fun pending(task: DownloadTask) {
        }

        override fun taskStart(task: DownloadTask) {
        }

        override fun connectStart(task: DownloadTask) {
        }

        override fun progress(task: DownloadTask) {
        }

        override fun completed(task: DownloadTask) {
            uploadFilterDownload(tag, isVip, true)
        }

        override fun paused(task: DownloadTask) {
        }

        override fun error(task: DownloadTask) {
            uploadFilterDownload(tag, isVip, false)
        }


        private fun uploadFilterDownload(tag: String, isVip: Boolean, success: Boolean) {
            BaseSeq103OperationStatistic.uploadSqe103StatisticData(tag,
                    Statistic103Constant.ART_FILTER_DOWNLOAD, if (isVip) {
                "1"
            } else {
                "2"
            }, if (success) {
                "1"
            } else {
                "2"
            }, "")
        }
    }
}