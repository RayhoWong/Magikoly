package com.glt.magikoly.function.resultreport.baby

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.glt.magikoly.BuyChannelApiProxy
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.constants.ErrorCode
import com.glt.magikoly.event.PredictionRequestSuccess
import com.glt.magikoly.event.ReportErrorEvent
import com.glt.magikoly.ext.postEvent
import com.glt.magikoly.function.FaceFunctionBean
import com.glt.magikoly.function.FaceFunctionManager
import com.glt.magikoly.function.ReportNames
import com.glt.magikoly.function.innerpick.InnerPickFragment
import com.glt.magikoly.function.main.ISubscribe
import com.glt.magikoly.function.main.ITabFragment
import com.glt.magikoly.function.main.ToolBarCallback
import com.glt.magikoly.function.poster.ISavePhoto
import com.glt.magikoly.function.resultreport.FaceReportFragment
import com.glt.magikoly.function.resultreport.TabInfo
import com.glt.magikoly.mvp.BaseSupportFragment
import com.glt.magikoly.permission.OnPermissionResult
import com.glt.magikoly.permission.PermissionSaveController
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.IStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.subscribe.SubscribeController
import com.glt.magikoly.view.GlobalProgressBar
import kotlinx.android.synthetic.main.fragment_baby_report.*
import magikoly.magiccamera.R
import java.util.*

