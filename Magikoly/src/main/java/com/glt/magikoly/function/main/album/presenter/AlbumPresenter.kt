package com.glt.magikoly.function.main.album.presenter

import android.Manifest
import android.annotation.SuppressLint
import android.support.annotation.RequiresPermission
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.function.ImageScanner
import com.glt.magikoly.function.main.album.IAlbumView
import com.glt.magikoly.function.main.album.ImageBean
import com.glt.magikoly.function.main.album.ImageFolderBean
import com.glt.magikoly.mvp.AbsPresenter
import com.glt.magikoly.permission.PermissionHelper
import com.glt.magikoly.thread.FaceThreadExecutorProxy
import java.util.*

class AlbumPresenter : AbsPresenter<IAlbumView>() {
    companion object {
        const val FAIL_NO_PERMISSION = 1
    }

    private val mLocalImages = ArrayList<ImageBean>()
    private val mImageFolders = ArrayList<ImageFolderBean>()


    fun getLocalImageFoldersAsync(listener: IGetFolderListener) {
        FaceThreadExecutorProxy.execute {
            getLocalImageFolders(listener)
        }
    }

    @SuppressLint("MissingPermission")
    @Synchronized
    private fun getLocalImageFolders(listener: IGetFolderListener) {
        if (!PermissionHelper.hasReadStoragePermission(FaceAppState.getContext())) {
            listener.onFail(FAIL_NO_PERMISSION)
            return
        }
        if (mLocalImages.isEmpty()) {
            getLocalImages2Global()
        }

        for (imageBean in ArrayList(mLocalImages)) {
            var isAdd = false
            val path = imageBean.mPath

            val pathSplit = path.split("/")

            if (pathSplit.size >= 3) {
                val folderName1 = pathSplit[pathSplit.size - 2] /*+ "/" + pathSplit[pathSplit.size - 2]*/
                if (mImageFolders.size > 0) {
                    for (folderBean in mImageFolders.indices) {//遍历是否包含
                        val folderName2 = mImageFolders[folderBean].mFolderName
                        if (folderName2 == folderName1) {
                            val images = mImageFolders[folderBean].mImageList
                            if (images != null) {
                                images.add(imageBean)
                                isAdd = true
                                break
                            }
                        }
                    }
                }
                if (!isAdd) {
                    val folderBean = ImageFolderBean()
                    folderBean.mFolderName = folderName1
                    val images = ArrayList<ImageBean>()
                    images.add(imageBean)
                    folderBean.mImageList = images
                    mImageFolders.add(folderBean)
                }
            }
        }
        mImageFolders.sort()
        listener.onSuccess(mImageFolders)
    }


    @RequiresPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    private fun getLocalImages2Global() {
        var localList = ImageScanner.getLocalImageFromMediaStore()
        mLocalImages.clear()
        mLocalImages.addAll(localList)
    }


    interface IGetFolderListener {
        fun onSuccess(folders: List<ImageFolderBean>)
        fun onFail(reason: Int)
    }

}