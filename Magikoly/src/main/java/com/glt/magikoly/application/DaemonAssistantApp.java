package com.glt.magikoly.application;

import android.content.Context;
import com.glt.magikoly.deamon.DaemonSdkProxy;

/**
 * 守护进程App
 *
 * @author yangguanxiang
 */
public class DaemonAssistantApp extends BaseApplication {

    public DaemonAssistantApp(String processName) {
        super(processName);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public boolean isMainProcess() {
        return false;
    }


    @Override
    public void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        DaemonSdkProxy.init(base);
    }

    @Override
    public void stopSdk(boolean stop) {
        DaemonSdkProxy.disableDaemon(getApplicationContext());
    }
}
