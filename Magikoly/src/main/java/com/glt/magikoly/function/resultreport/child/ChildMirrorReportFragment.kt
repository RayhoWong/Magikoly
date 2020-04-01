package com.glt.magikoly.function.resultreport.child

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.glt.magikoly.BuyChannelApiProxy
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.SafelyBitmap
import com.glt.magikoly.event.PredictionRequestSuccess
import com.glt.magikoly.event.ReportErrorEvent
import com.glt.magikoly.ext.postEvent
import com.glt.magikoly.function.FaceFunctionManager
import com.glt.magikoly.function.ReportNames
import com.glt.magikoly.function.main.ISubscribe
import com.glt.magikoly.function.main.ITabFragment
import com.glt.magikoly.function.main.ToolBarCallback
import com.glt.magikoly.function.poster.ISavePhoto
import com.glt.magikoly.function.resultreport.FaceReportFragment
import com.glt.magikoly.function.resultreport.TabInfo.CREATOR.SUB_TAB_CHILD
import com.glt.magikoly.function.resultreport.child.presenter.ChildMirrorReportPresenter
import com.glt.magikoly.mvp.BaseSupportFragment
import com.glt.magikoly.permission.OnPermissionResult
import com.glt.magikoly.permission.PermissionSaveController
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.IStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.subscribe.SubscribeController
import com.glt.magikoly.utils.DrawUtils
import com.glt.magikoly.utils.Logcat
import com.glt.magikoly.view.GlobalProgressBar
import kotlinx.android.synthetic.main.child_mirror_report_layout.*
import magikoly.magiccamera.R
import kotlin.math.max

