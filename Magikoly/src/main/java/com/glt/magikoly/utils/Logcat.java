package com.glt.magikoly.utils;


import android.util.Log;

/**
 * Created by kingyang on 2016/12/21.
 */

public class Logcat {
    private static boolean sIsEnable;

    public static void setEnable(boolean enable) {
        sIsEnable = enable;
    }

    public static void v(String tag, String msg) {
        if (sIsEnable) {
            Log.v(tag, msg);
        }
    }

    public static void v(String tag, String msg, Throwable t) {
        if (sIsEnable) {
            Log.v(tag, msg, t);
        }
    }

    public static void d(String tag, String msg) {
        if (sIsEnable) {
            Log.d(tag, msg);
        }
    }

    public static void d(String tag, String msg, Throwable t) {
        if (sIsEnable) {
            Log.d(tag, msg, t);
        }
    }

    public static void i(String tag, String msg) {
        if (sIsEnable) {
            Log.i(tag, msg);
        }
    }

    public static void i(String tag, String msg, Throwable t) {
        if (sIsEnable) {
            Log.i(tag, msg, t);
        }
    }

    public static void w(String tag, String msg) {
        if (sIsEnable) {
            Log.w(tag, msg);
        }
    }

    public static void w(String tag, String msg, Throwable t) {
        if (sIsEnable) {
            Log.w(tag, msg, t);
        }
    }

    public static void e(String tag, String msg) {
        if (sIsEnable) {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable t) {
        if (sIsEnable) {
            Log.e(tag, msg, t);
        }
    }
}
