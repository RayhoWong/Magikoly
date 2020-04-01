package com.glt.magikoly.deamon;

import android.content.Context;
import android.content.Intent;
import com.cs.bd.daemon.DaemonClient;
import com.cs.bd.daemon.DaemonConfigurations;
import com.glt.magikoly.FaceEnv;

/**
 * Created by kingyang on 2016/8/8.
 */
public class DaemonSdkProxy {
    public static void init(Context context) {
        //重要：以下接入代码，建议在业务代码之前执行，守护效果更好
        //设置测试模式，打开LOG
        if (FaceEnv.sSIT) {
            DaemonClient.getInstance().setDebugMode();
        }
        //初始化DaemonClient
        DaemonClient.getInstance().init(createDaemonConfigurations());
        DaemonClient.getInstance().onAttachBaseContext(context);
    }

    /**
     * 构建守护配置
     *
     * @return
     */
    private static DaemonConfigurations createDaemonConfigurations() {
        //构建被守护进程配置信息
        DaemonConfigurations.DaemonConfiguration configuration1 =
                new DaemonConfigurations.DaemonConfiguration(
                        FaceEnv.PROCESS_NAME, DaemonService.class.getCanonicalName(),
                        DaemonReceiver.class.getCanonicalName());
        //构建辅助进程配置信息
        DaemonConfigurations.DaemonConfiguration configuration2 =
                new DaemonConfigurations.DaemonConfiguration(
                        FaceEnv.PROCESS_DAEMON_ASSISTANT,
                        AssistantService.class.getCanonicalName(),
                        AssistantReceiver.class.getCanonicalName());
        //listener can be null
        DaemonConfigurations configs = new DaemonConfigurations(configuration1, configuration2);
        //开启守护效果统计
        configs.setStatisticsDaemonEffect(true);
        //设置唤醒常驻服务轮询时长
//        configs.setDaemonWatchInterval(60);
        return configs;
    }

    public static void startDaemonService(Context context) {
        try {
            context.startService(new Intent(context, DaemonService.class));
        } catch (Exception e) { //OPPO手机在这里会崩溃
            //do nothing
        }
    }

    public static void enableDaemon(Context context) {
        DaemonClient.getInstance().setDaemonPermiiting(context, true);
    }

    public static void disableDaemon(Context context) {
        DaemonClient.getInstance().setDaemonPermiiting(context, false);
        context.stopService(new Intent(context, DaemonService.class));
    }
}
