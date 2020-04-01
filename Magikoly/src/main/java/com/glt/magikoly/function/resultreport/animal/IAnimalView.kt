package com.glt.magikoly.function.resultreport.animal

import android.graphics.Bitmap
import android.graphics.Point
import com.glt.magikoly.function.resultreport.animal.presenter.AnimalPresenter
import com.glt.magikoly.mvp.IViewInterface

interface IAnimalView : IViewInterface<AnimalPresenter> {
    fun onAnimalLoadCompleted(image: Bitmap, landmark: HashMap<String, List<Point>>)
    fun onAnimalLoadFailed(errorCode: Int)
    fun onFaceDetectSuccess(landmark: HashMap<String, List<Point>>, feature: FloatArray?, startTime: Long)
    fun onFaceDetectError(errorCode: Int)
}
