package com.glt.magikoly.function.innerpick

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.glt.magikoly.event.ImageDetectEvent
import com.glt.magikoly.function.FaceFunctionBean
import com.glt.magikoly.function.main.album.AlbumFragment
import com.glt.magikoly.function.main.discovery.DiscoveryFragment
import com.glt.magikoly.function.takephoto.TakePhotoFragment
import com.glt.magikoly.mvp.BaseSupportFragment
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.statistic.Statistic103Constant.ALBUM_ENTER_BABY_INNER
import magikoly.magiccamera.R
import kotlinx.android.synthetic.main.fragment_inner_pick.*
import kotlinx.android.synthetic.main.include_face_common_toolbar.*
import me.yokeyword.fragmentation.ISupportFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *
 * @author rayhahah
 * @blog http://rayhahah.com
 * @time 2019/4/1
 * @tips 这个类是Object的子类
 * @fuction
 */

class InnerPickFragment : BaseSupportFragment<InnerPickPresenter>(), IInnerPick {

    companion object {
        const val REQUEST_ENTRANCE = "REQUEST_ENTRANCE"
        const val REQUEST_TITLE = "REQUEST_TITLE"
        private const val DEFAULT_TITLE = ""
        private val STATUS_ALBUM: Int = 0
        private val STATUS_DISCOVERY: Int = 1

        const val RESULT_ORIGINAL_PATH: String = "RESULT_ORIGINAL_PATH"
        const val RESULT_FACE_BEAN: String = "RESULT_FACE_BEAN"

        fun newInstance(fragment: BaseSupportFragment<*>, requestCode: Int,
                        entrance: String, title: String) {
            val innerPickFragment = InnerPickFragment()
            val bundle = Bundle()
            bundle.putString(REQUEST_ENTRANCE, entrance)
            bundle.putString(REQUEST_TITLE, title)
            innerPickFragment.arguments = bundle
            fragment.startForResult(innerPickFragment, requestCode)
        }
    }

    private lateinit var entrance: String
    private var mCurrentStatus = 0
    private var isPermissionCameraNever = false
    private var mDiscoveryFragment: DiscoveryFragment? = null

    override fun createPresenter(): InnerPickPresenter {
        return InnerPickPresenter()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_inner_pick, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)
        entrance = arguments?.getString(REQUEST_ENTRANCE, "") ?: ""
        tv_inner_pick_title.text = arguments?.getString(REQUEST_TITLE, DEFAULT_TITLE) ?: DEFAULT_TITLE
        cl_take_photo.setOnClickListener(this)
        cl_take_album.setOnClickListener(this)
        cl_take_discovey.setOnClickListener(this)
        initToolbar()

        loadRootFragment(R.id.fl_album_container, AlbumFragment.newInstance(entrance))
        mDiscoveryFragment = DiscoveryFragment.newInstance(entrance)
        loadRootFragment(R.id.fl_discovery_container, mDiscoveryFragment)
        setStatus()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onImageDetectEvent(event: ImageDetectEvent) {
        if (event.tag == entrance) {
            popResult(event.originalPath, event.faceFunctionBean)
            event.progressBarHandled = true
        }
    }


    override fun onClick(v: View) {
        super.onClick(v)
        when (v.id) {
            R.id.cl_take_photo -> {
                TakePhotoFragment.startTakePhoto(_mActivity, entrance, null)

//                if (PermissionHelper.hasCameraPermission(_mActivity)) {
//                    TakePhotoFragment.startTakePhoto(entrance)
//                    isPermissionCameraNever = false
//                } else {
//                    PermissionHelper.requestCameraPermission(_mActivity, object : OnPermissionResult {
//                        override fun onPermissionDeny(permission: String?, never: Boolean) {
//                            if (isPermissionCameraNever || PermissionHelper.isPermissionGroupDeny(_mActivity, permission)) {
//                                showPermissionDenyNeverDialog(_mActivity, getString(R.string.permission_tip_camera_never))
//                            }
//                            isPermissionCameraNever = never
//                        }
//
//                        override fun onPermissionGrant(permission: String?) {
//                            TakePhotoFragment.startTakePhoto(entrance)
//                            BaseSeq103OperationStatistic.uploadSqe103StatisticData(Statistic103Constant.CAMERA_PERMISSION_OBTAINED,
//                                    Statistic103Constant.ENTRANCE_CAMERA_CLICK)
//                            isPermissionCameraNever = false
//                        }
//                    }, -1)
//                    BaseSeq103OperationStatistic.uploadSqe103StatisticData(Statistic103Constant.CAMERA_PERMISSION_REQUEST,
//                            Statistic103Constant.ENTRANCE_CAMERA_CLICK)
//                }
            }
            R.id.cl_take_album -> {
                mCurrentStatus = STATUS_ALBUM
                setStatus()
            }
            R.id.cl_take_discovey -> {
                mCurrentStatus = STATUS_DISCOVERY
                setStatus()
            }
            else -> {
            }
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

    private fun setStatus() {
        if (cl_take_discovey == null) {
            return
        }
        when (mCurrentStatus) {
            STATUS_ALBUM -> {
                val lp = cl_take_discovey.layoutParams as ConstraintLayout.LayoutParams
                lp.verticalBias = 1f
                cl_take_discovey.layoutParams = lp

                fl_album_container.visibility = View.VISIBLE
                fl_discovery_container.visibility = View.GONE
                BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                        Statistic103Constant.ALBUM_ENTER, ALBUM_ENTER_BABY_INNER)
            }
            STATUS_DISCOVERY -> {
                val lp = cl_take_discovey.layoutParams as ConstraintLayout.LayoutParams
                lp.verticalBias = 0f
                cl_take_discovey.layoutParams = lp

                fl_album_container.visibility = View.GONE
                fl_discovery_container.visibility = View.VISIBLE
                BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                        Statistic103Constant.DISCOVERYTAB_ENTER, ALBUM_ENTER_BABY_INNER)
                mDiscoveryFragment?.apply {
                    setHotwordLines()
                }
            }
            else -> {
                val lp = cl_take_discovey.layoutParams as ConstraintLayout.LayoutParams
                lp.verticalBias = 1f
                cl_take_discovey.layoutParams = lp

                fl_album_container.visibility = View.VISIBLE
                fl_discovery_container.visibility = View.GONE
            }
        }
    }

    private fun initToolbar() {
        face_common_toolbar.setBackDrawable(R.drawable.icon_back_black_selector)
        face_common_toolbar.setTitle("")
        face_common_toolbar.setTitleColor(resources.getColor(R.color.toolbar_title_dark_color))
        face_common_toolbar.setTitleGravity(Gravity.START)
        face_common_toolbar.setOnTitleClickListener { view, back ->
            if (view.id == R.id.img_back) {
                popResult("", null)
            }
        }
    }


    private fun popResult(originalPath: String, faceFunctionBean: FaceFunctionBean?) {
        val bundle = Bundle()
        bundle.putString(RESULT_ORIGINAL_PATH, originalPath)
        bundle.putParcelable(RESULT_FACE_BEAN, faceFunctionBean)
        setFragmentResult(ISupportFragment.RESULT_OK, bundle)
        popTo(InnerPickFragment::class.java, true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }
}