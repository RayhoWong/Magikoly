package com.glt.magikoly.function.main.discovery

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.ad.inner.InnerAdController
import com.glt.magikoly.bean.net.SearchImageDTO
import com.glt.magikoly.config.DiscoverySearchConfigBean
import com.glt.magikoly.constants.ErrorCode
import com.glt.magikoly.dialog.DialogUtils
import com.glt.magikoly.event.ImageDetectEvent
import com.glt.magikoly.ext.runMain
import com.glt.magikoly.function.FaceFunctionBean
import com.glt.magikoly.function.FaceFunctionManager
import com.glt.magikoly.function.facesdk.FaceSdkProxy
import com.glt.magikoly.function.main.ITabFragment
import com.glt.magikoly.function.main.MainFragment.Companion.TAB_DISCOVERY
import com.glt.magikoly.function.main.ToolBarCallback
import com.glt.magikoly.function.main.discovery.presenter.DiscoveryPresenter
import com.glt.magikoly.function.main.multiface.MultiFaceFragment
import com.glt.magikoly.mvp.BaseSupportFragment
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.IStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.subscribe.SubscribeController
import com.glt.magikoly.utils.DrawUtils
import com.glt.magikoly.utils.KeyboardUtil
import com.glt.magikoly.utils.Logcat
import com.glt.magikoly.utils.ViewUtils
import com.glt.magikoly.view.EasySwipeRefreshLayout
import com.glt.magikoly.view.GlobalProgressBar
import com.glt.magikoly.view.tag.XFlowLayout
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import magikoly.magiccamera.R
import kotlinx.android.synthetic.main.main_discovery_layout.*
import org.greenrobot.eventbus.EventBus


class DiscoveryFragment() : BaseSupportFragment<DiscoveryPresenter>(), IDiscoveryView, IStatistic, ITabFragment,
        View.OnClickListener {

    companion object {

        const val RESULT_SPAN_COUNT = 3
        private const val DISCOVERY_SAVE_KEY_TAG = "DISCOVERY_SAVE_KEY_TAG"

        fun newInstance(tag: String): DiscoveryFragment {
            val discoveryFragment = DiscoveryFragment()
            discoveryFragment.startTag = tag
            return discoveryFragment
        }
    }

    override var fromClick = false
    override fun getTabLock(): Boolean = false

    override fun getStatus(): Int {
        return -1
    }

    override fun setStatus(status: Int) {

    }

    override fun getTabId(): Int = TAB_DISCOVERY

    override fun onTabFragmentVisible() {
    }

    override fun onTabFragmentInvisible() {
    }

    override fun reload() {
        //do nothing
    }

    override fun getToolBarTitle(): String {
        return FaceAppState.getContext().resources.getString(R.string.discovery)
    }

    override fun getToolBarBackDrawable(): Drawable? {
        return FaceAppState.getContext().resources.getDrawable(R.drawable.icon_menu_selector)
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

    override fun getBottomBarTitle(): String {
        return FaceAppState.getContext().resources.getString(R.string.discovery)
    }

    override fun getBottomBarIcon(): Array<Drawable>? {
        val normal = FaceAppState.getContext().resources.getDrawable(R.drawable.tab_discovery_normal)
        val highLight = FaceAppState.getContext().resources.getDrawable(
                R.drawable.tab_discovery_pressed)
        return arrayOf(normal, highLight)
    }

    override fun getEntrance(): String {
        return ""
    }

    override fun getTabCategory(): String {
        return ""
    }

    override fun createPresenter(): DiscoveryPresenter {
        return DiscoveryPresenter()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_discovery_layout, null)
    }

    private val mTags: MutableList<String> = arrayListOf()

    private lateinit var mInflater: LayoutInflater

    private lateinit var mTagAdapter: XFlowLayout.Adapter

    private var mCurrentPage: Int = 1
    private var mCurrentMode: Int = -1

    private var startTag: String = ""

    private val tagItemTopMargin = DrawUtils.dip2px(13f)
    private val tagItemBottomMargin = DrawUtils.dip2px(2f)
    private val tagItemRightMargin = DrawUtils.dip2px(12f)

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (outState == null) {
            return
        }
        if (startTag.isNotBlank()) {
            outState.putString(DISCOVERY_SAVE_KEY_TAG, startTag)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState == null) {
            return
        }
        startTag = savedInstanceState.getString(DISCOVERY_SAVE_KEY_TAG, "")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        mInflater = LayoutInflater.from(_mActivity)
        iv_search_clear.visibility = View.GONE
        iv_search_clear.setOnClickListener(this)
        iv_search_icon.setOnClickListener(this)
        initEditSearch()
        initSearchTag()
        initSearchResult()
        if (SubscribeController.getInstance().isVIP() || DiscoverySearchConfigBean.isOpenImageSearch()) {
            cl_search_view.visibility = View.VISIBLE
            if (mIsAdapte) {
                tfl_search_tag.setMaxLine(DiscoveryController.getInstance().getHotwordLine())
            } else {
                tfl_search_tag.setMaxLine(7)
            }
        } else {
            cl_search_view.visibility = View.GONE
            tfl_search_tag.setMaxLine(9)
        }
        mPresenter.obtainSearchHotWord()
    }

    private lateinit var searchResultImagesAdapter: SearchResultImagesAdapter

    private var mIsHotwordClick: Boolean = false

