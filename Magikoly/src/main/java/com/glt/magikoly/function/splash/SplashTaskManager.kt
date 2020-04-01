package com.glt.magikoly.function.splash

import android.os.CountDownTimer

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
 * @time 2019/1/14
 * @tips 这个类是Object的子类
 * @fuction
 */
/**
 * 启动页任务管理类
 * 7秒之内加载的数据和任务
 */
class SplashTaskManager private constructor() {

    companion object {
        const val PRELOAD_KEY_SPLASH_ANIMATION = "preload_key_splash_animation"
        const val PRELOAD_KEY_SUBSCRIBE_VIDEO = "preload_key_subscribe_video"
        const val PRELOAD_KEY_SUBSCRIBE_BANNER = "preload_key_subscribe_banner"
        const val PRELOAD_KEY_SUBSCRIBE_DATA = "preload_key_subscribe_data"
        const val PRELOAD_KEY_AD = "preload_key_ad"
        const val PRELOAD_KEY_LAUNCHING_PLAN = "preload_key_launching_plan"
        const val PRELOAD_KEY_BUYCHANNEL = "preload_key_buychannel"

        private const val FULL_COUNT_TIME: Long = 12000
        private const val INTERVAL_COUNT_TIME: Long = 500

        fun getInstance() = Holder.instance
    }

    object Holder {
        val instance = SplashTaskManager()
    }

    private val mTaskList: ArrayList<ISplashTask> = ArrayList()

    private val mTaskCounter: TaskCounter = TaskCounter(mTaskList)

    fun addSplashTask(newTask: ISplashTask) {
        for (task in mTaskList) {
            if (task.preloadKey() == newTask.preloadKey()) {
                return
            }
        }
        mTaskList.add(newTask)
    }

    fun start() {
        for (task in mTaskList) {
            task.startPreload()
        }
        startCount()
    }

    fun cancel() {
        mTaskCounter.cancel()
        mTaskCounter.onFinish()
    }

    fun clear() {
        if (mTaskCounter.taskList.isNotEmpty()) {
            mTaskCounter.taskList.clear()
        }
    }

    private fun startCount() {
        mTaskCounter.start()
    }

    class TaskCounter(val taskList: ArrayList<ISplashTask>) : CountDownTimer(FULL_COUNT_TIME, INTERVAL_COUNT_TIME) {

        override fun onFinish() {
            val taskAllFinished = isTaskAllFinished()
            for (index in taskList.size downTo 1) {
                taskList[index - 1].onCancelPreload(taskAllFinished)
            }
            taskList.clear()
        }

        override fun onTick(millisUntilFinished: Long) {
            for (task in taskList) {
                task.onTick(millisUntilFinished)
            }

            if (isTaskAllFinished()) {
                cancel()
                onFinish()
            }
        }


        private fun isTaskAllFinished(): Boolean {
            var finishCount = 0
            for (task in taskList) {
                if (task.isPreloadFinished()) {
                    finishCount += 1
                }
            }
            return finishCount == taskList.size
        }
    }

}