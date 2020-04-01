package com.glt.magikoly.function.main.faceimages

import com.glt.magikoly.function.FaceFunctionBean
import com.glt.magikoly.function.FaceImageInfo
import com.glt.magikoly.function.main.faceimages.presenter.FaceImagesPresenter
import com.glt.magikoly.mvp.IViewInterface

interface IFaceImagesView : IViewInterface<FaceImagesPresenter> {

    fun onLoadDataStart(faceImageList: ArrayList<FaceImageInfo>)
    fun addFaceImage(position: Int, faceImageInfo: FaceImageInfo)
    fun removeFaceImage(position: Int)
    fun refresh(faceImageList: ArrayList<FaceImageInfo>)
    fun hideEmptyView()
    fun onFaceDetectSuccess(originalPath: String, faceFunctionBean: FaceFunctionBean)
    fun onFaceDetectFail(originalPath: String, errorCode: Int, faceImageInfo: FaceImageInfo)
    fun showProgressBar()
    fun hideProgressBar()
    fun onLoadDataFinish()
}
