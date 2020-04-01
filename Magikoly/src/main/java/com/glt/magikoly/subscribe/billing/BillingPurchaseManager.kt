package com.glt.magikoly.subscribe.billing

import android.app.Activity
import com.glt.magikoly.AppsFlyProxy
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.BaseSeq59OperationStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.subscribe.SubscribeProxy
import com.glt.magikoly.subscribe.view.AbsSubscribeView
import com.glt.magikoly.utils.Machine
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
 * @fuction 主要负责与Google play同步逻辑
 */
class BillingPurchaseManager private constructor() {


    private val mBillingCallbackList: CopyOnWriteArrayList<IBillingCallback> = CopyOnWriteArrayList()
    private val mCurrentOrderList: CopyOnWriteArrayList<BillingOrder> = CopyOnWriteArrayList()

    init {
        mBillingCallbackList.add(VIPBillingCallback())
        mBillingCallbackList.add(SyncBillingCallback())
    }

    fun syncOrderStatus() {
        if (Machine.isNetworkOK(FaceAppState.getContext())) {
            SubscribeProxy.getInstance().getSubscribeDataOperator().beginTransaction()
            SubscribeProxy.getInstance().getSubscribeDataOperator().clearTable()
            mCurrentOrderList.clear()
//            purchasesResult.purchasesList.forEach {
//                val billingOrder = assembleBillingOrder(it)
//                SubscribeProxy.getInstance().getSubscribeDataOperator().addBillingOrder(billingOrder)
//                mCurrentOrderList.add(billingOrder)
//                BaseSeq59OperationStatistic.uploadPurchaseSync(it.sku, 0, "", billingOrder.orderId)
//            }
            SubscribeProxy.getInstance().getSubscribeDataOperator().setTransactionSuccessfully()
            SubscribeProxy.getInstance().getSubscribeDataOperator().endTransaction()
        } else {
            syncBillingOrdersFromDB()
        }
        for (callback in mBillingCallbackList) {
            callback.onSyncBillingOrderFinished(mCurrentOrderList)
        }
    }

    private fun syncBillingOrdersFromDB() {
        SubscribeProxy.getInstance().getSubscribeDataOperator().beginTransaction()
        val allBillingOrders = SubscribeProxy.getInstance().getSubscribeDataOperator().queryAllBillingOrders()
        mCurrentOrderList.clear()
        mCurrentOrderList.addAll(allBillingOrders)
        SubscribeProxy.getInstance().getSubscribeDataOperator().setTransactionSuccessfully()
        SubscribeProxy.getInstance().getSubscribeDataOperator().endTransaction()
    }

    fun subscribeViewPurchase(absSubscribeView: AbsSubscribeView, activity: Activity, productID: String, entrance: Int) {
        AppsFlyProxy.trackPurchaseClick()

        val billingOrder = assembleBillingOrder()
        billingOrder.entrance = entrance
        mCurrentOrderList.add(billingOrder)
        onPurchase(billingOrder)
        SubscribeProxy.getInstance().onPurchaseSuccess(billingOrder)

        BaseSeq59OperationStatistic.uploadPurchaseSuccess(billingOrder.skuId,
                absSubscribeView.getScene(),
                absSubscribeView.getStyleId(), billingOrder.orderId)
    }

    private fun onPurchase(billingOrder: BillingOrder) {
        if (mBillingCallbackList.isEmpty()) {
            return
        }

        for (callback in mBillingCallbackList) {
            callback.onPurchased(billingOrder)
        }
    }

    private fun onBillingError(productID: String, code: Int, styleId: String, sceneId: String) {
        BaseSeq103OperationStatistic.uploadSqe103StatisticData("2",
                Statistic103Constant.SUBS_SUCCESSFAIL,
                sceneId, styleId, code.toString())

        if (mBillingCallbackList.isEmpty()) {
            return
        }
        for (callBack in mBillingCallbackList) {
            callBack.onBillingError(productID, code)
        }
    }

    private fun assembleBillingOrder(): BillingOrder {
        return BillingOrder("", "", "", 0, 0)
    }


    /**
     * 添加GP信息回调
     */
    fun addBillingCallback(callback: IBillingCallback) {
        if (!mBillingCallbackList.contains(callback)) {
            mBillingCallbackList.add(callback)
            callback.onInitialized()
        }
    }

    /**
     * 移除回调
     */
    fun removeBillingCallback(callback: IBillingCallback) {
        if (mBillingCallbackList.isEmpty()) {
            return
        }

        if (mBillingCallbackList.contains(callback)) {
            mBillingCallbackList.remove(callback)
        }
    }

    companion object {
        fun getInstance() = Holder.instance
    }

    object Holder {
        val instance = BillingPurchaseManager()
    }
}