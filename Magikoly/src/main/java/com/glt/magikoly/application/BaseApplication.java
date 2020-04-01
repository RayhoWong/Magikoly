package com.glt.magikoly.application;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.glt.magikoly.FaceAppState;
import com.glt.magikoly.constants.ICustomAction;

/**
 * App基类
 * @author yangguanxiang
 *
 */
public abstract class BaseApplication extends Application implements IApplication {

	protected BaseApplicationDelegate mDelegate;

	protected String mProcessName;

	public BaseApplication(String processName) {
		mProcessName = processName;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mDelegate.onCreate();
		IntentFilter filter = new IntentFilter(ICustomAction.ACTION_KILL_SELF);
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				stopSdk(true);
				FaceAppState.exit();
			}
		}, filter);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDelegate.onConfigurationChanged(newConfig);
	}

	@Override
	public Context getApplicationContext() {
		return mDelegate.getApplicationContext(super.getApplicationContext());
	}

	@Override
	public void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		mDelegate = new BaseApplicationDelegate(this);
	}

	@Override
	public Resources getResources() {
		return mDelegate.getResources(super.getResources());
	}

	@Override
	public String getCustomProcessName() {
		return mProcessName;
	}

	@Override
	public void stopSdk(boolean stop) {

	}
}
