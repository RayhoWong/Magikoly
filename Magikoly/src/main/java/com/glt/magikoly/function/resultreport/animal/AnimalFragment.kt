package com.glt.magikoly.function.resultreport.animal

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Dialog
import android.graphics.*
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.opengl.GLES20
import android.os.Bundle
import android.os.Environment
import android.view.*
import android.view.animation.LinearInterpolator
import android.widget.PopupWindow
import android.widget.Toast
import com.glt.magikoly.BuyChannelApiProxy
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.SafelyBitmap
import com.glt.magikoly.config.AgingShutterConfigBean
import com.glt.magikoly.config.AnimalConfigBean
import com.glt.magikoly.constants.ErrorCode.FACE_NOT_FOUND
import com.glt.magikoly.dialog.WaitDialog
import com.glt.magikoly.event.PredictionRequestSuccess
import com.glt.magikoly.event.PredictionResultSaveEvent
import com.glt.magikoly.event.ReportErrorEvent
import com.glt.magikoly.ext.postEvent
import com.glt.magikoly.ext.runAsync
import com.glt.magikoly.ext.runMain
import com.glt.magikoly.function.FaceFunctionManager
import com.glt.magikoly.function.ReportNames
import com.glt.magikoly.function.main.ISubscribe
import com.glt.magikoly.function.main.ITabFragment
import com.glt.magikoly.function.main.ToolBarCallback
import com.glt.magikoly.function.poster.ISavePhoto
import com.glt.magikoly.function.resultreport.FaceReportFragment
import com.glt.magikoly.function.resultreport.TabInfo.CREATOR.SUB_TAB_ANIMAL
import com.glt.magikoly.function.resultreport.animal.presenter.AnimalPresenter
import com.glt.magikoly.gifmaker.GifMaker
import com.glt.magikoly.mvp.BaseSupportFragment
import com.glt.magikoly.opengl.AbsGLThread
import com.glt.magikoly.opengl.GLRenderController
import com.glt.magikoly.opengl.GLSurface
import com.glt.magikoly.opengl.egl.EGLEnvironment
import com.glt.magikoly.opengl.renderer.AnimalRenderer
import com.glt.magikoly.opengl.video.ExportVideoConfig
import com.glt.magikoly.opengl.video.ExportVideoManager
import com.glt.magikoly.permission.OnPermissionResult
import com.glt.magikoly.permission.PermissionSaveController
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.IStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.subscribe.SubscribeController
import com.glt.magikoly.subscribe.SubscribeProxy
import com.glt.magikoly.thread.FaceThreadExecutorProxy
import com.glt.magikoly.utils.BitmapUtils
import com.glt.magikoly.utils.DrawUtils
import com.glt.magikoly.utils.PersonDetectUtils
import com.glt.magikoly.utils.ToastUtils
import com.glt.magikoly.view.GlobalProgressBar
import magikoly.magiccamera.R
import kotlinx.android.synthetic.main.fragment_animal.*
import java.io.File
import java.nio.IntBuffer
import java.util.*
import javax.microedition.khronos.opengles.GL10
import kotlin.math.max


