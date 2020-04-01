package com.glt.magikoly;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.glt.magikoly.application.ApplicationProxy;
import com.glt.magikoly.data.DatabaseNames;
import com.glt.magikoly.data.PersistenceManager;

import java.lang.ref.WeakReference;


public class FaceAppState {

    private static Application sApp;
    private static ApplicationProxy sAppProxy;
    private static WeakReference<MagikolyActivity> sMainActivity;

    public static void init(Application app) {
        if (sApp != null) {
            return;
        }
        sApp = app;
    }

    public static Context getContext() {
        return sApp.getApplicationContext();
    }

    public static Context getBaseContext() {
        return sAppProxy.getBaseContext();
    }

    public static Application getApplication() {
        return sApp;
    }

    public static ApplicationProxy getApplicationProxy() {
        return sAppProxy;
    }

    public static MagikolyActivity getMainActivity() {
        if (sMainActivity != null) {
            return sMainActivity.get();
        }
        return null;
    }

    public static void setApplicationProxy(ApplicationProxy proxy) {
        sAppProxy = proxy;
    }


    public static void setMainActivity(MagikolyActivity activity) {
        if (activity == null) {
            throw new IllegalArgumentException("can not be set null");
        }
        sMainActivity = new WeakReference<>(activity);
    }

    public static void restart() {
        sAppProxy.finishAllActivities();
        Context context = getContext();
        final Intent intent =
                context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void exit() {
        PersistenceManager.closeDB(DatabaseNames.DB_FACE);
        sAppProxy.finishAllActivities();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
