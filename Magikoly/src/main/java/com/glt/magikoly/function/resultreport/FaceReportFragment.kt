package com.glt.magikoly.function.resultreport

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.glt.magikoly.BuyChannelApiProxy
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.ad.inner.InnerAdController
import com.glt.magikoly.ad.inner.InnerAdController.Companion.FUNCTION_PAGE_EXIT_AD_MODULE_ID
import com.glt.magikoly.ad.inner.InnerAdController.Companion.SWITCH_FUNCTION_AD_MODULE_ID
import com.glt.magikoly.constants.ErrorCode
import com.glt.magikoly.constants.ErrorInfoFactory
import com.glt.magikoly.dialog.DialogUtils
import com.glt.magikoly.event.*
import com.glt.magikoly.ext.postEvent
import com.glt.magikoly.ext.registerEventObserver
import com.glt.magikoly.ext.runMain
import com.glt.magikoly.ext.unregisterEventObserver
import com.glt.magikoly.function.FaceFunctionManager
import com.glt.magikoly.function.ethnicity.view.EthnicityResultFragment
import com.glt.magikoly.function.main.INewTabFragment
import com.glt.magikoly.function.main.ISubscribe
import com.glt.magikoly.function.main.ITabFragment
import com.glt.magikoly.function.resultreport.TabInfo.CREATOR.SUB_TAB_AGING
import com.glt.magikoly.function.resultreport.TabInfo.CREATOR.SUB_TAB_ANIMAL
import com.glt.magikoly.function.resultreport.TabInfo.CREATOR.SUB_TAB_ART_FILTER
import com.glt.magikoly.function.resultreport.TabInfo.CREATOR.SUB_TAB_BABY
import com.glt.magikoly.function.resultreport.TabInfo.CREATOR.SUB_TAB_CHILD
import com.glt.magikoly.function.resultreport.TabInfo.CREATOR.SUB_TAB_ETHNICITY
import com.glt.magikoly.function.resultreport.TabInfo.CREATOR.SUB_TAB_FILTER
import com.glt.magikoly.function.resultreport.TabInfo.CREATOR.SUB_TAB_GENDER
import com.glt.magikoly.function.resultreport.TabInfo.CREATOR.SUB_TAB_TUNING
import com.glt.magikoly.function.resultreport.TabInfo.CREATOR.TAB_ANALYSIS
import com.glt.magikoly.function.resultreport.TabInfo.CREATOR.TAB_BEAUTY
import com.glt.magikoly.function.resultreport.TabInfo.CREATOR.TAB_EFFECTS
import com.glt.magikoly.function.resultreport.aging.AgingShutterReportFragment
import com.glt.magikoly.function.resultreport.animal.AnimalFragment
import com.glt.magikoly.function.resultreport.artfilter.ArtFilterFragment
import com.glt.magikoly.function.resultreport.baby.BabyReportFragment
import com.glt.magikoly.function.resultreport.child.ChildMirrorReportFragment
import com.glt.magikoly.function.resultreport.filter.FilterFragment
import com.glt.magikoly.function.resultreport.gender.GenderReportFragment
import com.glt.magikoly.function.resultreport.tuning.TuningFragment
import com.glt.magikoly.function.views.BottomBarWithIndicator
import com.glt.magikoly.function.views.SecondaryMenu
import com.glt.magikoly.function.views.SecondaryMenuAdapter
import com.glt.magikoly.mvp.BaseSupportFragment
import com.glt.magikoly.pref.PrefConst
import com.glt.magikoly.pref.PrivatePreference
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.IStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.subscribe.SubscribeController
import com.glt.magikoly.view.GlobalProgressBar
import com.glt.magikoly.view.ProgressBarEvent
import com.glt.magikoly.view.ProgressBarEvent.Companion.EVENT_CANCEL_BY_USER
import kotlinx.android.synthetic.main.face_report_error_layout.*
import kotlinx.android.synthetic.main.face_report_fragment_layout.*
import kotlinx.android.synthetic.main.include_face_common_toolbar.*
import kotlinx.android.synthetic.main.layout_subscribe_entrance.*
import magikoly.magiccamera.R
import me.yokeyword.fragmentation.SupportFragment
import me.yokeyword.fragmentation.anim.FragmentAnimator
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class FaceReportFragment : BaseSupportFragment<FaceReportPresenter>(), IReport,
        BottomBarWithIndicator.OnTabListener,
        View.OnClickListener, SecondaryMenu.MenuItemClickListener {

    private val fragments = arrayOfNulls<ITabFragment>(9)
    private var mPreTabInfo: TabInfo? = null
    private lateinit var mCurTabInfo: TabInfo
    private var mPredictionRequestSuccessRecorder = HashSet<Int>()
    private var mFakeResultAdFlag = false

    companion object {
        private const val PRE_TAB_INFO_KEY = "pre_tab_info_key"
        private const val CUR_TAB_INFO_KEY = "cur_tab_info_key"
        private const val REQUEST_SUCCESS_RECORDER_KEY = "request_success_recorder_key"
        fun newInstance(): FaceReportFragment {
            return FaceReportFragment()
        }

        fun isDefaultEnter(currentEntrance: String): String {
            val campaign = BuyChannelApiProxy.getCampaign()
            var tag = ""
            if (campaign != null) {
                tag = when {
                    campaign.contains("old", true) -> {
                        "old"
                    }
                    campaign.contains("child", true) -> {
                        "child"
                    }
                    campaign.contains("baby", true) -> {
                        "baby"
                    }
                    campaign.contains("gen", true) -> {
                        "gen"
                    }
                    campaign.contains("cart", true) -> {
                        "cart"
                    }
                    campaign.contains("animal", true) -> {
                        "animal"
                    }
                    else -> {
                        "old"
                    }
                }
            } else {
                tag = "old"
            }
            return when (currentEntrance) {
                Statistic103Constant.ENTRANCE_MAIN -> {
                    "1"
                }
                Statistic103Constant.ENTRANCE_AGING -> {
                    if (tag == "old") {
                        "0"
                    } else {
                        "1"
                    }
                }
                Statistic103Constant.ENTRANCE_BABY -> {
                    if (tag == "baby") {
                        "0"
                    } else {
                        "1"
                    }
                }
                Statistic103Constant.ENTRANCE_GENDER -> {
                    if (tag == "gen") {
                        "0"
                    } else {
                        "1"
                    }
                }
                Statistic103Constant.ENTRANCE_CHILD -> {
                    if (tag == "child") {
                        "0"
                    } else {
                        "1"
                    }
                }
                Statistic103Constant.ENTRANCE_ETHNICITY -> {
                    "1"
                }
                Statistic103Constant.ENTRANCE_TURNING -> {
                    "1"
                }
                Statistic103Constant.ENTRANCE_ART_FILTER -> {
                    if (tag == "cart") {
                        "0"
                    } else {
                        "1"
                    }
                }
                Statistic103Constant.ENTRANCE_ANIMAL -> {
                    if (tag == "animal") {
                        "0"
                    } else {
                        "1"
                    }
                }
                else -> {
                    "1"
                }
            }
        }
    }

    init {
        fragmentAnimator = FragmentAnimator(0, 0, 0, 0)
    }

    override fun createPresenter(): FaceReportPresenter {
        return FaceReportPresenter()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(PRE_TAB_INFO_KEY, mPreTabInfo)
        outState.putParcelable(CUR_TAB_INFO_KEY, mCurTabInfo)
        outState.putSerializable(REQUEST_SUCCESS_RECORDER_KEY, mPredictionRequestSuccessRecorder)
    }

    override fun restoreInstanceState(outState: Bundle?) {
        outState?.getParcelable<TabInfo>(PRE_TAB_INFO_KEY)?.let {
            mPreTabInfo = it
        }
        outState?.getParcelable<TabInfo>(CUR_TAB_INFO_KEY)?.let {
            mCurTabInfo = it
        }
        outState?.getSerializable(REQUEST_SUCCESS_RECORDER_KEY)?.let {
            mPredictionRequestSuccessRecorder = it as HashSet<Int>
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.face_report_fragment_layout, null) as ViewGroup
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreInstanceState(savedInstanceState)
        loadMultiFragment(savedInstanceState)
        bl_report_fragment.stopBlur()

        secondary_menu.setMenuItemClickListener(this)
        result_error_btn.setOnClickListener(this)
        error_layout_img_back.setOnClickListener(this)
        LayoutPurchaseController.getInstance()
                .init(cl_subscribe_entrance, LayoutPurchaseController.Bean(), object : LayoutPurchaseController.OnClickAdapter() {
                    override fun onCloseClick() {
                        super.onCloseClick()
                        showAdAndExit(false)
                    }

                    override fun onAdFailed() {
                        super.onAdFailed()
                        var iTabFragment = fragments[mCurTabInfo.curSubTabId] as ITabFragment
                        if (iTabFragment is ISubscribe) {
                            iTabFragment.watchAdFinish = true
                        }
                        iTabFragment.reload()
                    }

                    override fun onAdClick() {
                        super.onAdClick()
                        BaseSeq103OperationStatistic.uploadSqe103StatisticData(Statistic103Constant.OBJ_RESULT,
                                Statistic103Constant.PURCHASE_VIDEO_AD_CLICK,
                                (fragments[mCurTabInfo.curSubTabId] as IStatistic).entrance,
                                (fragments[mCurTabInfo.curSubTabId] as IStatistic).tabCategory, isDefaultEnter((fragments[mCurTabInfo.curSubTabId] as IStatistic).entrance), BuyChannelApiProxy.getCampaign())
                        var iTabFragment = fragments[mCurTabInfo.curSubTabId]
                        if (iTabFragment is ISubscribe) {
                            iTabFragment.watchAdFinish = true
                        }
                        (iTabFragment as ITabFragment).reload()
                    }

                    override fun onWatchVideoStart() {
                        super.onWatchVideoStart()
                        BaseSeq103OperationStatistic.uploadSqe103StatisticData(Statistic103Constant.OBJ_RESULT,
                                Statistic103Constant.PURCHASE_VIDEO_SHOW,
                                (fragments[mCurTabInfo.curSubTabId] as IStatistic).entrance,
                                (fragments[mCurTabInfo.curSubTabId] as IStatistic).tabCategory, isDefaultEnter((fragments[mCurTabInfo.curSubTabId] as IStatistic).entrance), BuyChannelApiProxy.getCampaign())
                    }

                    override fun onWatchVideoClick() {
                        super.onWatchVideoClick()
                        BaseSeq103OperationStatistic.uploadSqe103StatisticData("2",
                                Statistic103Constant.PURCHASE_VIDEO_CLICK,
                                (fragments[mCurTabInfo.curSubTabId] as IStatistic).entrance,
                                (fragments[mCurTabInfo.curSubTabId] as IStatistic).tabCategory)

                        BaseSeq103OperationStatistic.uploadSqe103StatisticData("2",
                                Statistic103Constant.GUIDEBOTTOM_CLICK,
                                (fragments[mCurTabInfo.curSubTabId] as IStatistic).entrance, "0", isDefaultEnter((fragments[mCurTabInfo.curSubTabId] as IStatistic).entrance), BuyChannelApiProxy.getCampaign())
                    }

                    override fun onWatchVideoFinish() {
                        super.onWatchVideoFinish()
                        BaseSeq103OperationStatistic.uploadSqe103StatisticData(Statistic103Constant.OBJ_RESULT,
                                Statistic103Constant.PURCHASE_VIDEO_END,
                                (fragments[mCurTabInfo.curSubTabId] as IStatistic).entrance,
                                (fragments[mCurTabInfo.curSubTabId] as IStatistic).tabCategory, isDefaultEnter((fragments[mCurTabInfo.curSubTabId] as IStatistic).entrance), BuyChannelApiProxy.getCampaign())
                        var iTabFragment = fragments[mCurTabInfo.curSubTabId] as ITabFragment
                        if (iTabFragment is ISubscribe) {
                            iTabFragment.watchAdFinish = true
                        }
                        iTabFragment.reload()
                    }

                })
        if (FaceFunctionManager.demoFaceImageInfo != null) {
//            fragments.forEach {
//                it?.apply {
//                    bottom_bar.hideLock(getTabId(), false)
//                }
//            }
            runMain(1000) {
                GlobalProgressBar.hide()
            }
        }
    }

    override fun refreshToolBar(showFragment: ITabFragment) {
        //toolBar.setTitle(showFragment.getToolBarTitle())
        face_common_toolbar?.apply {
            face_common_toolbar.setBackDrawable(showFragment.getToolBarBackDrawable())
            face_common_toolbar.setMenuDrawable(showFragment.getToolBarMenuDrawable())
            face_common_toolbar.setOnTitleClickListener { _, back ->
                if (back) {
                    val callback = showFragment.getToolBarBackCallback()
                    if (callback == null || !callback.invoke()) {
                        confirmExitOrNot(false, showFragment.getTabId())
                    }
//                    if (showFragment.getToolBarBackCallback()?.invoke() != true) {
//                        confirmExitOrNot()
//                    }
                } else {
                    showFragment.getToolBarMenuCallback()?.invoke()
                }
            }
            face_common_toolbar.setItemColorFilter(showFragment.getToolBarItemColor()
                    ?: Color.WHITE)
        }
    }

    private fun refreshSecondaryMenu(showFragment: ITabFragment) {
        val menuInfos = ArrayList<SecondaryMenuAdapter.MenuInfo>()
        secondary_menu?.apply {
            secondary_menu.visibility = View.VISIBLE
            when (mCurTabInfo.tabId) {
                TAB_EFFECTS -> {
                    menuInfos.add(SecondaryMenuAdapter.MenuInfo(SUB_TAB_AGING, R.string.aging_camera,
                            R.drawable.secondary_menu_item_aging_normal,
                            R.drawable.secondary_menu_item_aging_pressed,
                            fragments[SUB_TAB_AGING]!!.getTabLock(),
                            fragments[SUB_TAB_AGING] == showFragment))
                    menuInfos.add(SecondaryMenuAdapter.MenuInfo(SUB_TAB_CHILD, R.string.child_mirror,
                            R.drawable.secondary_menu_item_child_normal,
                            R.drawable.secondary_menu_item_child_pressed,
                            fragments[SUB_TAB_CHILD]!!.getTabLock(),
                            fragments[SUB_TAB_CHILD] == showFragment))
                    menuInfos.add(SecondaryMenuAdapter.MenuInfo(SUB_TAB_BABY, R.string.baby_prediction,
                            R.drawable.secondary_menu_item_baby_normal,
                            R.drawable.secondary_menu_item_baby_pressed,
                            fragments[SUB_TAB_BABY]!!.getTabLock(),
                            fragments[SUB_TAB_BABY] == showFragment))
                    menuInfos.add(SecondaryMenuAdapter.MenuInfo(SUB_TAB_GENDER, R.string.gender_swap,
                            R.drawable.secondary_menu_item_gender_normal,
                            R.drawable.secondary_menu_item_gender_pressed,
                            fragments[SUB_TAB_GENDER]!!.getTabLock(),
                            fragments[SUB_TAB_GENDER] == showFragment))
                }
                TAB_BEAUTY -> {
                    if (fragments[SUB_TAB_ART_FILTER] == null) {
                        fragments[SUB_TAB_ART_FILTER] = ArtFilterFragment.newInstance()
                    }
                    menuInfos.add(SecondaryMenuAdapter.MenuInfo(SUB_TAB_ART_FILTER, R.string.art_filter,
                            R.drawable.secondary_menu_item_art_filter_normal,
                            R.drawable.secondary_menu_item_art_filter_pressed,
                            fragments[SUB_TAB_ART_FILTER]!!.getTabLock(),
                            fragments[SUB_TAB_ART_FILTER] == showFragment))
                    if (fragments[SUB_TAB_TUNING] == null) {
                        fragments[SUB_TAB_TUNING] = TuningFragment.newInstance()
                    }
                    menuInfos.add(SecondaryMenuAdapter.MenuInfo(SUB_TAB_TUNING, R.string.beauty,
                            R.drawable.secondary_menu_item_beauty_normal,
                            R.drawable.secondary_menu_item_beauty_pressed,
                            fragments[SUB_TAB_TUNING]!!.getTabLock(),
                            fragments[SUB_TAB_TUNING] == showFragment))
                }
                TAB_ANALYSIS -> {
                    menuInfos.add(SecondaryMenuAdapter.MenuInfo(SUB_TAB_ANIMAL, R.string.animal,
                            R.drawable.secondary_menu_item_animal_normal,
                            R.drawable.secondary_menu_item_animal_pressed,
                            fragments[SUB_TAB_ANIMAL]!!.getTabLock(),
                            fragments[SUB_TAB_ANIMAL] == showFragment))
                    menuInfos.add(SecondaryMenuAdapter.MenuInfo(SUB_TAB_ETHNICITY, R.string.ethnicity_analy,
                            R.drawable.secondary_menu_item_ethnicity_normal,
                            R.drawable.secondary_menu_item_ethnicity_pressed,
                            fragments[SUB_TAB_ETHNICITY]!!.getTabLock(),
                            fragments[SUB_TAB_ETHNICITY] == showFragment))
                }
            }
            secondary_menu.setMenuInfos(menuInfos)
        }
    }

    override fun onMenuItemClick(tabId: Int) {
        mCurTabInfo.preSubTabId = mCurTabInfo.curSubTabId
        mCurTabInfo.curSubTabId = tabId

        if (isNewFragment()) {
            handleNewSubFragment(mCurTabInfo.curSubTabId, true)
        } else {
            handleTabSelected(mCurTabInfo.curSubTabId, true)
        }
    }

    private fun handleNewSubFragment(tabId: Int, fromUser: Boolean) {
        val iTabFragment = fragments[tabId]
        if (iTabFragment != null) {
            if (iTabFragment is SupportFragment) {
                if ((iTabFragment as SupportFragment).isAdded) {
                    (iTabFragment as SupportFragment).pop()
                }
            }

            start(iTabFragment)

            BaseSeq103OperationStatistic.uploadSqe103StatisticData(Statistic103Constant.SUBPAGE_ENTER,
                    (iTabFragment as IStatistic).entrance)
            if (fromUser) {
                BaseSeq103OperationStatistic.uploadSqe103StatisticData(Statistic103Constant.FUNCTION_CLICK,
                        (iTabFragment as IStatistic).entrance)
                if (FaceFunctionManager.demoFaceImageInfo == null) {
                    InnerAdController.instance.getPendingAdBean(SWITCH_FUNCTION_AD_MODULE_ID)
                            ?.let { adBean ->
                                if (adBean.isInterstitialAd) {
                                    adBean.showInterstitialAd()
                                }
                                runMain(1000) {
                                    _mActivity?.let { activity ->
                                        InnerAdController.instance.loadAd(activity,
                                                SWITCH_FUNCTION_AD_MODULE_ID)
                                    }
                                }
                            }
                }
            } else {
                checkAndShowFaceResultAd()
            }
        } else {
            when (tabId) {
                SUB_TAB_TUNING -> {
                    fragments[tabId] = TuningFragment.newInstance()
                    handleNewSubFragment(tabId, fromUser)
                }
                SUB_TAB_ART_FILTER -> {
                    fragments[tabId] = ArtFilterFragment.newInstance()
                    handleNewSubFragment(tabId, fromUser)
                }
                else -> {
                }
            }
        }
    }

    private fun isNewFragment(): Boolean {
        if (mCurTabInfo.curSubTabId == SUB_TAB_TUNING || mCurTabInfo.curSubTabId == SUB_TAB_ART_FILTER) {
            return true
        }
        return false
    }

    override fun onTabSelected(indicatorBean: BottomBarWithIndicator.IndicatorBean, v: View?, fromUser: Boolean) {
        mPreTabInfo = mCurTabInfo
        mCurTabInfo = indicatorBean.tabInfo
        mCurTabInfo.preSubTabId = -1
        if (mCurTabInfo.tabId != 1) {
            EventBus.getDefault().post(BeautyNoSecectEvent())
        }
        handleTabSelected(mCurTabInfo.curSubTabId, fromUser)
    }

    private fun handleTabSelected(tabId: Int, fromUser: Boolean) {
        fragments[tabId]?.let {
            showHideFragment(it)
            refreshToolBar(it)
            refreshSecondaryMenu(it)
            checkAndShowAd(tabId, fromUser)
            it.onTabFragmentVisible()

            BaseSeq103OperationStatistic.uploadSqe103StatisticData(Statistic103Constant.SUBPAGE_ENTER,
                    (it as IStatistic).entrance)
            if (fromUser) {
                BaseSeq103OperationStatistic.uploadSqe103StatisticData(Statistic103Constant.FUNCTION_CLICK,
                        (it as IStatistic).entrance)
            }
        }
    }

    private fun checkAndShowAd(curTabId: Int, fromUser: Boolean) {
        if (FaceFunctionManager.demoFaceImageInfo == null) {
            InnerAdController.instance.loadAd(_mActivity,
                    InnerAdController.LOADING_PAGE_BOTTOM_AD_MODULE_ID,
                    object : InnerAdController.AdLoadListenerAdapter() {
                        override fun onAdLoadSuccess(adBean: InnerAdController.AdBean?) {
                            adBean?.let { bean ->
                                postEvent(AdLoadEvent(bean))
                            }
                        }
                    })
            if (!fromUser) {
                checkAndShowFaceResultAd()
            } else if (curTabId != SUB_TAB_FILTER) {
                InnerAdController.instance.getPendingAdBean(SWITCH_FUNCTION_AD_MODULE_ID)
                        ?.let { adBean ->
                            if (adBean.isInterstitialAd) {
                                adBean.showInterstitialAd()
                            }
                            runMain(1000) {
                                _mActivity?.let { activity ->
                                    InnerAdController.instance.loadAd(activity,
                                            SWITCH_FUNCTION_AD_MODULE_ID)
                                }
                            }
                        }
            }
        }
    }

    private fun loadMultiFragment(savedInstanceState: Bundle?) {
        bottom_bar.tabListener = this
        if (savedInstanceState != null) {
            fragments[SUB_TAB_AGING] = findChildFragment(AgingShutterReportFragment::class.java)
            fragments[SUB_TAB_CHILD] = findChildFragment(ChildMirrorReportFragment::class.java)
            fragments[SUB_TAB_BABY] = findChildFragment(BabyReportFragment::class.java)
            fragments[SUB_TAB_GENDER] = findChildFragment(GenderReportFragment::class.java)
            fragments[SUB_TAB_FILTER] = findChildFragment(FilterFragment::class.java)
            fragments[SUB_TAB_ETHNICITY] = findChildFragment(EthnicityResultFragment::class.java)
            fragments[SUB_TAB_ART_FILTER] = findChildFragment(ArtFilterFragment::class.java)
            fragments[SUB_TAB_TUNING] = findChildFragment(TuningFragment::class.java)
            fragments[SUB_TAB_ANIMAL] = findChildFragment(AnimalFragment::class.java)
        } else {
            fragments[SUB_TAB_AGING] = AgingShutterReportFragment.newInstance()
            fragments[SUB_TAB_CHILD] = ChildMirrorReportFragment.newInstance()
            fragments[SUB_TAB_BABY] = BabyReportFragment.newInstance()
            fragments[SUB_TAB_GENDER] = GenderReportFragment.newInstance()
            fragments[SUB_TAB_FILTER] = FilterFragment.newInstance()
            fragments[SUB_TAB_ETHNICITY] = EthnicityResultFragment.newInstance()
            fragments[SUB_TAB_ART_FILTER] = ArtFilterFragment.newInstance()
            fragments[SUB_TAB_TUNING] = TuningFragment.newInstance()
            fragments[SUB_TAB_ANIMAL] = AnimalFragment.newInstance()
            val firstTabId = getFirstTabId()
            loadMultipleRootFragment(R.id.fl_container, firstTabId, fragments[SUB_TAB_AGING],
                    fragments[SUB_TAB_CHILD], fragments[SUB_TAB_BABY], fragments[SUB_TAB_GENDER],
                    fragments[SUB_TAB_FILTER], fragments[SUB_TAB_ANIMAL], fragments[SUB_TAB_ETHNICITY])

            if (firstTabId == SUB_TAB_FILTER) {
                onMenuItemClick(SUB_TAB_ART_FILTER)
            }
        }
        val indicatorBeans = ArrayList<BottomBarWithIndicator.IndicatorBean>()
        if (mCurTabInfo.tabId == TAB_EFFECTS) {
            indicatorBeans.add(
                    BottomBarWithIndicator.IndicatorBean(resources.getString(R.string.effects),
                            null, mCurTabInfo))
        } else {
            indicatorBeans.add(
                    BottomBarWithIndicator.IndicatorBean(resources.getString(R.string.effects),
                            null, TabInfo(TAB_EFFECTS, SUB_TAB_AGING)))
        }
        if (mCurTabInfo.tabId == TAB_BEAUTY) {
            indicatorBeans.add(
                    BottomBarWithIndicator.IndicatorBean(resources.getString(R.string.filter), null,
                            mCurTabInfo))
        } else {
            indicatorBeans.add(
                    BottomBarWithIndicator.IndicatorBean(resources.getString(R.string.filter), null,
                            TabInfo(TAB_BEAUTY, SUB_TAB_FILTER)))
        }
        if (mCurTabInfo.tabId == TAB_ANALYSIS) {
            indicatorBeans.add(
                    BottomBarWithIndicator.IndicatorBean(resources.getString(R.string.analysis),
                            null, mCurTabInfo))
        } else {
            indicatorBeans.add(
                    BottomBarWithIndicator.IndicatorBean(resources.getString(R.string.analysis),
                            null, TabInfo(TAB_ANALYSIS, SUB_TAB_ANIMAL)))
        }
        bottom_bar.addAllIndicatorBean(indicatorBeans)
        bottom_bar.showTab(mCurTabInfo.tabId)
        post {
            val preference = PrivatePreference.getPreference(context)
            if (!preference.getBoolean(PrefConst.KEY_WHETHER_ENTER_FACE_REPORT, false)) {
                preference.putBoolean(PrefConst.KEY_WHETHER_ENTER_FACE_REPORT, true)
                preference.commit()
            }
        }
    }

    private fun getFirstTabId(): Int {
        val campaign = BuyChannelApiProxy.getCampaign()
        if (campaign != null) {
            return when {
                campaign.contains("child", true) -> {
                    mCurTabInfo = TabInfo(TAB_EFFECTS, SUB_TAB_CHILD)
                    SUB_TAB_CHILD
                }
                campaign.contains("baby", true) -> {
                    mCurTabInfo = TabInfo(TAB_EFFECTS, SUB_TAB_BABY)
                    SUB_TAB_BABY
                }
                campaign.contains("gen", true) -> {
                    mCurTabInfo = TabInfo(TAB_EFFECTS, SUB_TAB_GENDER)
                    SUB_TAB_GENDER
                }
                campaign.contains("cart", true) -> {
                    mCurTabInfo = TabInfo(TAB_BEAUTY, SUB_TAB_FILTER)
                    SUB_TAB_FILTER
                }
                campaign.contains("animal", true) -> {
                    mCurTabInfo = TabInfo(TAB_ANALYSIS, SUB_TAB_ANIMAL)
                    SUB_TAB_ANIMAL
                }
                else -> {
                    mCurTabInfo = TabInfo(TAB_EFFECTS, SUB_TAB_AGING)
                    SUB_TAB_AGING
                }
            }
        } else {
            mCurTabInfo = TabInfo(TAB_EFFECTS, SUB_TAB_AGING)
            return SUB_TAB_AGING
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.result_error_btn -> {
                bl_report_fragment.stopBlur()
                error_layout.visibility = View.GONE
                var iTabFragment = fragments[mCurTabInfo.curSubTabId] as ITabFragment
                iTabFragment.fromClick = true
                iTabFragment.reload()
                iTabFragment.fromClick = false
            }
            R.id.error_layout_img_back -> {
                doBackPressedSupport()
            }
        }
    }

    /**
     * 模糊层是否开启
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReportBlurEvent(event: ReportBlurEvent) {
        if (event.tab != mCurTabInfo.curSubTabId) {
            return
        }
        val isTrailCount = when (event.tab) {
            SUB_TAB_ETHNICITY -> {
                true
            }
            else -> {
                false
            }
        }
        if (secondary_menu.visibility == View.VISIBLE) {
            cl_subscribe_entrance.setPadding(0, 0, 0, secondary_menu.height)
        }
        if (event.isShow) {
            bl_report_fragment.startBlur()
            LayoutPurchaseController.getInstance().show(cl_subscribe_entrance, isTrailCount)
        } else {
            bl_report_fragment.stopBlur()
            LayoutPurchaseController.getInstance().hide(cl_subscribe_entrance)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onErrorLayoutEvent(event: ReportErrorEvent) {
        val fragment = fragments[mCurTabInfo.curSubTabId]
        when (fragment?.getStatus()) {
            ITabFragment.STATUS_OPEN -> {
                bl_report_fragment.stopBlur()
                error_layout.visibility = View.GONE
                LayoutPurchaseController.getInstance().hide(cl_subscribe_entrance)
                refreshSecondaryMenu(fragment)
            }
            ITabFragment.STATUS_ERROR -> {
                bl_report_fragment.startBlur()
                setStateViewResource(event.errorCode)
                error_layout.visibility = View.VISIBLE
                LayoutPurchaseController.getInstance().hide(cl_subscribe_entrance)
            }
            ITabFragment.STATUS_PURCHASE -> {
                bl_report_fragment.startBlur()
                error_layout.visibility = View.GONE

                val isTrialCount = when (mCurTabInfo.curSubTabId) {
                    SUB_TAB_ETHNICITY -> true
                    else -> false
                }
                secondary_menu?.let { menu ->
                    menu.post {
                        cl_subscribe_entrance?.let { entrance ->
                            if (menu.visibility == View.VISIBLE) {
                                entrance.setPadding(0, 0, 0, menu.height)
                            }
                            LayoutPurchaseController.getInstance().show(entrance, isTrialCount)
                        }
                    }
                }
            }
            else -> {
                bl_report_fragment.startBlur()
                setStateViewResource(event.errorCode)
                error_layout.visibility = View.VISIBLE
                LayoutPurchaseController.getInstance().hide(cl_subscribe_entrance)
            }
        }
    }

    private fun checkAndShowFaceResultAd() {
        if (!mFakeResultAdFlag) {
            mFakeResultAdFlag = true
            if (FaceFunctionManager.demoFaceImageInfo == null) {
            }
        }
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        registerEventObserver(this)
    }

    override fun onDetach() {
        super.onDetach()
        unregisterEventObserver(this)
        FaceFunctionManager.faceBeanMap.clear()
        FaceFunctionManager.currentFaceImagePath = null
        FaceFunctionManager.demoFaceImageInfo = null

    }

    override fun onDestroyView() {
        bottom_bar.destroy()
        super.onDestroyView()
    }

    override fun doBackPressedSupport(): Boolean {
        confirmExitOrNot(false, fragments[mCurTabInfo.curSubTabId]?.getTabId() ?: -1)
        return true
    }

    fun confirmExitOrNot(isNewFragment: Boolean = false, tabId: Int) {
        if (isNewFragment) {
            if (mPredictionRequestSuccessRecorder.contains(tabId)) {
                DialogUtils.showTipsDialog(_mActivity, R.string.confirm_exit, R.string.stay_here, R.string.yes_leaving,
                        Runnable {},
                        Runnable {
                            showAdAndExit(isNewFragment)
                        })
            } else {
                showAdAndExit(isNewFragment)
            }
        } else {
            if (mPredictionRequestSuccessRecorder.isNotEmpty()) {
                if (!mPredictionRequestSuccessRecorder.contains(mCurTabInfo.curSubTabId)) {
                    val subTabId = mPredictionRequestSuccessRecorder.last()
                    when (subTabId) {
                        SUB_TAB_AGING,
                        SUB_TAB_CHILD,
                        SUB_TAB_BABY,
                        SUB_TAB_GENDER -> {
                            if (mCurTabInfo.tabId != TAB_EFFECTS) {
                                bottom_bar.showTab(TAB_EFFECTS, false, false)
                            }

                        }
                        SUB_TAB_ANIMAL,
                        SUB_TAB_ETHNICITY -> {
                            if (mCurTabInfo.tabId != TAB_ANALYSIS) {
                                bottom_bar.showTab(TAB_ANALYSIS, false, false)
                            }
                        }
                    }
                    onMenuItemClick(subTabId)

                }
                DialogUtils.showTipsDialog(_mActivity, R.string.confirm_exit, R.string.stay_here, R.string.yes_leaving,
                        Runnable {},
                        Runnable {
                            showAdAndExit(isNewFragment)
                        })
            } else {
                showAdAndExit(isNewFragment)
            }
        }
    }

    private fun showAdAndExit(isNewFragment: Boolean) {
        if (SubscribeController.getInstance().isVIP()) {
            doExit(isNewFragment)
        } else {
            var ret = false
            if (!isNewFragment) {
                val adBean = InnerAdController.instance.getPendingAdBean(
                        FUNCTION_PAGE_EXIT_AD_MODULE_ID)
                if (adBean != null && adBean.isInterstitialAd) {
                    ret = adBean.showInterstitialAd(object :
                            InnerAdController.AdLoadListenerAdapter() {
                        override fun onAdClosed() {
                            doExit(isNewFragment)
                        }
                    })
                }
            }
            if (!ret) {
                doExit(isNewFragment)
            }
        }
    }

    private fun doExit(isNewFragment: Boolean) {
        if (isNewFragment) {
            mPredictionRequestSuccessRecorder.remove(mCurTabInfo.curSubTabId)
            if (fragments[mCurTabInfo.curSubTabId] is INewTabFragment) {
                (fragments[mCurTabInfo.curSubTabId] as INewTabFragment).onExit()
            }
        } else {
            pop()
        }
    }

    private fun setStateViewResource(errorCode: Int?) {
        val errorInfo = ErrorInfoFactory.getErrorInfo(errorCode)
        result_error_img.setImageResource(errorInfo.imageId)
        result_error_title.setText(errorInfo.titleId)
        result_error_desc.setText(errorInfo.descId)
        if (errorCode == ErrorCode.FATHER_GENDER_FAIL
                || errorCode == ErrorCode.MOTHER_GENDER_FAIL) {
            result_error_btn.setText(R.string.ok)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProgressBarEvent(event: ProgressBarEvent) {
        if (event.action == EVENT_CANCEL_BY_USER) {
            if (mCurTabInfo.preSubTabId > -1) {
                secondary_menu.switchMenu(mCurTabInfo.preSubTabId)
            } else if (mPreTabInfo != null && mPreTabInfo?.tabId != mCurTabInfo.tabId) {
                bottom_bar.showTab(mPreTabInfo!!.tabId)
            } else {
                pop()
            }
        }
    }

    /**
     * 保存事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPredictionResultSave(event: PredictionResultSaveEvent) {
        if (event.success) {
            if (event.showToast!!) {
                Toast.makeText(FaceAppState.getContext(), R.string.save_report_success,
                        Toast.LENGTH_LONG).show()
            }
            mPredictionRequestSuccessRecorder.remove(event.tabId)
        } else {
            if (event.showToast!!) {
                Toast.makeText(FaceAppState.getContext(), R.string.save_report_failed,
                        Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * 重置结果
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPredictionRequestInit(event: PredictionRequestInit) {
        mPredictionRequestSuccessRecorder.remove(event.tabId)
    }

    /**
     * 生成结果成功
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPredictionRequestSuccess(event: PredictionRequestSuccess) {
        mPredictionRequestSuccessRecorder.add(event.tabId)
    }

    fun getCurrentTabFragment(): ITabFragment? {
        val iTabFragment = fragments[mCurTabInfo.curSubTabId]
        return if (iTabFragment is ITabFragment) {
            iTabFragment
        } else {
            null
        }
    }

    override fun onFragmentResult(requestCode: Int, resultCode: Int, data: Bundle?) {
        super.onFragmentResult(requestCode, resultCode, data)
//        if (topFragment is BabyReportFragment) {
        (fragments[SUB_TAB_BABY] as BabyReportFragment).onFragmentResult(requestCode, resultCode, data)
//        }
    }
}