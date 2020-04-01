package com.glt.magikoly.function.resultreport.filter

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.glt.magikoly.BuyChannelApiProxy
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.SafelyBitmap
import com.glt.magikoly.config.AgingShutterConfigBean
import com.glt.magikoly.event.PredictionRequestInit
import com.glt.magikoly.event.ReportErrorEvent
import com.glt.magikoly.ext.postEvent
import com.glt.magikoly.function.FaceFunctionManager
import com.glt.magikoly.function.main.ISubscribe
import com.glt.magikoly.function.main.ITabFragment
import com.glt.magikoly.function.main.ToolBarCallback
import com.glt.magikoly.function.resultreport.FaceReportFragment
import com.glt.magikoly.function.resultreport.TabInfo.CREATOR.SUB_TAB_FILTER
import com.glt.magikoly.mvp.BaseSupportFragment
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.IStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import magikoly.magiccamera.R
import kotlinx.android.synthetic.main.fragment_filter.*

/**
 *
 * @author rayhahah
 * @blog http://rayhahah.com
 * @time 2019/2/18
 * @tips 这个类是Object的子类
 * @fuction
 */

class FilterFragment : BaseSupportFragment<FilterPresenter>(), IFilterView, IStatistic,
        ITabFragment, ISubscribe {

    override var watchAdFinish: Boolean = false
    override var fromClick = false

    override fun getTabLock(): Boolean = false

    private var mCurrentStatus: Int = -1

    override fun getStatus(): Int {
        return mCurrentStatus
    }

    override fun setStatus(status: Int) {
        mCurrentStatus = status
    }

    companion object {
        private const val RESULT_KEY = "result_key"
        fun newInstance(): FilterFragment {
            return FilterFragment()
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

    override fun getTabId(): Int = SUB_TAB_FILTER

    override fun getToolBarTitle(): String = ""

    override fun getToolBarBackDrawable(): Drawable? {
        return FaceAppState.getContext().resources.getDrawable(R.drawable.icon_back_selector)
    }

    override fun getToolBarMenuDrawable(): Drawable? {
        return null
    }

    override fun getToolBarBackCallback(): ToolBarCallback? = null

    override fun getToolBarMenuCallback(): ToolBarCallback? {
        return null
    }

    override fun getToolBarSelfCallback(): ToolBarCallback? = null

    override fun getToolBarItemColor(): Int? {
        return FaceAppState.getContext().resources.getColor(R.color.toolbar_title_dark_color)
    }

    override fun getBottomBarTitle(): String {
        return "Filter"
    }

    override fun getBottomBarIcon(): Array<Drawable>? = null

    override fun getEntrance(): String = Statistic103Constant.ENTRANCE_FILTER

    override fun getTabCategory(): String = AgingShutterConfigBean.getRequestType()

    override fun createPresenter(): FilterPresenter = FilterPresenter()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_filter, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreInstanceState(savedInstanceState)
        postEvent(PredictionRequestInit(getTabId()))
//        postEvent(PredictionResultSaveEvent(SUB_TAB_FILTER, true))


        val bitmap = if (FaceFunctionManager.demoFaceImageInfo == null) {
            FaceFunctionManager.faceBeanMap[FaceFunctionManager.currentFaceImagePath]?.photo?.getBitmap()
        } else {
            (resources.getDrawable(FaceFunctionManager.demoFaceImageInfo!!.imgId) as BitmapDrawable).bitmap
        }
        iv_filter_original.setImageBitmap(bitmap)
    }

    override fun onTabFragmentVisible() {
        stopBlurReport()
    }

    override fun onTabFragmentInvisible() {
    }

    override fun onDestroyView() {
        isExit = true
        clearImageTargets()
        super.onDestroyView()
    }

    override fun reload() {
    }


    private fun clearImageTargets() {
        Glide.with(this).pauseAllRequests()
        imageTargets.forEach {
            Glide.with(this).clear(it)
        }
        imageTargets.clear()
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
}