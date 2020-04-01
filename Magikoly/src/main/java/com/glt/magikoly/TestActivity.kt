package com.glt.magikoly

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.cs.bd.commerce.util.LogUtils
import com.glt.magikoly.function.facesdk.FaceSdkProxy
import com.glt.magikoly.permission.OnPermissionResult
import com.glt.magikoly.permission.PermissionHelper
import com.glt.magikoly.permission.PermissionSettingPage
import com.glt.magikoly.utils.FileUtils
import com.glt.magikoly.utils.PersonDetectUtils
import com.glt.magikoly.utils.ToastUtils
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import magikoly.magiccamera.R
import kotlinx.android.synthetic.main.activity_test.*
import me.yokeyword.fragmentation.ISupportFragment

class TestActivity : AppCompatActivity() {

    var mSrcBitmap: Bitmap? = null
    var mTextureBitmap: Bitmap? = null

    companion object {

        fun start(context: Context, filePath: String) {
            val intent = Intent(context, TestActivity::class.java)
            intent.putExtra("filePath", filePath)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)


        val stringExtra = intent.getStringExtra("filePath")
        mSrcBitmap = BitmapFactory.decodeFile(stringExtra)
        iv_original.setImageBitmap(mSrcBitmap)


        val portraitSeparation = PersonDetectUtils.portraitSeparation(mSrcBitmap!!)
        iv_texture.setImageBitmap(portraitSeparation)


        btn_ok.setOnClickListener {
            startOk()
        }

        iv_original.setOnClickListener {
            doAlbumClick(true)
        }

        iv_texture.setOnClickListener {
            doAlbumClick(false)
        }

    }

    private fun startOk() {

        if (mSrcBitmap == null || mTextureBitmap == null) {
            return
        }

        FaceSdkProxy.detectImage(false,false,mSrcBitmap!!,0,object  :OnSuccessListener<List<FirebaseVisionFace>>{
            override fun onSuccess(p0: List<FirebaseVisionFace>?) {
                p0?.get(0)?.let {
                    val faceFeature = PersonDetectUtils.getFaceFeature(mSrcBitmap!!, it)


                    FaceSdkProxy.detectImage(false,false,mTextureBitmap!!,0,object  :OnSuccessListener<List<FirebaseVisionFace>>{
                        override fun onSuccess(p0: List<FirebaseVisionFace>?) {
                            p0?.get(0)?.let {
                                val feature = PersonDetectUtils.getFaceFeature(mTextureBitmap!!, it)


                                ToastUtils.showToast("result = "+PersonDetectUtils.characterMatch(faceFeature!!,feature!!),Toast.LENGTH_SHORT)



                            }


                        }
                    },object : OnFailureListener{
                        override fun onFailure(p0: Exception) {
                            ToastUtils.showToast(p0.message,Toast.LENGTH_SHORT)
                        }

                    })


                }


            }
        },object : OnFailureListener{
            override fun onFailure(p0: Exception) {
                ToastUtils.showToast(p0.message,Toast.LENGTH_SHORT)
            }

        })



//
//        PersonDetectUtils.getFaceFeature(mSrcBitmap!!, null, object : PersonDetectUtils.FaceFeatureListener {
//            override fun success(srcResult: FloatArray) {
//                PersonDetectUtils.getFaceFeature(mTextureBitmap!!, null, object : PersonDetectUtils.FaceFeatureListener {
//                    override fun success(result: FloatArray) {
//                        PersonDetectUtils.characterMatch(srcResult, result)
//                    }
//
//                    override fun error(errorCode: Int) {
//                        ToastUtils.showToast("error", Toast.LENGTH_SHORT)
//                    }
//
//                })
//            }
//
//            override fun error(errorCode: Int) {
//                ToastUtils.showToast("srcError", Toast.LENGTH_SHORT)
//            }
//
//        })


    }


    fun faceDetect(bitmap: Bitmap,listener: OnSuccessListener<List<FirebaseVisionFace>>) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)

        val highAccuracyOpts = FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
//                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .build()

        val detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(highAccuracyOpts)

        detector.detectInImage(image).addOnSuccessListener (listener).addOnFailureListener {
            LogUtils.e(it.message)
        }

    }


    var mIsOrigin = true
    private val REQUEST_CODE_PHOTO = 1

    private fun doAlbumClick(isOriginal: Boolean) {
        mIsOrigin = isOriginal
        val context = this
        PermissionHelper.requestReadPermission(this, object : OnPermissionResult {
            override fun onPermissionGrant(permission: String) {

//                val intent = Intent(context, ResultActivity::class.java)
                val targetIntent = Intent(Intent.ACTION_GET_CONTENT)
                targetIntent.type = "image/*"
//                intent.putExtra(ResultActivity.KEY_TARGET_INTENT, targetIntent)
//                intent.putExtra(ResultActivity.KEY_REQUEST_CODE, REQUEST_CODE_PHOTO)
//                startActivity(targetIntent)//打开相册

                startActivityForResult(targetIntent, REQUEST_CODE_PHOTO)

            }

            override fun onPermissionDeny(permission: String, never: Boolean) {
                if (never) {
                    PermissionSettingPage.start(context, false)
                }
            }
        }, -1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PHOTO && resultCode == ISupportFragment.RESULT_OK &&
                data != null) {
            val uri = data.getData()

            val path = FileUtils.getPath(FaceAppState.getContext(), uri)

            if (mIsOrigin) {
                mSrcBitmap = BitmapFactory.decodeFile(path)

                iv_original.setImageBitmap(mSrcBitmap)

                val portraitSeparation = PersonDetectUtils.portraitSeparation(mSrcBitmap!!)
                iv_texture.setImageBitmap(portraitSeparation)
            } else {
                mTextureBitmap = BitmapFactory.decodeFile(path)

                iv_texture.setImageBitmap(mTextureBitmap)

//                PersonDetectUtils.portraitSeparation(mTextureBitmap!!, object : PersonDetectUtils.PortraitSeparationListener {
//                    override fun error(errorCode: Int) {
//                        ToastUtils.showToast("error", Toast.LENGTH_SHORT)
//                    }
//
//                    override fun success(result: Bitmap) {
//                        FaceThreadExecutorProxy.runOnMainThread {
//                            iv_result.setImageBitmap(result)
//                        }
//                    }
//
//                })

            }
        }

    }
}
