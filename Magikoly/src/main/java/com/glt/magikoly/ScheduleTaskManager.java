package com.glt.magikoly;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.SparseArray;

import com.android.volley.VolleyError;
import com.glt.magikoly.bean.net.HotwordResponseBean;
import com.glt.magikoly.config.AgingShutterConfigBean;
import com.glt.magikoly.config.AnimalConfigBean;
import com.glt.magikoly.config.ConfigManager;
import com.glt.magikoly.config.DiscoverySearchConfigBean;
import com.glt.magikoly.config.FilterSortConfigBean;
import com.glt.magikoly.config.InnerAdConfigBean;
import com.glt.magikoly.config.MainBannerConfigBean;
import com.glt.magikoly.constants.ICustomAction;
import com.glt.magikoly.function.FaceFunctionManager;
import com.glt.magikoly.function.main.discovery.DiscoveryController;
import com.glt.magikoly.net.RequestCallback;
import com.glt.magikoly.pref.PrefConst;
import com.glt.magikoly.pref.PrivatePreference;
import com.glt.magikoly.subscribe.billing.BillingPurchaseManager;
import com.glt.magikoly.thread.FaceThreadExecutorProxy;
import com.glt.magikoly.utils.AppUtils;
import com.glt.magikoly.utils.Machine;

import java.util.ArrayList;


/**
 * Created by kingyang on 2017/2/28.
 */

public class ScheduleTaskManager extends BroadcastReceiver {

    private static ScheduleTaskManager sInstance;
    private final AlarmManager mAlarmManager;
    private final WifiManager mWifiMgr;

    private Context mContext;
    private ArrayList<ScheduleTask> mPendingTasks = new ArrayList<>();
    private ScheduleTaskFactory mFactory = new ScheduleTaskFactory();

    public static ScheduleTaskManager getInstance() {
        if (sInstance == null) {
            sInstance = new ScheduleTaskManager();
        }
        return sInstance;
    }

