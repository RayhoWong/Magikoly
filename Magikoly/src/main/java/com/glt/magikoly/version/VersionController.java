package com.glt.magikoly.version;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import com.glt.magikoly.FaceAppState;
import com.glt.magikoly.pref.PrefConst;
import com.glt.magikoly.pref.PrivatePreference;
import com.glt.magikoly.statistic.BaseSeq19OperationStatistic;
import com.glt.magikoly.utils.Logcat;

/**
 * @author yangguanxiang
 */
public class VersionController {

    public static final int TIMEUNIT_SECOND = 1001;
    public static final int TIMEUNIT_MIN = 1002;
    public static final int TIMEUNIT_HOUR = 1003;
    public static final int TIMEUNIT_DAY = 1004;

    //	private boolean mHadPayFlag = false; // 是否已经付费

    private static boolean sIsFirstRun;
    private static int sLastVersionCode;
    private static int sCurrentVersionCode = -1;
    private static boolean sIsNewVersionFirstRun;
    private static Boolean sIsNewUser;
    private static boolean sIsInited;

    VersionController() {
    }

    /**
     * 获取上一个版本的versionCode，只在安装或更新桌面首次运行时生效，否则与当前versionCode相同
     *
     * @return
     */
    public static int getLastVersionCode() {
        return sLastVersionCode;
    }

    /**
     * 安装或更新桌面首次运行
     *
     * @return
     */
    public static boolean isNewVersionFirstRun() {
        return sIsNewVersionFirstRun;
    }

