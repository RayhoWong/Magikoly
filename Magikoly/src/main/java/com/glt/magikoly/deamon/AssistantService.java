package com.glt.magikoly.deamon;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.cs.bd.daemon.NotificationAssistService;

/**
 * Created by kingyang on 2016/8/8.
 */
public class AssistantService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();

        //设置为前台服务，降低被杀几率。参数里的两个服务必须配置在同一进程内。
//        DaemonClient.getInstance().setForgroundService(this, InnerAssistantService.class);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    /**
     * 内部服务，用于设置前台进程
     */
    public static class InnerAssistantService extends NotificationAssistService {
    }
}
