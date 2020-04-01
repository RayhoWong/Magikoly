package com.glt.magikoly.application;

import android.app.Application.ActivityLifecycleCallbacks;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

/**
 * app接口
 * @author yangguanxiang
 *
 */
public interface IApplication {
	void attachBaseContext(Context base);

	void onCreate();

	void onTerminate();

	Resources getResources();

	Context getApplicationContext();

	void onConfigurationChanged(Configuration newConfig);

	void onLowMemory();

	void onTrimMemory(int level);

	void registerComponentCallbacks(ComponentCallbacks callback);

	void unregisterComponentCallbacks(ComponentCallbacks callback);

	void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback);

	void unregisterActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback);

	boolean isMainProcess();

	String getCustomProcessName();

	void stopSdk(boolean stop);
}
