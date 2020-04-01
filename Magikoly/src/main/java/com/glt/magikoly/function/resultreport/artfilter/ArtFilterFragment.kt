package com.glt.magikoly.function.resultreport.artfilter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.glt.magikoly.BuyChannelApiProxy
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.SafelyBitmap
import com.glt.magikoly.bean.ArtFilterBean
import com.glt.magikoly.config.AgingShutterConfigBean
import com.glt.magikoly.download.DownloadListener
import com.glt.magikoly.download.DownloadManager
import com.glt.magikoly.download.DownloadTask
import com.glt.magikoly.event.PredictionRequestInit
import com.glt.magikoly.event.PredictionRequestSuccess
import com.glt.magikoly.ext.postEvent
import com.glt.magikoly.ext.runMain
import com.glt.magikoly.function.FaceFunctionManager
import com.glt.magikoly.function.ReportNames
import com.glt.magikoly.function.main.*
import com.glt.magikoly.function.poster.ISavePhoto
import com.glt.magikoly.function.resultreport.FaceReportFragment
import com.glt.magikoly.function.resultreport.LayoutPurchaseController
import com.glt.magikoly.function.resultreport.TabInfo
import com.glt.magikoly.function.resultreport.TabInfo.CREATOR.SUB_TAB_ART_FILTER
import com.glt.magikoly.mvp.BaseSupportFragment
import com.glt.magikoly.prisma.PrismaProxy
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.IStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.subscribe.SubscribeController
import com.glt.magikoly.utils.BitmapUtils
import com.glt.magikoly.utils.Duration
import com.glt.magikoly.utils.ToastUtils
import com.glt.magikoly.view.GlobalProgressBar
import kotlinx.android.synthetic.main.fragment_art_filter.*
import kotlinx.android.synthetic.main.layout_subscribe_entrance.*
import magikoly.magiccamera.R

/**
 *
 * @author rayhahah
 * @blog http://rayhahah.com
 * @time 2019/2/18
 * @tips 这个类是Object的子类
 * @fuction
 */

