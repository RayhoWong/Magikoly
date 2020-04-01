package com.glt.magikoly.function.resultreport.aging

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.glt.magikoly.BuyChannelApiProxy
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.SafelyBitmap
import com.glt.magikoly.constants.ErrorCode
import com.glt.magikoly.event.PredictionRequestSuccess
import com.glt.magikoly.event.ReportErrorEvent
import com.glt.magikoly.ext.postEvent
import com.glt.magikoly.ext.runMain
import com.glt.magikoly.function.FaceFunctionManager
import com.glt.magikoly.function.ReportNames
import com.glt.magikoly.function.main.ISubscribe
import com.glt.magikoly.function.main.ITabFragment
import com.glt.magikoly.function.main.ToolBarCallback
import com.glt.magikoly.function.poster.ISavePhoto
import com.glt.magikoly.function.resultreport.FaceReportFragment
import com.glt.magikoly.function.resultreport.TabInfo.CREATOR.SUB_TAB_AGING
import com.glt.magikoly.function.resultreport.aging.presenter.AgingShutterReportPresenter
import com.glt.magikoly.mvp.BaseSupportFragment
import com.glt.magikoly.permission.OnPermissionResult
import com.glt.magikoly.permission.PermissionSaveController
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.IStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.subscribe.SubscribeController
import com.glt.magikoly.utils.BitmapUtils
import com.glt.magikoly.utils.DrawUtils
import com.glt.magikoly.utils.Logcat
import com.glt.magikoly.view.GlobalProgressBar
import kotlinx.android.synthetic.main.aging_shutter_report_layout.*
import magikoly.magiccamera.R

