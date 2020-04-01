package com.glt.magikoly.function.main.faceimages

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.TextView
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.ad.inner.InnerAdController
import com.glt.magikoly.ad.inner.InnerAdController.Companion.FUNCTION_PAGE_EXIT_AD_MODULE_ID
import com.glt.magikoly.ad.inner.InnerAdController.Companion.SWITCH_FUNCTION_AD_MODULE_ID
import com.glt.magikoly.constants.ErrorCode
import com.glt.magikoly.dialog.DialogUtils
import com.glt.magikoly.dialog.TipsDialog
import com.glt.magikoly.event.CameraTransitionEvent
import com.glt.magikoly.event.ImageDetectEvent
import com.glt.magikoly.event.ImageScanStateEvent
import com.glt.magikoly.event.PermissionEvent
import com.glt.magikoly.ext.registerEventObserver
import com.glt.magikoly.ext.runMain
import com.glt.magikoly.ext.unregisterEventObserver
import com.glt.magikoly.function.*
import com.glt.magikoly.function.main.ITabFragment
import com.glt.magikoly.function.main.MainFragment.Companion.TAB_FACE_IMAGES
import com.glt.magikoly.function.main.ToolBarCallback
import com.glt.magikoly.function.main.faceimages.FaceImagesAdapter.Companion.VIEW_TYPE_CAMERA
import com.glt.magikoly.function.main.faceimages.presenter.FaceImagesPresenter
import com.glt.magikoly.function.resultreport.FaceReportFragment
import com.glt.magikoly.function.takephoto.TakePhotoFragment
import com.glt.magikoly.mvp.BaseSupportFragment
import com.glt.magikoly.permission.OnPermissionResult
import com.glt.magikoly.permission.PermissionHelper
import com.glt.magikoly.permission.PermissionSettingPage
import com.glt.magikoly.permission.Permissions
import com.glt.magikoly.pref.PrefConst
import com.glt.magikoly.pref.PrivatePreference
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.IStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.statistic.Statistic103Constant.ENTRANCE_MAIN
import com.glt.magikoly.subscribe.billing.BillingStatusManager
import com.glt.magikoly.utils.DrawUtils
import com.glt.magikoly.utils.FileUtils
import com.glt.magikoly.utils.Logcat
import com.glt.magikoly.utils.ViewUtils
import com.glt.magikoly.view.GlobalProgressBar
import kotlinx.android.synthetic.main.main_face_images_layout.*
import magikoly.magiccamera.R
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class FaceImagesFragment : BaseSupportFragment<FaceImagesPresenter>(), IFaceImagesView, IStatistic,
        ITabFragment,
        OnImageItemClickListener {

    override var fromClick = false
    override fun getTabLock(): Boolean = false

    override fun getStatus(): Int {
        return -1
    }

    override fun setStatus(status: Int) {
    }

    companion object {
        const val SPAN_SIZE = 3
        private const val FACE_IMAGE_SAVE_KEY_TAG = "FACE_IMAGE_SAVE_KEY_TAG"
        fun newInstance(tag: String): FaceImagesFragment {
            val faceImagesFragment = FaceImagesFragment()
            faceImagesFragment.startTag = tag
            return faceImagesFragment
        }
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FaceImagesAdapter
    private lateinit var txtImageScanState: TextView
    private var isPermissionReadNever = false
    private var isPermissionCameraNever = false
    private var isShowPermissionSettingPage = false
    private var progressBarAnim: ValueAnimator? = null
    private var hasEverLoadDataStarted = false //是否曾经开始加载过数据
    private var startTag: String = ""

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (outState == null) {
            return
        }
        if (startTag.isNotBlank()) {
            outState.putString(FACE_IMAGE_SAVE_KEY_TAG, startTag)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState == null) {
            return
        }
        startTag = savedInstanceState.getString(FACE_IMAGE_SAVE_KEY_TAG, "")
    }

    override fun getTabId(): Int = TAB_FACE_IMAGES

    override fun onTabFragmentVisible() {
    }

    override fun onTabFragmentInvisible() {
    }

    override fun getToolBarTitle(): String {
        return FaceAppState.getContext().resources.getString(R.string.app_name)
    }

    override fun getToolBarBackDrawable(): Drawable {
        return FaceAppState.getContext().resources.getDrawable(R.drawable.icon_menu_selector)
    }

    override fun getToolBarMenuDrawable(): Drawable? = null

    override fun getToolBarBackCallback(): ToolBarCallback? {
        return null
    }

    override fun getToolBarMenuCallback(): ToolBarCallback? {
        return null
    }

    override fun getToolBarSelfCallback(): ToolBarCallback? {
        return object : ToolBarCallback {
            override fun invoke(): Boolean {
                recyclerView.smoothScrollToPosition(0)
                return true
            }
        }
    }

    override fun getToolBarItemColor(): Int? {
        return Color.WHITE
    }

    override fun getBottomBarTitle(): String {
        return FaceAppState.getContext().resources.getString(R.string.faces)
    }

    override fun getBottomBarIcon(): Array<Drawable>? {
        val normal = FaceAppState.getContext().resources.getDrawable(R.drawable.tab_faces_normal)
        val highLight = FaceAppState.getContext().resources.getDrawable(
                R.drawable.tab_faces_pressed)
        return arrayOf(normal, highLight)
    }

    override fun reload() {
        //do nothing
    }

    override fun getEntrance(): String {
        return ""
    }

    override fun getTabCategory(): String {
        return ""
    }

    override fun showProgressBar() {
        img_progressbar?.run {
            adapter.isLoadingViewVisible = false
            if (img_progressbar.visibility != View.VISIBLE) {
                img_progressbar.visibility = View.VISIBLE
                progressBarAnim = ValueAnimator.ofFloat(0f, 360f)
                progressBarAnim?.apply {
                    duration = 1000
                    repeatCount = ValueAnimator.INFINITE
                    interpolator = LinearInterpolator()
                    addUpdateListener {
                        img_progressbar.rotation = it.animatedValue as Float
                        img_progressbar.invalidate()
                    }
                    start()
                }
            }
        }
    }

    override fun hideProgressBar() {
        img_progressbar?.run {
            if (!mPresenter.isLoadFinish()) {
                adapter.isLoadingViewVisible = true
            }
            if (img_progressbar.visibility == View.VISIBLE) {
                progressBarAnim?.cancel()
                img_progressbar.visibility = View.GONE
            }
        }
    }

    override fun createPresenter(): FaceImagesPresenter {
        return FaceImagesPresenter()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_face_images_layout, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.face_image_recycler_view)
        adapter = FaceImagesAdapter(recyclerView.context, mPresenter.getFaceImageList(), this)
        val layoutManager = GridLayoutManager(recyclerView.context, SPAN_SIZE)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val itemType = adapter.getItemViewType(position)
                return if (itemType == VIEW_TYPE_CAMERA) {
                    SPAN_SIZE
                } else {
                    1
                }
            }
        }
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                    outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                val margin = DrawUtils.dip2px(3f)
                outRect?.set(margin, margin, margin, margin)
            }
        })
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter
        txtImageScanState = view.findViewById(R.id.txt_image_scan_state)

        if (!mPresenter.handlePermission()) {
            mPresenter.loadFaceImages(false)
        }
    }

    override fun onSupportVisible() {
        super.onSupportVisible()
        mPresenter.handlePermission()
    }

    override fun onLoadDataStart(faceImageList: ArrayList<FaceImageInfo>) {
        adapter.setFaceImageList(faceImageList)
        adapter.notifyDataSetChanged()
        txtImageScanState.setText(R.string.searching_for_faces)
//        showImageScanState(true)
        adapter.isLoadingViewVisible = img_progressbar?.visibility != View.VISIBLE
        hasEverLoadDataStarted = true
    }

    private fun showImageScanState(show: Boolean) {
        if (show) {
            if (txtImageScanState.visibility != View.VISIBLE) {
                txtImageScanState.visibility = View.VISIBLE
                txtImageScanState.alpha = 0f
            }
            txtImageScanState.animate().alpha(1f).setDuration(500).setListener(null).start()
        } else {
            txtImageScanState.animate().alpha(0f).setDuration(500).setListener(null).start()
        }
    }

    override fun addFaceImage(position: Int, faceImageInfo: FaceImageInfo) {
        adapter.notifyItemInserted(position + 1) //+1是因为顶部多了拍照item
    }

    override fun removeFaceImage(position: Int) {
        adapter.notifyItemRemoved(position + 1)
    }

    override fun refresh(faceImageList: ArrayList<FaceImageInfo>) {
        adapter.setFaceImageList(faceImageList)
        if (mPresenter.hasNoFaceImage(faceImageList)) {
            info_layout.visibility = View.VISIBLE
            post {
                info_layout?.let {
                    (it.layoutParams as ViewGroup.MarginLayoutParams).topMargin = DrawUtils.dip2px(
                            278f)
                    it.requestLayout()
                }
            }
            if (PermissionHelper.hasReadStoragePermission(context)) {
                txt_info.setText(R.string.no_face_photo)
                btn_ok.setOnClickListener {
                    onCameraClick(null)
                }
            } else {
                if (!BillingStatusManager.getInstance().isVIP()) {
                    val demoList = ImageScanner.initDemoData()
                    adapter.setFaceImageList(demoList)
                    adapter.notifyDataSetChanged()
                    post {
                        info_layout?.let {
                            (it.layoutParams as ViewGroup.MarginLayoutParams).topMargin = DrawUtils.dip2px(
                                    257f)
                            it.requestLayout()
                        }
                    }

                }
                if (PermissionHelper.isPermissionGroupDeny(_mActivity,
                                Permissions.WRITE_EXTERNAL_STORAGE)) {
                    txt_info.setText(R.string.permission_tip_write_never)
                } else {
                    txt_info.setText(R.string.permission_tip_write_deny)
                }
                btn_ok.setOnClickListener {
                    FaceAppState.getMainActivity()?.apply {
                        if (PermissionHelper.hasReadStoragePermission(this)) {
                            mPresenter.loadFaceImages(false)
                            isPermissionReadNever = false
                        } else {
                            PermissionHelper.requestReadPermission(this,
                                    object : OnPermissionResult {
                                        override fun onPermissionDeny(permission: String?,
                                                                      never: Boolean) {
                                            if (isPermissionReadNever || PermissionHelper.isPermissionGroupDeny(
                                                            this@apply, permission)) {
//                                        showPermissionDenyNeverDialog(this@apply, getString(R.string.permission_tip_write_never))
                                                PermissionSettingPage.start(activity, false)
                                                isShowPermissionSettingPage = true
                                            }
                                            isPermissionReadNever = never

                                            if (isPermissionReadNever) {
                                                txt_info.setText(
                                                        R.string.permission_tip_write_never)
                                            }
                                        }

                                        override fun onPermissionGrant(permission: String?) {
                                            mPresenter.loadFaceImages(false)
                                            isPermissionReadNever = false
                                            BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                                                    Statistic103Constant.PHOTO_PERMISSION_OBTAINED,
                                                    Statistic103Constant.ENTRANCE_PHOTO_FOR_FIRST_GUIDE)
                                        }
                                    }, -1)
                            BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                                    Statistic103Constant.PHOTO_PERMISSION_REQUEST,
                                    Statistic103Constant.ENTRANCE_PHOTO_FOR_FIRST_GUIDE)
                        }
                    }
                }
            }
        } else {
            hideEmptyView()
            adapter.notifyDataSetChanged()
            hasEverLoadDataStarted = true
        }
    }

    override fun onLoadDataFinish() {
        if (txtImageScanState.visibility == View.VISIBLE) {
            txtImageScanState.setText(R.string.search_complete)
            txtImageScanState.animate().alpha(0f).setDuration(500)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)
                            txtImageScanState.visibility = View.GONE
                        }
                    }).setStartDelay(3000L).start()
        }
        adapter.isLoadingViewVisible = false
        adapter.notifyDataSetChanged()
    }

    /*override fun onSupportVisible() {
        super.onSupportVisible()
        val faceImageList = mPresenter.getFaceImageList()
        if (adapter.isLoadingViewVisible && mPresenter.hasNoFaceImage(faceImageList)) {
            if (PermissionHelper.isPermissionGroupDeny(_mActivity, Permissions.WRITE_EXTERNAL_STORAGE)) {
                txt_info.setText(R.string.permission_tip_write_never)
            } else if (!PermissionHelper.hasReadStoragePermission(_mActivity)) {
                txt_info.setText(R.string.permission_tip_write_deny)
            } else {
                txt_info.setText(R.string.no_face_photo)
            }
        } else if (faceImageList.isNotEmpty()){
            hideEmptyView()
        }
    }*/

    override fun hideEmptyView() {
        info_layout.visibility = View.GONE
    }

    override fun onCameraClick(cameraView: View?) {
        _mActivity?.let { activity ->
            if (TakePhotoFragment.startTakePhoto(activity, startTag, null)) {
                InnerAdController.instance.loadAd(activity, FUNCTION_PAGE_EXIT_AD_MODULE_ID)
                if (FaceFunctionManager.demoFaceImageInfo == null) {
                    InnerAdController.instance.loadAd(activity, SWITCH_FUNCTION_AD_MODULE_ID)
                }
            }
        }


//        FaceAppState.getMainActivity()?.run {
//            if (PermissionHelper.hasCameraPermission(this)) {
//                TakePhotoFragment.startTakePhoto(startTag)
//                isPermissionCameraNever = false
//            } else {
//                PermissionHelper.requestCameraPermission(this, object : OnPermissionResult {
//                    override fun onPermissionDeny(permission: String?, never: Boolean) {
//                        if (isPermissionCameraNever || PermissionHelper.isPermissionGroupDeny(
//                                        this@run, permission)) {
//                            showPermissionDenyNeverDialog(this@run,
//                                    getString(R.string.permission_tip_camera_never))
//                        }
//                        isPermissionCameraNever = never
//                    }
//
//                    override fun onPermissionGrant(permission: String?) {
//                        TakePhotoFragment.startTakePhoto(startTag)
//                        BaseSeq103OperationStatistic.uploadSqe103StatisticData(
//                                Statistic103Constant.CAMERA_PERMISSION_OBTAINED,
//                                Statistic103Constant.ENTRANCE_CAMERA_CLICK)
//                        isPermissionCameraNever = false
//                    }
//                }, -1)
//                BaseSeq103OperationStatistic.uploadSqe103StatisticData(
//                        Statistic103Constant.CAMERA_PERMISSION_REQUEST,
//                        Statistic103Constant.ENTRANCE_CAMERA_CLICK)
//            }
//
//        }
    }

