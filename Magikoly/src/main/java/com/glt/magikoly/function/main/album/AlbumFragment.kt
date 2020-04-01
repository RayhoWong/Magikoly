package com.glt.magikoly.function.main.album

import android.animation.ValueAnimator
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.ad.inner.InnerAdController
import com.glt.magikoly.function.FaceFunctionManager
import com.glt.magikoly.function.main.ITabFragment
import com.glt.magikoly.function.main.MainFragment.Companion.TAB_ALBUM
import com.glt.magikoly.function.main.ToolBarCallback
import com.glt.magikoly.function.main.album.presenter.AlbumPresenter
import com.glt.magikoly.function.main.album.presenter.AlbumPresenter.Companion.FAIL_NO_PERMISSION
import com.glt.magikoly.function.takephoto.TakePhotoFragment
import com.glt.magikoly.mvp.BaseSupportFragment
import com.glt.magikoly.permission.OnPermissionResult
import com.glt.magikoly.permission.PermissionHelper
import com.glt.magikoly.permission.PermissionSettingPage
import com.glt.magikoly.permission.Permissions
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.IStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.statistic.Statistic103Constant.ALBUM_ENTER_MAIN
import com.glt.magikoly.thread.FaceThreadExecutorProxy
import magikoly.magiccamera.R
import kotlinx.android.synthetic.main.main_album_layout.*
import me.yokeyword.fragmentation.SupportFragment

