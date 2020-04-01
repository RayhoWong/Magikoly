package com.glt.magikoly.deamon;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Process;
import com.cs.bd.daemon.DaemonClient;
import com.cs.bd.daemon.NotificationAssistService;
import com.glt.magikoly.constants.ICustomAction;
import com.glt.magikoly.pref.PrefConst;
import com.glt.magikoly.pref.PrivatePreference;
import com.glt.magikoly.statistic.BaseSeq19OperationStatistic;
import com.glt.magikoly.thread.FaceThreadExecutorProxy;
import com.glt.magikoly.utils.AppUtils;
import com.glt.magikoly.utils.Logcat;

/**
 * Created by kingyang on 2016/8/8.
 */
public class DaemonService extends Service {

    private static final String TAG = "DaemonService";
    private final static long EIGHT_HOURS = 8 * 60 * 60 * 1000; // 每隔8小时
    private final static long TWO_HOURS = 2 * 60 * 60 * 1000; // 每隔2小时
    private final static long ONE_MINUTE = 60 * 1000; // 每隔1分钟
    private BroadcastReceiver mReceiver;
    private AlarmManager mAlarmManager;


    @Override
    public void onCreate() {
        super.onCreate();
        //设置为前台服务，降低被杀几率。参数里的两个服务必须配置在同一进程内。
//        DaemonClient.getInstance().setForgroundService(this, InnerDaemonService.class);
        init();
//        new Thread() {
//            @Override
//            public void run() {
//                FunctionPurchaseManager.getInstance(getApplicationContext()).checkAdvertSetting();
//            }
//        }.start();
        startTask();
        Logcat.i(TAG, "DaemonService onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Logcat.i(TAG, "DaemonService onStartCommand");
        //统计守护效果
        DaemonClient.getInstance().statisticsDaemonEffect(this, intent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        Logcat.i(TAG, "DaemonService onDestroy");
    }

    private void init() {
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(ICustomAction.ACTION_UPLOAD_BASIC_STATISTIC)) {
                    startBasicInfoStaticTask(true);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(ICustomAction.ACTION_UPLOAD_BASIC_STATISTIC);
        registerReceiver(mReceiver, filter);
    }

    private void startTask() {
        startBasicInfoStaticTask(false);
    }

    /**
     * <br>
     * 功能简述:协议19 <br>
     */
    private void startBasicInfoStaticTask(boolean fromReceiver) {
        try {
            long now = System.currentTimeMillis();
            long lastCheckUpdate = getLastCheckedTime(PrefConst.KEY_UPLOAD_BASIC_INFO_CHECK_TIME); // 上一次的检查时间
            long triggerTime = -1;
            if (lastCheckUpdate == 0) {
                doStartUploadBasicInfoStatic(now);
                triggerTime = now + EIGHT_HOURS;
            } else {
                if (fromReceiver) {
                    doStartUploadBasicInfoStatic(now);
                    triggerTime = now + EIGHT_HOURS;
                } else {
                    if (now - lastCheckUpdate >= TWO_HOURS) {
                        doStartUploadBasicInfoStatic(now);
                        triggerTime = now + EIGHT_HOURS;
                    } else {
                        triggerTime = lastCheckUpdate + TWO_HOURS;
                    }
                }
            }
            Intent updateIntent = new Intent(ICustomAction.ACTION_UPLOAD_BASIC_STATISTIC);
            PendingIntent pendingIntent =
                    PendingIntent.getBroadcast(getApplicationContext(), 0, updateIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
            AppUtils.triggerAlarm(mAlarmManager, AlarmManager.RTC_WAKEUP, triggerTime,
                    pendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doStartUploadBasicInfoStatic(final long now) {
        FaceThreadExecutorProxy.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                Logcat.i(TAG, "doStartUploadBasicInfoStatic");
                BaseSeq19OperationStatistic.uploadBasicInfo();
                // 保存本次检查的时长
                setLastCheckedTime(PrefConst.KEY_UPLOAD_BASIC_INFO_CHECK_TIME, now);
            }
        });
    }

    private long getLastCheckedTime(String key) {
        PrivatePreference pref = PrivatePreference.getPreference(getApplicationContext());
        long lastCheckedTime = 0L;
        if (pref != null) {
            lastCheckedTime = pref.getLong(key, 0L);
        }
        return lastCheckedTime;
    }

    private void setLastCheckedTime(String key, long checkedTime) {
        PrivatePreference pref = PrivatePreference.getPreference(getApplicationContext());
        if (pref != null) {
            pref.putLong(key, checkedTime);
            pref.commit();
        }
    }

    /**
     * 内部服务，用于设置前台进程
     */
    public static class InnerDaemonService extends NotificationAssistService {
    }
}