class AnimalFragment : BaseSupportFragment<AnimalPresenter>(), IAnimalView, IStatistic,
        ITabFragment, ISubscribe, ISavePhoto {

    companion object {
        private const val HUMAN_BITMAP_KEY = "human_bitmap_key"
        private const val HUMAN_LANDMARK_KEY = "human_landmark_key"
        private const val ANIMAL_BITMAP_KEY = "animal_bitmap_key"
        private const val ANIMAL_LANDMARK_KEY = "animal_landmark_key"

        fun newInstance(): AnimalFragment {
            return AnimalFragment()
        }
    }

    private var mExportVideoManager: ExportVideoManager? = null

    private var humanBitmap: SafelyBitmap? = null
    private var humanLandmark: HashMap<String, List<Point>>? = null
    private var animalBitmap: SafelyBitmap? = null
    private var animalLandmark: HashMap<String, List<Point>>? = null

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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        humanBitmap?.let {
            outState.putParcelable(HUMAN_BITMAP_KEY, it)
        }
        animalBitmap?.let {
            outState.putParcelable(ANIMAL_BITMAP_KEY, it)
        }
        humanLandmark?.let {
            outState.putSerializable(HUMAN_LANDMARK_KEY, it)
        }
        animalLandmark?.let {
            outState.putSerializable(ANIMAL_LANDMARK_KEY, it)
        }
    }

    override fun restoreInstanceState(outState: Bundle?) {
        if (humanBitmap == null) {
            outState?.getParcelable<SafelyBitmap>(HUMAN_BITMAP_KEY)?.let {
                humanBitmap = it
            }
        }
        if (animalBitmap == null) {
            outState?.getParcelable<SafelyBitmap>(ANIMAL_BITMAP_KEY)?.let {
                animalBitmap = it
            }
        }
        if (humanLandmark == null) {
            outState?.getSerializable(HUMAN_LANDMARK_KEY)?.let {
                humanLandmark = it as HashMap<String, List<Point>>
            }
        }
        if (animalLandmark == null) {
            outState?.getSerializable(ANIMAL_LANDMARK_KEY)?.let {
                animalLandmark = it as HashMap<String, List<Point>>
            }
        }
    }

    override fun getTabId(): Int = SUB_TAB_ANIMAL

    override fun getToolBarTitle(): String = ""

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

    override fun getToolBarBackCallback(): ToolBarCallback? = null

    private var mIsCancelMP4: Boolean = false

    override fun getToolBarMenuCallback(): ToolBarCallback? {
        return object : ToolBarCallback {
            override fun invoke(): Boolean {
                if (FaceFunctionManager.demoFaceImageInfo == null) {

                    PermissionSaveController.requestPermission(activity,
                            object : OnPermissionResult {
                                override fun onPermissionDeny(permission: String?, never: Boolean) {

                                }

                                override fun onPermissionGrant(permission: String?) {


                                    val popupWindow = PopupWindow(layoutInflater.inflate(R.layout.popup_animal_save, null), ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                                    popupWindow.isOutsideTouchable = true
                                    popupWindow.animationStyle = R.style.mypopwindow_anim_style

                                    val location = IntArray(2)
                                    view!!.getLocationInWindow(location)


                                    popupWindow.contentView.findViewById<View>(R.id.video).setOnClickListener {
                                        if (sv_animal == null) {
                                            return@setOnClickListener
                                        }
                                        BaseSeq103OperationStatistic.uploadSqe103StatisticData("",
                                                Statistic103Constant.SAVEPHOTO_CLICK, entrance, "2",
                                                FaceReportFragment.isDefaultEnter(entrance),
                                                BuyChannelApiProxy.getCampaign())

                                        val waitDialog = WaitDialog(activity!!, Statistic103Constant.ENTRANCE_SAVE_VIDEO)
                                        waitDialog.setContent(R.string.exporting_video)
                                        waitDialog.show()
                                        mIsCancelMP4 = false
                                        waitDialog.setOnCancelListener {
                                            mIsCancelMP4 = true
                                            cancelGenerateMP4()
                                        }
                                        generateMP4({
                                            waitDialog.dismiss()
                                            if (!mIsCancelMP4) {
                                                ToastUtils.showToast(R.string.exporting_video_failed, Toast.LENGTH_SHORT)
                                            }
                                        }) {
                                            postEvent(PredictionResultSaveEvent(getTabId(), true, false))

                                            waitDialog.dismiss()
                                            ToastUtils.showToast(R.string.video_saved_to_gallery, Toast.LENGTH_SHORT)
                                        }
                                        popupWindow.dismiss()
                                    }

                                    popupWindow.contentView.findViewById<View>(R.id.gif).setOnClickListener {
                                        if (sv_animal == null) {
                                            return@setOnClickListener
                                        }
                                        BaseSeq103OperationStatistic.uploadSqe103StatisticData("",
                                                Statistic103Constant.SAVEPHOTO_CLICK, entrance, "3",
                                                FaceReportFragment.isDefaultEnter(entrance),
                                                BuyChannelApiProxy.getCampaign())

                                        saveGif()
                                        popupWindow.dismiss()
                                    }
                                    popupWindow.showAtLocation(view, Gravity.START or Gravity.TOP, resources.displayMetrics.widthPixels - DrawUtils.dip2px(25f) - DrawUtils.dip2px(124f), location[1] - DrawUtils.dip2px(15f))


//                                    saveGif()
//
//                                    generateMP4 { path ->
//                                        MediaScannerConnection.scanFile(FaceAppState.getContext(), arrayOf(path), null
//                                        ) { p, uri ->
//                                            postEvent(PredictionResultSaveEvent(getTabId(), true))
//                                        }
//                                    }
                                }
                            }, entrance)
                    return true
                } else {
                    return false
                }
            }
        }
    }

    private var mIsSavingGif = false

    private fun saveGif() {
        val waitDialog = WaitDialog(activity!!, Statistic103Constant.ENTRANCE_SAVE_GIF)
        waitDialog.setContent(R.string.exporting_gif)
        waitDialog.show()
        waitDialog.setOnCancelListener {
            mIsSavingGif = false
            mGlRendererController.removeSurface(mPuffSurface)
            mPuffSurface = null
        }
        if (mPuffSurface != null) {
            mGlRendererController.removeSurface(mPuffSurface!!)
        }
        mWindowSurface?.apply {
            mPuffSurface = GLSurface(viewport.width,
                    viewport.height)
            mGlRendererController.addSurface(mPuffSurface!!)
        }

        mIsSavingGif = true
        generateGIF(getTabId(), mGlRendererController, mPuffSurface!!, {
            mGlRendererController.removeSurface(mPuffSurface)
            mPuffSurface = null
            waitDialog.setNotCancel(R.string.exporting_gif_please_wait)

        }) { success: Boolean, path: String ->
            waitDialog.dismiss()
            if (success) {
                mIsSavingGif = false

                postEvent(PredictionResultSaveEvent(getTabId(), true, false))

                ToastUtils.showToast(R.string.gif_saved_to_gallery, Toast.LENGTH_SHORT)
            } else {
                ToastUtils.showToast(R.string.exporting_gif_failed, Toast.LENGTH_SHORT)
            }
        }
    }

    override fun getToolBarSelfCallback(): ToolBarCallback? = null

    override fun getToolBarItemColor(): Int? {
        return FaceAppState.getContext().resources.getColor(R.color.toolbar_title_dark_color)
    }

    override fun getBottomBarTitle(): String {
        return FaceAppState.getContext().resources.getString(R.string.animal)
    }

    override fun getBottomBarIcon(): Array<Drawable>? = null

    override fun getEntrance(): String = Statistic103Constant.ENTRANCE_ANIMAL

    override fun getTabCategory(): String = AgingShutterConfigBean.getRequestType()

    override fun createPresenter(): AnimalPresenter = AnimalPresenter()

    private fun isDataLoaded(): Boolean {
        return !(animalBitmap == null || animalLandmark == null || humanBitmap == null
                || humanLandmark == null)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_animal, null)
    }

    private lateinit var mGlRendererController: GLRenderController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreInstanceState(savedInstanceState)
        initAnimalRender()

