package com.glt.magikoly.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 桌面状态栏处理模块
 * 
 * @author yuankai
 * @version 1.0
 */
public class StatusBarHandler {
	/**
	 * Window flag: request a translucent status bar with minimal system-provided
	 * background protection.
	 *
	 * <p>This flag can be controlled in your theme through the
	 * {@link android.R.attr#windowTranslucentStatus} attribute; this attribute
	 * is automatically set for you in the standard translucent decor themes
	 * such as
	 * {@link android.R.style#Theme_Holo_NoActionBar_TranslucentDecor},
	 * {@link android.R.style#Theme_Holo_Light_NoActionBar_TranslucentDecor},
	 * {@link android.R.style#Theme_DeviceDefault_NoActionBar_TranslucentDecor}, and
	 * {@link android.R.style#Theme_DeviceDefault_Light_NoActionBar_TranslucentDecor}.</p>
	 *
	 * <p>When this flag is enabled for a window, it automatically sets
	 * the system UI visibility flags {@link View#SYSTEM_UI_FLAG_LAYOUT_STABLE} and
	 * {@link View#SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN}.</p>
	 */
	public static final int FLAG_TRANSLUCENT_STATUS = 0x04000000; // WindowManager.FLAG_TRANSLUCENT_STATUS
	
	public static final int FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS = 0x80000000;
	/**
	 * Window flag: request a translucent navigation bar with minimal system-provided
	 * background protection.
	 *
	 * <p>This flag can be controlled in your theme through the
	 * {@link android.R.attr#windowTranslucentNavigation} attribute; this attribute
	 * is automatically set for you in the standard translucent decor themes
	 * such as
	 * {@link android.R.style#Theme_Holo_NoActionBar_TranslucentDecor},
	 * {@link android.R.style#Theme_Holo_Light_NoActionBar_TranslucentDecor},
	 * {@link android.R.style#Theme_DeviceDefault_NoActionBar_TranslucentDecor}, and
	 * {@link android.R.style#Theme_DeviceDefault_Light_NoActionBar_TranslucentDecor}.</p>
	 *
	 * <p>When this flag is enabled for a window, it automatically sets
	 * the system UI visibility flags {@link View#SYSTEM_UI_FLAG_LAYOUT_STABLE} and
	 * {@link View#SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION}.</p>
	 */
	public static final int FLAG_TRANSLUCENT_NAVIGATION = 0x08000000; // WindowManager.FLAG_TRANSLUCENT_NAVIGATION

	public final static String FIELD_FULL_SCREEN_WIDTH = "full_screen_width";
	public final static String FIELD_FULL_SCREEN_HEIGHT = "full_screen_height";
	public final static String FIELD_UPDATE_DB = "update_db";
	public final static int TYPE_FULLSCREEN_RETURN_HEIGHT = 1; // 全屏的时候才返回状态栏高度的值
	public final static int TYPE_NOT_FULLSCREEN_RETURN_HEIGHT = 2; // 非全屏的时候才返回状态栏高度的值
	private int mDefaultStatusBarHeight;
	private int mStatusbarHeight = -1;
//	private boolean mHasFirstNotify = false; // 第一次必须发送屏幕状态
	private Integer mTransparentValue;
	private final static int STATUS_BAR_DEFAULT_HEIGHT_DP = 25;
	private Context mContext;
	private boolean mDetectedSupportAPITransparentStatusBar;
	private boolean mIsSupportAPITransparentStatusBar;

	public static final int NAVBAR_LOCATION_RIGHT = 1;
	public static final int NAVBAR_LOCATION_BOTTOM = 2;
	private int mNavBarWidth; // 虚拟键宽度
	private int mNavBarHeight; // 虚拟键高度
	public int mNavBarLocation;

	private int mTabletStatusBarHeight;
	
