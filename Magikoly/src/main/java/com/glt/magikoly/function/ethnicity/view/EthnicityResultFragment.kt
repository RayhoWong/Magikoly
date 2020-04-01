package com.glt.magikoly.function.ethnicity.view

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.glt.magikoly.BuyChannelApiProxy
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.bean.net.EthnicityReportDTO
import com.glt.magikoly.event.PredictionRequestSuccess
import com.glt.magikoly.event.ReportErrorEvent
import com.glt.magikoly.ext.postEvent
import com.glt.magikoly.ext.runMain
import com.glt.magikoly.function.FaceFunctionManager
import com.glt.magikoly.function.ReportNames
import com.glt.magikoly.function.ethnicity.polygon.PolygonChartBean
import com.glt.magikoly.function.ethnicity.polygon.PolygonChartLayout
import com.glt.magikoly.function.ethnicity.presenter.EthnicityResultPresenter
import com.glt.magikoly.function.main.ISubscribe
import com.glt.magikoly.function.main.ITabFragment
import com.glt.magikoly.function.main.ToolBarCallback
import com.glt.magikoly.function.poster.ISavePhoto
import com.glt.magikoly.function.resultreport.FaceReportFragment
import com.glt.magikoly.function.resultreport.TabInfo.CREATOR.SUB_TAB_ETHNICITY
import com.glt.magikoly.mvp.BaseSupportFragment
import com.glt.magikoly.permission.OnPermissionResult
import com.glt.magikoly.permission.PermissionSaveController
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.IStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.subscribe.SubscribeController
import com.glt.magikoly.utils.BitmapUtils
import com.glt.magikoly.view.GlobalProgressBar
import com.glt.magikoly.view.RoundedImageView
import magikoly.magiccamera.R
import java.util.*

/**
 * @desc: 种族分析结果页
 * @auther:duwei
 * @date:2019/1/14
 */