//    private fun showPermissionDenyNeverDialog(activity: Activity, content: String) {
//        val dialog = TipsDialog(activity)
//        dialog.setContent(content)
//        dialog.setupOKButton(R.string.permission_ok, View.OnClickListener {
//            dialog.dismiss()
//            PermissionSettingPage.start(activity, false)
//        })
//        dialog.show()
//    }

    override fun onImageItemClick(faceImageInfo: FaceImageInfo) {
        if (ViewUtils.isFastClick()) {
            return
        }
        BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                Statistic103Constant.FEATURED_CLICK, "")
        if (faceImageInfo.isDemo()) {
            FaceFunctionManager.demoFaceImageInfo = faceImageInfo as DemoFaceImageInfo
            GlobalProgressBar.show(ENTRANCE_MAIN)
            FaceAppState.getMainActivity()?.start(FaceReportFragment.newInstance())
            val entrance = when (faceImageInfo.imgId) {
                R.drawable.demo1 -> "1"
                R.drawable.demo2 -> "2"
                R.drawable.demo3 -> "3"
                else -> ""
            }
            BaseSeq103OperationStatistic.uploadSqe103StatisticData(Statistic103Constant.DEMO_CLICK,
                    entrance)
        } else {
            if (FileUtils.isExist(faceImageInfo.imagePath)) {
                GlobalProgressBar.show(ENTRANCE_MAIN)
                mPresenter.detectFaces(FaceAppState.getContext(), faceImageInfo)

            } else {
                DialogUtils.showErrorDialog(_mActivity, ErrorCode.IMAGE_NOT_FOUND, Runnable {
                    mPresenter.loadFaceImages(true)
                })
            }
        }
        _mActivity?.let { activity ->
            InnerAdController.instance.loadAd(activity, FUNCTION_PAGE_EXIT_AD_MODULE_ID)
            if (FaceFunctionManager.demoFaceImageInfo == null) {
                InnerAdController.instance.loadAd(activity, SWITCH_FUNCTION_AD_MODULE_ID)
            }
        }
    }

    override fun onFaceDetectSuccess(originalPath: String, faceFunctionBean: FaceFunctionBean) {
        runMain {
            faceFunctionBean.category = Statistic103Constant.CATEGORY_FACE_IMAGE
            EventBus.getDefault().post(ImageDetectEvent(startTag, originalPath, faceFunctionBean))
        }
    }

    override fun onFaceDetectFail(originalPath: String, errorCode: Int,
                                  faceImageInfo: FaceImageInfo) {
        runMain {
            GlobalProgressBar.hide()
            DialogUtils.showErrorDialog(_mActivity, errorCode, Runnable {
                if (errorCode == ErrorCode.NETWORK_ERROR) {
                    GlobalProgressBar.show(ENTRANCE_MAIN)
                    mPresenter.detectFaces(FaceAppState.getContext(), faceImageInfo)
                }
            })
        }
    }

    override fun onStart() {
        super.onStart()
        if (isShowPermissionSettingPage) {
            isShowPermissionSettingPage = false
            mPresenter.loadFaceImages(false)
        } else {
            if (hasEverLoadDataStarted) { //曾经加载过数据才开启增量扫描
                mPresenter.loadIncrementFaceImages()
            }
        }
    }

    override fun onDestroyView() {
        progressBarAnim?.cancel()
        super.onDestroyView()
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        registerEventObserver(this)
    }

    override fun onDetach() {
        super.onDetach()
        unregisterEventObserver(this)
    }

    @Subscribe
    fun onImageScanStateEvent(event: ImageScanStateEvent) {
        showImageScanState(event.show)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPermissionChangeEvent(event: PermissionEvent) {
        Logcat.d("xiaowu_permission", "event:${event.permission}")
        if (Permissions.WRITE_EXTERNAL_STORAGE == event.permission
                || Permissions.READ_EXTERNAL_STORAGE == event.permission) {
            val faceImageList = mPresenter.getFaceImageList()
            if (PermissionHelper.isPermissionGroupDeny(_mActivity,
                            Permissions.WRITE_EXTERNAL_STORAGE)) {
                txt_info.setText(R.string.permission_tip_write_never)
            } else if (!PermissionHelper.hasReadStoragePermission(_mActivity)) {
                txt_info.setText(R.string.permission_tip_write_deny)
            } else if (mPresenter.isLoadFinish() && mPresenter.hasNoFaceImage(faceImageList)) {
                txt_info.setText(R.string.no_face_photo)
            } else if (mPresenter.isLoadStart()) {
                hideEmptyView()
            } else {
                mPresenter.loadFaceImages(false)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCameraTransitionEvent(event: CameraTransitionEvent) {
        if (event.action == CameraTransitionEvent.EVENT_HIDE && !event.cameraOpenSuccess) {
            if (!mPresenter.handlePermission()) {
                mPresenter.loadFaceImages(false)
            }
        }
    }
}