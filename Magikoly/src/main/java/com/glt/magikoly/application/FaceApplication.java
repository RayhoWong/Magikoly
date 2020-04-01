package com.glt.magikoly.application;

import com.glt.magikoly.ScheduleTaskManager;
import com.glt.magikoly.deamon.DaemonSdkProxy;

public class FaceApplication extends BaseApplication implements BaseApplicationDelegate.OnGoogleAdvertisingIdInitListener {

    private String mGoogleAdId = "UNABLE-TO-RETRIEVE";

    public FaceApplication(String processName) {
        super(processName);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DaemonSdkProxy.init(getApplicationContext());
        DaemonSdkProxy.startDaemonService(getApplicationContext());
        mDelegate.initGoogleAdvertisingId(this);
        mDelegate.initAdSDK();
        ScheduleTaskManager.getInstance().startScheduleTasks();
    }

    @Override
    public boolean isMainProcess() {
        return true;
    }

    @Override
    public void onGoogleAdvertisingIdInited(String googleId) {
        mGoogleAdId = googleId;
    }
}