//    private lateinit var loadMoreDelegate: LoadMoreDelegate

    private fun initSearchResult() {
        rv_search_result.apply {
            initSearchResultAdapter(this)
            //setLayoutManager
            layoutManager = GridLayoutManager(_mActivity, RESULT_SPAN_COUNT)

            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?,
                                            state: RecyclerView.State?) {
                    super.getItemOffsets(outRect, view, parent, state)
                    val margin = DrawUtils.dip2px(4f)
                    outRect?.set(margin, margin, margin, margin)
                }
            })
//            loadMoreDelegate = LoadMoreDelegate(searchResultImagesAdapter) {
//                if (mIsLoadMoreEnable) {
//                    tfl_search_tag.visibility = View.GONE
//                    mPresenter.search(et_search_text.text.toString(), mCurrentMode, mCurrentPage)
//                }
//            }
//            loadMoreDelegate.attach(rv_search_result)
            itemAnimator = DefaultItemAnimator()
//            adapter = searchResultImagesAdapter

            srl_search_result.setFooterView(R.layout.discovery_refresh_footer)
            srl_search_result.setSwipeUpRefreshEnable(true)
            srl_search_result.setSwipeDownRefreshEnable(false)
            srl_search_result.setRecycleView(this)
            srl_search_result.setAdapter(searchResultImagesAdapter)
            srl_search_result.setSwipeRefreshListener(object : EasySwipeRefreshLayout.SwipeRefreshListener {
                override fun onFooterItemStateChanged(isLoading: Boolean, itemView: View, info: String?) {
                    val tvPowerBing = itemView.findViewById<TextView>(R.id.tv_power_bing)
                    val clPowerBing = itemView.findViewById<ConstraintLayout>(R.id.cl_power_bing)
                    if (isLoading) {
                        if (mIsLoadMoreEnable) {
                            if (!TextUtils.isEmpty(info)) {
                                tvPowerBing.text = info
                            } else {
                                tvPowerBing.text = resources.getText(R.string.load_more)
                            }
                            tvPowerBing.visibility = View.VISIBLE
                            clPowerBing.visibility = View.VISIBLE
                        } else {
                            tvPowerBing.text = resources.getText(R.string.power_by_bing)
                            tvPowerBing.visibility = View.VISIBLE
                            clPowerBing.visibility = View.VISIBLE
                        }
                    } else {
                        tvPowerBing.text = resources.getText(R.string.power_by_bing)
                        tvPowerBing.visibility = View.VISIBLE
                        clPowerBing.visibility = View.VISIBLE
                    }
                }

                override fun onSwipeDownRefresh() {
                }

                override fun onSwipeUpRefresh() {
                    if (mIsLoadMoreEnable) {
                        nsl_search_tag.visibility = View.GONE
                        tfl_search_tag.visibility = View.GONE
                        mPresenter.search(et_search_text.text.toString(), mCurrentMode, mCurrentPage)
                    }
                }
            })
            adapter = srl_search_result.adapterWrapper

        }
    }

    private fun initSearchResultAdapter(recyclerView: RecyclerView) {
        searchResultImagesAdapter =
                SearchResultImagesAdapter(recyclerView, arrayListOf(), object : OnSearchResultClickListener {
                    override fun onImageItemClick(searchResult: SearchImageDTO) {
                        if (ViewUtils.isFastClick()) {
                            return
                        }
                        if (KeyboardUtil.isSoftInputVisible(_mActivity)) {
                            KeyboardUtil.hideKeyboard(et_search_text)
                        }
                        detectFace(searchResult)
                        val isBabyEntrance = if (startTag == Statistic103Constant.ENTRANCE_BABY) {
                            "1"
                        } else {
                            "0"
                        }
                        if (mIsHotwordClick) {
                            BaseSeq103OperationStatistic.uploadSqe103StatisticData(et_search_text.text.toString(), Statistic103Constant.SEARCHRESULT_CLICK, "2", isBabyEntrance)
                        } else {
                            BaseSeq103OperationStatistic.uploadSqe103StatisticData(et_search_text.text.toString(), Statistic103Constant.SEARCHRESULT_CLICK, "1", isBabyEntrance)
                        }
                        _mActivity?.let { activity ->
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

                    fun detectFace(searchResult: SearchImageDTO) {
                        GlobalProgressBar.show(startTag)
                        FaceFunctionManager.detectFace(FaceAppState.getContext(), searchResult.original
                                ?: "", object : FaceSdkProxy.OnDetectResult {
                            override fun onDetectMultiFaces(originalPath: String, originBitmap: Bitmap, faces: List<FirebaseVisionFace>, onDetectResult: FaceSdkProxy.OnDetectResult) {
                                FaceAppState.getMainActivity()?.start(MultiFaceFragment.newInstance(startTag, searchResult.original
                                        ?: "", originBitmap, faces, onDetectResult))
                            }

                            override fun onDetectSuccess(originalPath: String, faceFunctionBean: FaceFunctionBean) {
                                runMain {
                                    faceFunctionBean.category = if (mIsHotwordClick) Statistic103Constant.CATEGORY_HOT_WORLD else Statistic103Constant.CATEGORY_SEARCH
                                    EventBus.getDefault().post(ImageDetectEvent(startTag, originalPath, faceFunctionBean))
                                }
                            }

                            override fun onDetectFail(originalPath: String, errorCode: Int) {
                                GlobalProgressBar.hide()
                                DialogUtils.showErrorDialog(_mActivity, errorCode, Runnable {
                                    if (errorCode == ErrorCode.NETWORK_ERROR) {
                                        detectFace(searchResult)
                                    }
                                })
                            }
                        })
                    }
                })
    }

    private var mShowHotword: Int = 0

    private fun initSearchTag() {
        nsl_search_tag.visibility = View.VISIBLE
        tfl_search_tag.visibility = View.VISIBLE
        mTagAdapter = object : XFlowLayout.Adapter() {
            override fun getItemCount(): Int = mTags.size

            override fun getItemViewByPos(pos: Int): View {
                if (pos > mShowHotword) {
                    mShowHotword = pos
                }
                if (pos == 0 && mShowHotword != 0) {
                    BaseSeq103OperationStatistic.uploadSqe103StatisticData(mShowHotword.toString(), Statistic103Constant.POPULARSEARCHES_SHOW, "", "")
                }

                val itemView = mInflater.inflate(R.layout.item_search_tag, null, false)
                val layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, DrawUtils.dip2px(38f))
                layoutParams.topMargin = tagItemTopMargin
                layoutParams.rightMargin = tagItemRightMargin
                layoutParams.bottomMargin = tagItemBottomMargin
                itemView.layoutParams = layoutParams
                itemView.findViewById<TextView>(R.id.tv_search_tag_item).text = mTags[pos]
                itemView.setOnClickListener {
                    et_search_text.setText(mTags[pos])
                    mCurrentPage = 1
                    mCurrentMode = 1
                    nsl_search_tag.visibility = View.GONE
                    tfl_search_tag.visibility = View.GONE
                    srl_search_result.visibility = View.GONE
                    showProgress()
                    mPresenter.search(mTags[pos], mCurrentMode, mCurrentPage)
                    mIsHotwordClick = true

                    BaseSeq103OperationStatistic.uploadSqe103StatisticData(mTags[pos], Statistic103Constant.POPULARSEARCHES_CLICK, "", "")
                }
                return itemView
            }
        }
        tfl_search_tag.setAdapter(mTagAdapter)

    }

    private fun initEditSearch() {
        et_search_text.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (et_search_text.text.toString().isBlank()) {
                    return@setOnEditorActionListener true
                }

                KeyboardUtil.hideKeyboard(et_search_text)
                mCurrentPage = 1
                mCurrentMode = 2
                nsl_search_tag.visibility = View.GONE
                tfl_search_tag.visibility = View.GONE
                srl_search_result.visibility = View.GONE
                showProgress()
                mPresenter.search(et_search_text.text.toString(), mCurrentMode, mCurrentPage)
                BaseSeq103OperationStatistic.uploadSqe103StatisticData(et_search_text.text.toString(),
                        Statistic103Constant.SEARCH_CLICK, "", "")
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        et_search_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (KeyboardUtil.isSoftInputVisible(_mActivity)) {
                    mIsHotwordClick = false
                }

                if (s.toString().isBlank()) {
                    iv_search_clear.visibility = View.GONE
                } else {
                    iv_search_clear.visibility = View.VISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv_search_clear -> {
//                startActivity(Intent(_mActivity,BeautyTestActivity::class.java))
                clickClear()
                BaseSeq103OperationStatistic.uploadSqe103StatisticData(mTags.size.toString(), Statistic103Constant.POPULARSEARCHES_SHOW, "", "")
            }
            R.id.iv_search_icon -> {
                if (et_search_text.text.toString().isBlank()) {
                    return
                }

                mCurrentPage = 1
                mCurrentMode = 2
                srl_search_result.visibility = View.GONE
                nsl_search_tag.visibility = View.GONE
                tfl_search_tag.visibility = View.GONE
                showProgress()
                mPresenter.search(et_search_text.text.toString(), mCurrentMode, mCurrentPage)
                BaseSeq103OperationStatistic.uploadSqe103StatisticData(et_search_text.text.toString(),
                        Statistic103Constant.SEARCH_CLICK, "", "")
            }
            else -> {
            }
        }
    }

    override fun obtainHotWordSuccess(hotword: List<String>) {
        mTags.clear()
        mTags.addAll(hotword)
        nsl_search_tag.visibility = View.VISIBLE
        tfl_search_tag.visibility = View.VISIBLE
        adapteSearchTag()
        srl_search_result.visibility = View.GONE
        mCurrentPage = 1
        tv_search_result_title.text = getString(R.string.discovery_poplur_search)


//        BaseSeq103OperationStatistic.uploadSqe103StatisticData(hotword.size.toString(), Statistic103Constant.POPULARSEARCHES_SHOW, "", "")
        mTagAdapter.notifyDataChanged()
    }


    private var mIsAdapte: Boolean = false

    fun adapteSearchTag() {
        if (mIsAdapte) {
            return
        }
        nsl_search_tag.post {
            val height = nsl_search_tag.height
            tfl_search_tag.getChildAt(0)?.post {
                val h = tfl_search_tag.getChildAt(0).height
                val lines = height / (tagItemTopMargin + h + tagItemBottomMargin)
                tfl_search_tag.setMaxLine(lines)
                DiscoveryController.getInstance().recordHotwordLine(lines)
                mTagAdapter.notifyDataChanged()
                tfl_search_tag.postInvalidate()
                Logcat.d("tfl_search_tag", "height=" + height + ",h=" + (h + tagItemTopMargin) + ",lines=" + lines)
                mIsAdapte = true
            }
        }
    }

    fun setHotwordLines() {
        if (mIsAdapte) {
            return
        }
        tfl_search_tag?.post {
            tfl_search_tag.getChildAt(0)?.post {
                if (tfl_search_tag == null) {
                    return@post
                }
                tfl_search_tag.setMaxLine(DiscoveryController.getInstance().getHotwordLine())
                mTagAdapter.notifyDataChanged()
                tfl_search_tag.postInvalidate()
            }
            mIsAdapte = true
        }
    }

    override fun obtainHotWordFailure(msg: String) {

    }

    private var mIsLoadMoreEnable: Boolean = true

    private var progressBarAnim: ValueAnimator? = null

    fun showProgress() {
        if (iv_progress.visibility != View.VISIBLE) {
            iv_progress.visibility = View.VISIBLE
            progressBarAnim = ValueAnimator.ofFloat(0f, 360f)
            progressBarAnim?.apply {
                duration = 1000
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
                addUpdateListener {
                    iv_progress?.apply {
                        rotation = it.animatedValue as Float
                        invalidate()
                    }
                }
                start()
            }
        }
    }

    fun hideProgress() {
        iv_progress?.apply {
            if (visibility == View.VISIBLE) {
                progressBarAnim?.cancel()
                visibility = View.GONE
            }
        }
    }

    override fun obtainSearchContentSuccess(content: List<SearchImageDTO>, page: Int) {
        if (mCurrentPage == 1 && iv_progress.visibility == View.GONE) {
            return
        }
        srl_search_result.visibility = View.VISIBLE
        hideProgress()
        tv_search_result_title.text = getString(R.string.discovery_online_result)

        if (page == 1) {
            if (mIsHotwordClick) {
                BaseSeq103OperationStatistic.uploadSqe103StatisticData(et_search_text.text.toString(), Statistic103Constant.SEARCHRESULT_SHOW, "2", "")
            } else {
                BaseSeq103OperationStatistic.uploadSqe103StatisticData(et_search_text.text.toString(), Statistic103Constant.SEARCHRESULT_SHOW, "1", "")
            }

            if (srl_search_result.adapterWrapper.adapter is SearchResultImagesAdapter) {
                val adapter = srl_search_result.adapterWrapper.adapter as SearchResultImagesAdapter
                adapter.autoNotify(adapter.searchImageList, content, { old: SearchImageDTO, new: SearchImageDTO ->
                    return@autoNotify old.thumbnail == new.thumbnail
                }, { old, new ->
                    return@autoNotify old.thumbnail == new.thumbnail
                })
                srl_search_result.adapterWrapper.notifyDataSetChanged()
                srl_search_result.invalidate()
            }
//            searchResultImagesAdapter.autoNotify(searchResultImagesAdapter.searchImageList, content, { old: SearchImageDTO, new: SearchImageDTO ->
//                return@autoNotify old.thumbnail == new.thumbnail
//            }, { old, new ->
//                return@autoNotify old.thumbnail == new.thumbnail
//            })
        } else {

            if (srl_search_result.adapterWrapper.adapter is SearchResultImagesAdapter) {
                val adapter = srl_search_result.adapterWrapper.adapter as SearchResultImagesAdapter
                val result = adapter.searchImageList.map {
                    it
                }.toMutableList()
                result.addAll(content)

                adapter.autoNotify(adapter.searchImageList, result, { old: SearchImageDTO, new: SearchImageDTO ->
                    return@autoNotify old.thumbnail == new.thumbnail
                }, { old, new ->
                    return@autoNotify old.thumbnail == new.thumbnail
                })
                srl_search_result.adapterWrapper.notifyDataSetChanged()
                srl_search_result.invalidate()
            }

//            val result = searchResultImagesAdapter.searchImageList.map {
//                it
//            }.toMutableList()
//            result.addAll(content)
//
//            searchResultImagesAdapter.autoNotify(searchResultImagesAdapter.searchImageList, result, { old: SearchImageDTO, new: SearchImageDTO ->
//                return@autoNotify old.thumbnail == new.thumbnail
//            }, { old, new ->
//                return@autoNotify old.thumbnail == new.thumbnail
//            })
        }
//        loadMoreDelegate.loadMoreComplete()

        if (content.size < 20) {
            mIsLoadMoreEnable = false
            srl_search_result.setSwipeUpRefreshFinish(resources.getString(R.string.power_by_bing))
//            srl_search_result.setSwipeUpRefreshEnable(false)
        } else {
            mCurrentPage += 1
            mIsLoadMoreEnable = true
            srl_search_result.setSwipeUpRefreshFinish(null)
            srl_search_result.setSwipeUpRefreshEnable(true)
        }
    }

    override fun obtainSearchContentFailure(msg: String, page: Int) {
        mIsLoadMoreEnable = true
        srl_search_result.setSwipeUpRefreshFinish(null)
        srl_search_result.setSwipeUpRefreshEnable(true)
    }

    override fun doBackPressedSupport(): Boolean {
        if (tfl_search_tag.visibility == View.GONE) {
            clickClear()
            return true
        }
        return false
    }

    private fun clickClear() {
        if (srl_search_result.adapterWrapper.adapter is SearchResultImagesAdapter) {
            (srl_search_result.adapterWrapper.adapter as SearchResultImagesAdapter).searchImageList.clear()
            (srl_search_result.adapterWrapper.adapter as SearchResultImagesAdapter).notifyDataSetChanged()
            srl_search_result.adapterWrapper.notifyDataSetChanged()
            srl_search_result.invalidate()
        }
        //                searchResultImagesAdapter.searchImageList.clear()
        //                searchResultImagesAdapter.notifyDataSetChanged()
        //                searchResultImagesAdapter.autoNotify(searchResultImagesAdapter.searchImageList, arrayListOf(), { old: SearchImageDTO, new: SearchImageDTO ->
        //                    return@autoNotify old.thumbnail == new.thumbnail
        //                }, { old, new ->
        //                    return@autoNotify old.thumbnail == new.thumbnail
        //                })
        et_search_text.setText("")
        nsl_search_tag.visibility = View.VISIBLE
        tfl_search_tag.visibility = View.VISIBLE
        srl_search_result.visibility = View.GONE
        tv_search_result_title.text = getString(R.string.discovery_poplur_search)
        hideProgress()
        mCurrentPage = 1
    }
}