class BabyReportFragment : BaseSupportFragment<BabyReportPresenter>(),
        IStatistic, ITabFragment, ISubscribe, ISavePhoto, BabyReportView {

    private var mUseFreeCount: Boolean = false
    override var watchAdFinish: Boolean = false
    override var fromClick = false

    private var resultBitmap: Bitmap? = null

    private var parentBitmapLeft: Bitmap? = null
    private var parentBitmapRight: Bitmap? = null
    private var faceBeanLeft: FaceFunctionBean? = null
    private var faceBeanRight: FaceFunctionBean? = null

    private var chooseParentLeft: Boolean = false
    private var chooseParentRight: Boolean = false

    private var bitmapLeftSet = false
    private var bitmapRightSet = false


    companion object {
        const val REQUEST_CODE_GET_PHOTO = 0X001
        fun newInstance(): BabyReportFragment {
            return BabyReportFragment()
        }
    }

    private var mCurrentStatus: Int = -1

    override fun getEntrance(): String = Statistic103Constant.ENTRANCE_BABY

    override fun getTabCategory(): String = ""

    override fun getTabId(): Int = TabInfo.SUB_TAB_BABY

    override fun getToolBarTitle(): String = ""

    override fun getToolBarBackCallback(): ToolBarCallback? = null

    override fun getToolBarSelfCallback(): ToolBarCallback? = null
    override fun createPresenter(): BabyReportPresenter = BabyReportPresenter()

    private var mCurrentError = -1

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
                                        mPresenter.saveBabyReport(parentBitmapLeft,
                                                parentBitmapRight, resultBitmap)
//                            savePoster(getTabId(), getString(R.string.poster_result_title_aging),
//                                    getString(R.string.poster_result_desc_aging),
//                                    FaceFunctionManager.faceBeanMap[FaceFunctionManager.currentFaceImagePath]!!.photo.getBitmap(), it,
//                                    BitmapFactory.decodeResource(resources, R.drawable.poster_bg_aging))
                                    }
                                }
                            }, entrance)
                    return true
                }
                return false
            }
        }
    }

    override fun getToolBarBackDrawable(): Drawable? {
        return FaceAppState.getContext().resources.getDrawable(R.drawable.icon_back_selector)
    }

    override fun getToolBarMenuDrawable(): Drawable? {
        return if (FaceFunctionManager.demoFaceImageInfo == null) {
            if (resultBitmap != null) {
                FaceAppState.getContext().resources.getDrawable(
                        R.drawable.icon_save_black_selector)
            } else {
                null
            }
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
        if (FaceFunctionManager.demoFaceImageInfo == null) {
            if (!SubscribeController.getInstance().isVIP(SubscribeController.PERMISSION_WITH_TRIAL) && resultBitmap != null && !watchAdFinish && !SubscribeController.getInstance().isFreeCount() && !mUseFreeCount) {
                startBlurReport()
                BaseSeq103OperationStatistic.uploadSqe103StatisticData("",
                        Statistic103Constant.FAKEREPORT_ENTER, entrance, tabCategory,
                        FaceReportFragment.isDefaultEnter(entrance), BuyChannelApiProxy.getCampaign())
            } else {
//                if (resultBitmap != null) {
//                    mPresenter.startAnalysis(faceBeanLeft, faceBeanRight)
//                }
                stopBlurReport()
                if (fromClick) {
                    mCurrentError = -1 //需重置，要不然结果回来后还是
                    chooseParentRight = true
                    InnerPickFragment.newInstance(this.parentFragment as BaseSupportFragment<*>,
                            REQUEST_CODE_GET_PHOTO, entrance, _mActivity.resources.getString(R.string.inner_pick_title))
                } else {
                    if (mCurrentError != -1 && resultBitmap != null) {
                        mPresenter.startAnalysis(faceBeanLeft, faceBeanRight)
                        showLoading(entrance)
                    } else if (mCurrentError == ErrorCode.FATHER_GENDER_FAIL
                            || mCurrentError == ErrorCode.MOTHER_GENDER_FAIL) {
                        showErrorDialog(mCurrentError)
                        mCurrentError = -1
                    } else {
                        if (!mUseFreeCount && !SubscribeController.getInstance().isVIP(SubscribeController.PERMISSION_WITH_TRIAL) && resultBitmap != null && !watchAdFinish ) {
                            mUseFreeCount = true
                            SubscribeController.getInstance().subFreeCount()
                        }
                    }

                }

            }
        } else {
            stopBlurReport()
        }
    }

    override fun getStatus(): Int {
        return mCurrentStatus
    }

    override fun setStatus(status: Int) {
        mCurrentStatus = status
    }

    override fun getFilePrefix(): String = ReportNames.AGING_REPORT_PREFIX

    override fun getGPColor(): Int = Color.WHITE

    override fun getTabLock(): Boolean = FaceFunctionManager.demoFaceImageInfo == null
            && !SubscribeController.getInstance().isVIP(SubscribeController.PERMISSION_WITH_TRIAL) && !watchAdFinish

    override fun onTabFragmentVisible() {
        reload()
        if (resultBitmap == null) {
            BaseSeq103OperationStatistic.uploadSqe103StatisticData("",
                    Statistic103Constant.BABY_INITIAL_ENTER, "", "",
                    FaceReportFragment.isDefaultEnter(entrance), BuyChannelApiProxy.getCampaign())
        }
    }

    override fun onTabFragmentInvisible() {
        hideErrorEvent()
    }

    private fun stopBlurReport() {
        setStatus(ITabFragment.STATUS_OPEN)
        postEvent(ReportErrorEvent())
        if (!getTabLock()) {
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_baby_report, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        choose_parent_left.setOnClickListener(this)
        choose_parent_right.setOnClickListener(this)
        if (FaceFunctionManager.demoFaceImageInfo == null) {
            val faceBean = FaceFunctionManager.faceBeanMap[FaceFunctionManager.currentFaceImagePath]
            faceBeanLeft = faceBean
            parentBitmapLeft = faceBean?.face?.getBitmap()
        } else {
            faceBeanLeft = FaceFunctionBean().apply {
                faceInfo = FaceFunctionManager.demoFaceImageInfo?.faceInfo!!
                imageInfo = FaceFunctionManager.demoFaceImageInfo?.imageInfo!!
            }
            parentBitmapLeft = (resources.getDrawable(
                    FaceFunctionManager.demoFaceImageInfo!!.imgId) as BitmapDrawable).bitmap
            when (FaceFunctionManager.demoFaceImageInfo!!.imgId) {
                R.drawable.demo1 -> {
                    parentBitmapRight = (resources.getDrawable(R.drawable.demo2) as BitmapDrawable).bitmap
                    resultBitmap = (resources.getDrawable(R.drawable.demo_baby1) as BitmapDrawable).bitmap
                }
                R.drawable.demo2 -> {
                    val random = Random()
                    if (random.nextInt(2) == 0) {
                        parentBitmapRight = (resources.getDrawable(
                                R.drawable.demo1) as BitmapDrawable).bitmap
                        resultBitmap = (resources.getDrawable(
                                R.drawable.demo_baby1) as BitmapDrawable).bitmap
                    } else {
                        parentBitmapRight = (resources.getDrawable(
                                R.drawable.demo3) as BitmapDrawable).bitmap
                        resultBitmap = (resources.getDrawable(
                                R.drawable.demo_baby2) as BitmapDrawable).bitmap
                    }
                }
                R.drawable.demo3 -> {
                    parentBitmapRight = (resources.getDrawable(R.drawable.demo2) as BitmapDrawable).bitmap
                    resultBitmap = (resources.getDrawable(R.drawable.demo_baby2) as BitmapDrawable).bitmap
                }
            }
        }
        val male = "M" == faceBeanLeft?.faceInfo?.gender
        if (male) {
            fragment_baby_left.setText(R.string.fragment_baby_name_dad)
            fragment_baby_right.setText(R.string.fragment_baby_name_mom)
        } else {
            fragment_baby_left.setText(R.string.fragment_baby_name_mom)
            fragment_baby_right.setText(R.string.fragment_baby_name_dad)
        }
        parent_photo_left.setImageBitmap(parentBitmapLeft)
        parent_photo_right.setImageBitmap(parentBitmapRight)
        baby_photo.setImageBitmap(resultBitmap)
        bitmapLeftSet = true
        if (isVisible && GlobalProgressBar.isShown()) {
            hideLoading()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.choose_parent_right -> {
                if (FaceFunctionManager.demoFaceImageInfo == null) {
                    chooseParentRight = true
                    BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                            Statistic103Constant.PARTNER_CLICK, "")
                    InnerPickFragment.newInstance(this.parentFragment as BaseSupportFragment<*>,
                            REQUEST_CODE_GET_PHOTO, entrance,
                            _mActivity.resources.getString(R.string.inner_pick_title))
                }
            }
        }
    }

    override fun onFragmentResult(requestCode: Int, resultCode: Int, data: Bundle?) {
        super.onFragmentResult(requestCode, resultCode, data)

        mUseFreeCount = false

        if (chooseParentRight) {
            data?.let {
                val imgPath = it.getString(InnerPickFragment.RESULT_ORIGINAL_PATH)
                val faceBean = it.getParcelable<FaceFunctionBean>(
                        InnerPickFragment.RESULT_FACE_BEAN)
                FaceFunctionManager.faceBeanMap[imgPath] = faceBean
                parentBitmapRight = faceBean?.face?.getBitmap()
                faceBeanRight = faceBean
                parent_photo_right.setImageBitmap(parentBitmapRight)
            }
            chooseParentRight = false
            bitmapRightSet = true
        }

        if (bitmapLeftSet && bitmapRightSet) {
            if (isSameGender()) {//性别相同就别检查了
                if (faceBeanLeft?.faceInfo?.gender == "M") {
                    showErrorDialog(ErrorCode.MOTHER_GENDER_FAIL)
                } else {
                    showErrorDialog(ErrorCode.FATHER_GENDER_FAIL)
                }
            } else {
                resultBitmap = null
                baby_photo?.setImageBitmap(resultBitmap)
                watchAdFinish = false
                postEvent(ReportErrorEvent())
                mPresenter.startAnalysis(faceBeanLeft, faceBeanRight)
                showLoading(entrance)
            }
        }
    }

    override fun showBabyImage(url: String?) {
        Glide.with(FaceAppState.getContext()).asBitmap().load(url).into(object : SimpleTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                postEvent(PredictionRequestSuccess(getTabId()))
                resultBitmap = resource
                if (parentFragment is FaceReportFragment) {
                    val faceReportFragment = parentFragment as FaceReportFragment
                    faceReportFragment.getCurrentTabFragment()?.apply {
                        faceReportFragment.refreshToolBar(this)
                    }
                }
                baby_photo?.setImageBitmap(resource)
                hideLoading()
                reload()
            }
        })
    }


    private fun isSameGender(): Boolean {
        var left = faceBeanLeft?.faceInfo?.gender
        var right = faceBeanRight?.faceInfo?.gender

        if (!TextUtils.isEmpty(left) && !TextUtils.isEmpty(right)) {
            return left == right
        }
        return true
    }

    override fun showErrorDialog(errorCode: Int) {
        mCurrentError = errorCode
        hideLoading()
        showErrorEvent(errorCode)
    }

    override fun showLoading(entrance: String) {
        GlobalProgressBar.show(entrance)
    }

    override fun hideLoading() {
        GlobalProgressBar.hide()
    }
}