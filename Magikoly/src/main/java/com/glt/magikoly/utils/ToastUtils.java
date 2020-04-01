package com.glt.magikoly.utils;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;
import com.glt.magikoly.FaceAppState;
import com.glt.magikoly.thread.FaceThreadExecutorProxy;

/**
 * 
 * @author yangguanxiang
 *
 */
public class ToastUtils {
	//壁纸提示信息时间1秒
	public static final int TIPS_TIME_DURATION = 1000;
	public static final int TIPS_TIME_SETWALLPAPER_DURATION = 2000;

	public static void showToast(int resId, int duration) {
		showToast(FaceAppState.getContext(),
				FaceAppState.getContext().getString(resId), duration);
	}

	public static void showToast(String text, int duration) {
		showToast(FaceAppState.getContext(), text, duration);
	}

	public static void showToast(final Context context, final int resId, final int duration) {
		if (Looper.myLooper() != Looper.getMainLooper()) {
			FaceThreadExecutorProxy.runOnMainThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(context, FaceAppState.getContext().getString(resId),
							duration).show();
				}
			});
		} else {
			Toast.makeText(context, FaceAppState.getContext().getString(resId), duration)
					.show();
		}
	}

	public static void showToast(final Context context, final String text, final int duration) {
		if (Looper.myLooper() != Looper.getMainLooper()) {
			FaceThreadExecutorProxy.runOnMainThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(context, text, duration).show();
				}
			});
		} else {
			Toast.makeText(context, text, duration).show();
		}
	}
}