class ArtFilterFragment : BaseSupportFragment<ArtFilterPresenter>(), IArtFilterView, IStatistic,
        ITabFragment, ISubscribe, INewTabFragment, ISavePhoto, View.OnTouchListener {

    override fun onExit() {
        popSelf()
    }

    override fun getFilePrefix(): String = ReportNames.ART_FILTER_REPORT_PREFIX

    override fun getGPColor(): Int = Color.WHITE

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
        fun newInstance(): ArtFilterFragment {
            return ArtFilterFragment()
        }
    }

    private val imageTargets = ArrayList<SimpleTarget<Bitmap>>()
    private var isExit: Boolean = false
    private var resultBitmap: SafelyBitmap? = null
    private var originalBitmap: SafelyBitmap? = null
    private var mCurrentPercent: Float = 100f
    private var mWatchAdFilter: HashMap<String, Boolean> = HashMap()

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

    override fun getTabId(): Int = SUB_TAB_ART_FILTER

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
        return null
    }

    override fun getToolBarSelfCallback(): ToolBarCallback? = null

    override fun getToolBarItemColor(): Int? {
        return FaceAppState.getContext().resources.getColor(R.color.toolbar_title_dark_color)
    }

    override fun getBottomBarTitle(): String {
        return getString(R.string.art_filter)
    }

    override fun getBottomBarIcon(): Array<Drawable>? = null

    override fun getEntrance(): String = Statistic103Constant.ENTRANCE_ART_FILTER

    override fun getTabCategory(): String = AgingShutterConfigBean.getRequestType()

    override fun createPresenter(): ArtFilterPresenter = ArtFilterPresenter()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_art_filter, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreInstanceState(savedInstanceState)
        GlobalProgressBar.hide()
        postEvent(PredictionRequestInit(getTabId()))
        mWatchAdFilter.clear()
        mCurrentPercent = 100f

        val bitmap = if (FaceFunctionManager.demoFaceImageInfo == null) {
            iv_apply.visibility = View.VISIBLE
            FaceFunctionManager.faceBeanMap[FaceFunctionManager.currentFaceImagePath]?.photo?.getBitmap()
        } else {
            iv_apply.visibility = View.GONE
            (resources.getDrawable(FaceFunctionManager.demoFaceImageInfo!!.imgId) as BitmapDrawable).bitmap
        }

        bitmap?.apply {
            iv_original.setImageBitmap(this)
            iv_transfer_original.setImageBitmap(this)
            originalBitmap = SafelyBitmap(this)

            seek_bar_current.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    val percent = 1.0f * progress / seekBar.max
                    iv_transfer.alpha = percent
                    mCurrentPercent = percent

                    if (mCurrentPercent == 0f) {
                        iv_apply.visibility = View.GONE
                    } else {
                        if (iv_apply.visibility == View.GONE) {
                            iv_apply.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
            iv_original.imageAlpha = (originalAlpha * 255).toInt()
            iv_original.invalidate()


            initLayoutPurchase()
            iv_close.setOnClickListener(this@ArtFilterFragment)
            iv_apply.setOnClickListener(this@ArtFilterFragment)
            iv_see_original.setOnTouchListener(this@ArtFilterFragment)
            cl_transfer.setOnTouchListener(this@ArtFilterFragment)
//        iv_transfer_original.setOnTouchListener(this)
//        iv_transfer.setOnTouchListener(this)

            DownloadManager.instance.setDownloadGroupListener(mDownloadListener, DownloadManager.DOWNLOAD_GROUP_FILTER)
            initRv()
            mPresenter.loadFilterData(_mActivity) {
                mFilterAdapter?.data?.clear()
                mFilterAdapter?.data?.addAll(it)
                mFilterAdapter?.notifyDataSetChanged()

                onClick(mFilterData.get(0), 0, true)
            }

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

    private fun initLayoutPurchase() {
        LayoutPurchaseController.getInstance().init(cl_subscribe_entrance, LayoutPurchaseController.Bean(
                false, false, false, if (FaceFunctionManager.demoFaceImageInfo == null) {
            false
        } else {
            true
        }, R.drawable.btn_purple_line_bold_bg_white
        ), object : LayoutPurchaseController.OnClickAdapter() {

            override fun onHomeClick() {
                super.onHomeClick()
                BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                        Statistic103Constant.BACKTOHOME_CLICK, "")
                popTo(MainFragment::class.java, false)
            }

            override fun onCloseClick() {
                super.onCloseClick()
//                showAdAndExit(false)
            }

            override fun onAdFailed() {
                super.onAdFailed()
                mSelectBean?.apply {
                    mWatchAdFilter.put(tag, true)
                    onClick(this, mCurrentPosition, true)
                }
            }

            override fun onAdClick() {
                super.onAdClick()
                BaseSeq103OperationStatistic.uploadSqe103StatisticData(Statistic103Constant.OBJ_RESULT,
                        Statistic103Constant.PURCHASE_VIDEO_AD_CLICK,
                        entrance,
                        tabCategory, FaceReportFragment.isDefaultEnter(entrance), BuyChannelApiProxy.getCampaign())

                mSelectBean?.apply {
                    mWatchAdFilter.put(tag, true)
                    onClick(this, mCurrentPosition, true)
                }
            }

            override fun onWatchVideoStart() {
                super.onWatchVideoStart()
                BaseSeq103OperationStatistic.uploadSqe103StatisticData(Statistic103Constant.OBJ_RESULT,
                        Statistic103Constant.PURCHASE_VIDEO_SHOW,
                        entrance,
                        tabCategory, FaceReportFragment.isDefaultEnter(entrance), BuyChannelApiProxy.getCampaign())
            }

            override fun onWatchVideoClick() {
                super.onWatchVideoClick()
                mSelectBean?.apply {
                    mWatchAdFilter.put(tag, false)
                }
                BaseSeq103OperationStatistic.uploadSqe103StatisticData("2",
                        Statistic103Constant.PURCHASE_VIDEO_CLICK,
                        entrance,
                        tabCategory)

                BaseSeq103OperationStatistic.uploadSqe103StatisticData("2",
                        Statistic103Constant.GUIDEBOTTOM_CLICK,
                        entrance, "0", FaceReportFragment.isDefaultEnter(entrance), BuyChannelApiProxy.getCampaign())
            }

            override fun onWatchVideoFinish() {
                super.onWatchVideoFinish()
                BaseSeq103OperationStatistic.uploadSqe103StatisticData(Statistic103Constant.OBJ_RESULT,
                        Statistic103Constant.PURCHASE_VIDEO_END,
                        entrance,
                        tabCategory, FaceReportFragment.isDefaultEnter(entrance), BuyChannelApiProxy.getCampaign())
//                iTabFragment.reload()

                mSelectBean?.apply {
                    mWatchAdFilter.put(tag, true)
                    onClick(this, mCurrentPosition, true)
                }
            }
        })
    }

    private var mCurrentPosition: Int = 0

    private val mDownloadListener: DownloadListener = object : DownloadListener {
        override fun progress(task: DownloadTask) {
        }

        override fun completed(task: DownloadTask) {
            mSelectBean?.apply {
                if (mCurrentPosition == 0) {
                    return
                }

                if (isLocal) {
                    return
                }

                if (DownloadManager.instance.getDownloadTaskId(zipUrl, DownloadManager.sBaseFilePath,
                                DownloadManager.instance.getRealFileName(tag, zipUrl)) == task.id) {

                    downloadStatus = ArtFilterBean.DOWNLOAD_STATUS_DOWNLOADED
                    onClick(this, mCurrentPosition, true)
                }
            }
        }

        override fun pending(task: DownloadTask) {
        }

        override fun taskStart(task: DownloadTask) {
        }

        override fun connectStart(task: DownloadTask) {
        }

        override fun paused(task: DownloadTask) {
        }

        override fun error(task: DownloadTask) {
            mFilterData.forEachIndexed { index, artFilterBean ->
                if (DownloadManager.instance.getDownloadTaskId(artFilterBean.zipUrl,
                                DownloadManager.sBaseFilePath,
                                DownloadManager.instance.getRealFileName(artFilterBean.tag, artFilterBean.zipUrl)) == task.id) {
                    artFilterBean.downloadStatus = ArtFilterBean.DOWNLOAD_STATUS_WAIT
                    artFilterBean.isCheck = false
                    mFilterAdapter?.notifyItemChanged(index)
                    ToastUtils.showToast(R.string.network_error_tips, Toast.LENGTH_SHORT)
                    onClick(mFilterData.get(0), 0, true)
                }
            }
        }
    }

    private val mFilterClickListener: OnClickListener = object : OnClickListener {

        override fun onItemClick(artFilterBean: ArtFilterBean, holder: ArtFilterViewHolder, position: Int) {
            onClick(artFilterBean, position)
        }
    }

    private var mSelectBean: ArtFilterBean? = null

    fun onClick(artFilterBean: ArtFilterBean, position: Int, isRefresh: Boolean = false) {
        if (seek_bar_current == null) {
            return
        }

        if (position == mCurrentPosition && !isRefresh) {
            return
        }

        seek_bar_current.visibility = View.VISIBLE
        iv_transfer.visibility = View.VISIBLE
        iv_apply.visibility = View.GONE

        uploadFilterClick(artFilterBean)

        LayoutPurchaseController.getInstance().hide(cl_subscribe_entrance)
        if (artFilterBean.sort == 0) {
            seek_bar_current.visibility = View.GONE
            iv_transfer.visibility = View.GONE
            iv_apply.visibility = View.GONE
//
//            if (FaceFunctionManager.demoFaceImageInfo == null) {
//                iv_apply.visibility = View.VISIBLE
//            } else {
//                iv_apply.visibility = View.GONE
//            }
            notifyClickData(artFilterBean, true, false, false, position)
            return
        }
        if (artFilterBean.isVIP) {
            if (SubscribeController.getInstance().isVIP(SubscribeController.PERMISSION_WITH_TRIAL)
                    || mWatchAdFilter.get(artFilterBean.tag) == true || SubscribeController.getInstance().isFreeCount() || artFilterBean.isUseFreeCount) {
                when (mPresenter.obtainDownloadStatus(artFilterBean)) {
                    ArtFilterBean.DOWNLOAD_STATUS_WAIT -> {
                        seek_bar_current.visibility = View.GONE
                        iv_transfer.visibility = View.GONE
                        mPresenter.doDownload(artFilterBean.tag, artFilterBean.zipUrl, artFilterBean.isVIP)
                        notifyClickData(artFilterBean, true, false, true, position)
                    }
                    ArtFilterBean.DOWNLOAD_STATUS_DOWNLOADED -> {
                        artFilterBean.downloadStatus = ArtFilterBean.DOWNLOAD_STATUS_DOWNLOADED
                        mPresenter.obtainDestPath(artFilterBean)
                        doTransfer(artFilterBean.destPath, artFilterBean.isLocal) { success ->
                            if (success) {
                                if (FaceFunctionManager.demoFaceImageInfo == null) {
                                    postEvent(PredictionRequestSuccess(getTabId()))
                                    iv_apply.visibility = View.VISIBLE
                                } else {
                                    iv_apply.visibility = View.GONE
                                }
                                if (!artFilterBean.isUseFreeCount && !SubscribeController.getInstance().isVIP(SubscribeController.PERMISSION_WITH_TRIAL) && mWatchAdFilter.get(artFilterBean.tag) != true) {
                                    artFilterBean.isUseFreeCount = true
                                    SubscribeController.getInstance().subFreeCount()
                                }

                                notifyClickData(artFilterBean, true, false, false, position)
                            } else {
                                mPresenter.obtainFilterBeanDefault(artFilterBean)
                                onClick(artFilterBean, position)
                            }
                        }
                    }
                    ArtFilterBean.DOWNLOAD_STATUS_DOWNLOADING -> {
                    }
                    else -> {
                    }
                }
            } else {
                showSubscribeCover(artFilterBean, position)
                notifyClickData(artFilterBean, true, true, false, position)
            }
        } else {
            when (mPresenter.obtainDownloadStatus(artFilterBean)) {
                ArtFilterBean.DOWNLOAD_STATUS_WAIT -> {
                    seek_bar_current.visibility = View.GONE
                    iv_transfer.visibility = View.GONE
                    mPresenter.doDownload(artFilterBean.tag, artFilterBean.zipUrl, artFilterBean.isVIP)
                    artFilterBean.downloadStatus = ArtFilterBean.DOWNLOAD_STATUS_DOWNLOADING
                    notifyClickData(artFilterBean, true, false, true, position)
                }
                ArtFilterBean.DOWNLOAD_STATUS_DOWNLOADED -> {
                    artFilterBean.downloadStatus = ArtFilterBean.DOWNLOAD_STATUS_DOWNLOADED
                    mPresenter.obtainDestPath(artFilterBean)
                    doTransfer(artFilterBean.destPath, artFilterBean.isLocal) { success ->
                        if (success) {
                            if (FaceFunctionManager.demoFaceImageInfo == null) {
                                postEvent(PredictionRequestSuccess(getTabId()))
                                iv_apply.visibility = View.VISIBLE
                            } else {
                                iv_apply.visibility = View.GONE
                            }
                            notifyClickData(artFilterBean, true, false, false, position)
                        } else {
                            mPresenter.obtainFilterBeanDefault(artFilterBean)
                            onClick(artFilterBean, position)
                        }
                    }
                }
                ArtFilterBean.DOWNLOAD_STATUS_DOWNLOADING -> {
                }
                else -> {
                }
            }
        }
    }

    private fun uploadFilterClick(artFilterBean: ArtFilterBean) {
        BaseSeq103OperationStatistic.uploadSqe103StatisticData(artFilterBean.tag,
                Statistic103Constant.ART_FILTER_CLICK, if (artFilterBean.isVIP) {
            "1"
        } else {
            "2"
        }, "", if (artFilterBean.isLocal) {
            "1"
        } else {
            "2"
        })
    }

    private fun uploadFilterTransfer(artFilterBean: ArtFilterBean?, success: Boolean) {
        artFilterBean?.apply {
            BaseSeq103OperationStatistic.uploadSqe103StatisticData(artFilterBean.tag,
                    Statistic103Constant.ART_FILTER_APPLY, if (artFilterBean.isVIP) {
                "1"
            } else {
                "2"
            }, if (success) {
                "1"
            } else {
                "2"
            }, if (artFilterBean.isLocal) {
                "1"
            } else {
                "2"
            })
        }
    }

    private fun showSubscribeCover(artFilterBean: ArtFilterBean, position: Int) {
        BaseSeq103OperationStatistic.uploadSqe103StatisticData("",
                Statistic103Constant.FAKEREPORT_ENTER, entrance, "",
                FaceReportFragment.isDefaultEnter(entrance), BuyChannelApiProxy.getCampaign())
        LayoutPurchaseController.getInstance().show(cl_subscribe_entrance, false,
                artFilterBean.blurCover,
                FaceFunctionManager.demoFaceImageInfo != null)
    }

    private fun notifyClickData(artFilterBean: ArtFilterBean,
                                isCheck: Boolean, isLock: Boolean,
                                isStartDownload: Boolean, position: Int) {
        val indexOf = mFilterData.indexOf(artFilterBean)
        if (isCheck) {
            mFilterData.forEach {
                it.isCheck = false
            }
            mFilterData.get(indexOf).isCheck = true

            mCurrentPosition = position
            mSelectBean = artFilterBean
        }


        mFilterData.get(indexOf).isLock = isLock
        if (isStartDownload) {
            mFilterData.get(indexOf).downloadStatus = ArtFilterBean.DOWNLOAD_STATUS_DOWNLOADING
        } else {
            mFilterData.get(indexOf).downloadStatus = mPresenter.obtainDownloadStatus(mFilterData.get(indexOf))
        }

        if (isCheck) {
            mFilterAdapter?.notifyDataSetChanged()
        } else {
            mFilterAdapter?.notifyItemChanged(indexOf)
        }
    }

    private var mIsTransfering: Boolean = false


    /**
     * 应用滤镜
     */
    private fun doTransfer(destPath: String, isLocal: Boolean, onFinish: (success: Boolean) -> Unit) {
        val bitmap = originalBitmap?.getBitmap()
        bitmap?.apply {
            GlobalProgressBar.show(entrance, false, true)
            Duration.setStart("ArtFilter")
            mIsTransfering = true
            if (isLocal) {
                PrismaProxy.getInstance().transferAssets(_mActivity, destPath, this) {
                    GlobalProgressBar.hide()
                    mSelectBean?.apply {
                        if (it == null) {
                            uploadFilterTransfer(mSelectBean, false)
                            onFinish(false)
                        } else {
                            if (seek_bar_current == null) {
                                return@apply
                            }

                            seek_bar_current.progress = 100
                            iv_transfer.alpha = mCurrentPercent
                            iv_transfer?.setImageBitmap(it)
                            resultBitmap = SafelyBitmap(it)
                            uploadFilterTransfer(mSelectBean, true)
                            onFinish(true)
                            Duration.logDuration("ArtFilter")
                        }
                    }
                }
            } else {
                PrismaProxy.getInstance().transferZip(destPath, this) {
                    GlobalProgressBar.hide()
                    mSelectBean?.apply {
                        if (it == null) {
                            uploadFilterTransfer(mSelectBean, false)
                            onFinish(false)
                        } else {
                            if (seek_bar_current == null) {
                                return@apply
                            }
                            seek_bar_current.progress = 100
                            iv_transfer.alpha = mCurrentPercent
                            iv_transfer?.setImageBitmap(it)
                            resultBitmap = SafelyBitmap(it)
                            uploadFilterTransfer(mSelectBean, true)
                            onFinish(true)
                            Duration.logDuration("ArtFilter")
                        }
                    }
                }
            }
        }
    }

    private var mFilterAdapter: ArtFilterAdapter? = null
    private var mFilterLayoutManager: LinearLayoutManager? = null
    private var mFilterData: ArrayList<ArtFilterBean> = ArrayList()

    private fun initRv() {
        mFilterLayoutManager = LinearLayoutManager(_mActivity, LinearLayoutManager.HORIZONTAL, false)
        mFilterAdapter = ArtFilterAdapter(rv_filter, mFilterData, mFilterClickListener, originalBitmap?.getBitmap(), mPresenter)
        rv_filter.layoutManager = mFilterLayoutManager
        rv_filter.adapter = mFilterAdapter
    }

    override fun onTabFragmentVisible() {
    }

    override fun onTabFragmentInvisible() {
    }

    override fun onDestroyView() {
        isExit = true
        clearImageTargets()
        DownloadManager.instance.removeGroupAndUnderGroupListener(DownloadManager.DOWNLOAD_GROUP_FILTER)

        switchAnimator?.cancel()
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

    override fun onClick(v: View) {
        super.onClick(v)
        when (v.id) {
            R.id.iv_close -> {
                doExit()
            }
            R.id.iv_apply -> {
                saveFilter()
            }
            else -> {
            }
        }
    }

    private fun saveFilter() {
        if (mCurrentPosition == 0) {
            originalBitmap?.apply {
                val result = getBitmap()
                result?.apply {
                    BaseSeq103OperationStatistic.uploadSqe103StatisticData(mSelectBean?.tag ?: "",
                            Statistic103Constant.SAVEPHOTO_CLICK, entrance, "",
                            FaceReportFragment.isDefaultEnter(entrance), BuyChannelApiProxy.getCampaign())
                    saveResultImage(SUB_TAB_ART_FILTER, this) {
                        doExit()
                    }
                }
            }
            return
        }
        if (resultBitmap == null || resultBitmap?.getBitmap() == null) {
            return
        }

        GlobalProgressBar.show(entrance, false, true)
        resultBitmap?.getBitmap()?.apply {
            //        val generateResultBitmap = generateResultBitmap(originalBitmap, )
            var number = mCurrentPercent * 100
            val generateResultBitmap = getTransparentBitmap(this, number.toInt())
            originalBitmap?.apply {
                val base = getBitmap()
                base?.apply {
                    if (base.width >= generateResultBitmap.width && base.height >= generateResultBitmap.height) {
                        val left = (base.width - generateResultBitmap.width) / 2
                        val top = (base.height - generateResultBitmap.height) / 2
                        val right = left + generateResultBitmap.width
                        val bottom = top + generateResultBitmap.height
                        val clipBitmap = BitmapUtils.clipBitmap(base, Rect(left, top, right, bottom))
                        val result = BitmapUtils.composeBitmap(clipBitmap, generateResultBitmap,
                                0, 0, generateResultBitmap.width, generateResultBitmap.height)
                        GlobalProgressBar.hide()
                        BaseSeq103OperationStatistic.uploadSqe103StatisticData(mSelectBean?.tag
                                ?: "",
                                Statistic103Constant.SAVEPHOTO_CLICK, entrance, "",
                                FaceReportFragment.isDefaultEnter(entrance), BuyChannelApiProxy.getCampaign())
                        saveResultImage(SUB_TAB_ART_FILTER, result) {
                            doExit()
                        }
                    } else {
                        val left = (generateResultBitmap.width - base.width) / 2
                        val top = (generateResultBitmap.height - base.height) / 2
                        val right = left + base.width
                        val bottom = top + base.height
                        val clipBitmap = BitmapUtils.clipBitmap(generateResultBitmap, Rect(left, top, right, bottom))
                        val result = BitmapUtils.composeBitmap(base, clipBitmap,
                                0, 0, base.width, base.height)
                        GlobalProgressBar.hide()
                        BaseSeq103OperationStatistic.uploadSqe103StatisticData(mSelectBean?.tag
                                ?: "",
                                Statistic103Constant.SAVEPHOTO_CLICK, entrance, "",
                                FaceReportFragment.isDefaultEnter(entrance), BuyChannelApiProxy.getCampaign())
                        saveResultImage(SUB_TAB_ART_FILTER, result) {
                            doExit()
                        }
                    }
                }
            }
        }

    }

    /**
     * @param number:0-100
     */
    fun getTransparentBitmap(sourceImg: Bitmap, number: Int): Bitmap {
        var sourceImg = sourceImg
        var number = number
        val argb = IntArray(sourceImg.width * sourceImg.height)

        sourceImg.getPixels(argb, 0, sourceImg.width, 0, 0, sourceImg
                .width, sourceImg.height)// 获得图片的ARGB值
        number = number * 255 / 100
        for (i in argb.indices) {
            argb[i] = number shl 24 or (argb[i] and 0x00FFFFFF)
        }
        sourceImg = Bitmap.createBitmap(argb, sourceImg.width, sourceImg
                .height, Bitmap.Config.ARGB_8888)
        return sourceImg
    }

    override fun doBackPressedSupport(): Boolean {
        doExit()
        return true
    }

    private fun doExit() {
        runMain {
            val faceReportFragment = findFragment(FaceReportFragment::class.java)
            if (faceReportFragment != null) {
                mSelectBean = null
                mCurrentPosition = -1
                faceReportFragment.confirmExitOrNot(true, getTabId())
            }
        }
    }

    private fun popSelf() {
        val faceReportFragment = findFragment(FaceReportFragment::class.java)
        if (faceReportFragment != null) {
            faceReportFragment.onMenuItemClick(TabInfo.SUB_TAB_FILTER)
        }
        pop()
    }


    override fun onTouch(v: View, event: MotionEvent?): Boolean {
        when (v.id) {
            R.id.iv_see_original, R.id.cl_transfer, R.id.iv_transfer_original -> {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        showOriginalPhoto()
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        hideOriginalPhoto()
                    }
                }
                return true
            }
            else -> {
            }
        }
        return false
    }

    private var switchAnimator: ValueAnimator? = null
    private var originalAlpha = 0f

    private fun hideOriginalPhoto() {
        switchAnimator?.cancel()
        switchAnimator = ValueAnimator.ofFloat(originalAlpha, 0f)
        switchAnimator?.duration = (1000f * originalAlpha).toLong()
        if (switchAnimator!!.duration > 0) {
            switchAnimator?.addUpdateListener {
                originalAlpha = it.animatedValue as Float

                if (iv_original == null) {
                    return@addUpdateListener
                }
                iv_original.imageAlpha = (originalAlpha * 255).toInt()
                iv_original.invalidate()
            }
            switchAnimator?.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    if (originalAlpha == 0f) {
                        iv_original.visibility = View.INVISIBLE
                    }
                }
            })
            switchAnimator?.start()
        }
    }

    private fun showOriginalPhoto() {
        switchAnimator?.cancel()
        iv_original.visibility = View.VISIBLE
        switchAnimator = ValueAnimator.ofFloat(originalAlpha, 1f)
        switchAnimator!!.duration = (1000f * (1f - originalAlpha)).toLong()
        if (switchAnimator!!.duration > 0) {
            switchAnimator!!.addUpdateListener {
                originalAlpha = it.animatedValue as Float
                iv_original.imageAlpha = (originalAlpha * 255).toInt()
                iv_original.invalidate()
            }
            switchAnimator!!.start()
        }
    }

}