	StatusBarHandler(Context context) {
		mDefaultStatusBarHeight = STATUS_BAR_DEFAULT_HEIGHT_DP;
		mContext = context;
		Class<?> clazz = null;
		Object obj = null;
		Field field = null;
		try {
			clazz = Class.forName("com.android.internal.R$dimen");
			obj = clazz.newInstance();
			if (Machine.isMeizu()) {
				try {
					field = clazz.getField("status_bar_height_large");
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
			if (field == null) {
				field = clazz.getField("status_bar_height");
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		if (field != null && obj != null) {
			try {
				int id = Integer.parseInt(field.get(obj).toString());
				mStatusbarHeight = context.getResources().getDimensionPixelSize(id);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

		if (Machine.isTablet(context)
				&& mStatusbarHeight > DrawUtils.dip2px(STATUS_BAR_DEFAULT_HEIGHT_DP)) {
			//状态栏高度大于25dp的平板，状态栏通常在下方
			mStatusbarHeight = 0;
		} else {
			if (mStatusbarHeight <= 0
					|| mStatusbarHeight > DrawUtils.dip2px(STATUS_BAR_DEFAULT_HEIGHT_DP * 2)) {
				//安卓默认状态栏高度为25dp，如果获取的状态高度大于2倍25dp的话，这个数值可能有问题，用回桌面定义的值从新获取。出现这种可能性较低，只有小部分手机出现
				if (DrawUtils.sVirtualDensity == -1) {
					mStatusbarHeight = DrawUtils.dip2px(mDefaultStatusBarHeight);
				} else {
					mStatusbarHeight = (int) (mDefaultStatusBarHeight * DrawUtils.sVirtualDensity + 0.5f);
				}
			}
		}
	}

	public int getStatusbarHeight() {
		return mStatusbarHeight;
	}

	/**
	 * 透明通知栏
	 * @param window
	 * @param isTransparent
	 */
	public static void setStatusBarTransparentKitKat(Window window, boolean isTransparent) {
		if (Machine.IS_SDK_ABOVE_KITKAT) {
			if (isTransparent) {
				window.addFlags(FLAG_TRANSLUCENT_STATUS);
			} else {
				window.clearFlags(FLAG_TRANSLUCENT_STATUS);
			}
		}
	}

	/**
	 * <br>功能简述: 获取设置透明状态栏的system ui visibility的值， 这是部分有提供接口的rom使用的
	 *
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public final Integer getStatusBarTransparentValue() {
		if (mTransparentValue != null) {
			return mTransparentValue;
		}
		String[] systemSharedLibraryNames = mContext.getPackageManager()
				.getSystemSharedLibraryNames();
		String fieldName = null;
		for (String lib : systemSharedLibraryNames) {
			if ("touchwiz".equals(lib)) {
				fieldName = "SYSTEM_UI_FLAG_TRANSPARENT_BACKGROUND";
			} else if (lib.startsWith("com.sonyericsson.navigationbar")) {
				fieldName = "SYSTEM_UI_FLAG_TRANSPARENT";
			} else if (lib.startsWith("com.htc.")) {
				//TODO HTC的透明设置方式暂时没有找到，先不做
			}
		}

		if (fieldName != null) {
			try {
				Field field = View.class.getField(fieldName);
				if (field != null) {
					Class<?> type = field.getType();
					if (type == int.class) {
						int value = field.getInt(null);
						mTransparentValue = value;
					}
				}
			} catch (Exception e) {
			}
		}
		return mTransparentValue;
	}

	public final boolean supportTransparentStatusBar() {
		Integer state = getStatusBarTransparentValue();
        return state != null;
    }
	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public final void setStatusBarTransparent(Window window, boolean transparent) {
		int support = isSupportAPITransparentStatusBar() ? 1 : 0;
		switch (support) {
			case 1 :
				if (window == null || window.getDecorView() == null) {
					return;
				}
				Integer state = null;
				if (transparent) {
					state = getStatusBarTransparentValue();
					if (state != null) {
						window.getDecorView().setSystemUiVisibility(state);
					}
				} else {
					window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
				}
				break;
			case 0 :
				break;
			default :
				break;
		}
	}

	/**
	 * 透明虚拟键
	 * @param window
	 * @param isTransparent
	 */
	public static void setNavBarTransparentKitKat(Window window, boolean isTransparent) {
		if (Machine.IS_SDK_ABOVE_KITKAT) {
			if (isTransparent) {
				window.addFlags(FLAG_TRANSLUCENT_NAVIGATION);
			} else {
				window.clearFlags(FLAG_TRANSLUCENT_NAVIGATION);
			}
		}
	}

	public boolean isSupportAPITransparentStatusBar() {
		if (!mDetectedSupportAPITransparentStatusBar) {
			mIsSupportAPITransparentStatusBar = Build.VERSION.SDK_INT >= 17
					&& !Machine.IS_SDK_ABOVE_KITKAT && supportTransparentStatusBar();
			mDetectedSupportAPITransparentStatusBar = true;
		}
		return mIsSupportAPITransparentStatusBar;
	}


	public synchronized void resetNavBarHeight(Context context) {
		mNavBarWidth = DrawUtils.sRealWidthPixels - DrawUtils.sWidthPixels;
		mNavBarHeight = DrawUtils.sRealHeightPixels - DrawUtils.sHeightPixels;
		mNavBarLocation = getNavBarLocation();
	}
	
	/**
	 * 虚拟键在下面时
	 * @return
	 */
	public int getNavBarHeight() {
		if (Machine.IS_SDK_ABOVE_KITKAT && Machine.canHideNavBar()) {
			return mNavBarHeight;
		}
		return 0;
	}
	
	/**
	 * 横屏，虚拟键在右边时
	 * @return
	 */
	public int getNavBarWidth() {
		if (Machine.IS_SDK_ABOVE_KITKAT && Machine.canHideNavBar()) {
			return mNavBarWidth;
		}
		return 0;
	}
	
	public int getNavBarLocation() {
		if (DrawUtils.sRealWidthPixels > DrawUtils.sWidthPixels) {
			return NAVBAR_LOCATION_RIGHT;
		}
		return NAVBAR_LOCATION_BOTTOM;
	}

	/**
	 * 判断虚拟导航键是否存在
	 * 需要注意存在用户用root的方式隐藏了虚拟导航键，然后用手势返回或呼出home的情况
	 * 这时候sRealHeightPixels = sHeightPixels，sRealWidthPixels = sWidthPixels
	 * @return
	 */
	public boolean isNavBarAvailable() {
        return DrawUtils.sRealHeightPixels > DrawUtils.sHeightPixels
                || DrawUtils.sRealWidthPixels > DrawUtils.sWidthPixels;
    }
	
	public boolean isSupportSystemMenu() {
		//部分用户（HTC One_M8、LG-F460K）反馈找不到menu键，因此对这些机型做特殊处理，始终显示菜单键
        return isNavBarAvailable() && !Machine.isSimilarModel(Machine.NOT_SUPPORT_SYSTEM_MENU_MODEL);
	}
	
	/**
	 * android5.0状态栏透明
	 * @param window
	 * @param b
	 */
	public static void setStatusBarTransparentLollipop(Window window, boolean b) {
		// TODO Auto-generated method stub
		window.addFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		
		try {
			Method method = window.getClass().getMethod("setStatusBarColor", int.class);
			boolean access = method.isAccessible();
			method.setAccessible(true);
			method.invoke(window, Color.TRANSPARENT);
			method.setAccessible(access);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**
	 * android5.0导航栏透明
	 * @param window
	 * @param b
	 */
	public static void setNavBarTransparentLollipop(Window window, boolean b) {
		// TODO Auto-generated method stub
		window.addFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		try {
			Method method = window.getClass().getMethod("setNavigationBarColor", int.class);
			boolean access = method.isAccessible();
			method.setAccessible(true);
			method.invoke(window, Color.TRANSPARENT);
			method.setAccessible(access);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
