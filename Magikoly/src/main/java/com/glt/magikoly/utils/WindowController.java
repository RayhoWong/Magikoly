package com.glt.magikoly.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import java.lang.reflect.Field;

/**
 * Android窗口控制类,外部通过单例形式进行调用
 *
 * @author yuankai
 * @version 1.0
 */
public class WindowController {

	private static Point sOutSize = new Point();
	private static Context sContext;

	public static void init(Context context) {
		sContext = context;
	}

	/**
	 * 是否竖屏
	 *
	 * @return
	 */
	public static boolean isPortrait() {
		WindowManager wm = (WindowManager) sContext.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		int width = 0;
		int height = 0;
		if (Machine.IS_ICS) {
			display.getSize(sOutSize);
			width = sOutSize.x;
			height = sOutSize.y;
		} else {
			width = display.getWidth();
			height = display.getHeight();
		}
		return width < height;
	}

	public static int getScreenHeight() {
		if (Machine.isTablet(sContext) && !Machine.IS_SDK_ABOVE_KITKAT) {
			return DrawUtils.getTabletScreenHeight(sContext);
		}
		return DrawUtils.getRealHeight(sContext);
	}

	// LH TODO:移到ApplicationProxy里面
	/**
	 * 屏幕宽度(px)
	 *
	 * @return
	 */
	public static int getScreenWidth() {
		if (Machine.isTablet(sContext) && !Machine.IS_SDK_ABOVE_KITKAT) {
			return DrawUtils.getTabletScreenWidth(sContext);
		}
		return DrawUtils.getRealWidth(sContext);
	}

	/**
	 * 获得屏幕较短的边的长度
	 *
	 * @return
	 */
	public static int getSmallerBound() {
		return (getScreenWidth() <= getScreenHeight()) ? getScreenWidth() : getScreenHeight();
	}

	/**
	 * 获得屏幕较长的边的长度
	 *
	 * @return
	 */
	public static int getLongerBound() {
		return (getScreenWidth() > getScreenHeight()) ? getScreenWidth() : getScreenHeight();
	}

	//获取状态栏高度
	public static  int getStatusBarHeight(Context context) {
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0;
		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			return context.getResources().getDimensionPixelSize(x);
		} catch (Exception e1) {
			Logcat.d("CustomWebLog", "get status bar height fail");
			e1.printStackTrace();
			return 0;
		}
	}

	/**
	 * https://developer.android.google.cn/training/system-ui/immersive.html
	 *隐藏导航栏，
	 * @param view
	 * @param
	 */
	public static void hideNavigationBar(View view) {
		// Hide both the navigation bar and the status bar.
		// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
		// a general rule, you should design your app to hide the status bar whenever you
		// hide the navigation bar.SYSTEM_UI_FLAG_FULLSCREEN 和SYSTEM_UI_FLAG_HIDE_NAVIGATION
		//显示NavigationBar
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
		if (Machine.IS_JELLY_BEAN) {
			uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_FULLSCREEN;
			if (Machine.IS_SDK_ABOVE_KITKAT) {
				uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
			}
		}
		view.setSystemUiVisibility(uiOptions);
	}

	/**
	 * 获取是否全屏
	 *
	 * @return 是否全屏
	 */
	public static boolean isFullScreen(Activity activity) {
		if (activity == null) {
			return false;
		}
		boolean ret = false;
		try {
			WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
			ret = (attrs.flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * 设置是否全屏
	 *
	 * @param activity
	 *            上下文
	 * @param isFullScreen
	 *            是否全屏
	 */
	public static void setFullScreen(Activity activity, boolean isFullScreen) {
		if (activity == null) {
			return;
		}
		try {
			if (isFullScreen) {
				activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			} else {
				activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