    private ScheduleTaskManager() {
        mContext = FaceAppState.getContext();
        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        mWifiMgr = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        SparseArray<ScheduleTask> tasks = mFactory.getAllScheduleTasks();
        for (int i = 0; i < tasks.size(); i++) {
            ScheduleTask task = tasks.get(tasks.keyAt(i));
            filter.addAction(task.getAction());
        }
        mContext.registerReceiver(this, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            Parcelable parcelableExtra = intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (parcelableExtra instanceof NetworkInfo) {
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                int type = networkInfo.getType();
                NetworkInfo.State state = networkInfo.getState();
                if (type == ConnectivityManager.TYPE_MOBILE) {
                    if (state == NetworkInfo.State.CONNECTED) {
                        startPendingTasks();
                    }
                }
            }
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }
            WifiInfo wifiInfo = mWifiMgr.getConnectionInfo();
            Parcelable parcelableExtra = intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (parcelableExtra instanceof NetworkInfo) {
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                NetworkInfo.State state = networkInfo.getState();
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    if (wifiInfo != null && state == NetworkInfo.State.CONNECTED && wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                        startPendingTasks();
                    }
                }
            }
        } else {
            SparseArray<ScheduleTask> taskMap = mFactory.getAllScheduleTasks();
            for (int i = 0; i < taskMap.size(); i++) {
                int key = taskMap.keyAt(i);
                ScheduleTask task = taskMap.get(key);
                if (task.onReceive(action)) {
                    break;
                }
            }
        }
    }

    private void startPendingTasks() {
        if (!Machine.isNetworkOK(mContext)) {
            return;
        }
        FaceThreadExecutorProxy.runOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                for (final ScheduleTask task : mPendingTasks) {
                    FaceThreadExecutorProxy.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            task.restart();
                        }
                    });
                    SystemClock.sleep(1000);
                }
                mPendingTasks.clear();
            }
        });
    }

    private void addPendingTask(final ScheduleTask task) {
        FaceThreadExecutorProxy.runOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                for (final ScheduleTask t : mPendingTasks) {
                    if (t.mId == task.mId) {
                        //已经添加，返回
                        return;
                    }
                }
                mPendingTasks.add(task);
            }
        });
    }

    private abstract class ScheduleTask {

        private int mId = -1;
        private long mInterval;
        private String mCheckedTimeKey;
        private String mAction;
        private PendingIntent mPendingIntent;
        private Runnable mStartRunnable;

        public ScheduleTask(int id, long interval, String checkedTimeKey, String action) {
            mId = id;
            mInterval = interval;
            mCheckedTimeKey = checkedTimeKey;
            mAction = action;
        }

        public int getId() {
            return mId;
        }

        public long getInterval() {
            return mInterval;
        }

        public String getCheckedTimeKey() {
            return mCheckedTimeKey;
        }

        public String getAction() {
            return mAction;
        }

        public void start(long delay) {
            if (mStartRunnable == null) {
                mStartRunnable = new Runnable() {
                    @Override
                    public void run() {
                        startIntervalTask(false, mInterval, mCheckedTimeKey, mAction);
                    }
                };
            }
            FaceThreadExecutorProxy.runOnMainThread(mStartRunnable, delay);
        }

        public void action() {
            if (mCheckedTimeKey == null || mAction == null) {
                throw new IllegalArgumentException();
            }
            if (needNetwork() && !Machine.isNetworkOK(mContext)) {
                addPendingTask(this);
            } else {
                if (doAction()) {
                    startIntervalTask(true, mInterval, mCheckedTimeKey, mAction);
                }
            }
        }

        public void restart() {
            if (mCheckedTimeKey == null || mAction == null) {
                throw new IllegalArgumentException();
            }
            if (doAction()) {
                startIntervalTask(true, mInterval, mCheckedTimeKey, mAction);
            }
        }

        public void cancel() {
            if (mStartRunnable != null) {
                FaceThreadExecutorProxy.cancel(mStartRunnable);
            }
            if (mPendingIntent != null) {
                mAlarmManager.cancel(mPendingIntent);
            }
        }


        public boolean onReceive(String action) {
            if (mAction.equals(action)) {
                action();
                return true;
            }
            return false;
        }


        /**
         * 间隔指定时间执行任务
         *
         * @param fromReceiver   是否从Receiver回调
         * @param interval       任务间隔
         * @param checkedTimeKey
         * @param action
         */
        public void startIntervalTask(boolean fromReceiver, long interval, String checkedTimeKey,
                                      String action) {
            try {
                long now = System.currentTimeMillis();
                if (fromReceiver) {
                    setLastCheckedTime(checkedTimeKey, now);
                }
                long toNextIntervalTime = 0; // 下一次上传间隔时间
                long lastCheckUpdate = getLastCheckedTime(checkedTimeKey); // 上一次的检查时间
                if (lastCheckUpdate == 0L) {
                    toNextIntervalTime = 0;
                } else if (now - lastCheckUpdate >= interval) {
                    toNextIntervalTime = 0;
                } else {
                    // 动态调整下一次的间隔时间
                    toNextIntervalTime = interval - (now - lastCheckUpdate);
                }

                if (toNextIntervalTime == 0) {
                    Intent updateIntent = new Intent(action);
                    mContext.sendBroadcast(updateIntent);
                } else {
                    final long triggerTime = System.currentTimeMillis() + toNextIntervalTime;
                    if (mPendingIntent == null) {
                        Intent updateIntent = new Intent(action);
                        mPendingIntent = PendingIntent.getBroadcast(mContext, 0, updateIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                    }
                    AppUtils.triggerAlarm(mAlarmManager, AlarmManager.RTC_WAKEUP, triggerTime, mPendingIntent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void setLastCheckedTime(String key, long checkedTime) {
            PrivatePreference pref = PrivatePreference.getPreference(mContext);
            if (pref != null) {
                pref.putLong(key, checkedTime);
                pref.commit();
            }
        }

        public long getLastCheckedTime(String key) {
            PrivatePreference pref = PrivatePreference.getPreference(mContext);
            long lastCheckedTime = 0L;
            if (pref != null) {
                lastCheckedTime = pref.getLong(key, 0L);
            }
            return lastCheckedTime;
        }

        /**
         * 该任务要做的事情全写在这个方法里
         */
        public abstract boolean doAction();

        /**
         * 该任务是否需要网络
         *
         * @return
         */
        public abstract boolean needNetwork();
    }

    public void cancelTask(int taskId) {
        ScheduleTask task = mFactory.getScheduleTask(taskId);
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * 强制执行Task
     */
    public void startTask(int taskId) {
        mFactory.getScheduleTask(taskId).action();
    }

    public void startScheduleTasks() {
        mFactory.getScheduleTask(ScheduleTaskFactory.TASK_ID_PURCHASE_FUNCTION_CONFIG).start(0);
        mFactory.getScheduleTask(ScheduleTaskFactory.TASK_ID_AGING_SHUTTER_CONFIG).start(0);
        mFactory.getScheduleTask(ScheduleTaskFactory.TASK_ID_DISCOVERY_HOTWORD_CONFIG).start(0);
        mFactory.getScheduleTask(ScheduleTaskFactory.TASK_ID_DISCOVERY_SEARCH_CONFIG).start(0);
        mFactory.getScheduleTask(ScheduleTaskFactory.TASK_ID_ART_FILTER_CONFIG).start(0);
        mFactory.getScheduleTask(ScheduleTaskFactory.TASK_ID_INNER_AD_CONFIG).start(0);
        mFactory.getScheduleTask(ScheduleTaskFactory.TASK_ID_MAIN_BANNER_CONFIG).start(0);
        mFactory.getScheduleTask(ScheduleTaskFactory.TASK_ID_ANIMAL_CONFIG).start(0);
    }


    private class ScheduleTaskFactory {
        public static final int TASK_ID_PURCHASE_FUNCTION_CONFIG = 0;
        public static final int TASK_ID_AGING_SHUTTER_CONFIG = 1;
        public static final int TASK_ID_DISCOVERY_HOTWORD_CONFIG = 2;
        public static final int TASK_ID_DISCOVERY_SEARCH_CONFIG = 3;
        public static final int TASK_ID_INNER_AD_CONFIG = 4;
        public static final int TASK_ID_ART_FILTER_CONFIG = 6;
        public static final int TASK_ID_MAIN_BANNER_CONFIG = 7;
        public static final int TASK_ID_ANIMAL_CONFIG = 8;

        private SparseArray<ScheduleTask> mTaskMap = new SparseArray<>();

        public ScheduleTaskFactory() {
            initTasks();
        }

        private void initTasks() {
            /**
             * 在startTasks，启动任务
             */
            ScheduleTask task = createPurchaseFunctionConfigTask(TASK_ID_PURCHASE_FUNCTION_CONFIG);
            mTaskMap.put(task.getId(), task);
            task = createAgingShutterConfigTask(TASK_ID_AGING_SHUTTER_CONFIG);
            mTaskMap.put(task.getId(), task);
            task = createDiscoveryHotwordConfigTask(TASK_ID_DISCOVERY_HOTWORD_CONFIG);
            mTaskMap.put(task.getId(), task);
            task = createDiscoverySearchConfigTask(TASK_ID_DISCOVERY_SEARCH_CONFIG);
            mTaskMap.put(task.getId(), task);
            task = createInnerAdConfigTask(TASK_ID_INNER_AD_CONFIG);
            mTaskMap.put(task.getId(), task);
            task = createArtFilterConfigTask(TASK_ID_ART_FILTER_CONFIG);
            mTaskMap.put(task.getId(), task);
            task = createMainBannerConfigTask(TASK_ID_MAIN_BANNER_CONFIG);
            mTaskMap.put(task.getId(), task);
            task = createAnimalConfigTask(TASK_ID_ANIMAL_CONFIG);
            mTaskMap.put(task.getId(), task);
        }

        public ScheduleTask getScheduleTask(int taskId) {
            ScheduleTask task = mTaskMap.get(taskId);
            return task;
        }

        public SparseArray<ScheduleTask> getAllScheduleTasks() {
            return mTaskMap.clone();
        }

        private ScheduleTask createPurchaseFunctionConfigTask(int taskId) {
            return new ScheduleTask(taskId, AlarmManager.INTERVAL_HOUR * 8,
                    PrefConst.KEY_PURCHASE_FUNCTION_CONFIG_LAST_CHECK_TIME,
                    ICustomAction.ACTION_PURCHASE_FUNCTION_CONFIG) {
                @Override
                public boolean doAction() {
                    BillingPurchaseManager.Companion.getInstance().syncOrderStatus();
                    return true;
                }

                @Override
                public boolean needNetwork() {
                    return true;
                }
            };
        }

        private ScheduleTask createAgingShutterConfigTask(int taskId) {
            return new ScheduleTask(taskId, AlarmManager.INTERVAL_HOUR * 8,
                    PrefConst.KEY_AGING_SHUTTER_CONFIG_LAST_CHECK_TIME,
                    ICustomAction.ACTION_AGING_SHUTTER_CONFIG) {
                @Override
                public boolean doAction() {
                    ConfigManager.getInstance()
                            .requestConfig(FaceAppState.getContext(), AgingShutterConfigBean.SID,
                                    null);
                    return true;
                }

                @Override
                public boolean needNetwork() {
                    return true;
                }
            };
        }

        private ScheduleTask createDiscoverySearchConfigTask(int taskId) {
            return new ScheduleTask(taskId, AlarmManager.INTERVAL_HOUR * 8,
                    PrefConst.KEY_DISCOVERY_SEARCH_CONFIG_LAST_CHECK_TIME,
                    ICustomAction.ACTION_DISCOVERY_SEARCH_CONFIG) {
                @Override
                public boolean doAction() {
                    ConfigManager.getInstance()
                            .requestConfig(FaceAppState.getContext(), DiscoverySearchConfigBean.SID,
                                    null);
                    return true;
                }

                @Override
                public boolean needNetwork() {
                    return true;
                }
            };
        }

        private ScheduleTask createDiscoveryHotwordConfigTask(int taskId) {
            return new ScheduleTask(taskId, AlarmManager.INTERVAL_HOUR * 8,
                    PrefConst.KEY_DISCOVERY_HOTWORD_CONFIG_LAST_CHECK_TIME,
                    ICustomAction.ACTION_DISCOVERY_HOTWORD_CONFIG) {
                @Override
                public boolean doAction() {
                    FaceFunctionManager.INSTANCE.requestHotword(new RequestCallback<HotwordResponseBean>() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }

                        @Override
                        public void onResponse(HotwordResponseBean response) {
                            if (response != null && response.getHotwords() != null) {
                                DiscoveryController.Companion.getInstance().saveHotwordOnline(response);
                            }
                        }
                    });
                    return true;
                }

                @Override
                public boolean needNetwork() {
                    return true;
                }
            };
        }

        private ScheduleTask createInnerAdConfigTask(int taskId) {
            return new ScheduleTask(taskId, AlarmManager.INTERVAL_HOUR * 8,
                    PrefConst.KEY_INNER_AD_CONFIG_LAST_CHECK_TIME,
                    ICustomAction.ACTION_INNER_AD_CONFIG) {
                @Override
                public boolean doAction() {
                    ConfigManager.getInstance()
                            .requestConfig(FaceAppState.getContext(), InnerAdConfigBean.SID, null);
                    return true;
                }

                @Override
                public boolean needNetwork() {
                    return true;
                }
            };
        }

        private ScheduleTask createArtFilterConfigTask(int taskId) {
            return new ScheduleTask(taskId, AlarmManager.INTERVAL_HOUR * 8,
                    PrefConst.KEY_ART_FILTER_CONFIG_LAST_CHECK_TIME,
                    ICustomAction.ACTION_ART_FILTER_CONFIG) {
                @Override
                public boolean doAction() {
                    ConfigManager.getInstance()
                            .requestConfig(FaceAppState.getContext(), FilterSortConfigBean.SID, null);
                    return true;
                }

                @Override
                public boolean needNetwork() {
                    return true;
                }
            };
        }

        private ScheduleTask createMainBannerConfigTask(int taskId) {
            return new ScheduleTask(taskId, AlarmManager.INTERVAL_HOUR * 8,
                    PrefConst.KEY_MAIN_BANNER_CONFIG_LAST_CHECK_TIME,
                    ICustomAction.ACTION_MAIN_BANNER_CONFIG) {
                @Override
                public boolean doAction() {
                    ConfigManager.getInstance()
                            .requestConfig(FaceAppState.getContext(), MainBannerConfigBean.SID, null);
                    return true;
                }

                @Override
                public boolean needNetwork() {
                    return true;
                }
            };
        }

        private ScheduleTask createAnimalConfigTask(int taskId) {
            return new ScheduleTask(taskId, AlarmManager.INTERVAL_HOUR * 8,
                    PrefConst.KEY_ANIMAL_CONFIG_LAST_CHECK_TIME,
                    ICustomAction.ACTION_ANIMAL_CONFIG) {
                @Override
                public boolean doAction() {
                    ConfigManager.getInstance()
                            .requestConfig(FaceAppState.getContext(), AnimalConfigBean.SID, null);
                    return true;
                }

                @Override
                public boolean needNetwork() {
                    return true;
                }
            };
        }
    }
}