class AlbumFragment : BaseSupportFragment<AlbumPresenter>(),
        IAlbumView, IStatistic, ITabFragment, AlbumPresenter.IGetFolderListener {
    override var fromClick = false
    override fun getTabLock(): Boolean = false

    override fun setStatus(status: Int) {
    }

    override fun getStatus(): Int {
        return -1
    }

    override fun reload() {
        //do nothing
    }

    private var mFolderBeans = ArrayList<ImageFolderBean>()
    private var hasResult = false
    private var isLoading = false
    private var isPermissionCameraNever = false
    private var progressBarAnim: ValueAnimator? = null
    private var startTag: String = ""
    /**
     * 1 : tab
     * 2 : 拍摄页内部
     */
    var enterFrom: String = ALBUM_ENTER_MAIN

    companion object {

        private const val ALBUM_SAVE_KEY_TAG = "ALBUM_SAVE_KEY_TAG"

        fun newInstance(tag: String): AlbumFragment {
            val albumFragment = AlbumFragment()
            albumFragment.startTag = tag
            return albumFragment
        }
    }

    override fun getTabId(): Int = TAB_ALBUM

    override fun onTabFragmentVisible() {
    }

    override fun onTabFragmentInvisible() {
    }

    override fun getToolBarTitle(): String = FaceAppState.getContext().resources.getString(R.string.album)
    override fun getBottomBarTitle(): String = FaceAppState.getContext().resources.getString(R.string.album)

    override fun getToolBarBackDrawable(): Drawable =
            FaceAppState.getContext().resources.getDrawable(R.drawable.icon_menu_selector)

    override fun getBottomBarIcon(): Array<Drawable>? {
        val normal = FaceAppState.getContext().resources.getDrawable(R.drawable.tab_album_normal)
        val highLight = FaceAppState.getContext().resources.getDrawable(
                R.drawable.tab_album_pressed)
        return arrayOf(normal, highLight)
    }

    override fun getToolBarMenuDrawable(): Drawable? {
        return null
    }

    override fun getToolBarBackCallback(): ToolBarCallback? {
        return null
    }

    override fun getToolBarMenuCallback(): ToolBarCallback? {
        return null
    }

    override fun getToolBarSelfCallback(): ToolBarCallback? {
        return null
    }

    override fun getToolBarItemColor(): Int? {
        return FaceAppState.getContext().resources.getColor(R.color.toolbar_title_dark_color)
    }

    override fun getEntrance(): String = ""
    override fun getTabCategory(): String = ""


    override fun createPresenter(): AlbumPresenter = AlbumPresenter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_album_layout, null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (outState == null) {
            return
        }
        if (startTag.isNotBlank()) {
            outState.putString(ALBUM_SAVE_KEY_TAG, startTag)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState == null) {
            return
        }
        startTag = savedInstanceState.getString(ALBUM_SAVE_KEY_TAG, "")
    }

    private var isPermissionWriteNever = false
    private var isShowPermissionSettingPage = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        face_album_recycler_view.apply {
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(AlbumItemDecorator())
            adapter = AlbumAdapter(mFolderBeans) {
                (parentFragment as SupportFragment).start(AlbumDetailFragment.newInstance(startTag, it))
                BaseSeq103OperationStatistic.uploadSqe103StatisticData(Statistic103Constant.ALBUM_CLICK, "")
            }
        }

        no_photo_btn.setOnClickListener {
            _mActivity?.let { activity ->
                if (TakePhotoFragment.startTakePhoto(activity, startTag, null)) {
                    if (startTag == Statistic103Constant.ENTRANCE_MAIN) {
                        InnerAdController.instance.loadAd(activity,
                                InnerAdController.FUNCTION_PAGE_EXIT_AD_MODULE_ID)
                        if (FaceFunctionManager.demoFaceImageInfo == null) {
                            InnerAdController.instance.loadAd(activity,
                                    InnerAdController.SWITCH_FUNCTION_AD_MODULE_ID)
                        }
                    }
                }
            }


//            FaceAppState.getMainActivity()?.run {
//                if (PermissionHelper.hasCameraPermission(this)) {
//                    TakePhotoFragment.startTakePhoto(startTag)
//                    isPermissionCameraNever = false
//                } else {
//                    PermissionHelper.requestCameraPermission(this, object : OnPermissionResult {
//                        override fun onPermissionDeny(permission: String?, never: Boolean) {
//                            if (isPermissionCameraNever || PermissionHelper.isPermissionGroupDeny(this@run, permission)) {
//                                showPermissionDenyNeverDialog(this@run, getString(R.string.permission_tip_camera_never))
//                            }
//                            isPermissionCameraNever = never
//                        }
//
//                        override fun onPermissionGrant(permission: String?) {
//                            TakePhotoFragment.startTakePhoto(startTag)
//                            BaseSeq103OperationStatistic.uploadSqe103StatisticData(Statistic103Constant.CAMERA_PERMISSION_OBTAINED,
//                                    Statistic103Constant.ENTRANCE_ALBUM_EMPTY)
//                            isPermissionCameraNever = false
//                        }
//                    }, -1)
//                    BaseSeq103OperationStatistic.uploadSqe103StatisticData(Statistic103Constant.CAMERA_PERMISSION_REQUEST,
//                            Statistic103Constant.ENTRANCE_ALBUM_EMPTY)
//                }
//
//            }
        }

        ok_request_permission.setOnClickListener {
            FaceAppState.getMainActivity()?.apply {
                PermissionHelper.requestReadPermission(this, object : OnPermissionResult {
                    override fun onPermissionDeny(permission: String?, never: Boolean) {
                        if (isPermissionWriteNever || PermissionHelper.isPermissionGroupDeny(this@apply, permission)) {
                            //showPermissionDenyNeverDialog(this@apply)
                            PermissionSettingPage.start(activity, false)
                            isShowPermissionSettingPage = true
                        }
                        isPermissionWriteNever = never
                        if (isPermissionWriteNever) {
                            no_permission_text.setText(R.string.permission_tip_write_never_album)
                        }
                    }

                    override fun onPermissionGrant(permission: String?) {
                        isLoading = true
                        mPresenter.getLocalImageFoldersAsync(this@AlbumFragment)
                        BaseSeq103OperationStatistic.uploadSqe103StatisticData(Statistic103Constant.PHOTO_PERMISSION_OBTAINED,
                                Statistic103Constant.ENTRANCE_PHOTO_FOR_ALBUM_GUIDE)
                    }
                }, -1)
                BaseSeq103OperationStatistic.uploadSqe103StatisticData(Statistic103Constant.PHOTO_PERMISSION_REQUEST,
                        Statistic103Constant.ENTRANCE_PHOTO_FOR_ALBUM_GUIDE)
            }
        }
        if (isEmpty) {
            layout_no_photo.visibility = View.VISIBLE
        }
    }

    override fun onSupportVisible() {
        super.onSupportVisible()
        if (!hasResult && !isLoading) {
            showProgressBar()
            isLoading = true
            mPresenter.getLocalImageFoldersAsync(this)
        }
        if (isLoading) {
            showProgressBar()
        }

        if (PermissionHelper.isPermissionGroupDeny(_mActivity, Permissions.WRITE_EXTERNAL_STORAGE)) {
            no_permission_text.setText(R.string.permission_tip_write_never_album)
        } else {
            no_permission_text.setText(R.string.no_permission_album)
        }
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

    override fun hideProgressBar() {
        if (img_progressbar.visibility == View.VISIBLE) {
            progressBarAnim?.cancel()
            img_progressbar.visibility = View.GONE
        }
    }

    override fun showProgressBar() {
        if (img_progressbar.visibility != View.VISIBLE) {
            img_progressbar.visibility = View.VISIBLE
            progressBarAnim = ValueAnimator.ofFloat(0f, 360f)
            progressBarAnim?.apply {
                duration = 1000
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
                addUpdateListener { animator ->
                    img_progressbar?.let { progressBar ->
                        progressBar.rotation = animator.animatedValue as Float
                        progressBar.invalidate()
                    }
                }
                start()
            }
        }
    }

    private var isEmpty: Boolean = false

    override fun onSuccess(folders: List<ImageFolderBean>) {
        hasResult = true
        isLoading = false
        FaceThreadExecutorProxy.runOnMainThread {
            if (img_progressbar==null) {
                return@runOnMainThread
            }

            hideProgressBar()
            if (layout_no_permission.visibility == View.VISIBLE) {
                layout_no_permission.visibility = View.GONE
            }
            if (folders.isEmpty()) {
                showEmptyLayout()
                return@runOnMainThread
            } else {
                isEmpty = false
                layout_no_photo.visibility = View.GONE
            }
            mFolderBeans.clear()
            mFolderBeans.addAll(folders)
            face_album_recycler_view.adapter.notifyDataSetChanged()
            FaceThreadExecutorProxy.execute {
                var num = 0
                for (folder in folders) {
                    num += folder.mImageList?.size!!
                }
                if (startTag == Statistic103Constant.ENTRANCE_MAIN) {
                    BaseSeq103OperationStatistic.uploadSqe103StatisticData(folders.size.toString(),
                            Statistic103Constant.ALBUM_SHOW, enterFrom, num.toString())
                } else {
                    BaseSeq103OperationStatistic.uploadSqe103StatisticData(folders.size.toString(),
                            Statistic103Constant.ALBUM_SHOW, Statistic103Constant.ALBUM_ENTER_BABY_INNER, num.toString())
                }
                enterFrom = ALBUM_ENTER_MAIN
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (isShowPermissionSettingPage && PermissionHelper.hasReadStoragePermission(context)) {
            isShowPermissionSettingPage = false
            mPresenter.getLocalImageFoldersAsync(this@AlbumFragment)
        }
    }

    override fun onFail(reason: Int) {
        hasResult = false
        isLoading = false

        FaceThreadExecutorProxy.runOnMainThread {
            if (img_progressbar==null) {
                return@runOnMainThread
            }
            hideProgressBar()
            if (reason == FAIL_NO_PERMISSION) {
                layout_no_permission.visibility = View.VISIBLE
            }
        }

    }

    private fun showEmptyLayout() {
        isEmpty = true
        layout_no_photo.visibility = View.VISIBLE
    }

}

