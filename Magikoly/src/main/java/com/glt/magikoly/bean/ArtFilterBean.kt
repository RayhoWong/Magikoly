package com.glt.magikoly.bean

/**
 *
 * @author rayhahah
 * @blog http://rayhahah.com
 * @time 2019/7/24
 * @tips 这个类是Object的子类
 * @fuction
 */
class ArtFilterBean(
        var cover: Int,
        var tag: String,
        var name: String,
        var zipUrl: String,
        var destPath: String,
        var isVIP: Boolean,
        var isLocal: Boolean,
        var sort: Int,
        var blurCover: Int,
        var isCheck: Boolean,
        var downloadStatus: Int,
        var isLock: Boolean,
        var isUseFreeCount: Boolean = false
) {

    companion object {
        const val DOWNLOAD_STATUS_WAIT = 0
        const val DOWNLOAD_STATUS_DOWNLOADED = 1
        const val DOWNLOAD_STATUS_DOWNLOADING = 2
    }

}

class ArtFilterData(val data: List<FilterBean>)

class FilterBean(
        val isLocal: Boolean,
        val tag: String,
        val name: String,
        val isVIP: Boolean,
        val url: String,
        val sort: Int)