class AgingShutterReportFragment : BaseSupportFragment<AgingShutterReportPresenter>(),
        IAgingShutterReportView,
        IStatistic, ITabFragment, ISubscribe, ISavePhoto {

    private var mUseFreeCount: Boolean = false
    override var watchAdFinish: Boolean = false
    override var fromClick = false

    companion object {
        private const val RESULT_KEY = "result_key"
        fun newInstance(): AgingShutterReportFragment {
            return AgingShutterReportFragment()
        }
    }

    private var switchAnimator: ValueAnimator? = null
    private var originalAlpha = 0f
    private val imageTargets = ArrayList<SimpleTarget<Bitmap>>()
    private var isExit: Boolean = false
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
                    val origPhoto = FaceFunctionManager.faceBeanMap[FaceFunctionManager.currentFaceImagePath]?.photo?.getBitmap()
                    PermissionSaveController.requestPermission(activity,
                            object : OnPermissionResult {
                                override fun onPermissionDeny(permission: String?, never: Boolean) {

                                }

                                override fun onPermissionGrant(permission: String?) {
                                    if (origPhoto != null && resultBitmap != null) {
//                                        savePoster(getTabId(),
//                                                getString(R.string.poster_result_title_aging),
//                                                getString(R.string.poster_result_desc_aging),
//                                                origPhoto, resultBitmap?.getBitmap(),
//                                                BitmapFactory.decodeResource(resources,
//                                                        R.drawable.poster_bg_aging))
                                        saveResultImage(getTabId(), resultBitmap?.getBitmap()!!)
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

    override fun onAgingReportGenerateSuccess(url: String) {
        val target = object : SimpleTarget<Bitmap>() {

            private var retryCount = 0

            override fun onResourceReady(oldFace: Bitmap, transition: Transition<in Bitmap>?) {
                if (isVisible) {
                    hideErrorEvent()
                    GlobalProgressBar.hide()
                }
                val faceBean = FaceFunctionManager.faceBeanMap[FaceFunctionManager.currentFaceImagePath]
                if (isExit || faceBean == null) {
                    return
                }
                imageTargets.remove(this)
                val bitmap = faceBean?.photo?.getBitmap()
                val base = bitmap?.copy(bitmap.config, true)
                val rect = faceBean?.faceRect
                resultBitmap = SafelyBitmap(BitmapUtils.composeBitmap(base, oldFace, rect!!.left, rect.top,
                        rect.width(), rect.height()))
                img_result.setImageBitmap(resultBitmap?.getBitmap())
                img_original.visibility = View.INVISIBLE
                stopBlurReport()

                postEvent(PredictionRequestSuccess(getTabId()))
            }

            override fun onLoadFailed(errorDoncreaonrawable: Drawable?) {
                if (isExit) {
                    GlobalProgressBar.hide()
                    return
                }
                if (retryCount < 3) {
                    retryCount++
                    runMain {
                        Glide.with(this@AgingShutterReportFragment).asBitmap().load(url).into(this)
                    }
                } else {
                    if (isVisible) {
                        GlobalProgressBar.hide()
                        showErrorEvent(ErrorCode.IMAGE_LOAD_FAIL)
                    }
                    imageTargets.remove(this)
                    isExit = true
                    runMain {
                        clearImageTargets()
                    }
                }
            }
        }
        imageTargets.add(target)
        Glide.with(this).asBitmap().load(url).into(target)
    }

    override fun onAgingReportGenerateFail(errorCode: Int) {
        if (isVisible) {
            GlobalProgressBar.hide()
            showErrorEvent(errorCode)
        }
    }

    override fun createPresenter(): AgingShutterReportPresenter {
        return AgingShutterReportPresenter()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.aging_shutter_report_layout, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreInstanceState(savedInstanceState)
        if (FaceFunctionManager.demoFaceImageInfo == null) {
            img_original.setImageBitmap(FaceFunctionManager.faceBeanMap[FaceFunctionManager.currentFaceImagePath]?.photo?.getBitmap())
            resultBitmap?.let {
                img_result.setImageBitmap(it.getBitmap())
                img_original.visibility = View.INVISIBLE
            }
        } else {
            img_original.setImageResource(FaceFunctionManager.demoFaceImageInfo!!.imgId)
            resultBitmap = SafelyBitmap((resources.getDrawable(
                    FaceFunctionManager.demoFaceImageInfo!!.oldId) as BitmapDrawable).bitmap)
            img_result.setImageBitmap(resultBitmap?.getBitmap())
            img_original.visibility = View.INVISIBLE
        }

        val touchListener = View.OnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    showOriginalPhoto()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    hideOriginalPhoto()
                }
            }
            true
        }
        img_rotate_image.setOnClickListener {
            rotateImage()
        }
        img_see_original.setOnTouchListener(touchListener)
        img_result.setOnTouchListener(touchListener)
        post {
            if (img_original != null && poster_water_mask != null) {
                val rect = Rect()
                val rectF = RectF()
                img_original.drawable.copyBounds(rect)
                img_original.imageMatrix
                rectF.set(rect)
                img_original.imageMatrix.mapRect(rectF)
                var bottomMargin = img_result.height - rectF.bottom.toInt() + DrawUtils.dip2px(18f)
                bottomMargin = if (bottomMargin > DrawUtils.dip2px(80f)) bottomMargin else DrawUtils.dip2px(80f)
                (poster_water_mask.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = bottomMargin
                (poster_water_mask.layoutParams as ViewGroup.MarginLayoutParams).rightMargin = img_result.width - rectF.right.toInt() + DrawUtils.dip2px(
                        18f)
                poster_water_mask.requestLayout()
            }
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
        img_original.run {
            rotation = curImageAngle
            scaleX = scale
            scaleY = scale
        }
    }

    private fun showOriginalPhoto() {
        switchAnimator?.cancel()
        img_original.visibility = View.VISIBLE
        switchAnimator = ValueAnimator.ofFloat(originalAlpha, 1f)
        switchAnimator!!.duration = (1000f * (1f - originalAlpha)).toLong()
        if (switchAnimator!!.duration > 0) {
            switchAnimator!!.addUpdateListener {
                originalAlpha = it.animatedValue as Float
                img_original.imageAlpha = (originalAlpha * 255).toInt()
                img_original.invalidate()
            }
            switchAnimator!!.start()
        }
    }

    private fun hideOriginalPhoto() {
        switchAnimator?.cancel()
        switchAnimator = ValueAnimator.ofFloat(originalAlpha, 0f)
        switchAnimator?.duration = (1000f * originalAlpha).toLong()
        if (switchAnimator!!.duration > 0) {
            switchAnimator?.addUpdateListener {
                originalAlpha = it.animatedValue as Float
                img_original.imageAlpha = (originalAlpha * 255).toInt()
                img_original.invalidate()
            }
            switchAnimator?.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    if (originalAlpha == 0f) {
                        img_original.visibility = View.INVISIBLE
                    }
                }
            })
            switchAnimator?.start()
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
            mPresenter.generateAgingReport(mUseFreeCount)
        }
    }

    /* override fun onPhotoSaved(success: Boolean) {
         if (success) {
             Toast.makeText(FaceAppState.getContext(), R.string.save_report_success,
                     Toast.LENGTH_LONG).show()
         } else {
             Toast.makeText(FaceAppState.getContext(), R.string.save_report_failed,
                     Toast.LENGTH_LONG).show()
         }
     }*/

    override fun onDestroyView() {
        isExit = true
        clearImageTargets()
        switchAnimator?.cancel()
        super.onDestroyView()
    }

    private fun clearImageTargets() {
        Glide.with(this).pauseAllRequests()
        imageTargets.forEach {
            Glide.with(this).clear(it)
        }
        imageTargets.clear()
    }

//    private fun backToMain() {
//        (parentFragment as SupportFragment).popTo(MainFragment::class.java, false)
//    }

    override fun getFilePrefix(): String {
        return ReportNames.AGING_REPORT_PREFIX
    }

    override fun getTabId(): Int = SUB_TAB_AGING
    override fun getEntrance(): String = Statistic103Constant.ENTRANCE_AGING

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
        return FaceAppState.getContext().resources.getString(R.string.aging_camera)
    }


    override fun getGPColor(): Int {
        return Color.parseColor("#ffe972")
    }
}