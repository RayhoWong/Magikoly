package com.glt.magikoly.function.resultreport.gender

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.glt.magikoly.BuyChannelApiProxy
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.SafelyBitmap
import com.glt.magikoly.config.AgingShutterConfigBean
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
import com.glt.magikoly.function.resultreport.TabInfo.CREATOR.SUB_TAB_GENDER
import com.glt.magikoly.mvp.BaseSupportFragment
import com.glt.magikoly.permission.OnPermissionResult
import com.glt.magikoly.permission.PermissionSaveController
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.IStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.subscribe.SubscribeController
import com.glt.magikoly.thread.FaceThreadExecutorProxy
import com.glt.magikoly.view.GlobalProgressBar
import kotlinx.android.synthetic.main.fragment_gender_report.*
import magikoly.magiccamera.R

/**
 *
 * @author rayhahah
 * @blog http://rayhahah.com
 * @time 2019/2/18
 * @tips 这个类是Object的子类
 * @fuction
 */

class GenderReportFragment : BaseSupportFragment<GenderReportPresenter>(), IGenderReportView, IStatistic,
        ITabFragment, ISubscribe, ISavePhoto {

    private var mUseFreeCount: Boolean = false
    override var watchAdFinish: Boolean = false
    override var fromClick = false

    override fun getTabLock(): Boolean = FaceFunctionManager.demoFaceImageInfo == null
            && !SubscribeController.getInstance().isVIP(SubscribeController.PERMISSION_WITH_TRIAL)
            && !watchAdFinish

    private var mCurrentStatus: Int = -1

    override fun getStatus(): Int {
        return mCurrentStatus
    }

    override fun setStatus(status: Int) {
        mCurrentStatus = status
    }

    companion object {
        private const val RESULT_KEY = "result_key"
        fun newInstance(): GenderReportFragment {
            return GenderReportFragment()
        }
    }

    private val imageTargets = ArrayList<SimpleTarget<Bitmap>>()
    private var isExit: Boolean = false
    private var resultBitmap: SafelyBitmap? = null

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

    override fun getTabId(): Int = SUB_TAB_GENDER

    override fun getToolBarTitle(): String = ""

    override fun getToolBarBackDrawable(): Drawable? {
        return FaceAppState.getContext().resources.getDrawable(R.drawable.icon_back_selector)
    }

    override fun getToolBarMenuDrawable(): Drawable? {
        return if (FaceFunctionManager.demoFaceImageInfo == null)
            FaceAppState.getContext().resources.getDrawable(R.drawable.icon_save_black_selector)
        else
            null
    }

    override fun getToolBarBackCallback(): ToolBarCallback? = null

    override fun getToolBarMenuCallback(): ToolBarCallback? {
        return if (FaceFunctionManager.demoFaceImageInfo == null) {
            object : ToolBarCallback {
                override fun invoke(): Boolean {
                    BaseSeq103OperationStatistic.uploadSqe103StatisticData("",
                            Statistic103Constant.SAVEPHOTO_CLICK, entrance, tabCategory,
                            FaceReportFragment.isDefaultEnter(entrance), BuyChannelApiProxy.getCampaign())
                    PermissionSaveController.requestPermission(activity,
                            object : OnPermissionResult {
                                override fun onPermissionDeny(permission: String?, never: Boolean) {

                                }

                                override fun onPermissionGrant(permission: String?) {
                                    resultBitmap?.let { result ->
                                        val faceBean = FaceFunctionManager.faceBeanMap[FaceFunctionManager.currentFaceImagePath]
                                        faceBean?.let { faceBean ->
                                            val male = "M" == faceBean.faceInfo.gender
                                            var posterTitle = if (!male) getString(
                                                    R.string.poster_result_title_gender_male) else getString(
                                                    R.string.poster_result_title_gender_female)
                                            var posterDesc = if (!male) getString(
                                                    R.string.poster_result_desc_gender_male) else getString(
                                                    R.string.poster_result_desc_gender_female)
                                            savePoster(getTabId(), posterTitle, posterDesc,
                                                    faceBean.photo.getBitmap(),
                                                    result.getBitmap(),
                                                    BitmapFactory.decodeResource(resources,
                                                            R.drawable.poster_bg_gender))
//                                            saveResultImage(getTabId(), result.getBitmap()!!)
                                        }
                                    }
                                }
                            }, entrance)
                    return true
                }
            }
        } else {
            null
        }
    }

    override fun getToolBarSelfCallback(): ToolBarCallback? = null

    override fun getToolBarItemColor(): Int? {
        return FaceAppState.getContext().resources.getColor(R.color.toolbar_title_dark_color)
    }

    override fun getBottomBarTitle(): String {
        return FaceAppState.getContext().resources.getString(R.string.gender_swap)
    }

    override fun getBottomBarIcon(): Array<Drawable>? = null

    override fun getEntrance(): String = Statistic103Constant.ENTRANCE_GENDER

    override fun getTabCategory(): String = AgingShutterConfigBean.getRequestType()

    override fun createPresenter(): GenderReportPresenter = GenderReportPresenter()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_gender_report, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreInstanceState(savedInstanceState)

        val faceBean = FaceFunctionManager.faceBeanMap[FaceFunctionManager.currentFaceImagePath]

        iv_gender_original.isOval = true
        iv_gender_original.setIsDrawCircleLine(true)
        iv_gender_original.setCircleLineColor(Color.parseColor("#ffffff"))

        if (FaceFunctionManager.demoFaceImageInfo == null) {
            iv_gender_original.setImageBitmap(faceBean?.photo?.getBitmap())
            if (resultBitmap != null) {
                iv_gender_report.setImageBitmap(resultBitmap?.getBitmap())
            } else {
                iv_gender_report.setImageBitmap(faceBean?.photo?.getBitmap())
            }
        } else {
            iv_gender_original.setImageResource(FaceFunctionManager.demoFaceImageInfo!!.imgId)
            resultBitmap = SafelyBitmap((resources.getDrawable(
                    FaceFunctionManager.demoFaceImageInfo!!.genderId) as BitmapDrawable).bitmap)
            iv_gender_report.setImageBitmap(resultBitmap?.getBitmap())
        }
    }

    override fun onTabFragmentVisible() {
        if (FaceFunctionManager.demoFaceImageInfo == null) {
            if (!SubscribeController.getInstance().isVIP(SubscribeController.PERMISSION_WITH_TRIAL) && resultBitmap == null && !SubscribeController.getInstance().isFreeCount()) {
                GlobalProgressBar.hide()
                startBlurReport()
                BaseSeq103OperationStatistic.uploadSqe103StatisticData("",
                        Statistic103Constant.FAKEREPORT_ENTER, entrance, tabCategory,
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

    override fun onDestroyView() {
        isExit = true
        clearImageTargets()
        super.onDestroyView()
    }

    override fun reload() {
        loadData(mUseFreeCount)
    }

    fun loadData(useFreeCount: Boolean? = false) {


        if (mPresenter == null) {
            mPresenter = GenderReportPresenter()
        }
        if (mPresenter.isGenerating) {
            GlobalProgressBar.show(entrance)
        } else if (resultBitmap == null) {
            mUseFreeCount = useFreeCount!!
            GlobalProgressBar.show(entrance)
            mPresenter.generateGenderReport(mUseFreeCount)
        }
    }

    private fun startBlurReport() {
        setStatus(ITabFragment.STATUS_PURCHASE)
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


    private fun showErrorEvent(errorCode: Int = ErrorCode.NETWORK_ERROR) {
        setStatus(ITabFragment.STATUS_ERROR)
        postEvent(ReportErrorEvent(errorCode))
    }

    private fun hideErrorEvent() {
        setStatus(ITabFragment.STATUS_OPEN)
        postEvent(ReportErrorEvent())
    }


    override fun onFaceDetectFail(errorCode: Int) {
    }

    private fun clearImageTargets() {
        Glide.with(this).pauseAllRequests()
        imageTargets.forEach {
            Glide.with(this).clear(it)
        }
        imageTargets.clear()
    }

    override fun onGenderReportGenerateSuccess(url: String) {
        stopBlurReport()
        val target = object : SimpleTarget<Bitmap>() {

            private var retryCount = 0

            override fun onResourceReady(oldFace: Bitmap, transition: Transition<in Bitmap>?) {
                if (isVisible) {
                    GlobalProgressBar.hide()
                    hideErrorEvent()
                }
                if (isExit) {
                    return
                }
                imageTargets.remove(this)
                resultBitmap = SafelyBitmap(oldFace)
                iv_gender_report.setImageBitmap(oldFace)

                postEvent(PredictionRequestSuccess(getTabId()))
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                if (isExit) {
                    GlobalProgressBar.hide()
                    return
                }
                if (retryCount < 3) {
                    retryCount++
                    FaceThreadExecutorProxy.runOnMainThread {
                        Glide.with(this@GenderReportFragment).asBitmap().load(url).into(this)
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

    override fun onGenderReportGenerateFail(errorCode: Int) {
        if (isVisible) {
            GlobalProgressBar.hide()
            showErrorEvent(errorCode)
        }
    }

    /*override fun onPhotoSaved(success: Boolean) {
    }*/

    override fun getFilePrefix(): String {
        return ReportNames.GENDER_REPORT_PREFIX
    }

    override fun getGPColor(): Int {
        return Color.parseColor("#2ee4ff")
    }
}