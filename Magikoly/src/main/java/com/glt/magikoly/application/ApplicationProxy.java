package com.glt.magikoly.application;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.SystemClock;
import android.support.multidex.MultiDexApplication;

import com.glt.magikoly.BuyChannelApiProxy;
import com.glt.magikoly.FaceAppState;
import com.glt.magikoly.FaceEnv;
import com.glt.magikoly.data.DatabaseHelper;
import com.glt.magikoly.data.DatabaseNames;
import com.glt.magikoly.data.PersistenceManager;
import com.glt.magikoly.download.DownloadProxy;
import com.glt.magikoly.download.FileDownloaderStrategy;
import com.glt.magikoly.utils.AppUtils;
import com.glt.magikoly.utils.Logcat;
import com.google.firebase.FirebaseApp;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;


/**
 * 
 * @author yangguanxiang
 * 
 */
public class ApplicationProxy extends MultiDexApplication {

	private IApplication mAppImpl;
	private HashSet<Activity> mActivitySet = new HashSet<>();

	private void initAppImpl(Context context) {
		String processName = null;
		int retryCount = 0;
		int maxRetry = 2;
		while (processName == null) {
			processName = AppUtils.getCurProcessName(context);
			if (processName == null) {
				retryCount++;
				if (retryCount > maxRetry) {
					break;
				}
				SystemClock.sleep(500);
			}
		}
		if (processName == null || FaceEnv.PROCESS_NAME.equals(processName)) {
			mAppImpl = new FaceApplication(processName);
		} else if (FaceEnv.PROCESS_DAEMON_ASSISTANT.equals(processName)) {
			mAppImpl = new DaemonAssistantApp(processName);
		}
		if (mAppImpl == null) {
			mAppImpl = new FaceApplication(processName);
		}
		Logcat.i("Test", "processName: " + processName + " mAppImpl: "
				+ mAppImpl.getClass().getSimpleName());
	}

	@Override
	public void onCreate() {
		super.onCreate();
		FaceEnv.loadConfig(this);
        Logcat.setEnable(FaceEnv.sSIT);
//		LeakCanary.install(this);
		//提前加载类AsyncTask
		//原因：由于其他线程和AsyncTask在装载时都在竞争相同的资源,导致AsyncTask竞争失败，进一步导致class loader装载它失败
//		try {
//			Class.forName("android.os.AsyncTask");
//		} catch (Throwable t) {
//			// ignored
//		}
		FaceAppState.setApplicationProxy(this);
		try {
			FirebaseApp.initializeApp(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		initPersistenceManager();
		BuyChannelApiProxy.preInit(this);
		mAppImpl.onCreate();

		DownloadProxy.init(this,new FileDownloaderStrategy());
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		mAppImpl.onTerminate();
	}

	@Override
	public Resources getResources() {
		if (mAppImpl == null) {
			return super.getResources();
		}
		return mAppImpl.getResources();
	}

	@Override
	public Context getApplicationContext() {
		if (mAppImpl == null) {
			return super.getApplicationContext();
		}
		return mAppImpl.getApplicationContext();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mAppImpl.onConfigurationChanged(newConfig);
	}

	@Override
	protected void attachBaseContext(final Context base) {
		super.attachBaseContext(base);
		initAppImpl(base);
		mAppImpl.attachBaseContext(base);
	}

	/**
	 * API MINI = 19
	 * 清除应用所有的数据，/data/data/package & sdcard/Android/data/package
	 * 该方法执行会杀死应用
	 */
	public void cleanApplicationData() {
		if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
			ActivityManager mgr = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			mgr.clearApplicationUserData(); // note: it has a return value!
		}
		String command = "pm clear " + getPackageName();
		try {
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void finishAllActivities() {
		for (Activity activity : mActivitySet) {
			activity.finish();
		}
		mActivitySet.clear();
	}

	private void initPersistenceManager() {
		HashMap<String, PersistenceManager.DatabaseCreator> map = new HashMap<>();
		map.put(DatabaseNames.DB_FACE, new PersistenceManager.DatabaseCreator() {
			@Override
			public Object create(String dbName) {
				boolean isMainProcess;
				if (mAppImpl != null) {
					isMainProcess = mAppImpl.isMainProcess();
				} else {
					isMainProcess = AppUtils.isMainProcess(getApplicationContext());
				}
				if (isMainProcess) {
					return new DatabaseHelper(getApplicationContext(),
							DatabaseNames.DB_FACE);
				} else {
					return getContentResolver();
				}
			}
		});
		PersistenceManager.initDatabaseCreator(map);
	}
}
