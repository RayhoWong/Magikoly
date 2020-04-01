package com.glt.magikoly.function.main.album

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.ad.inner.InnerAdController
import com.glt.magikoly.constants.ErrorCode
import com.glt.magikoly.dialog.DialogUtils
import com.glt.magikoly.event.ImageDetectEvent
import com.glt.magikoly.event.UserStatusEvent
import com.glt.magikoly.ext.registerEventObserver
import com.glt.magikoly.ext.runMain
import com.glt.magikoly.ext.unregisterEventObserver
import com.glt.magikoly.function.FaceFunctionBean
import com.glt.magikoly.function.FaceFunctionManager
import com.glt.magikoly.function.facesdk.FaceSdkProxy
import com.glt.magikoly.function.main.album.presenter.AlbumDetailPresenter
import com.glt.magikoly.function.main.multiface.MultiFaceFragment
import com.glt.magikoly.mvp.BaseSupportFragment
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.statistic.Statistic103Constant.ENTRANCE_MAIN
import com.glt.magikoly.subscribe.billing.BillingStatusManager
import com.glt.magikoly.utils.ViewUtils
import com.glt.magikoly.view.GlobalProgressBar
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import kotlinx.android.synthetic.main.album_detail_layout.*
import kotlinx.android.synthetic.main.include_face_common_toolbar.*
import magikoly.magiccamera.R
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * @desc:
 * @auther:duwei
 * @date:2019/2/14
 */
class AlbumDetailFragment : BaseSupportFragment<AlbumDetailPresenter>(), AlbumDetailAdapter.ItemClickListener {
    private var mData: List<ImageBean>? = null
    private var startTag: String = ""

    companion object {
        private const val BUNDLE_BEAN = "bundle_bean"
        private const val ALBUM_DETAIL_SAVE_KEY_TAG = "ALBUM_DETAIL_SAVE_KEY_TAG"
        fun newInstance(tag: String, folderBean: ImageFolderBean) =
                AlbumDetailFragment().apply {
                    var bundle = Bundle()
                    startTag = tag
                    bundle.putParcelable(BUNDLE_BEAN, folderBean)
                    arguments = bundle
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val folderBean = arguments?.getParcelable<ImageFolderBean>(BUNDLE_BEAN)
        mData = folderBean?.mImageList
    }


    override fun createPresenter(): AlbumDetailPresenter = AlbumDetailPresenter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.album_detail_layout, null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (outState == null) {
            return
        }
        if (startTag.isNotBlank()) {
            outState.putString(ALBUM_DETAIL_SAVE_KEY_TAG, startTag)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState == null) {
            return
        }
        startTag = savedInstanceState.getString(ALBUM_DETAIL_SAVE_KEY_TAG, "")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        face_common_toolbar.setTitle(resources.getString(R.string.album_detail_title))
        face_common_toolbar.setTitleColor(resources.getColor(R.color.toolbar_title_dark_color))
        face_common_toolbar.setTitleGravity(Gravity.START)
        face_common_toolbar.setBackDrawable(R.drawable.icon_back_black_selector)
        face_common_toolbar.setOnTitleClickListener { _, back ->
            if (back) {
                pop()
            }
        }
        album_detail_rv.apply {
            adapter = AlbumDetailAdapter(mData!!, this@AlbumDetailFragment)
            layoutManager = GridLayoutManager(activity, 3)
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: Rect?, view: View?,
                                            parent: RecyclerView?, state: RecyclerView.State?) {
                    super.getItemOffsets(outRect, view, parent, state)

                    outRect?.set(
                            AlbumDetailAdapter.SPACE, AlbumDetailAdapter.SPACE,
                            AlbumDetailAdapter.SPACE, AlbumDetailAdapter.SPACE
                    )
                }
            })
        }


    }





    private fun removeBannerAdView() {
        if (banner_ad_container?.visibility != View.GONE) {
            InnerAdController.instance.needLoadLocalListBannerAd = false
            banner_ad_content_container?.removeAllViews()
            banner_ad_container?.visibility = View.GONE
        }
    }

    override fun onItemClick(imgPath: String) {
        if (activity == null) {
            return
        }
        if (ViewUtils.isFastClick()) {
            return
        }
        BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                Statistic103Constant.ALBUM_PHOTO_CLICK, "")


        detectFace(imgPath)

        _mActivity?.let { activity ->
            if (startTag == ENTRANCE_MAIN) {
                InnerAdController.instance.loadAd(activity,
                        InnerAdController.FUNCTION_PAGE_EXIT_AD_MODULE_ID)
                if (FaceFunctionManager.demoFaceImageInfo == null) {
                    InnerAdController.instance.loadAd(activity,
                            InnerAdController.SWITCH_FUNCTION_AD_MODULE_ID)
                }
            }
        }
    }

    private fun detectFace(imgPath: String) {
        GlobalProgressBar.show(startTag)
        FaceFunctionManager.detectFace(FaceAppState.getContext(), imgPath, object : FaceSdkProxy.OnDetectResult {
            override fun onDetectMultiFaces(originalPath: String, originBitmap: Bitmap, faces: List<FirebaseVisionFace>, onDetectResult: FaceSdkProxy.OnDetectResult) {
                FaceAppState.getMainActivity()?.start(
                        MultiFaceFragment.newInstance(startTag, imgPath, originBitmap, faces, onDetectResult))
            }

            override fun onDetectSuccess(originalPath: String, faceFunctionBean: FaceFunctionBean) {
                runMain {
                    faceFunctionBean.category = Statistic103Constant.CATEGORY_ALBUM
//                    EventBus.getDefault().post(ProgressBarEvent(EVENT_HIDE, startTag))
                    val event = ImageDetectEvent(startTag, originalPath, faceFunctionBean)
                    EventBus.getDefault().post(event)
                    if (!event.progressBarHandled) {
                        GlobalProgressBar.hide()
                    }
//                    FaceAppState.getMainActivity()?.start(FaceReportFragment.newInstance())
                }
            }

            override fun onDetectFail(originalPath: String, errorCode: Int) {
                GlobalProgressBar.hide()
                DialogUtils.showErrorDialog(_mActivity, errorCode, Runnable {
                    if (errorCode == ErrorCode.NETWORK_ERROR) {
                        detectFace(imgPath)
                    }
                })
            }
        })
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        registerEventObserver(this)
    }

    override fun onDetach() {
        super.onDetach()
        unregisterEventObserver(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUserStatusEvent(event: UserStatusEvent) {
        if (event.status == BillingStatusManager.STATUS_VIP) {
            removeBannerAdView()
        }
    }
}