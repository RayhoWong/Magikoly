package com.glt.magikoly.subscribe.billing

import com.glt.magikoly.event.UserStatusEvent
import com.glt.magikoly.ext.postEvent
import com.glt.magikoly.pref.PrefConst
import com.glt.magikoly.pref.PrefDelegate
import com.glt.magikoly.utils.Logcat

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
 * @fuction 主要负责用户状态的管理
 */
class BillingStatusManager private constructor() {

    private val mStaticVIPIds = emptyList<String>()
    private var mBillingStatus: Int by PrefDelegate(PrefConst.KEY_BILLING_STATUS, STATUS_NORMAL)
    private var mBillingNetVIPIds: String by PrefDelegate(PrefConst.KEY_BILLING_NET_VIP_IDS, "")
    private var mBillingRateClickTime: Long by PrefDelegate(PrefConst.KEY_BILLING_RATE_CLICK_TIME, -1)
    private var mBillingShareTime: Long by PrefDelegate(PrefConst.KEY_BILLING_SHARE_TIME, -1)

    private var mBillingFreeCount: Int by PrefDelegate(PrefConst.KEY_BILLING_FREE_COUNT, 0)


    fun isVIPProductId(productId: String): Boolean {
        if (productId in mStaticVIPIds) {
            return true
        }
        val netVIPIds = mBillingNetVIPIds.split(",")
        if (netVIPIds.isEmpty()) {
            return false
        }

        if (productId in netVIPIds) {
            return true
        }
        return false
    }

    fun saveVIPProductId(productIds: List<String>) {
        val saveProductIds = ArrayList<String>()
        val netVIPIds = mBillingNetVIPIds.split(",").filter { return@filter it != "" }
        for (productId in productIds) {
            for (productId in saveProductIds) {
                continue
            }
            if (productId in mStaticVIPIds) {
                continue
            }
            if (productId in netVIPIds) {
                continue
            }
            saveProductIds.add(productId)
        }
        saveProductIds.addAll(netVIPIds)
        mBillingNetVIPIds = saveProductIds.joinToString(",")
    }

    fun registerVIP() {
        mBillingStatus = STATUS_VIP
        postEvent(UserStatusEvent(STATUS_VIP))
    }

    fun unregisterVIP() {
        mBillingStatus = STATUS_NORMAL
        postEvent(UserStatusEvent(STATUS_NORMAL))
    }

    fun isVIP(): Boolean {
        return mBillingStatus == STATUS_VIP
//                return true
    }

    fun recordRateClickTime() {
        if (isClickTrialVIP()) {
            return
        }
        mBillingRateClickTime = System.currentTimeMillis()
    }

    fun recordShareTime() {
        if (isClickTrialVIP()) {
            return
        }
        mBillingShareTime = System.currentTimeMillis()
    }

    fun isClickedRate(): Boolean {
        return mBillingRateClickTime.toInt() != -1
    }

    fun recordFreeCount(freeCount: Int) {
        mBillingFreeCount = freeCount
        Logcat.e("FreeCount", "count = " + mBillingFreeCount)
    }

    fun subFreeCount() {
        mBillingFreeCount -= 1

        Logcat.e("FreeCount", "count = " + mBillingFreeCount)
    }

    fun haveFreeCount(): Boolean {
        return mBillingFreeCount > 0
    }

    fun isClickTrialVIP(): Boolean {
        if (mBillingRateClickTime.toInt() != -1 &&
                System.currentTimeMillis() - mBillingRateClickTime < CLICK_TIME_TRIAL_INTERVAL) {
            return true
        }
        if (mBillingShareTime.toInt() != -1 &&
                System.currentTimeMillis() - mBillingShareTime < CLICK_TIME_TRIAL_INTERVAL) {
            return true
        }
        return false
    }

    companion object {
        const val STATUS_NORMAL = 0
        const val STATUS_VIP = 1
        private const val CLICK_TIME_TRIAL_INTERVAL = 24 * 60 * 60 * 1000

        fun getInstance() = Holder.instance
    }

    object Holder {
        val instance = BillingStatusManager()
    }
}