//        human_view.setImageBitmap(humanBitmap?.getBitmap())
//        result_view.setImageBitmap(animalBitmap?.getBitmap())
//        contour_view.landmark = humanLandmark
//        contour_view.invalidate()
    }

    override fun onTabFragmentVisible() {
        if (FaceFunctionManager.demoFaceImageInfo == null) {
            if (!SubscribeController.getInstance().isVIP(
                            SubscribeController.PERMISSION_WITH_TRIAL) && animalBitmap == null && !SubscribeController.getInstance().isFreeCount()) {
                GlobalProgressBar.hide()
                startBlurReport()
                BaseSeq103OperationStatistic.uploadSqe103StatisticData("",
                        Statistic103Constant.FAKEREPORT_ENTER, entrance, tabCategory,
                        FaceReportFragment.isDefaultEnter(entrance),
                        BuyChannelApiProxy.getCampaign())
            } else {
                if (!isDataLoaded()) {
                    loadData()
                } else {
                    stopBlurReport()
                }
            }
        } else {
            if (!isDataLoaded()) {
                loadData()
            } else {
                stopBlurReport()
            }
        }
    }

    override fun onTabFragmentInvisible() {
    }

    override fun reload() {
        loadData()
    }

    override fun getFilePrefix(): String = ReportNames.ANIMAL_REPORT_PREFIX

    override fun getGPColor(): Int = 0

    override fun onFaceDetectSuccess(landmark: HashMap<String, List<Point>>, feature: FloatArray?,
                                     startTime: Long) {
        humanLandmark = landmark
//        contour_view?.landmark = humanLandmark
//        contour_view?.invalidate()
        sv_animal?.let { view ->
            val viewWidth = view.width.toFloat()
            val viewHeight = view.height.toFloat()
            if (feature != null) {
                mPresenter.loadAnimal(viewWidth, viewHeight, feature, startTime)
            } else {
                if (FaceFunctionManager.demoFaceImageInfo != null) {
                    mPresenter.loadAnimal(viewWidth, viewHeight,
                            FaceFunctionManager.demoFaceImageInfo?.faceInfo?.gender!!,
                            FaceFunctionManager.demoFaceImageInfo?.animal!!, startTime)
                } else {
                    mPresenter.loadAnimal(viewWidth, viewHeight, feature, startTime)
                }
            }
        }
    }

    override fun onFaceDetectError(errorCode: Int) {
        if (isVisible) {
            GlobalProgressBar.hide()
            showErrorEvent(errorCode)
        }
    }

    override fun onAnimalLoadCompleted(image: Bitmap,
                                       landmark: HashMap<String, List<Point>>) {
//        result_view?.setImageBitmap(image)

        animalBitmap = SafelyBitmap(image)
        animalLandmark = landmark


        val maxWidth = max(animalBitmap?.getBitmap()?.width ?: 0,
                humanBitmap?.getBitmap()?.width ?: 0)
        val maxHeight = max(animalBitmap?.getBitmap()?.height ?: 0,
                humanBitmap?.getBitmap()?.height ?: 0)
        //保证动物和人类两张Bitmap的大小一致
        val humanRawBitmap = humanBitmap?.getBitmap()!!
        if (humanRawBitmap.width < maxWidth || humanRawBitmap.height < maxHeight) {
            val fixedBitmap = createFixedBitmap(humanRawBitmap, maxWidth, maxHeight)
            humanRawBitmap.recycle()
            humanBitmap = SafelyBitmap(fixedBitmap)
        }
        val animalRawBitmap = animalBitmap?.getBitmap()!!
        if (animalRawBitmap.width < maxWidth || animalRawBitmap.height < maxHeight) {
            val fixedBitmap = createFixedBitmap(animalRawBitmap, maxWidth, maxHeight)
            animalRawBitmap.recycle()
            animalBitmap = SafelyBitmap(fixedBitmap)
        }


//        contour_view?.landmark = animalLandmark
//        contour_view?.invalidate()
        postEvent(PredictionRequestSuccess(getTabId()))
        if (isVisible) {
            stopBlurReport()
            GlobalProgressBar.hide()
            initHumanRenderData()
            initAnimalRenderData()

        }
    }

    private fun createFixedBitmap(src: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val base = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(base)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.isFilterBitmap = true
        val l = (targetWidth - src.width) / 2f
        val t = (targetHeight - src.height) / 2f
        val r = l + src.width
        val b = t + src.height
        val rect = RectF(l, t, r, b)
        canvas.drawBitmap(src, null, rect, paint)
        return base
    }


    private var mToAnimalValueAnimator: ValueAnimator? = null
    private var mToHumanValueAnimator: ValueAnimator? = null

    private var mIsToAnimalCancel: Boolean = false
    private var mIsToHumanCancel: Boolean = false

    private val ANIM_INTERVAL = 50
    private val ANIM_DURATION: Long = 3000

    private fun initAnimalAnim() {
        if (mToAnimalValueAnimator == null) {
            mToAnimalValueAnimator = ValueAnimator.ofInt(0, ANIM_INTERVAL)
            mToAnimalValueAnimator?.apply {
                interpolator = LinearInterpolator()
                duration = ANIM_DURATION
                addUpdateListener {
                    setAnimalCurrentProgress(it)
                }
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        if (mIsToAnimalCancel) {
                            return
                        }
                        FaceThreadExecutorProxy.runOnMainThread({
                            startToHumanAnim()
                        }, 1000)
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                        mIsToAnimalCancel = true
                    }

                    override fun onAnimationStart(animation: Animator?) {
                        mIsToAnimalCancel = false
                    }
                })
            }
        }

        if (mToHumanValueAnimator == null) {
            mToHumanValueAnimator = ValueAnimator.ofInt(ANIM_INTERVAL, 0)
            mToHumanValueAnimator?.apply {
                interpolator = LinearInterpolator()
                duration = ANIM_DURATION
                addUpdateListener {
                    setAnimalCurrentProgress(it)
                }
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        if (mIsToHumanCancel) {
                            return
                        }
                        FaceThreadExecutorProxy.runOnMainThread({
                            startToAnimalAnim()
                        }, 1000)
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                        mIsToHumanCancel = true
                    }

                    override fun onAnimationStart(animation: Animator?) {
                        mIsToHumanCancel = false
                    }
                })
            }
        }
    }

    private fun startToAnimalAnim() {
        if (mToHumanValueAnimator?.isRunning == false
                && mToAnimalValueAnimator?.isRunning == false) {
            mToAnimalValueAnimator?.start()
        }
    }

    private fun startToHumanAnim() {
        if (mToHumanValueAnimator?.isRunning == false
                && mToAnimalValueAnimator?.isRunning == false) {
            mToHumanValueAnimator?.start()
        }
    }

    private fun setAnimalCurrentProgress(it: ValueAnimator) {
        if (mIsSavingGif) {
            return
        }
        val value = it.animatedValue as Int
        if (checkGLController()) {
            mGlRendererController.postRenderRunnable {
                (mGlRendererController.renderer as AnimalRenderer).setProgress(value * 1f / ANIM_INTERVAL)
            }
        }
    }

    override fun onAnimalLoadFailed(errorCode: Int) {
        if (isVisible) {
            GlobalProgressBar.hide()
            showErrorEvent(errorCode)
        }
    }


    private fun loadData() {
        if (isDataLoaded()) {
            return
        }
        if (mPresenter.isLoading) {
            GlobalProgressBar.show(entrance)
            return
        }
        GlobalProgressBar.show(entrance)
        sv_animal?.let { view ->
            val humanSrcBitmap = if (FaceFunctionManager.demoFaceImageInfo != null) {
                val options = BitmapFactory.Options()
                options.inScaled = false
                BitmapFactory.decodeResource(resources, FaceFunctionManager.demoFaceImageInfo!!.imgId, options)
//                (resources.getDrawable(FaceFunctionManager.demoFaceImageInfo!!.imgId) as BitmapDrawable).bitmap
            } else {
                FaceFunctionManager.faceBeanMap[FaceFunctionManager.currentFaceImagePath]?.face?.getBitmap()
            }
            runMain(1000) {
                runAsync {
                    if (humanSrcBitmap == null) {
                        onFaceDetectError(FACE_NOT_FOUND)
                        return@runAsync
                    }

                    var target = BitmapUtils.centerScaleBitmapForViewSize(humanSrcBitmap,
                            view.width.toFloat(), view.height.toFloat())
                    if (FaceFunctionManager.demoFaceImageInfo != null) {
                        humanBitmap = SafelyBitmap(target)
                        val xOffset = (humanSrcBitmap!!.width / 2f).toInt()
                        val yOffset = (humanSrcBitmap.height / 2f).toInt()
                        val scale = 1f * target.width / humanSrcBitmap.width
                        runMain {
                            mPresenter.loadDemoAnimal(FaceFunctionManager.demoFaceImageInfo!!,
                                    xOffset, yOffset, scale)
                        }
                    } else {
                        target = PersonDetectUtils.portraitSeparation(target)
                        if (target == null) {
                            runMain {
                                onFaceDetectError(FACE_NOT_FOUND)
                            }
                        } else {
                            humanBitmap = SafelyBitmap(target)
                            runMain {
                                mPresenter.detectFace(target)
                            }
                        }
                    }
                }
            }
        }
    }

    private var mWindowSurface: GLSurface? = null
    private var mPuffSurface: GLSurface? = null
    private var mSurfaceCallback: SurfaceHolder.Callback? = null

    private fun initAnimalRender() {
        mGlRendererController = GLRenderController(EGLEnvironment(), AnimalRenderer(_mActivity))
        mGlRendererController.startRender()
        if (mSurfaceCallback == null) {
            mSurfaceCallback = object : SurfaceHolder.Callback {
                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                    if (checkGLController()) {
                        if (mWindowSurface == null) {
                            mWindowSurface = GLSurface(holder.surface, width, height)
                        } else {
                            mWindowSurface?.set(holder.surface, width, height)
                        }

                        mWindowSurface?.let {
                            mGlRendererController.addSurface(it)
                            initHumanRenderData()
                            initAnimalRenderData()
                            mGlRendererController.requestRender()
                        }
                    }
                }

                override fun surfaceDestroyed(holder: SurfaceHolder?) {
                }

                override fun surfaceCreated(holder: SurfaceHolder?) {
                }
            }
        }
        sv_animal?.holder?.removeCallback(mSurfaceCallback)
        sv_animal?.holder?.addCallback(mSurfaceCallback)
        sv_animal?.visibility = View.VISIBLE
    }

    private fun showErrorEvent(errorCode: Int) {
        setStatus(ITabFragment.STATUS_ERROR)
        postEvent(ReportErrorEvent(errorCode))
    }

    private fun hideErrorEvent() {
        setStatus(ITabFragment.STATUS_OPEN)
        postEvent(ReportErrorEvent())
    }

    private fun startBlurReport() {
        sv_animal?.visibility = View.INVISIBLE
        setStatus(ITabFragment.STATUS_PURCHASE)
        postEvent(ReportErrorEvent())
    }

    private fun stopBlurReport() {
        sv_animal?.visibility = View.VISIBLE
        hideErrorEvent()
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

    private fun initAnimalRenderData() {
        if (checkGLController() && animalBitmap?.getBitmap() != null) {
            mGlRendererController.postRenderCallback(object : AbsGLThread.Callback {
                override fun renderBefore() {
                    setRendererAnimalData(mGlRendererController.renderer as AnimalRenderer)
                }

                override fun renderAfter() {
                    mGlRendererController.render()
                    FaceThreadExecutorProxy.runOnMainThread({
                        initAnimalAnim()
                        startToAnimalAnim()
                    }, 100)
                }
            })
        }
    }

    private fun setRendererAnimalData(animalRenderer: AnimalRenderer) {
        animalRenderer.initTargetFrameBuffer(animalBitmap?.getBitmap(),
                convertLandMarkToFloatArray(animalLandmark ?: HashMap(),
                        animalBitmap?.getBitmap()?.width ?: 0,
                        animalBitmap?.getBitmap()?.height ?: 0))
        animalRenderer.setProgress(0f)
    }

    private fun initHumanRenderData() {
        if (checkGLController() && humanBitmap?.getBitmap() != null) {
            mGlRendererController.postRenderRunnable {
                setRendererHumanData((mGlRendererController.renderer as AnimalRenderer))
            }
        }
    }

    private fun setRendererHumanData(animalRenderer: AnimalRenderer) {
        animalRenderer.initSrcFrameBuffer(humanBitmap?.getBitmap(),
                convertLandMarkToFloatArray(humanLandmark ?: HashMap(),
                        humanBitmap?.getBitmap()?.width ?: 0,
                        humanBitmap?.getBitmap()?.height ?: 0))
        animalRenderer.setProgress(0f)
    }

    private fun checkGLController(): Boolean {
        if (mGlRendererController.renderer is AnimalRenderer) {
            return true
        }
        return false
    }

    private fun convertLandMarkToFloatArray(landmark: HashMap<String, List<Point>>, width: Int, height: Int): FloatArray {
        val result = ArrayList<Float>()
        for (key in AnimalConfigBean.AnimalConfig.KEY_ARRAY) {
            landmark[key]?.forEach {
                val x = width / 2f + it.x.toFloat()
                val y = height / 2f - it.y.toFloat()
                result.add(x)
                result.add(y)
            }
        }
        return result.toFloatArray()
    }

    override fun onStart() {
        super.onStart()
        if (mIsStop) {
            initAnimalRender()
            mIsStop = false
        }
    }


    private var mIsStop: Boolean = false

    override fun onStop() {
        super.onStop()
        if (checkGLController()) {
            mIsStop = true
            mGlRendererController.stopRender()
            sv_animal?.visibility = View.INVISIBLE
        }
    }

    override fun onDestroy() {
        if (checkGLController()) {
            mGlRendererController.release()
        }
        super.onDestroy()
    }

    private fun cancelGenerateMP4() {
        mExportVideoManager?.apply {
            stopMerge()
            releaseEncoder()
        }
        mExportVideoManager = null
    }

    private fun generateMP4(onError: () -> Unit, onFinished: (String) -> Unit) {
        cancelGenerateMP4()
        val basePath = Environment.getExternalStorageDirectory().absolutePath + File.separator + Environment.DIRECTORY_PICTURES
        val path = basePath + File.separator + getFilePrefix() + System.currentTimeMillis() + ".mp4"
        val renderer = AnimalRenderer(_mActivity)
        mExportVideoManager = ExportVideoManager()
        val builder = ExportVideoConfig.newBuilder()
                .setContext(_mActivity)
                .setDuration(10f)
                .setFps(24)
                .setOutputWidth(sv_animal.width)
                .setOutputHeight(sv_animal.width)
                .setOutputPath(path)
                .setListener(object : ExportVideoConfig.onExportVideoListener {
                    override fun release() {
                        renderer.onDestroy()
//                        cancelGenerateMP4()
                    }

                    override fun onError(throwable: Throwable?) {
                        onError()
                    }

                    override fun onCreate() {
                    }

                    override fun onMakeCurrent(width: Int, height: Int) {
                        renderer.onCreated()
                        renderer.onSurfaceChanged(width, height)

                        setRendererHumanData(renderer)
                        setRendererAnimalData(renderer)
                    }

                    override fun onDrawFrame(frame: Int, progress: Float) {
                        val currentProgress = progress / 100f
                        if (currentProgress >= 0 && currentProgress <= 0.4f) {
                            renderer.setProgress(currentProgress / 0.4f)
                        } else if (currentProgress > 0.4f && currentProgress <= 0.5f) {
                            renderer.setProgress(1f)
                        } else if (currentProgress > 0.5 && currentProgress <= 0.9f) {
                            val p = (currentProgress - 0.5f) / 0.4f
                            renderer.setProgress((1 - p))
                        } else if (currentProgress > 0.9 && currentProgress <= 1f) {
                            renderer.setProgress(0f)
                        }
                        renderer.onDrawFrame(null)

                        if (progress >= 99) {
                            onFinished(path)
                        }
                    }
                })
        mExportVideoManager?.setConfig(builder.build())
        mExportVideoManager?.init()
        mExportVideoManager?.startMerge()
    }

    fun generateGIF(tabId: Int,
                    glRenderController: GLRenderController,
                    puffSurface: GLSurface,
                    onBeginCompossGIF: () -> Unit,
                    onFinish: (success: Boolean, path: String) -> Unit = { s, p -> }) {
        val result: HashMap<Int, Bitmap> = HashMap()
        getAFrame(glRenderController, puffSurface, 0f) { bitmap: Bitmap?, finish: Boolean, progress: Int ->
            if (bitmap != null) {
                result.put(progress, bitmap)
                if (finish) {
                    onBeginCompossGIF()
                    val gifMaker = GifMaker(150)
                    val basePath = Environment.getExternalStorageDirectory().absolutePath + File.separator + Environment.DIRECTORY_PICTURES
                    val path = basePath + File.separator + getFilePrefix() + System.currentTimeMillis() + ".gif"
                    val gifArray = ArrayList<Bitmap>()
                    if (result.size >= 10) {
                        gifArray.add(result.get(0)!!)
                        gifArray.add(result.get(1)!!)
                        gifArray.add(result.get(2)!!)
                        gifArray.add(result.get(3)!!)
                        gifArray.add(result.get(4)!!)
                        gifArray.add(result.get(5)!!)
                        gifArray.add(result.get(6)!!)
                        gifArray.add(result.get(7)!!)
                        gifArray.add(result.get(8)!!)
                        gifArray.add(result.get(9)!!)
                        gifArray.add(result.get(10)!!)
                        gifArray.add(result.get(10)!!)
                        gifArray.add(result.get(9)!!)
                        gifArray.add(result.get(8)!!)
                        gifArray.add(result.get(7)!!)
                        gifArray.add(result.get(6)!!)
                        gifArray.add(result.get(5)!!)
                        gifArray.add(result.get(4)!!)
                        gifArray.add(result.get(3)!!)
                        gifArray.add(result.get(2)!!)
                        gifArray.add(result.get(1)!!)
                        gifArray.add(result.get(0)!!)
                        gifMaker.makeGifInThread(gifArray, path) {
                            MediaScannerConnection.scanFile(FaceAppState.getContext(), arrayOf(it), null
                            ) { path, uri ->
                                postEvent(PredictionResultSaveEvent(tabId, true))
                                onFinish(true, path)
                                for (bitmap in gifArray) {
                                    bitmap.recycle()
                                }
                            }
                        }
                    }
                }
            } else {
                for (entry in result) {
                    result[entry.key]?.recycle()
                }
            }
        }
    }

    private fun getAFrame(glRenderController: GLRenderController, puffSurface: GLSurface, progress: Float, onFinish: (Bitmap?, Boolean, Int) -> Unit) {
        if (mIsSavingGif) {
            glRenderController.postRenderCallback(object : AbsGLThread.Callback {
                override fun renderBefore() {
                    if (mIsSavingGif) {
                        (glRenderController.renderer as AnimalRenderer).setProgress(progress)
                    }
                }

                override fun renderAfter() {
                    if (mIsSavingGif) {
                        getCurrentPuffBitmap(glRenderController, puffSurface) {
                            if (mIsSavingGif) {
                                if (progress >= 1f) {
                                    onFinish(it, true, (progress * 10).toInt())
                                } else {
                                    onFinish(it, false, (progress * 10).toInt())
                                    getAFrame(glRenderController, puffSurface, progress + 0.1f, onFinish)
                                }
                            }
                        }
                    } else {
                        onFinish(null, true, 0)
                    }
                }
            })
        }
    }


    private fun getCurrentPuffBitmap(glRenderController: GLRenderController,
                                     puffSurface: GLSurface,
                                     finish: (Bitmap?) -> Unit) {
        var bitmap: Bitmap? = null
        glRenderController.postRunnable {
            puffSurface?.let {
                val ib = IntBuffer.allocate(it.viewport.width * it.viewport.height)
                GLES20.glReadPixels(
                        0,
                        0,
                        it.viewport.width,
                        it.viewport.height,
                        GL10.GL_RGBA,
                        GL10.GL_UNSIGNED_BYTE,
                        ib
                )

                bitmap = BitmapUtils.compressByScale(
                        frameToBitmap(it.viewport.width, it.viewport.height, ib)
                        , it.viewport.width / 4, it.viewport.height / 4)
                finish(bitmap)
            }
        }
    }


    /**
     * 将数据转换成bitmap(OpenGL和Android的Bitmap色彩空间不一致，这里需要做转换)
     *
     * @param width 图像宽度
     * @param height 图像高度
     * @param ib 图像数据
     * @return bitmap
     */
    private fun frameToBitmap(width: Int, height: Int, ib: IntBuffer): Bitmap {
        val pixs = ib.array()
        // 扫描转置(OpenGl:左上->右下 Bitmap:左下->右上)
        for (y in 0 until height / 2) {
            for (x in 0 until width) {
                val pos1 = y * width + x
                val pos2 = (height - 1 - y) * width + x

                val tmp = pixs[pos1]
                pixs[pos1] = pixs[pos2] and -0xff0100 or (pixs[pos2] shr 16 and 0xff) or
                        (pixs[pos2] shl 16 and 0x00ff0000) // ABGR->ARGB
                pixs[pos2] = tmp and -0xff0100 or (tmp shr 16 and 0xff) or (tmp shl 16 and 0x00ff0000)
            }
        }
        if (height % 2 == 1) { // 中间一行
            for (x in 0 until width) {
                val pos = (height / 2 + 1) * width + x
                pixs[pos] = pixs[pos] and -0xff0100 or (pixs[pos] shr 16 and 0xff) or (pixs[pos] shl 16 and 0x00ff0000)
            }
        }

        return Bitmap.createBitmap(pixs, width, height, Bitmap.Config.ARGB_4444)
    }

}