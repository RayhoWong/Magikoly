package com.glt.magikoly.function.main.multiface

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.glt.magikoly.function.FaceFunctionBean
import com.glt.magikoly.function.facesdk.FaceSdkProxy
import com.glt.magikoly.function.views.MultiFaceImageView
import com.glt.magikoly.mvp.BaseSupportFragment
import com.glt.magikoly.view.GlobalProgressBar
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import magikoly.magiccamera.R
import kotlinx.android.synthetic.main.include_face_common_toolbar.*

class MultiFaceFragment : BaseSupportFragment<MultiFacePresenter>(), IMultiFace {
    override fun onDetectMultiFaces(originalPath: String, originBitmap: Bitmap,
                                    faces: List<FirebaseVisionFace>,
                                    onDetectResult: FaceSdkProxy.OnDetectResult) {
    }

    override fun onDetectSuccess(originalPath: String, faceFunctionBean: FaceFunctionBean) {
        pop()
        onDetectResult.onDetectSuccess(originalPath, faceFunctionBean)
    }

    override fun onDetectFail(originalPath: String, errorCode: Int) {
        onDetectResult.onDetectFail(originalPath, errorCode)
    }

    private val imageFaces: MultiFaceImageView by lazy { view?.findViewById(R.id.img_image_faces) as MultiFaceImageView }
    private lateinit var resource: Bitmap
    private lateinit var faces: List<FirebaseVisionFace>
    private lateinit var onDetectResult: FaceSdkProxy.OnDetectResult
    private lateinit var imgPath: String
    private lateinit var entrance: String
    private var  init = false
    companion object {

        fun newInstance(entrance: String, imgPath: String, resource: Bitmap,
                        faces: List<FirebaseVisionFace>,
                        onDetectResult: FaceSdkProxy.OnDetectResult): MultiFaceFragment {
            var multiFaceFragment = MultiFaceFragment()
            multiFaceFragment.resource = resource
            multiFaceFragment.faces = faces
            multiFaceFragment.onDetectResult = onDetectResult
            multiFaceFragment.imgPath = imgPath
            multiFaceFragment.entrance = entrance
            multiFaceFragment.init = true
            return multiFaceFragment
        }
    }

    override fun createPresenter(): MultiFacePresenter {
        return MultiFacePresenter()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.multi_face_fragment, null)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!init) {
            pop()
            return
        }
        imageFaces.entrance = entrance
        imageFaces.setImageWithFaces(resource, faces)
        imageFaces.setOnFaceSelectedListener(mPresenter)
        face_common_toolbar.setOnTitleClickListener { view, back ->
            if (back) {
                pop()
            }
        }
        post { GlobalProgressBar.hide() }
    }

    override fun getOriginalPath(): String {
        return imgPath
    }
}