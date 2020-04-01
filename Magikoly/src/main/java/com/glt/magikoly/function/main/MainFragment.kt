package com.glt.magikoly.function.main

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.ad.inner.InnerAdController
import com.glt.magikoly.event.AdLoadEvent
import com.glt.magikoly.event.ChangeTabEvent
import com.glt.magikoly.event.ImageDetectEvent
import com.glt.magikoly.event.UserStatusEvent
import com.glt.magikoly.ext.postEvent
import com.glt.magikoly.function.FaceFunctionManager
import com.glt.magikoly.function.feedback.FeedbackFragment
import com.glt.magikoly.function.main.album.AlbumFragment
import com.glt.magikoly.function.main.discovery.DiscoveryFragment
import com.glt.magikoly.function.main.faceimages.FaceImagesFragment
import com.glt.magikoly.function.resultreport.FaceReportFragment
import com.glt.magikoly.pref.PrefConst
import com.glt.magikoly.pref.PrivatePreference
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.Statistic103Constant.*
import com.glt.magikoly.subscribe.SubscribeController
import com.glt.magikoly.subscribe.SubscribeProxy
import com.glt.magikoly.subscribe.SubscribeScene
import com.glt.magikoly.subscribe.billing.BillingOrder
import com.glt.magikoly.utils.AppUtils
import com.glt.magikoly.utils.Logcat
import com.glt.magikoly.view.DiscoveryGuideLayer
import kotlinx.android.synthetic.main.include_face_common_toolbar.*
import kotlinx.android.synthetic.main.main_layout.*
import kotlinx.android.synthetic.main.setting_main_layout.*
import magikoly.magiccamera.R
import me.yokeyword.fragmentation.SupportFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainFragment : SupportFragment(), View.OnClickListener {

    companion object {
        private const val SERVICE_AGREEMENT = "http://magikoly.com/Magikoly_statement.html"
        const val PRIVACY_POLICY = "http://magikoly.com/Magikoly_privacy.html"
        private const val CUR_TAB_KEY = "cur_tab_key"
        private const val KEY_ENTER_AD_MODULE_ID = "key_enter_ad_module_id"
        const val TAB_FACE_IMAGES = 0
        const val TAB_ALBUM = 1
        const val TAB_DISCOVERY = 2

        fun newInstance(): MainFragment {
            val fragment = MainFragment()
            return fragment
        }
    }

    private val fragments = arrayOfNulls<ITabFragment>(3)

    private val drawerLayout: DrawerLayout by lazy {
        view?.findViewById(R.id.drawerLayout) as DrawerLayout
    }

    private var mCurrentTab: Int = TAB_FACE_IMAGES
    private var enterFrom = ALBUM_ENTER_MAIN


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CUR_TAB_KEY, mCurrentTab)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.item_feedback -> {
                FaceAppState.getMainActivity()?.start(FeedbackFragment.newInstance())
                drawerLayout.closeDrawer(Gravity.START)
//                AnimalDataTool.createAnimalData()
//                MobclickAgent.reportError(context, RuntimeException("just for test"))
//                MobclickAgent.onEvent(context, "str1")
            }

            R.id.item_privacy_policy -> {
                FaceAppState.getMainActivity()?.let {
                    AppUtils.openBrowser(it, PRIVACY_POLICY)
                    drawerLayout.closeDrawer(Gravity.START)
                }
//                val map = HashMap<String, String>()
//                map["key1"] = "value1"
//                MobclickAgent.onEvent(context, "str1", map)
            }

            R.id.item_service -> {
                FaceAppState.getMainActivity()?.let {
                    AppUtils.openBrowser(it, SERVICE_AGREEMENT)
                    drawerLayout.closeDrawer(Gravity.START)
                }
//                MobclickAgent.onEvent(context, "str1", "str2")
            }

            R.id.item_upgrade -> {
                launchSubscribe()
                drawerLayout.closeDrawer(Gravity.START)
            }

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_layout, null)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mCurrentTab = savedInstanceState?.getInt(CUR_TAB_KEY) ?: TAB_FACE_IMAGES
        initView(view)
        loadMultiFragment()