    /**
     * 获取当前versionCode
     *
     * @return
     */
    public static int getCurrentVersionCode() {
        if (sCurrentVersionCode == -1) {
            Context context = FaceAppState.getContext();
            PackageManager pm = context.getPackageManager();
            try {
                PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
                sCurrentVersionCode = info.versionCode;
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return sCurrentVersionCode;
    }

    public static void init() {
        if (sIsInited) {
            return;
        }
        checkFirstRun();
        if (!sIsFirstRun) {
            checkNewVersionFirstRun();
        } else {
            onFirstRun();
        }
        if (sIsNewVersionFirstRun) {
            onNewVersionFirstRun();
        }


        sIsInited = true;
        Logcat.i("VersionController", "sFirstRun: " + isFirstRun());
        Logcat.i("VersionController", "sNewVersionFirstRun: " + isNewVersionFirstRun());
        Logcat.i("VersionController", "sIsNewUser: " + isNewUser());
        Logcat.i("VersionController", "sLastVersionCode: " + getLastVersionCode());
        Logcat.i("VersionController", "sCurrentVersionCode: " + getCurrentVersionCode());
    }


    /**
     * 检测是否为第一次运行
     */
    private static void checkFirstRun() {
        Context context = FaceAppState.getContext();
        PrivatePreference preference = PrivatePreference.getPreference(context);
        sIsFirstRun = preference.getInt(PrefConst.KEY_LAST_VERSION_CODE, -1) == -1;
    }


    private static void onFirstRun() {
        PrivatePreference pref = PrivatePreference.getPreference(FaceAppState.getContext());
        pref.putInt(PrefConst.KEY_LAST_VERSION_CODE, getCurrentVersionCode());
        pref.putLong(PrefConst.KEY_FIRST_RUN_TIME, System.currentTimeMillis());
        pref.commit();
        sIsNewVersionFirstRun = true;
        saveIsNewUserPref(true);
    }

    //安装桌面首次运行
    public static boolean isFirstRun() {
        return sIsFirstRun;
    }

    /**
     * <br>功能简述:检查是否是该版本第一次运行
     * <br>功能详细描述:
     * <br>注意:
     */
    private static void checkNewVersionFirstRun() {
        Context context = FaceAppState.getContext();
        PrivatePreference pref = PrivatePreference.getPreference(context);
        sLastVersionCode = pref.getInt(PrefConst.KEY_LAST_VERSION_CODE, 0);
        int curVersionCode = getCurrentVersionCode();
        if (curVersionCode != -1 && curVersionCode != sLastVersionCode) {
            sIsNewVersionFirstRun = true;
            pref.putInt(PrefConst.KEY_LAST_VERSION_CODE, curVersionCode);
            pref.commit();
        }
    }

    private static void onNewVersionFirstRun() {
        PrivatePreference pref = PrivatePreference.getPreference(FaceAppState.getContext());
        if (sLastVersionCode > 0) {
            // 如果上次版本号大于0，证明该用户是升级用户，如果是全新用户，上次版本号为0
            // 这里保存该用户不是新用户，因为上次版本号不是0，已经运行过了
            saveIsNewUserPref(false);
            BaseSeq19OperationStatistic.uploadBasicInfo();
        }
    }

    //是否是全新用户
    public static boolean isNewUser() {
        if (sIsNewUser == null) {
            Context context = FaceAppState.getContext();
            PrivatePreference sharedPreferences = PrivatePreference.getPreference(context);
            sIsNewUser = sharedPreferences.getBoolean(PrefConst.KEY_IS_NEW_USER, true);
        }
        return sIsNewUser;
    }

    //记录当前用户是否是全新用户
    private static void saveIsNewUserPref(boolean isNewUser) {
        sIsNewUser = isNewUser;
        Context context = FaceAppState.getContext();
        PrivatePreference sharedPreferences = PrivatePreference.getPreference(context);
        sharedPreferences.putBoolean(PrefConst.KEY_IS_NEW_USER, isNewUser);
        sharedPreferences.commit();
    }

    /**
     * 获取当前GO桌面版本号
     */
    public static int getVersionCode() {
        try {
            Context c = FaceAppState.getContext();
            PackageInfo info = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            return info.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static int getCdays() {
        int cdays = 1;
        PrivatePreference pref = PrivatePreference.getPreference(FaceAppState.getContext());
        long firstRunTime = pref.getLong(PrefConst.KEY_FIRST_RUN_TIME, 0);
        if (firstRunTime > 0) {
            long diff = System.currentTimeMillis() - firstRunTime;
            cdays = Math.round(diff / 1000 / 86400);
            if (cdays < 1) {
                cdays = 1;
            } else {
                cdays += 1;
            }
            Logcat.d("xiaowu_install", "cday: " + cdays + " diff: " + diff / 1000 / 86400);
        }
        return cdays;
    }


    /**
     * 获取具体首次启动的时间
     *
     * @param timeUnit 时间类型：秒、分、时、天
     * @return 相应类型的时间间隔
     */
    public static float getFirstRunInterval(int timeUnit) {
        float interval = 0;
        int time = 1;
        PrivatePreference pref = PrivatePreference.getPreference(FaceAppState.getContext());
        long firstRunTime = pref.getLong(PrefConst.KEY_FIRST_RUN_TIME, 0);
        if (firstRunTime > 0) {
            long diff = System.currentTimeMillis() - firstRunTime;
            switch (timeUnit) {
                case TIMEUNIT_SECOND:
                    time = 1;
                    break;
                case TIMEUNIT_MIN:
                    time = 60;
                    break;
                case TIMEUNIT_HOUR:
                    time = 3600;
                    break;
                case TIMEUNIT_DAY:
                    time = 86400;
                    break;
                default:
                    break;
            }
            interval = diff / 1000 / time;
        }
        return interval;
    }

    /**
     * App进入次数
     */
    public static void saveEnterCount() {
        PrivatePreference pre = PrivatePreference.getPreference(FaceAppState.getContext());
        long current = getAppEnterCount();
        current += 1;
        pre.putLong(PrefConst.KEY_APP_ENTER_TIMES, current);
        pre.commit();
    }

    public static long getAppEnterCount() {
        return PrivatePreference.getPreference(FaceAppState.getContext())
                .getLong(PrefConst.KEY_APP_ENTER_TIMES, 0);
    }
}
