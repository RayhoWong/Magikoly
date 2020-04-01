package com.glt.magikoly.utils

object ViewUtils {

    private const val CLICK_TIME = 500 //快速点击间隔时间

    private var lastClickTime: Long = 0

    // 判断按钮是否快速点击
    fun isFastClick(): Boolean {
        val time = System.currentTimeMillis()
        if (time - lastClickTime < CLICK_TIME) {//判断系统时间差是否小于点击间隔时间
            return true
        }
        lastClickTime = time
        return false
    }
}