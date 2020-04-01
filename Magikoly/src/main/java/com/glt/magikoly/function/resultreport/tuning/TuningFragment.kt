package com.glt.magikoly.function.resultreport.tuning

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.glt.magikoly.BuyChannelApiProxy
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.constants.ErrorCode
import com.glt.magikoly.event.BeautyNoSecectEvent
import com.glt.magikoly.event.PredictionRequestInit
import com.glt.magikoly.event.PredictionRequestSuccess
import com.glt.magikoly.event.ReportErrorEvent
import com.glt.magikoly.ext.postEvent
import com.glt.magikoly.ext.runMain
import com.glt.magikoly.function.FaceFunctionManager
import com.glt.magikoly.function.ReportNames
import com.glt.magikoly.function.facesdk.FaceSdkProxy
import com.glt.magikoly.function.main.INewTabFragment
import com.glt.magikoly.function.main.ISubscribe
import com.glt.magikoly.function.main.ITabFragment
import com.glt.magikoly.function.main.ToolBarCallback
import com.glt.magikoly.function.poster.ISavePhoto
import com.glt.magikoly.function.resultreport.FaceReportFragment
import com.glt.magikoly.function.resultreport.TabInfo.CREATOR.SUB_TAB_FILTER
import com.glt.magikoly.function.resultreport.TabInfo.CREATOR.SUB_TAB_TUNING
import com.glt.magikoly.permission.OnPermissionResult
import com.glt.magikoly.permission.PermissionSaveController
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.IStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import kotlinx.android.synthetic.main.fragment_tunnig.*
import magikoly.magiccamera.R
import me.yokeyword.fragmentation.SupportFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class TuningFragment : SupportFragment(), INewTabFragment, IStatistic, ITabFragment, ISubscribe, ISavePhoto, View.OnClickListener {
    override fun onExit() {
        popSelf()
    }

    companion object {
        fun newInstance(): TuningFragment {
            return TuningFragment()
        }
    }

    var beautyLevel = 50.0f
    var whiteningLevel = 50.0f
    var bigEyesLevel = 50.0f
    var faceShapeLevel = 50.0f

    override var watchAdFinish: Boolean = false

    override var fromClick = false

    private var mCurrentStatus: Int = -1

    private var mCurrentFeature: Int = 1

    override fun getEntrance(): String = Statistic103Constant.ENTRANCE_TURNING

    override fun getTabCategory(): String = ""

    override fun getTabId(): Int = SUB_TAB_TUNING

    override fun getToolBarTitle(): String = ""

    override fun getToolBarBackCallback(): ToolBarCallback? = null

    override fun getToolBarSelfCallback(): ToolBarCallback? = null

    override fun getToolBarMenuCallback(): ToolBarCallback? = null

    override fun getToolBarBackDrawable(): Drawable? {
        return FaceAppState.getContext().resources.getDrawable(R.drawable.icon_back_selector)
    }

    override fun getToolBarMenuDrawable(): Drawable? {
        return if (FaceFunctionManager.demoFaceImageInfo == null) {
            FaceAppState.getContext().resources.getDrawable(
                    R.drawable.icon_save_black_selector)
        } else {
            null
        }
    }

    override fun getToolBarItemColor(): Int? {
        return FaceAppState.getContext().resources.getColor(R.color.toolbar_title_dark_color)
    }

    override fun getBottomBarTitle(): String = ""

    override fun getBottomBarIcon(): Array<Drawable>? = null

    override fun reload() {
    }

    override fun getStatus(): Int {
        return mCurrentStatus
    }

    override fun setStatus(status: Int) {
        mCurrentStatus = status
    }

    override fun getFilePrefix(): String = ReportNames.BEAUTY_REPORT_PREFIX

    override fun getGPColor(): Int = Color.WHITE

    override fun getTabLock(): Boolean = false

    override fun onTabFragmentVisible() {
        stopBlurReport()
        gpu_image_view.visibility = View.VISIBLE
    }

    override fun onTabFragmentInvisible() {
        hideErrorEvent()
    }

    private fun stopBlurReport() {
        setStatus(ITabFragment.STATUS_OPEN)
        postEvent(ReportErrorEvent())
        BaseSeq103OperationStatistic.uploadSqe103StatisticData("",
                Statistic103Constant.FUNCTION_ENTER, entrance, tabCategory,
                FaceReportFragment.isDefaultEnter(entrance), BuyChannelApiProxy.getCampaign())
    }

    private fun startBlurReport() {
        runMain {
            setStatus(ITabFragment.STATUS_PURCHASE)
            postEvent(ReportErrorEvent())
        }
    }

    private fun showErrorEvent() {
        setStatus(ITabFragment.STATUS_ERROR)
        postEvent(ReportErrorEvent(ErrorCode.NETWORK_ERROR))
    }

    private fun hideErrorEvent() {
        setStatus(ITabFragment.STATUS_OPEN)
        postEvent(ReportErrorEvent())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tunnig, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postEvent(PredictionRequestInit(getTabId()))

        cl_eye.setOnClickListener(this)
        cl_face_shape.setOnClickListener(this)
        cl_whitening.setOnClickListener(this)
        cl_smooth.setOnClickListener(this)
        iv_close.setOnClickListener(this)
        iv_apply.setOnClickListener(this)

        onFeatureClick(mCurrentFeature)
        val bitmap = if (FaceFunctionManager.demoFaceImageInfo == null) {
            iv_apply.visibility = View.VISIBLE
            FaceFunctionManager.faceBeanMap[FaceFunctionManager.currentFaceImagePath]?.photo?.getBitmap()
        } else {
            iv_apply.visibility = View.GONE
            (resources.getDrawable(FaceFunctionManager.demoFaceImageInfo!!.imgId) as BitmapDrawable).bitmap
        }
        if (bitmap == null) {
            popSelf()
            return
        }
        gpu_image_view.setBitmap(bitmap)

        gpu_image_view.runOnGLThread {
            if (FaceFunctionManager.demoFaceImageInfo == null) {
//                postEvent(PredictionRequestSuccess(getTabId()))
            }

            post {
                if (FaceFunctionManager.demoFaceImageInfo == null) {
                    BaseSeq103OperationStatistic.uploadSqe103StatisticData("",
                            Statistic103Constant.FUNCTION_ENTER, entrance,
                            FaceFunctionManager.faceBeanMap[FaceFunctionManager.currentFaceImagePath]?.category,
                            FaceReportFragment.isDefaultEnter(entrance), BuyChannelApiProxy.getCampaign())
                } else {
                    BaseSeq103OperationStatistic.uploadSqe103StatisticData("",
                            Statistic103Constant.FUNCTION_ENTER, entrance,
                            Statistic103Constant.CATEGORY_DEMO,
                            FaceReportFragment.isDefaultEnter(entrance), BuyChannelApiProxy.getCampaign())
                }
                /* if (poster_water_mask != null && gpu_image_view != null) {
                     val outRect = Rect()
                     gpu_image_view.getDrawMarginRect(outRect)
                     (poster_water_mask.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = outRect.bottom + DrawUtils.dip2px(18f)
                     (poster_water_mask.layoutParams as ViewGroup.MarginLayoutParams).rightMargin = outRect.right + DrawUtils.dip2px(18f)
                     poster_water_mask.visibility = View.VISIBLE
                     poster_water_mask.requestLayout()
                 }*/
            }
        }

        seek_bar_current.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val percent = 1.0f * progress / seekBar.max
                when (mCurrentFeature) {
                    1 -> {
                        bigEyesLevel = percent * 100
                        gpu_image_view.setBigEyeLevel(percent)
                    }
                    2 -> {
                        faceShapeLevel = percent * 100
                        gpu_image_view.setFeatureLevel(percent)
                    }
                    3 -> {
                        whiteningLevel = percent * 100
                        gpu_image_view.setBeautyLevel(percent)
                    }
                    4 -> {
                        beautyLevel = percent * 100
                        gpu_image_view.setTenderSkinLevel(percent)
                    }
                    else -> {
                    }
                }
                postEvent(PredictionRequestSuccess(getTabId()))
                gpu_image_view.UpdateView()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        gpu_image_view.setCaptureCallback { buffer, width, height ->
            buffer?.apply {
                /**
                 * 保存图片回调
                 */
                val matrix = Matrix()
                matrix.reset()
                matrix.setRotate(180f)
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bitmap.copyPixelsFromBuffer(buffer)
                val fBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
                saveResultImage(getTabId(), fBitmap) {
                    doExit()
                }
                BaseSeq103OperationStatistic.uploadSqe103StatisticData("",
                        Statistic103Constant.SAVEPHOTO_CLICK, entrance, "",
                        FaceReportFragment.isDefaultEnter(entrance), BuyChannelApiProxy.getCampaign())
                BaseSeq103OperationStatistic.uploadSqe103StatisticData("",
                        Statistic103Constant.BEAUTY_PIC, whiteningLevel.toString(), beautyLevel.toString())
            }

        }
        frameLayout.requestLayout()

        FaceSdkProxy.detectFaceEyeInfo(bitmap, gpu_image_view) {
            if (it) {
                gpu_image_view?.apply {
                    gpu_image_view.setBigEyeLevel(bigEyesLevel / 100)
                    gpu_image_view.setFeatureLevel(faceShapeLevel / 100)
                    gpu_image_view.setBeautyLevel(whiteningLevel / 100)
                    gpu_image_view.setTenderSkinLevel(beautyLevel / 100)
                    gpu_image_view.UpdateView()
                }
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.cl_eye -> {
                onFeatureClick(1)
            }
            R.id.cl_face_shape -> {
                onFeatureClick(2)
            }
            R.id.cl_whitening -> {
                onFeatureClick(3)
            }
            R.id.cl_smooth -> {
                onFeatureClick(4)
            }
            R.id.iv_close -> {
                doExit()
            }
            R.id.iv_apply -> {
                BaseSeq103OperationStatistic.uploadSqe103StatisticData("",
                        Statistic103Constant.SAVEPHOTO_CLICK, entrance, "",
                        FaceReportFragment.isDefaultEnter(entrance), BuyChannelApiProxy.getCampaign())
                BaseSeq103OperationStatistic.uploadSqe103StatisticData(bigEyesLevel.toString(),
                        Statistic103Constant.BIGEYES_VALUE, "", "")
                BaseSeq103OperationStatistic.uploadSqe103StatisticData(faceShapeLevel.toString(),
                        Statistic103Constant.FACESHAPE_VALUE, "", "")
                BaseSeq103OperationStatistic.uploadSqe103StatisticData(whiteningLevel.toString(),
                        Statistic103Constant.WHITENING_VALUE, "", "")
                BaseSeq103OperationStatistic.uploadSqe103StatisticData(beautyLevel.toString(),
                        Statistic103Constant.SMOOTH_VALUE, "", "")
                PermissionSaveController.requestPermission(activity, object : OnPermissionResult {
                    override fun onPermissionDeny(permission: String?, never: Boolean) {
                    }

                    override fun onPermissionGrant(permission: String?) {
                        gpu_image_view?.getCaptureFrame()
                    }
                }, entrance)
            }
            else -> {
            }
        }


    }

    private fun onFeatureClick(position: Int) {
        iv_eye.setImageResource(R.drawable.icon_beauty_eye_normal)
        tv_eye.setTextColor(_mActivity.resources.getColor(R.color.beauty_text_color_normal))
        iv_face_shape.setImageResource(R.drawable.icon_beauty_faceshape_normal)
        tv_face_shape.setTextColor(_mActivity.resources.getColor(R.color.beauty_text_color_normal))
        iv_whitening.setImageResource(R.drawable.icon_beauty_whitening_noraml)
        tv_whitening.setTextColor(_mActivity.resources.getColor(R.color.beauty_text_color_normal))
        iv_smooth.setImageResource(R.drawable.icon_beauty_smooth_normal)
        tv_smooth.setTextColor(_mActivity.resources.getColor(R.color.beauty_text_color_normal))
        mCurrentFeature = position
        when (position) {
            1 -> {
                iv_eye.setImageResource(R.drawable.icon_beauty_eye_select)
                tv_eye.setTextColor(_mActivity.resources.getColor(R.color.beauty_text_color_select))
                seek_bar_current.setProgress(bigEyesLevel.toInt())
            }
            2 -> {
                iv_face_shape.setImageResource(R.drawable.icon_beauty_faceshape_select)
                tv_face_shape.setTextColor(_mActivity.resources.getColor(R.color.beauty_text_color_select))
                seek_bar_current.setProgress(faceShapeLevel.toInt())
            }
            3 -> {
                iv_whitening.setImageResource(R.drawable.icon_beauty_whitening_select)
                tv_whitening.setTextColor(_mActivity.resources.getColor(R.color.beauty_text_color_select))
                seek_bar_current.setProgress(whiteningLevel.toInt())
            }
            4 -> {
                iv_smooth.setImageResource(R.drawable.icon_beauty_smooth_select)
                tv_smooth.setTextColor(_mActivity.resources.getColor(R.color.beauty_text_color_select))
                seek_bar_current.setProgress(beautyLevel.toInt())
            }
            else -> {
            }
        }

        gpu_image_view.UpdateView()
    }

    override fun onResume() {
        super.onResume()
        gpu_image_view.onResume()
    }

    override fun onPause() {
        super.onPause()
        gpu_image_view.onPause()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNoSelect(event: BeautyNoSecectEvent) {
        gpu_image_view.visibility = View.INVISIBLE
    }

    override fun onBackPressedSupport(): Boolean {
        super.onBackPressedSupport()
        doExit()
        return true
    }

    private fun doExit() {
        runMain {
            val faceReportFragment = findFragment(FaceReportFragment::class.java)
            if (faceReportFragment != null) {
                faceReportFragment.confirmExitOrNot(true, getTabId())
            }
        }
    }


    private fun popSelf() {
        val faceReportFragment = findFragment(FaceReportFragment::class.java)
        if (faceReportFragment != null) {
            faceReportFragment.onMenuItemClick(SUB_TAB_FILTER)
        }
        pop()
    }
}