package com.glt.magikoly.download

import com.glt.magikoly.FaceAppState
import java.io.File

class DownloadManager private constructor() {


    //group下的所有监听
    private val mDownloadListenerMap: HashMap<Int, HashMap<Int, DownloadListener?>> = hashMapOf()
    //group监听
    private val mDownloadGroupListenerMap: HashMap<Int, DownloadListener?> = hashMapOf()

    companion object {
        val instance: DownloadManager by lazy { DownloadManager() }
        const val DOWNLOAD_GROUP_NULL = -1
        const val DOWNLOAD_GROUP_FILE = 1
        const val DOWNLOAD_GROUP_FILTER = 2
        val sBaseFilePath = FaceAppState.getContext().cacheDir.path ?: ""
    }


    fun startDownload(url: String, baseFilePath: String, fileName: String, listener: DownloadListener?, downloadGroup: Int? = DOWNLOAD_GROUP_NULL) {
        startDownload(url, baseFilePath, fileName, listener, null, downloadGroup)
    }

    fun startDownload(url: String, baseFilePath: String, fileName: String, listener: DownloadListener?, statisticListener: DownloadListener?, downloadGroup: Int? = DOWNLOAD_GROUP_NULL) {

        val downloadStatus = getDownloadStatus(url, baseFilePath, fileName)
        if (downloadStatus == DownloadStatus.STATUS_COMPLETED) {
            return
        }


        var map = mDownloadListenerMap.get(downloadGroup!!)
        if (map == null) {
            map = hashMapOf()
            mDownloadListenerMap.put(downloadGroup, map)
        }

        map.put(DownloadProxy.getInstance().getDownloadTaskId(url, baseFilePath, fileName), listener)

        if (downloadStatus == DownloadStatus.STATUS_DOWNLOADING) {
            return
        }

        DownloadProxy.getInstance().startDownload(FaceAppState.getContext(), url, baseFilePath, fileName, object : DownloadListener {
            override fun pending(task: DownloadTask) {
                statisticListener?.pending(task)
                mDownloadListenerMap.get(downloadGroup)?.get(task.id)?.pending(task)
                mDownloadGroupListenerMap.get(downloadGroup)?.pending(task)
            }

            override fun taskStart(task: DownloadTask) {
                statisticListener?.taskStart(task)
                mDownloadListenerMap.get(downloadGroup)?.get(task.id)?.taskStart(task)
                mDownloadGroupListenerMap.get(downloadGroup)?.taskStart(task)
            }

            override fun connectStart(task: DownloadTask) {
                statisticListener?.connectStart(task)
                mDownloadListenerMap.get(downloadGroup)?.get(task.id)?.connectStart(task)
                mDownloadGroupListenerMap.get(downloadGroup)?.connectStart(task)
            }

            override fun progress(task: DownloadTask) {
                statisticListener?.progress(task)
                mDownloadListenerMap.get(downloadGroup)?.get(task.id)?.progress(task)
                mDownloadGroupListenerMap.get(downloadGroup)?.progress(task)
            }

            override fun completed(task: DownloadTask) {
                statisticListener?.completed(task)
                mDownloadListenerMap.get(downloadGroup)?.get(task.id)?.completed(task)
                mDownloadGroupListenerMap.get(downloadGroup)?.completed(task)
            }

            override fun paused(task: DownloadTask) {
                statisticListener?.paused(task)
                mDownloadListenerMap.get(downloadGroup)?.get(task.id)?.paused(task)
                mDownloadGroupListenerMap.get(downloadGroup)?.paused(task)
            }

            override fun error(task: DownloadTask) {
                statisticListener?.error(task)
                mDownloadListenerMap.get(downloadGroup)?.get(task.id)?.error(task)
                mDownloadGroupListenerMap.get(downloadGroup)?.error(task)
            }

        })
    }


    fun setDownloadGroupListener(downloadListener: DownloadListener, downloadGroup: Int? = DOWNLOAD_GROUP_NULL) {
        mDownloadGroupListenerMap.put(downloadGroup!!, downloadListener)
    }

    fun setDownloadListener(url: String, baseFilePath: String, fileName: String, downloadListener: DownloadListener, downloadGroup: Int? = DOWNLOAD_GROUP_NULL) {
        var map = mDownloadListenerMap.get(downloadGroup!!)
        if (map == null) {
            map = hashMapOf()
            mDownloadListenerMap.put(downloadGroup, map)
        }

        map.put(DownloadProxy.getInstance().getDownloadTaskId(url, baseFilePath, fileName), downloadListener)
    }

    fun removeDownloadListener(downloadListener: DownloadListener, downloadGroup: Int? = DOWNLOAD_GROUP_NULL) {
        val map = mDownloadListenerMap.get(downloadGroup)
        val values = map?.keys
        values?.forEach {
            if (downloadListener == map[it]) {
                map.remove(it)
                return@forEach
            }
        }
    }

    fun removeGroupListener(downloadGroup: Int? = DOWNLOAD_GROUP_NULL) {
        mDownloadGroupListenerMap.remove(downloadGroup!!)
    }

    fun removeGroupAndUnderGroupListener(downloadGroup: Int? = DOWNLOAD_GROUP_NULL) {
        mDownloadGroupListenerMap.remove(downloadGroup!!)
        mDownloadListenerMap.remove(downloadGroup)
    }


    fun pauseDownload(url: String, baseFilePath: String, fileName: String) {
        val downloadTaskId = DownloadProxy.getInstance().getDownloadTaskId(url, baseFilePath, fileName)
        DownloadProxy.getInstance().pauseDownload(downloadTaskId)

    }

    fun cancelDownload(url: String, baseFilePath: String, fileName: String) {
        DownloadProxy.getInstance().cancelDownload(url, baseFilePath, fileName)
    }

    fun getDownloadProgress(url: String, baseFilePath: String, fileName: String): DownloadTask {
        return DownloadProxy.getInstance().getDownloadProgress(url, baseFilePath, fileName)
    }

    /**
     * @return {@link com.glt.magikoly.download.DownloadStatus}
     */
    fun getDownloadStatus(url: String, baseFilePath: String, fileName: String): Int {
        val downloadStatus = DownloadProxy.getInstance().getDownloadStatus(url, baseFilePath, fileName)
        if (downloadStatus == DownloadStatus.STATUS_NONE) {
            if (File(baseFilePath + File.separator + fileName).exists()) {
                return DownloadStatus.STATUS_COMPLETED
            }

        }

        return downloadStatus
    }

    fun getDownloadTaskId(url: String, baseFilePath: String, fileName: String): Int {
        return DownloadProxy.getInstance().getDownloadTaskId(url, baseFilePath, fileName)
    }

    fun getDestPath(baseFilePath: String, fileName: String): String {
        return baseFilePath + File.separator + fileName
    }

    fun getRealFileName(name: String, url: String): String {
        return name + "." + url.substringAfterLast(".")
    }
}