class ChildMirrorReportFragment : BaseSupportFragment<ChildMirrorReportPresenter>(),
        IChildMirrorReportView,
        IStatistic, ITabFragment, ISubscribe, ISavePhoto {

    private var mUseFreeCount: Boolean = false
    override var watchAdFinish: Boolean = false
    override var fromClick = false

    companion object {
        private const val RESULT_KEY = "result_key"
        fun newInstance(): ChildMirrorReportFragment {
            return ChildMirrorReportFragment()
        }
    }

    private var resultBitmap: SafelyBitmap? = null
    private var mCurrentStatus: Int = -1

    override fun getTabLock(): Boolean = FaceFunctionManager.demoFaceImageInfo == null
            && !SubscribeController.getInstance().isVIP(SubscribeController.PERMISSION_WITH_TRIAL) && !watchAdFinish

    override fun getStatus(): Int {
        return mCurrentStatus
    }

    override fun setStatus(status: Int) {
        mCurrentStatus = status
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        resultBitmap?.let {
            outState.putParcelable(RESULT_KEY, it)
        }
    }

    override fun restoreInstanceState(outState: Bundle?) {
        if (resultBitmap == null) {
            outState?.getParcelable<SafelyBitmap>(RESULT_KEY)?.let {
                resultBitmap = it
            }
        }
    }

    override fun getToolBarMenuCallback(): ToolBarCallback? {
        return object : ToolBarCallback {
            override fun invoke(): Boolean {
                if (FaceFunctionManager.demoFaceImageInfo == null) {
                    BaseSeq103OperationStatistic.uploadSqe103StatisticData("",
                            Statistic103Constant.SAVEPHOTO_CLICK, entrance, "",
                            FaceReportFragment.isDefaultEnter(entrance), BuyChannelApiProxy.getCampaign())
                    PermissionSaveController.requestPermission(activity,
                            object : OnPermissionResult {
                                override fun onPermissionDeny(permission: String?, never: Boolean) {

                                }

                                override fun onPermissionGrant(permission: String?) {
                                    resultBitmap?.let {
                                        saveResultImage(getTabId(), it.getBitmap()!!)
                                    }
                                }
                            }, entrance)
                    return true
                } else {
                    return false
                }
            }
        }
    }

    override fun reload() {
        loadData(mUseFreeCount)
    }

    override fun onChildReportGenerateSuccess(result: Bitmap) {
        if (isVisible) {
            hideErrorEvent()
            GlobalProgressBar.hide()
        }
        val faceBean = FaceFunctionManager.faceBeanMap[FaceFunctionManager.currentFaceImagePath]
        if (isDetached || faceBean == null) {
            return
        }
        resultBitmap = SafelyBitmap(result)
        img_result.setImageBitmap(resultBitmap?.getBitmap())
        layoutWaterMark()
        stopBlurReport()

        postEvent(PredictionRequestSuccess(getTabId()))
    }

    override fun onChildReportGenerateFail(errorCode: Int) {
        if (isVisible) {
            GlobalProgressBar.hide()
            showErrorEvent(errorCode)
        }
    }

    override fun createPresenter(): ChildMirrorReportPresenter {
        return ChildMirrorReportPresenter()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.child_mirror_report_layout, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreInstanceState(savedInstanceState)
        if (FaceFunctionManager.demoFaceImageInfo == null) {
            if (resultBitmap != null) {
                img_result.setImageBitmap(resultBitmap?.getBitmap())
            } else {
                img_result.setImageBitmap(
                        FaceFunctionManager.faceBeanMap[FaceFunctionManager.currentFaceImagePath]?.face?.getBitmap())
            }
        } else {
            resultBitmap = SafelyBitmap((resources.getDrawable(
                    FaceFunctionManager.demoFaceImageInfo!!.childId) as BitmapDrawable).bitmap)
            img_result.setImageBitmap(resultBitmap?.getBitmap())
        }

        img_rotate_image.setOnClickListener {
            rotateImage()
        }
        post {
            layoutWaterMark()
        }
    }

    private fun layoutWaterMark() {
        if (img_result != null && poster_water_mask != null) {
            val rect = Rect()
            val rectF = RectF()
            img_result.drawable.copyBounds(rect)
            img_result.imageMatrix
            rectF.set(rect)
            img_result.imageMatrix.mapRect(rectF)
            var bottomMargin = img_result.height - rectF.bottom.toInt() + DrawUtils.dip2px(18f)
            bottomMargin = if (bottomMargin > DrawUtils.dip2px(80f)) bottomMargin else DrawUtils.dip2px(80f)
            (poster_water_mask.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = bottomMargin
            (poster_water_mask.layoutParams as ViewGroup.MarginLayoutParams).rightMargin = img_result.width - rectF.right.toInt() + DrawUtils.dip2px(
                    18f)
            poster_water_mask.requestLayout()
        }
    }

    override fun onTabFragmentVisible() {
        if (FaceFunctionManager.demoFaceImageInfo == null) {
            if (!SubscribeController.getInstance().isVIP(SubscribeController.PERMISSION_WITH_TRIAL) && resultBitmap == null && !SubscribeController.getInstance().isFreeCount()) {
                GlobalProgressBar.hide()
                startBlurReport()
                BaseSeq103OperationStatistic.uploadSqe103StatisticData("",
                        Statistic103Constant.FAKEREPORT_ENTER, entrance, "",
                        FaceReportFragment.isDefaultEnter(entrance), BuyChannelApiProxy.getCampaign())
            } else {
                if (resultBitmap == null) {
                    loadData(!SubscribeController.getInstance().isVIP(SubscribeController.PERMISSION_WITH_TRIAL))
                } else {
                    stopBlurReport()
                }
            }
        } else {
            stopBlurReport()
        }
    }

    override fun onTabFragmentInvisible() {
        hideErrorEvent()
    }

    private var curImageAngle = 0f
    private var isImageRotating = false

    private fun rotateImage() {
        if (isImageRotating) {
            return
        }
        isImageRotating = true
        var scale = 1f
        var nextAngle = curImageAngle + 90f
        curImageAngle = nextAngle
        if (curImageAngle == 360f) {
            curImageAngle = 0f
        }
        Logcat.i("Test", "angle: $nextAngle")
        img_result.animate().setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                img_result?.rotation = curImageAngle
                isImageRotating = false
            }
        })
        when (nextAngle) {
            90f, 270f -> {
                val targetScale = 1f * img_result.width / resultBitmap!!.getHeight()
                var currentScale = if (resultBitmap!!.getWidth() < resultBitmap!!.getHeight()) {
                    1f * img_result.height / resultBitmap!!.getHeight()
                } else {
                    1f * img_result.width / resultBitmap!!.getWidth()
                }
                scale = targetScale / currentScale
                img_result.animate().rotation(nextAngle).scaleX(scale).scaleY(scale).setDuration(500).start()
            }
            180f, 360f -> {
                img_result.animate().rotation(nextAngle).scaleX(scale).scaleY(scale).setDuration(500).start()
            }
        }
    }

    private fun startBlurReport() {
        setStatus(ITabFragment.STATUS_PURCHASE)
        postEvent(ReportErrorEvent())
    }

    private fun showErrorEvent(errorCode: Int) {
        setStatus(ITabFragment.STATUS_ERROR)
        postEvent(ReportErrorEvent(errorCode))
    }

    private fun hideErrorEvent() {
        setStatus(ITabFragment.STATUS_OPEN)
        postEvent(ReportErrorEvent())
    }

    private fun stopBlurReport() {
        setStatus(ITabFragment.STATUS_OPEN)
        postEvent(ReportErrorEvent())
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
    }

    private fun loadData(useFreeCount: Boolean? = false) {
        if (mPresenter.isGenerating) {
            GlobalProgressBar.show(entrance)
        } else if (resultBitmap == null) {
            mUseFreeCount = useFreeCount!!

            GlobalProgressBar.show(entrance)
            val src = FaceFunctionManager.faceBeanMap[FaceFunctionManager.currentFaceImagePath]?.face?.getBitmap()
            src?.let {
                val longBound = max(it.width, it.height)
                val scale = if (longBound > 400f) 400f / longBound else 1f
                val width = it.width * scale
                val height = it.height * scale
                mPresenter.requestChildReport(
                        ThumbnailUtils.extractThumbnail(it, width.toInt(), height.toInt()),mUseFreeCount)
            }
        }
    }

    override fun getFilePrefix(): String {
        return ReportNames.CHILD_REPORT_PREFIX
    }

    override fun getTabId(): Int = SUB_TAB_CHILD
    override fun getEntrance(): String = Statistic103Constant.ENTRANCE_CHILD

    override fun getTabCategory(): String = ""
    override fun getToolBarTitle(): String = ""

    override fun getToolBarBackCallback(): ToolBarCallback? = null
    override fun getBottomBarIcon(): Array<Drawable>? = null
    override fun getToolBarSelfCallback(): ToolBarCallback? = null

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

    override fun getBottomBarTitle(): String {
        return FaceAppState.getContext().resources.getString(R.string.child_mirror)
    }


    override fun getGPColor(): Int {
        return Color.parseColor("#ffe972")
    }
}