class EthnicityResultFragment : BaseSupportFragment<EthnicityResultPresenter>(), IEthnicityResultView
        , IStatistic, ITabFragment, ISubscribe, ISavePhoto {

    private var mUseFreeCount: Boolean = false
    override var watchAdFinish: Boolean = false
    override var fromClick = false

    override fun getTabLock(): Boolean = FaceFunctionManager.demoFaceImageInfo == null
            && !SubscribeController.getInstance().isVIP(SubscribeController.PERMISSION_WITH_TRIAL) && !watchAdFinish

    private var mCurrentStatus: Int = -1
    private var dataList: ArrayList<PolygonChartBean>? = null
    private lateinit var mChartLayout: PolygonChartLayout
    private lateinit var mPhoto: RoundedImageView
    private var hasResult = false
    private var isRequesting = false


    companion object {
        private const val DATA_LIST_KEY = "data_list_key"
        private const val CUR_STATUS_KEY = "cur_status_key"

        fun newInstance() = EthnicityResultFragment()
    }

    override fun getStatus(): Int {
        return mCurrentStatus
    }

    override fun setStatus(status: Int) {
        mCurrentStatus = status
    }

    override fun reload() {
        GlobalProgressBar.show(entrance)
        mPresenter.startEthnicityAnalysis(mUseFreeCount)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        dataList?.let {
            outState.putParcelableArrayList(DATA_LIST_KEY, it)
        }
        outState.putInt(CUR_STATUS_KEY, mCurrentStatus)
    }

    override fun restoreInstanceState(outState: Bundle?) {
        if (dataList == null) {
            outState?.getParcelableArrayList<PolygonChartBean>(DATA_LIST_KEY)?.let {
                dataList = it
            }
        }
        outState?.getInt(CUR_STATUS_KEY)?.let {
            mCurrentStatus = it
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.function_ethnicity_result, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreInstanceState(savedInstanceState)
        view.apply {
            mChartLayout = findViewById(R.id.ethnicity_chart_layout)
            mPhoto = findViewById(R.id.ethnicity_photo)

            if (FaceFunctionManager.demoFaceImageInfo == null) {
                dataList?.let {
                    mChartLayout.setData(it)
                    mPhoto.setImageBitmap(FaceFunctionManager.faceBeanMap[FaceFunctionManager.currentFaceImagePath]?.face?.getBitmap())
                }
            } else {
                hasResult = true
                isRequesting = false
                dataList = mPresenter.generateViewData(
                        FaceFunctionManager.demoFaceImageInfo!!.ethnicityReportDTO!!)
                mChartLayout.setData(dataList)
                mPhoto.setImageResource(FaceFunctionManager.demoFaceImageInfo!!.imgId)
            }
        }
    }

    override fun onTabFragmentVisible() {
        if (FaceFunctionManager.demoFaceImageInfo == null) {
            if (!SubscribeController.getInstance().isVIP(SubscribeController.PERMISSION_WITH_TRIAL)
                    && !SubscribeController.getInstance().isFreeCount()
                    && dataList == null) {
                GlobalProgressBar.hide()
                startBlurReport()
                BaseSeq103OperationStatistic.uploadSqe103StatisticData("",
                        Statistic103Constant.FAKEREPORT_ENTER, entrance, "",
                        FaceReportFragment.isDefaultEnter(entrance), BuyChannelApiProxy.getCampaign())
            } else {
                if (dataList == null) {
                    loadData(!SubscribeController.getInstance().isVIP(SubscribeController.PERMISSION_WITH_TRIAL))
                } else {
                    stopBlurReport()
                }
            }
        } else {
            stopBlurReport()
        }
    }

    fun loadData(useFreeCount: Boolean? = false) {
        if (isRequesting) {
            GlobalProgressBar.show(entrance)
        } else if (!hasResult) {
            if (mPresenter == null) {
                mPresenter = EthnicityResultPresenter()
            }
            isRequesting = true
            GlobalProgressBar.show(entrance)

            mUseFreeCount = useFreeCount!!
            mPresenter.startEthnicityAnalysis(mUseFreeCount)
        }
    }

    override fun onTabFragmentInvisible() {
        hideErrorEvent()
    }

    override fun onEthnicityAnalyseSuccess(response: EthnicityReportDTO?) {
        hasResult = true
        isRequesting = false
        if (isVisible) {
            GlobalProgressBar.hide()
            hideErrorEvent()
        }

        dataList = mPresenter.generateViewData(response!!)
        mChartLayout.setData(dataList)
        mPhoto.setImageBitmap(
                FaceFunctionManager.faceBeanMap[FaceFunctionManager.currentFaceImagePath]?.face?.getBitmap())

        stopBlurReport()
        postEvent(PredictionRequestSuccess(getTabId()))
    }

    override fun onEthnicityAnalyseFailed(errorMsg: Int) {
        isRequesting = false
        if (isVisible) {
            GlobalProgressBar.hide()
            showErrorEvent(errorMsg)
        }
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

    private fun startBlurReport() {
        runMain {
            setStatus(ITabFragment.STATUS_PURCHASE)
            postEvent(ReportErrorEvent())
        }
    }

    private fun showErrorEvent(errorMsg: Int) {
        setStatus(ITabFragment.STATUS_ERROR)
        postEvent(ReportErrorEvent(errorMsg))
    }

    private fun hideErrorEvent() {
        setStatus(ITabFragment.STATUS_OPEN)
        postEvent(ReportErrorEvent())
    }

    override fun doBackPressedSupport(): Boolean {
        return getToolBarBackCallback().invoke()
    }

    override fun createPresenter(): EthnicityResultPresenter {
        return EthnicityResultPresenter()
    }


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
                                    val oldBackground = mChartLayout.background
                                    mChartLayout.background = ColorDrawable(Color.WHITE)
                                    savePoster(getTabId(),
                                            getString(R.string.poster_result_title_ethnicity),
                                            getString(R.string.poster_result_desc_ethnicity),
                                            FaceFunctionManager.faceBeanMap[FaceFunctionManager.currentFaceImagePath]!!.photo?.getBitmap(),
                                            BitmapUtils.getBitmapForView(mChartLayout, 1f),
                                            BitmapFactory.decodeResource(resources,
                                                    R.drawable.poster_bg_ethnicity))
                                    mChartLayout.background = oldBackground
                                    BaseSeq103OperationStatistic.uploadSqe103StatisticData("",
                                            Statistic103Constant.SAVEPHOTO_CLICK, entrance, "",
                                            FaceReportFragment.isDefaultEnter(entrance), BuyChannelApiProxy.getCampaign())
//                        mPresenter.saveReport(dataList)
                                }
                            }, entrance)
                    return true
                }
            }
        } else {
            null
        }
    }

    /*  override fun onPhotoSaved(success: Boolean) {
          if (success) {
              Toast.makeText(FaceAppState.getContext(), R.string.save_report_success,
                      Toast.LENGTH_LONG).show()
          } else {
              Toast.makeText(FaceAppState.getContext(), R.string.save_report_failed,
                      Toast.LENGTH_LONG).show()
          }
      }*/

    override fun getTabId(): Int = SUB_TAB_ETHNICITY

    override fun getEntrance(): String = Statistic103Constant.ENTRANCE_ETHNICITY

    override fun getTabCategory(): String = ""

    override fun getToolBarTitle(): String = ""

    override fun getToolBarBackDrawable(): Drawable? =
            FaceAppState.getContext().resources.getDrawable(R.drawable.icon_back_selector)

    override fun getToolBarMenuDrawable(): Drawable? =
            if (FaceFunctionManager.demoFaceImageInfo == null) {
                FaceAppState.getContext().resources.getDrawable(R.drawable.icon_save_black_selector)
            } else {
                null
            }


    override fun getToolBarItemColor(): Int =
            FaceAppState.getContext().resources.getColor(R.color.toolbar_title_dark_color)

    override fun getBottomBarTitle(): String = FaceAppState.getContext().resources.getString(R.string.ethnicity_analy)

    override fun getToolBarBackCallback(): ToolBarCallback = object : ToolBarCallback {
        override fun invoke(): Boolean {
//            RateController.get().showRateIfNecessary(RateController.ENTRANCE_RESULT_BACK)
            return false
        }
    }

    override fun getToolBarSelfCallback(): ToolBarCallback? = null

    override fun getBottomBarIcon(): Array<Drawable>? = null

    override fun getFilePrefix(): String {
        return ReportNames.ETHNICITY_REPORT_PREFIX
    }

    override fun getGPColor(): Int {
        return Color.parseColor("#ffe972")
    }
}