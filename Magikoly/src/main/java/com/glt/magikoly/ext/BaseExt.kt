package com.glt.magikoly.ext

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.thread.FaceThreadExecutorProxy
import com.glt.magikoly.utils.BitmapUtils
import com.glt.magikoly.utils.Duration
import com.glt.magikoly.utils.JsonHelper
import com.glt.magikoly.utils.Logcat
import com.google.gson.reflect.TypeToken
import org.greenrobot.eventbus.EventBus

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

fun postEvent(event: Any) {
    EventBus.getDefault().post(event)
}

fun registerEventObserver(any: Any) {
    EventBus.getDefault().register(any)
}

fun unregisterEventObserver(any: Any) {
    EventBus.getDefault().unregister(any)
}

fun Any.log(log: String = "${this}", tag: String = "magikoly") {
    Logcat.w(tag, "${this::class.java.name} = " + log)
}

fun l(log: String = "", tag: String = "lzh") {
    Logcat.w(tag, "log= " + log)
}

fun ltime(msg: Any, startTime: Long = 0, tag: String = "lzh"): Long {
    val currentTimeMillis = System.currentTimeMillis()
    Logcat.d(tag, msg.toString() + "=${currentTimeMillis - startTime}")
    return currentTimeMillis
}

fun getAppContext(): Context {
    return FaceAppState.getContext()
}

/**
 * 对象转Json
 */
inline fun <reified T : Any> T.toJson(): String {
    return JsonHelper.getInstance().gson().toJson(this, T::class.java)
}

/**
 * Json转对象
 */
inline fun <reified T> String.parse(): T {
    return JsonHelper.getInstance().gson().fromJson(this, T::class.java)
}


/**
 * Json转对象
 */
inline fun <reified T> String.parseList(): T {
    return JsonHelper.getInstance().gson().fromJson<T>(this, object : TypeToken<T>() {}.type)
}

fun runMain(delay: Long = 0, main: () -> Unit) {
    FaceThreadExecutorProxy.runOnMainThread({
        main()
    }, delay)
}

fun runAsync(async: () -> Unit) {
    FaceThreadExecutorProxy.execute{ async() }
}

fun runAsyncInSingleThread(delay: Long = 0, async: () -> Unit) {
    FaceThreadExecutorProxy.runOnAsyncThread(async, delay)
}

fun <T> runTask(async: () -> T, main: (T) -> Unit) {
    FaceThreadExecutorProxy.execute {
        val result = async()
        FaceThreadExecutorProxy.runOnMainThread {
            main(result)
        }
    }
}


fun ImageView.recyclerTag(context: Context, id: Int, urlBig: String) {
    this.getTag(id)?.let {
        if ((it as String) != urlBig) {
            Glide.with(context).clear(this)
        }
    }
    this.setTag(id, urlBig)
}

fun Bitmap.toByteArray(): ByteArray {
    return BitmapUtils.toByteArray(this)
}
