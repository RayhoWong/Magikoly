package com.glt.magikoly.subscribe

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.data.operator.SubscribeDataOperator
import com.glt.magikoly.function.FaceFunctionBean
import com.glt.magikoly.subscribe.billing.BillingOrder
import com.glt.magikoly.subscribe.billing.BillingPurchaseManager
import com.glt.magikoly.subscribe.billing.BillingStatusManager
import com.glt.magikoly.subscribe.view.AbsSubscribeView
import java.util.concurrent.CopyOnWriteArrayList

/**
 * ┌───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┐
 * │Esc│ │ F1│ F2│ F3│ F4│ │ F5│ F6│ F7│ F8│ │ F9│F10│F11│F12│ │P/S│S L│P/B│ ┌┐    ┌┐    ┌┐
 * └───┘ └───┴───┴───┴───┘ └───┴───┴───┴───┘ └───┴───┴───┴───┘ └───┴───┴───┘ └┘    └┘    └┘
 * ┌──┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───────┐┌───┬───┬───┐┌───┬───┬───┬───┐
 * │~`│! 1│@ 2│# 3│$ 4│% 5│^ 6│& 7│* 8│( 9│) 0│_ -│+ =│ BacSp ││Ins│Hom│PUp││N L│ / │ * │ - │
 * ├──┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─────┤├───┼───┼───┤├───┼───┼───┼───┤
 * │Tab │ Q │ W │ E │ R │ T │ Y │ U │ I │ O │ P │{ [│} ]│ | \ ││Del│End│PDn││ 7 │ 8 │ 9 │   │
 * ├────┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴─────┤└───┴───┴───┘├───┼───┼───┤ + │
 * │Caps │ A │ S │ D │ F │ G │ H │ J │ K │ L │: ;│" '│ Enter  │             │ 4 │ 5 │ 6 │   │
 * ├─────┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴────────┤    ┌───┐    ├───┼───┼───┼───┤
 * │Shift  │ Z │ X │ C │ V │ B │ N │ M │< ,│> .│? /│  Shift   │    │ ↑ │    │ 1 │ 2 │ 3 │   │
 * ├────┬──┴─┬─┴──┬┴───┴───┴───┴───┴───┴──┬┴───┼───┴┬────┬────┤┌───┼───┼───┐├───┴───┼───┤ E││
 * │Ctrl│Ray │Alt │         Space         │ Alt│code│fuck│Ctrl││ ← │ ↓ │ → ││   0   │ . │←─┘│
 * └────┴────┴────┴───────────────────────┴────┴────┴────┴────┘└───┴───┴───┘└───────┴───┴───┘
 *
 * @author Rayhahah
 * @blog http://rayhahah.com
 * @time 2019/1/9
 * @tips 这个类是Object的子类
 * @fuction
 */

class SubscribeProxy private constructor() {

    companion object {
        fun getInstance() = Holder.instance
    }

    object Holder {
        val instance = SubscribeProxy()
    }

    private val mSubscribeListenerList: CopyOnWriteArrayList<Listener> by lazy {
        CopyOnWriteArrayList<Listener>()
    }

    private val mSubscribeDataOperator: SubscribeDataOperator by lazy {
        SubscribeDataOperator(FaceAppState.getContext())
    }

    var mIsHijackkHome: Boolean = false

    fun getSubscribeDataOperator(): SubscribeDataOperator = mSubscribeDataOperator

    private var mCurrentListener: Listener? = null

    private var mCurrentImageOriginal: Bitmap? = null
    private var mFaceFunctionBean: FaceFunctionBean? = null
    fun getCurrentImageOriginal(): Bitmap? = mCurrentImageOriginal
    fun getFaceFunctionBean(): FaceFunctionBean? = mFaceFunctionBean
    fun setCurrentImageOriginal(image: Bitmap?, faceFunctionBean: FaceFunctionBean?) {
        mCurrentImageOriginal = image
        mFaceFunctionBean = faceFunctionBean
    }

    /**
     * 启动订阅
     */
    fun launchSubscribe(context: Context, @SubscribeScene sceneId: Int, listener: Listener?) {
        if (listener != null) {
            mSubscribeListenerList.add(listener)
            if (sceneId != SubscribeScene.LAUNCH_CLOSE) {
                mCurrentListener = listener
            }
        }

        if (BillingStatusManager.getInstance().isVIP()) {
            onShow(false, sceneId)
            return
        }

//        onShow(true, sceneId)
        onShow(false, sceneId)
//        SubscribeActivity.start(context, sceneId, isStartLocal)
    }

    /**
     * 付费
     */
    fun pay(activity: Activity, productId: String, absSubscribeView: AbsSubscribeView) {
        BillingPurchaseManager.getInstance()
                .subscribeViewPurchase(absSubscribeView, activity, productId, absSubscribeView.getScene())
    }


    private fun onShow(isShow: Boolean, scene: Int) {
        if (mSubscribeListenerList.isEmpty()) {
            return
        }

        for (listener in mSubscribeListenerList) {
            listener.onShow(isShow, scene)
        }
    }

    /**
     * 支付成功回调
     */
    fun onPurchaseSuccess(billingOrder: BillingOrder) {
        if (mSubscribeListenerList.isEmpty()) {
            return
        }
        for (index in mSubscribeListenerList.size downTo 1) {
            mSubscribeListenerList[index - 1].onPurchaseSuccess(billingOrder)
        }
    }

    fun onDismiss(absSubscribeView: AbsSubscribeView) {
        if (mSubscribeListenerList.isEmpty()) {
            return
        }
        for (listener in mSubscribeListenerList) {
            listener.onDismiss(absSubscribeView)
        }
    }


    /**
     * 添加订阅回调监听
     */
    fun addSubscribeListener(listener: Listener) {
        if (!mSubscribeListenerList.contains(listener)) {
            mSubscribeListenerList.add(listener)
        }
    }

    /**
     * 移除订阅监听
     */
    fun removeSubscribeListener(listener: Listener) {
        if (mSubscribeListenerList.isEmpty()) {
            return
        }

        if (mSubscribeListenerList.contains(listener)) {
            mSubscribeListenerList.remove(listener)
        }
    }

    /**
     * 会自动清理本身，如果需要自定义清理则重现Listener
     */
    open class BaseListener : Listener {

        override fun onPurchaseSuccess(billingOrder: BillingOrder) {
            SubscribeProxy.getInstance().removeSubscribeListener(this)
        }

        override fun onShow(isShow: Boolean, scene: Int) {
            if (!isShow) {
                SubscribeProxy.getInstance().removeSubscribeListener(this)
            }

        }

        override fun onDismiss(absSubscribeView: AbsSubscribeView) {
            SubscribeProxy.getInstance().removeSubscribeListener(this)
        }

    }

    interface Listener {
        fun onShow(sShow: Boolean, scene: Int)
        fun onDismiss(absSubscribeView: AbsSubscribeView)
        fun onPurchaseSuccess(billingOrder: BillingOrder)
    }
}