//        TakePhotoFragment.tryInitTakePhotoFragment("")

        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerStateChanged(newState: Int) {
                super.onDrawerStateChanged(newState)
                if (newState == DrawerLayout.STATE_SETTLING) {
                    item_upgrade?.let {
                        val visible = if (SubscribeController.getInstance().isVIP()) View.GONE else View.VISIBLE
                        if (it.visibility != visible) {
                            it.visibility = visible
                        }
                    }
                }
            }
        })
        BaseSeq103OperationStatistic.uploadSqe103StatisticData(MAIN_ENTER, "")


        val adBean = InnerAdController.instance.getPendingAdBean(InnerAdController.LAUNCHING_AD_MODULE_ID)
        if (adBean != null) {
            Logcat.d("MainFragment", InnerAdController.LAUNCHING_AD_MODULE_ID.toString() + " load")
        }

        InnerAdController.instance.needLoadLocalListBannerAd = true


    }

    private fun createMultiFragment(tab: Int): Boolean {
        var isNewCreate = false
        var fragment: ITabFragment? = null
        when (tab) {
            TAB_FACE_IMAGES -> {
                fragment = findChildFragment(FaceImagesFragment::class.java)
                if (fragment == null) {
                    fragment = FaceImagesFragment.newInstance(ENTRANCE_MAIN)
                    isNewCreate = true
                }
            }
            TAB_ALBUM -> {
                fragment = findChildFragment(AlbumFragment::class.java)
                if (fragment == null) {
                    fragment = AlbumFragment.newInstance(ENTRANCE_MAIN)
                    isNewCreate = true
                }
            }
            TAB_DISCOVERY -> {
                fragment = findChildFragment(DiscoveryFragment::class.java)
                if (fragment == null) {
                    fragment = DiscoveryFragment.newInstance(ENTRANCE_MAIN)
                    isNewCreate = true
                }
            }
        }
        fragments[tab] = fragment
        return isNewCreate
    }

    private fun loadMultiFragment() {
        var isNewCreate = createMultiFragment(TAB_FACE_IMAGES)
        createMultiFragment(TAB_ALBUM)
        createMultiFragment(TAB_DISCOVERY)
        if (isNewCreate) {
            loadMultipleRootFragment(R.id.content_container, TAB_FACE_IMAGES,
                    fragments[TAB_FACE_IMAGES],
                    fragments[TAB_ALBUM],
                    fragments[TAB_DISCOVERY])
        }
        fragments.forEach { fragment ->
            val iconArray = fragment?.getBottomBarIcon()
            iconArray?.let {
                bottom_bar.addItem(BottomBarTab(_mActivity, it[0], it[1],
                        fragment?.getBottomBarTitle()))
            }
        }
        bottom_bar.setCurrentItem(mCurrentTab)
    }


    override fun onSupportVisible() {
        super.onSupportVisible()
        InnerAdController.instance.loadAd(_mActivity,
                InnerAdController.LOADING_PAGE_BOTTOM_AD_MODULE_ID,
                object : InnerAdController.AdLoadListenerAdapter() {
                    override fun onAdLoadSuccess(adBean: InnerAdController.AdBean?) {
                        adBean?.let {
                            postEvent(AdLoadEvent(it))
                        }
                    }
                })
    }

    private fun initView(view: View) {
        face_common_toolbar.setTitleGravity(Gravity.LEFT)
        face_common_toolbar.setTitleColor(resources.getColor(R.color.toolbar_title_dark_color))
        bottom_bar.setOnTabSelectedListener(object : BottomBar.OnTabSelectedListener {
            override fun onTabSelected(position: Int, prePosition: Int) {
                showHideFragment(fragments[position], fragments[prePosition])

                refreshToolBar(fragments[position])
                if (position == TAB_FACE_IMAGES) {
                    img_top_bg.visibility = View.VISIBLE
                } else {
                    img_top_bg.visibility = View.INVISIBLE
                }

                if (position == TAB_DISCOVERY) {
                    BaseSeq103OperationStatistic.uploadSqe103StatisticData(DISCOVERYTAB_ENTER, enterFrom)
                    if (enterFrom != ALBUM_ENTER_MAIN) {
                        enterFrom = ALBUM_ENTER_MAIN
                    }
                    DiscoveryGuideLayer.getInstance().hide()
                } else if (position == TAB_ALBUM) {
                    val fragment = fragments[position] as AlbumFragment
                    fragment.enterFrom = enterFrom
                    BaseSeq103OperationStatistic.uploadSqe103StatisticData(ALBUM_ENTER, enterFrom)
                    if (enterFrom != ALBUM_ENTER_MAIN) {
                        enterFrom = ALBUM_ENTER_MAIN
                    }
                }

                mCurrentTab = position
            }

            override fun onTabUnselected(position: Int) {

            }

            override fun onTabReselected(position: Int) {
                refreshToolBar(fragments[mCurrentTab])
                img_top_bg?.let {
                    if (position == TAB_FACE_IMAGES) {
                        it.visibility = View.VISIBLE
                    } else {
                        it.visibility = View.INVISIBLE
                    }
                }
            }
        })

        if (SubscribeController.getInstance().isVIP()) {
            item_upgrade.visibility = View.GONE
        }
        item_feedback.setOnClickListener(this)
        item_upgrade.setOnClickListener(this)
        item_service.setOnClickListener(this)
        item_privacy_policy.setOnClickListener(this)
        drawer_left.setOnClickListener(this)
    }

    private fun refreshToolBar(fragment: ITabFragment?) {
        face_common_toolbar?.let {
            it.setTitle(fragment?.getToolBarTitle() ?: null)
            it.setBackDrawable(fragment?.getToolBarBackDrawable() ?: null)
            it.setMenuDrawable(fragment?.getToolBarMenuDrawable() ?: null)
            it.setOnTitleClickListener { view, back ->
                if (view == it) {
                    fragment?.getToolBarSelfCallback()?.invoke()
                } else {
                    if (back) {
                        if (fragment?.getToolBarBackCallback()?.invoke() != true) {
                            drawerLayout.openDrawer(Gravity.START)
                            BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                                    SIDEBAR_CLICK, "")
                        }
                    } else {
                        if (fragment?.getToolBarMenuCallback()?.invoke() != true) {
                            // EventBus.getDefault().post(ProgressBarEvent(true))
                            BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                                    INTRODUCTION_CLICK, "")
                            PrivatePreference.getPreference(context).apply {
                                if (!getBoolean(PrefConst.KEY_WHETHER_CLICK_FACE_VIDEO_SOURCE, false)) {
                                    putBoolean(PrefConst.KEY_WHETHER_CLICK_FACE_VIDEO_SOURCE, true)
                                    commit()
                                }
                            }
                            var location = IntArray(2)
                            view.getLocationInWindow(location)
                        }
                    }
                }
            }
            it.setItemColorFilter(fragment?.getToolBarItemColor() ?: Color.WHITE)
        }
    }

    private fun launchSubscribe() {
        SubscribeController.getInstance()
                .launch(_mActivity, SubscribeScene.SETTING, object : SubscribeProxy.BaseListener() {

                    override fun onPurchaseSuccess(billingOrder: BillingOrder) {
                        super.onPurchaseSuccess(billingOrder)
                        if (setting_item_upgrade != null) {
                            setting_item_upgrade.visibility = View.GONE
                        }
                    }
                })
    }


    override fun onBackPressedSupport(): Boolean {
        if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.START)
            return true
        }
        return if (mCurrentTab != TAB_FACE_IMAGES) {
            bottom_bar.setCurrentItem(TAB_FACE_IMAGES)
            true
        } else {
            super.onBackPressedSupport()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChangeTabEvent(event: ChangeTabEvent) {
        val tab = event.tabId
        enterFrom = event.enterFrom
        bottom_bar.setCurrentItem(tab)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onImageDetectEvent(event: ImageDetectEvent) {
        if (event.tag == ENTRANCE_MAIN) {
            FaceFunctionManager.currentFaceImagePath = event.originalPath
            FaceFunctionManager.faceBeanMap[event.originalPath] = event.faceFunctionBean
            event.progressBarHandled = true
            start(FaceReportFragment.newInstance())
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUserStatusEvent(event: UserStatusEvent) {

    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

}