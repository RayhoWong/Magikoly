package com.glt.magikoly.subscribe

import android.app.Activity
import com.glt.magikoly.subscribe.billing.BillingStatusManager

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
 * @time 2019/1/17
 * @tips 这个类是Object的子类
 * @fuction
 */

/**
 * 对外接口提供类
 * 整合支付与用户状态判断相关
 */
class SubscribeController private constructor() {

    companion object {
        fun getInstance() = Holder.instance

        const val PERMISSION_WITH_TRIAL = 1
    }

    object Holder {
        val instance = SubscribeController()
    }

    fun isVIP(vararg permissions: Int): Boolean {
//        return true
        if (BillingStatusManager.getInstance().isVIP()) {
            return true
        }
        for (permission in permissions) {
            if (permission == PERMISSION_WITH_TRIAL) {
                if (BillingStatusManager.getInstance().isClickTrialVIP()) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 判断用户是否是VIP
     */
//    fun isVIP(): Boolean {
//        if (BillingStatusManager.getInstance().isVIP()) {
//            return true
//        }
//        return false
////        return true
//    }

    /**
     * 是否存在免费次数
     */
    fun isFreeCount():Boolean{
        return BillingStatusManager.getInstance().haveFreeCount()
    }

    fun subFreeCount(){
        BillingStatusManager.getInstance().subFreeCount()
    }


    /**
     * 启动订阅
     *
     * @param listener : 继承SubscribeProxy.BaseListener, 内部自动在不需要时移除订阅监听
     */
    fun launch(context: Activity, @SubscribeScene sceneId: Int,
               listener: SubscribeProxy.BaseListener?) {
        SubscribeProxy.getInstance().launchSubscribe(context, sceneId, listener)
    }

    /**
     * 添加订阅回调监听
     */
    fun addSubscribeListener(listener: SubscribeProxy.Listener) {
        SubscribeProxy.getInstance().addSubscribeListener(listener)
    }

    /**
     * 移除订阅监听
     */
    fun removeSubscribeListener(listener: SubscribeProxy.Listener) {
        SubscribeProxy.getInstance().removeSubscribeListener(listener)
    }
}