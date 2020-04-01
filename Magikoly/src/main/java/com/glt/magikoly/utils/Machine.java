package com.glt.magikoly.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.LocaleList;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.*;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * a
 * 
 * @author jiangxuwen
 * 
 */
// CHECKSTYLE:OFF
public class Machine {
	public static int LEPHONE_ICON_SIZE = 72;
	private static boolean sCheckTablet = false;
	private static boolean sIsTablet = false;

	// 硬件加速
	public static int LAYER_TYPE_NONE = 0x00000000;
	public static int LAYER_TYPE_SOFTWARE = 0x00000001;
	public static int LAYER_TYPE_HARDWARE = 0x00000002;
	public static boolean IS_FROYO = Build.VERSION.SDK_INT >= 8;
	public static boolean IS_HONEYCOMB = Build.VERSION.SDK_INT >= 11;
	public static boolean IS_HONEYCOMB_MR1 = Build.VERSION.SDK_INT >= 12;
	public static boolean IS_ICS = Build.VERSION.SDK_INT >= 14;
	public static boolean IS_ICS_MR1 = Build.VERSION.SDK_INT >= 15 && Build.VERSION.RELEASE.equals("4.0.4");// HTC
																											// oneX
																											// 4.0.4系统
	public static boolean IS_JELLY_BEAN = Build.VERSION.SDK_INT >= 16;
	public static final boolean IS_JELLY_BEAN_MR1 = Build.VERSION.SDK_INT == 17; // 4.2
	public static final boolean IS_JELLY_BEAN_3 = Build.VERSION.SDK_INT >= 18; // 4.3
	public static final boolean IS_SDK_ABOVE_KITKAT = Build.VERSION.SDK_INT >= 19; // sdk是否4.4或以上
	public static final boolean IS_SDK_ABOVE_LOLIP = Build.VERSION.SDK_INT >= 21; // sdk否5.0或以上
	public static final boolean IS_SDK_5 = Build.VERSION.SDK_INT == 21; // sdk是否5.0
	public static final boolean IS_SDK_ABOVE_6 = Build.VERSION.SDK_INT >= 23; // sdk是否6.0或以上
	public static final boolean IS_SDK_6 = Build.VERSION.SDK_INT == 23; // sdk是否6.0

	public static final boolean IS_SDK_7 = Build.VERSION.SDK_INT == 24; // sdk是否7.0
	public static final boolean IS_SDK_ABOVE_7 = Build.VERSION.SDK_INT >= 24; // sdk是否7.0或以上
	public static final boolean IS_SDK_ABOVE_8 = Build.VERSION.SDK_INT >= 26; // sdk是否8.0或以上
	public static final boolean IS_SDK_ABOVE_8_1 = Build.VERSION.SDK_INT >= 27; // sdk是否8.0或以上

	public static boolean sLevelUnder3 = Build.VERSION.SDK_INT < 11;// 版本小于3.0
	private static Method sAcceleratedMethod = null;

	private final static String LEPHONEMODEL[] = { "3GW100", "3GW101", "3GC100", "3GC101" };
	private final static String MEIZUBOARD[] = { "m9", "M9", "mx", "MX", "mx2", "MX2", "mx3",
			"MX3", "mx4", "MX4" };
	private final static String M9BOARD[] = { "m9", "M9" };
	private final static String NUBIO[] = { "MSM8974" };
	private final static String ACER[] = { "Acer" };
	private final static String HW_D2_0082_BOARD[] = { "D2-0082", "d2-0082" };
	private final static String ONE_X_MODEL[] = { "HTC One X", "HTC One S", "HTC Butterfly", "HTC One XL",
			"htc one xl", "HTC Droid Incredible 4G LTE", "HTC 802w" };
	private final static String KITKAT_WITHOUT_NAVBAR[] = { "xt1030", "xt1080", "droid ultra", "droid maxx" };

	public final static String NOT_SUPPORT_SYSTEM_MENU_MODEL[] = { "HTC One_M8", "LG-F460K", "LG-D850", "LG-D851",
			"LG-D855", "LG G3", "VS985 4G", "LG-D724", "G Vista" }; // 部分用户，在虚拟键上没有menu海苔条，影响功能，做特殊处理

	private final static String PAY_NOT_BY_GETJER_COUNTRY[] = { "us", "gb", "de", "ru", "jp", "au", "fr", "it", "ca",
			"br", "es", "se", "tw", "mx", "nl", "no", "kr", "cn" }; // 不通过亚太付费规则购买付费功能的国家
	private final static String MXBOARD[] = { "mx", "MX", "mx2", "MX2", "mx3", "MX3", "mx4", "MX4" };
	private static final String[] C8816 = { "C8816" };
	private static final String[] HT22I = {"montblanc"};
	private final static String SONYC2305[] = { "arima89_we_s_jb2" };
	// 老用户推荐360的国家
	private static final String[] ON_RECOMMEND_COUNTRIES_FOR_SECURITY = new String[] { "us", "in", "kr", "ph" };
	// 老用户推荐遨游的国家 V4.15临时去掉，面向全世界
	// private static final String[] ON_RECOMMEND_COUNTRIES_FOR_MATHON = new
	// String[] { "us", "in", "ph", "id" };

	private static boolean sSupportGLES20 = false;
	private static boolean sDetectedDevice = false;

	// 用于判断设备是否支持绑定widget
	private static boolean sSupportBindWidget = false;
	// 是否已经进行过绑定widget的判断
	private static boolean sDetectedBindWidget = false;

	public final static String[] S5360_MODEL = { "GT-S5360" };

	private static String sInstallDate = null; // apk安装时间,缓存起来，避免每次IO访问

	private final static int HIDE_SMARTBAR = 2;

	public static final int NETTYPE_MOBILE = 0; // 中国移动
	public static final int NETTYPE_UNICOM = 1; // 中国联通
	public static final int NETTYPE_TELECOM = 2; // 中国电信

	private static final String VPS_VERSION = "01.01.01"; // 目前在用的VPS版本

	private final static String LG_ROM_4_4_UP_PREFIX[] = { "KRT16S", "KOT49I", "KVT49L" }; // 目前LG定制的Rom4.4以上版本名前缀

	// LG定制ROM检测级别
	public final static int LG_OS_CHECK_LEVEL1 = 1;
	public final static int LG_OS_CHECK_LEVEL2 = 2;
	public final static int LG_OS_CHECK_LEVEL3 = 3;
	public final static int LG_OS_CHECK_LEVEL4 = 4;

	public static boolean isLephone() {
		final String model = android.os.Build.MODEL;
		if (model == null) {
			return false;
		}
		final int size = LEPHONEMODEL.length;
		for (int i = 0; i < size; i++) {
			if (model.equals(LEPHONEMODEL[i])) {
				return true;
			}
		}
		return false;
	}

	public static boolean isM9() {
		return isPhone(M9BOARD);
	}

	public static boolean isMeizu() {
		return isPhone(MEIZUBOARD);
	}

	public static boolean isNubio() {
		return isPhone(NUBIO);
	}

	public static boolean isAcer() {
		return isPhone(ACER);
	}

	public static boolean isONE_X() {
		return isModel(ONE_X_MODEL);
	}

	public static boolean isHW_D2_0082() {
		return isPhone(HW_D2_0082_BOARD);
	}

	public static boolean isMX() {
		return isPhone(MXBOARD);
	}

	/**
	 * 检查机型是否需要默认开启3DCore
	 *
	 * @return bool
	 */
	public static boolean needToOpen3DCore() {
		// V4.05开始默认全部开放3DCore
		return true;// checkModel(USE_3DCORE_DEVICE_MODEL) || IS_JELLY_BEAN;
	}

	private static boolean isPhone(String[] boards) {
		final String board = android.os.Build.BOARD;
		if (board == null) {
			return false;
		}
		final int size = boards.length;
		for (int i = 0; i < size; i++) {
			if (board.equals(boards[i])) {
				return true;
			}
		}
		return false;
	}

	public static boolean isSimilarModel(String[] models) {
		final String board = android.os.Build.MODEL;
		if (board == null) {
			return false;
		}
		final int size = models.length;
		try {
			for (int i = 0; i < size; i++) {
				if (board.contains(models[i]) || board.contains(models[i].toLowerCase())
						|| board.contains(models[i].toUpperCase())) {
					return true;
				}
			}
		} catch (Exception e) {
		}
		return false;
	}

	public static boolean isModel(String[] models) {
		final String board = android.os.Build.MODEL;
		if (board == null) {
			return false;
		}
		final int size = models.length;
		try {
			for (int i = 0; i < size; i++) {
				if (board.equals(models[i]) || board.equals(models[i].toLowerCase())
						|| board.equals(models[i].toUpperCase())) {
					return true;
				}
			}
		} catch (Exception e) {
		}
		return false;
	}

	/**
	 * 因为主题2.0新起进程，无法获取GoLauncher.getContext()， 所以重载此方法，以便主题2.0调用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isCnUser(Context context) {
//		boolean result = false;
//
//		if (context != null) {
//			// 从系统服务上获取了当前网络的MCC(移动国家号)，进而确定所处的国家和地区
//			TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//
//			// SIM卡状态
//			boolean simCardUnable = manager.getSimState() != TelephonyManager.SIM_STATE_READY;
//			String simOperator = manager.getSimOperator();
//
//			if (simCardUnable || TextUtils.isEmpty(simOperator)) {
//				// 如果没有SIM卡的话simOperator为null，然后获取本地信息进行判断处理
//				// 获取当前国家或地区，如果当前手机设置为简体中文-中国，则使用此方法返回CN
//				String curCountry = Locale.getDefault().getCountry();
//				if (curCountry != null && curCountry.contains("CN")) {
//					// 如果获取的国家信息是CN，则返回TRUE
//					result = true;
//				} else {
//					// 如果获取不到国家信息，或者国家信息不是CN
//					result = false;
//				}
//			} else if (simOperator.startsWith("460")) {
//				// 如果有SIM卡，并且获取到simOperator信息。
//				/**
//				 * 中国大陆的前5位是(46000) 中国移动：46000、46002 中国联通：46001 中国电信：46003
//				 */
//				result = true;
//			}
//		}
//
//		return result;

		boolean result = false;
		TelephonyManager manager =
				(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

		// SIM卡状态
		boolean simCardEnable = manager.getSimState() == TelephonyManager.SIM_STATE_READY;
		String simOperator = manager.getSimOperator();
		if (simCardEnable) {
			if (simOperator.startsWith("460")) {
				result = true;
			}
		}
		if (!result) {
			String country = getSimCountryIso(context);
			if (country != null && country.toUpperCase().contains("CN")) {
				result = true;
			}
		}
		return result;
	}

	// 判断当前设备是否为平板
	private static boolean isPad(Context context) {
		// if (DrawUtils.sDensity >= 1.5 || DrawUtils.sDensity <= 0) {
		// return false;
		// }
		// if (DrawUtils.sWidthPixels < DrawUtils.sHeightPixels) {
		// if (DrawUtils.sWidthPixels > 480 && DrawUtils.sHeightPixels > 800) {
		// return true;
		// }
		// } else {
		// if (DrawUtils.sWidthPixels > 800 && DrawUtils.sHeightPixels > 480) {
		// return true;
		// }
		// }
		// return false;
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	public static boolean isTablet(Context context) {
		if (sCheckTablet) {
			return sIsTablet;
		}
		sCheckTablet = true;
		sIsTablet = isPad(context);
		return sIsTablet;
	}

	/**
	 * 判断当前网络是否可以使用
	 * 
	 * @author huyong
	 * @param context
	 * @return
	 */
	public static boolean isNetworkOK(Context context) {
		boolean result = false;
		if (context != null) {
			try {
				ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				if (cm != null) {
					NetworkInfo networkInfo = cm.getActiveNetworkInfo();
					if (networkInfo != null && networkInfo.isConnected()) {
						result = true;
					}
				}
			} catch (NoSuchFieldError e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	/**
	 * 检测手机WIFI有没有打开的方法
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWifiEnable(Context context) {
		boolean result = false;
		if (context != null) {
//			ConnectivityManager connectivityManager = (ConnectivityManager) context
//					.getSystemService(Context.CONNECTIVITY_SERVICE);
//			if (connectivityManager != null) {
//				NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
//				if (networkInfo != null && networkInfo.isConnected()
//						&& networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
//					result = true;
//				}
//			}
			WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			return wifiManager.isWifiEnabled();
		}
		return result;
	}
	
	/**
	 * 是否连接上了wifi
	 * @param context
	 * @return
	 */
	public static boolean isWifiConnected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetworkInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiNetworkInfo != null && wifiNetworkInfo.isConnected();
    }
	
	public static String getWifiName(Context context) {
		String result = "Wifi";
		if (context != null) {
			WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = wifiManager.getConnectionInfo();
			if (info != null) {
				SupplicantState state = info.getSupplicantState();
				if (state == SupplicantState.COMPLETED) {
					if (info.getSSID() == null) {
						return result;
					}
					return info.getSSID();
				} else {
					return result;
				}
			}
		}
		return result;
	}

	/**
	 * 设置硬件加速
	 * 
	 */
	public static void setHardwareAccelerated(View view, int mode) {
		if (sLevelUnder3) {
			return;
		}
		try {
			if (null == sAcceleratedMethod) {
				sAcceleratedMethod = View.class.getMethod("setLayerType", Integer.TYPE, Paint.class);
			}
			sAcceleratedMethod.invoke(view, Integer.valueOf(mode), null);
		} catch (Throwable e) {
			sLevelUnder3 = true;
		}
	}

	public static boolean isIceCreamSandwichOrHigherSdk() {
		return Build.VERSION.SDK_INT >= 14;
	}

	/**
	 * 获取Android中的Linux内核版本号
	 * 
	 */
	public static String getLinuxKernel() {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec("cat /proc/version");
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (null == process) {
			return null;
		}

		// get the output line
		InputStream outs = process.getInputStream();
		InputStreamReader isrout = new InputStreamReader(outs);
		BufferedReader brout = new BufferedReader(isrout, 8 * 1024);
		String result = "";
		String line;

		// get the whole standard output string
		try {
			while ((line = brout.readLine()) != null) {
				result += line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (result.equals("")) {
			String Keyword = "version ";
			int index = result.indexOf(Keyword);
			line = result.substring(index + Keyword.length());
			if (null != line) {
				index = line.indexOf(" ");
				return line.substring(0, index);
			}
		}
		return null;
	}

	/**
	 * 获得手机内存的可用空间大小
	 * 
	 * @author kingyang
	 */
	public static long getAvailableInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	/**
	 * 获得手机内存的总空间大小
	 * 
	 * @author kingyang
	 */
	public static long getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	/**
	 * 获得手机sdcard的可用空间大小
	 * 
	 * @author kingyang
	 */
	public static long getAvailableExternalMemorySize() {
		File path = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	/**
	 * 获得手机sdcard的总空间大小
	 * 
	 * @author kingyang
	 */
	public static long getTotalExternalMemorySize() {
		File path = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	/**
	 * 是否存在SDCard
	 * 
	 * @author chenguanyu
	 * @return
	 */
	public static boolean isSDCardExist() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	/**
	 * 获取当前的语言
	 * 
	 * @author zhoujun
	 * @param context
	 * @return
	 */
	public static String getLanguage(Context context) {
		String language = context.getResources().getConfiguration().locale.getLanguage();
		return language;
	}

	public static String getLanguageWithLocale(Context context) {
		Locale locale = context.getResources().getConfiguration().locale;
		String language = locale.getLanguage();
		if (language.equals("zh")) {
			language = language + "_" + Locale.getDefault().getCountry();
		}
		return language;
	}

	/**
	 * 判断应用软件是否运行在前台
	 * 
	 * @param context
	 * @param packageName
	 *            应用软件的包名
	 * @return
	 */
	public static boolean isTopActivity(Context context, String packageName) {
		return isTopActivity(context, packageName, null);
	}

	/**
	 * 判断某一Activity是否运行在前台
	 * 
	 * @param context
	 * @param packageName
	 *            应用软件的包名
	 * @return
	 */
	public static boolean isTopActivity(Context context, String packageName, String className) {
		try {
			ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
			if (tasksInfo.size() > 0) {
				// Activity位于堆栈的顶层,如果Activity的类为空则判断的是当前应用是否在前台
				if (packageName.equals(tasksInfo.get(0).topActivity.getPackageName())
						&& (className == null || className.equals(tasksInfo.get(0).topActivity.getClassName()))) {
					return true;
				}
			}
		} catch (Exception e) {
		}
		return false;
	}

	/**
	 * <br>
	 * 功能简述:获取Android ID的方法 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @return
	 */
	public static String getAndroidId(Context context) {
		String androidId = null;
		if (context != null) {
			androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
		}
		return androidId;
	}

	/**
	 * 获取国家
	 * 
	 * @param context
	 * @return
	 */
	public static String getCountry(Context context) {
		String ret = null;

		try {
			TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			if (telManager != null) {
				ret = telManager.getSimCountryIso().toLowerCase();
			}
		} catch (Throwable e) {
			 e.printStackTrace();
		}
		if (ret == null || ret.equals("")) {
//			ret = Locale.getDefault().getCountry().toLowerCase();
			ret = getCountry();
		}
		return ret;
	}

	private static String getCountry() {
		if (Build.VERSION.SDK_INT >= 24) {
			LocaleList localeList = LocaleList.getDefault();
			for (int i = 0; i < localeList.size(); i++) {
				String country = localeList.get(i).getCountry();
				if (country != null && !country.isEmpty()) {
					return country.toLowerCase();
				}
			}
		}
		return Locale.getDefault().getCountry().toLowerCase();
	}

	/**
	 * <br>
	 * 功能简述:老用户推荐360的国家 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param curCountry
	 * @return
	 */
	public static boolean isAllowCountry(String curCountry) {
		boolean result = false;
		for (String country : ON_RECOMMEND_COUNTRIES_FOR_SECURITY) {
			if (country.equals(curCountry)) {
				result = true;
			}
		}
		return result;
	}

	/**
	 * <br>
	 * 功能简述:老用户推荐遨游浏览器的国家 <br>
	 * 功能详细描述:V4.15临时修改为面向全世界开放，即为全部老用户 <br>
	 * 注意:
	 * 
	 * @return
	 */
	public static boolean isMathonCountory(String curCountry) {
		boolean result = true;
		// for (String country : ON_RECOMMEND_COUNTRIES_FOR_MATHON) {
		// if (country.equals(curCountry)) {
		// result = true;
		// }
		// }
		return result;
	}

	/**
	 * 判断是否为韩国用户
	 * 
	 * @return
	 */
	public static boolean isKorea(Context context) {
		boolean isKorea = false;

		String country = getCountry(context);
		if (country.equals("kr")) {
			isKorea = true;
		}

		return isKorea;
	}

	/**
	 * <br>
	 * 功能简述: 判断当前用户是否是特定国家用户 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param context
	 * @param countryCodes
	 *            待检测的国家代号集合
	 * @return true 指定国家中包含用户当前所在国家 false 指定国家中不包含用户当前所在国家
	 */
	public static boolean checkUserCountry(Context context, String... countryCodes) {
		String localCountryCode = getCountry(context);
		for (String countryCode : countryCodes) {
			if (countryCode.equalsIgnoreCase(localCountryCode)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 是否支持OpenGL2.0
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isSupportGLES20(Context context) {
		if (!sDetectedDevice) {
			ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			ConfigurationInfo info = am.getDeviceConfigurationInfo();
			sSupportGLES20 = info.reqGlEsVersion >= 0x20000;
			sDetectedDevice = true;
		}
		return sSupportGLES20;
	}

	/**
	 * <br>
	 * 功能简述:是否使用getjar付功能付费的国家 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isPurchaseByGetjarContury(Context context) {
		boolean bRet = true;
		String country = getCountry(context);

		// 圣诞限免活动 -----begin
		// if (isLimitFreeDate() && isValidLimitFreeInstall(context)) {
		// return true;
		// }
		// 圣诞限免活动结束
		for (int i = 0; i < PAY_NOT_BY_GETJER_COUNTRY.length; i++) {
			if (PAY_NOT_BY_GETJER_COUNTRY[i].equals(country)) {
				bRet = false;
				break;
			}
		}
		return bRet;
	}

	/**
	 * <br>
	 * 功能简述:圣诞限免 23-24 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @return
	 */
	public static boolean isLimitFreeDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		String today = sdf.format(date);
        return today.compareTo("2013-12-23") >= 0 && today.compareTo("2013-12-25") < 0;
    }

	public static boolean canHideNavBar() {
        return !isSimilarModel(KITKAT_WITHOUT_NAVBAR);
    }

	public static boolean isSupportBindWidget(Context context) {
		if (!sDetectedBindWidget) {
			sSupportBindWidget = false;
			if (Build.VERSION.SDK_INT >= 16) {
				try {
					// 在某些设备上，没有支持"android.appwidget.action.APPWIDGET_BIND"的activity
					Intent intent = new Intent("android.appwidget.action.APPWIDGET_BIND");
					PackageManager packageManager = context.getPackageManager();
					List<ResolveInfo> list = packageManager.queryIntentActivities(intent, 0);
					if (list == null || list.size() <= 0) {
						sSupportBindWidget = false;
					} else {
						// 假如有支持上述action的activity，还需要判断是否已经进行了授权创建widget
						AppWidgetManager.class.getMethod("bindAppWidgetIdIfAllowed", int.class, ComponentName.class);
						sSupportBindWidget = true;
					}
				} catch (NoSuchMethodException e) { // 虽然是4.1以上系统，但是不支持绑定权限，仍按列表方式添加系统widget
					e.printStackTrace();
				}
			}
			sDetectedBindWidget = true;
		}
		return sSupportBindWidget;
	}

	/**
	 * <br>
	 * 功能简述:判断api是否大于等于9 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @return
	 */
	public static boolean isSDKGreaterNine() {
		boolean result = false;
		if (Build.VERSION.SDK_INT >= 9) {
			result = true;
		}
		return result;
	}

	/**
	 * <br>
	 * 功能简述:获取android版本 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @return
	 */
	public static int getAndroidSDKVersion() {
		return Build.VERSION.SDK_INT;
	}

	/**
	 * 获取设备Gmail帐号
	 * 
	 * @param context
	 * @return
	 */
	public static Pattern compileEmailAddress() {
		return Pattern.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@" + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "("
				+ "\\." + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+");
	}

	public static String getGmail(Context context) {
		// API level 8+
		Pattern emailPattern = compileEmailAddress();
		Account[] accounts = AccountManager.get(context).getAccounts();
		for (Account account : accounts) {
			if (emailPattern.matcher(account.name).matches()) {
				String possibleEmail = account.name;
				return possibleEmail;
			}
		}
		return null;
	}

	public static String[] getGmails(Context context) {
		// API level 8+
		Account[] accounts = AccountManager.get(context).getAccountsByType("com.google");
		String[] accounts_str = new String[accounts.length];
		for (int i = 0; i < accounts.length; i++) {
			accounts_str[i] = accounts[i].name;
		}
		return accounts_str;
	}

	/**
	 *将€7.99截取价格部分字串
	 * @param str
	 * @return
     */
	public static String getPrice(String str) {
		Pattern p = Pattern.compile("(\\d+\\.\\d+)");
		Matcher m = p.matcher(str);
		if (m.find()) {
			return m.group(1) == null ? "" : m.group(1);
		} else {
			p = Pattern.compile("(\\d+)");
			m = p.matcher(str);
			if (m.find()) {
				return m.group(1) == null ? "" : m.group(1);
			} else {
				return "";
			}
		}
	}

	public static String getSign(String str) {
		Pattern p = Pattern.compile("(\\W+)");
		Matcher m = p.matcher(str);
		if (m.find()) {
			return m.group(1) == null ? "" : m.group(1);
		}
		return "";
	}

	@SuppressLint("NewApi")
	public static void hideSmartbar(Window window) {
		if (isMeizu() && Build.VERSION.SDK_INT >= 14) {
			window.getDecorView().setSystemUiVisibility(HIDE_SMARTBAR);// 2隐藏，0显示
		}
	}

	/**
	 * 获取网络类型
	 * 
	 * @author huyong
	 * @param context
	 * @return 1 for 移动，2 for 联通，3 for 电信，-1 for 不能识别
	 */
	public static int getNetWorkType(Context context) {
		int netType = -1;
		/*String simOperator = getSimOperator(context);
		if (simOperator != null) {
			if (simOperator.startsWith("46000") || simOperator.startsWith("46002")) {
				// 因为移动网络编号46000下的IMSI已经用完，
				// 所以虚拟了一个46002编号，134/159号段使用了此编号
				// 中国移动
				netType = NETTYPE_MOBILE;
			} else if (simOperator.startsWith("46001")) {
				// 中国联通
				netType = NETTYPE_UNICOM;
			} else if (simOperator.startsWith("46003")) {
				// 中国电信
				netType = NETTYPE_TELECOM;
			}
		}*/
		return netType;
	}

	/**
	 * 是否cmwap连接
	 * 
	 * @author huyong
	 * @param context
	 * @return
	 */
	public static boolean isCWWAPConnect(Context context) {
		boolean result = false;
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivity.getActiveNetworkInfo();
		if (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE) {
			if (Proxy.getDefaultHost() != null || Proxy.getHost(context) != null) {
				result = true;
			}
		}

		return result;
	}

	/**
	 * 获取网关
	 * 
	 * @author huyong
	 * @param context
	 * @return
	 */
	public static String getProxyHost(Context context) {
		return Proxy.getHost(context);
	}

	public static int getProxyPort(Context context) {
		return Proxy.getPort(context);
	}

	// 获取本地IP函数
	public static String getLocalIPAddress() {
		try {
			for (Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces(); enumeration
					.hasMoreElements();) {
				NetworkInterface intf = enumeration.nextElement();
				for (Enumeration<InetAddress> enumIPAddr = intf.getInetAddresses(); enumIPAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIPAddr.nextElement();
					// 如果不是回环地址
					if (!inetAddress.isLoopbackAddress()) {
						// 直接返回本地IP地址
						return inetAddress.getHostAddress();
					}
				}
			}
		} catch (SocketException ex) {
		}
		return null;
	}

//	/**
//	 * 获取用户运营商代码
//	 *
//	 * @return
//	 */
//	public static String getSimOperator(Context context) {
//		String simOperator = "000";
//		try {
//			if (context != null) {
//				// 从系统服务上获取了当前网络的MCC(移动国家号)，进而确定所处的国家和地区
//				TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//				simOperator = manager.getSimOperator();
//			}
//		} catch (Throwable e) {
//			// TODO: handle exception
//		}
//
//		// ====MODIFY====Zhu Qiyong====2014.09.23====
//		// 当客户端运营商编码不存在时，simOperator可能被设置为空，服务器端不会受理
//		// 故如果simOperator字符串为空，则置为“000”
//		return TextUtils.isEmpty(simOperator) ? "000" : simOperator;
//		// ====END MODIFY====
//	}

	/**
	 * 获取当前网络状态，wifi，GPRS，3G，4G
	 * 
	 * @param context
	 * @return
	 */
	public static String buildNetworkState(Context context) {
		// build Network conditions
		String ret = "";
		try {
			ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkinfo = manager.getActiveNetworkInfo();
			if (networkinfo != null && networkinfo.getType() == ConnectivityManager.TYPE_WIFI) {
				ret = "WIFI";
			} else if (networkinfo != null && networkinfo.getType() == ConnectivityManager.TYPE_MOBILE) {
				int subtype = networkinfo.getSubtype();
				switch (subtype) {
				case TelephonyManager.NETWORK_TYPE_1xRTT:
				case TelephonyManager.NETWORK_TYPE_CDMA:
				case TelephonyManager.NETWORK_TYPE_EDGE:
				case TelephonyManager.NETWORK_TYPE_GPRS:
				case TelephonyManager.NETWORK_TYPE_IDEN:
					// 2G
					ret = "2G" /*
								 * + "(typeid = " + networkinfo.getType() +
								 * "  typename = " + networkinfo.getTypeName() +
								 * "  subtypeid = " + networkinfo.getSubtype() +
								 * "  subtypename = " +
								 * networkinfo.getSubtypeName() + ")"
								 */;
					break;
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
				case TelephonyManager.NETWORK_TYPE_HSDPA:
				case TelephonyManager.NETWORK_TYPE_HSPA:
				case TelephonyManager.NETWORK_TYPE_HSUPA:
				case TelephonyManager.NETWORK_TYPE_UMTS:
					// 3G,4G
					ret = "3G/4G" /*
								 * + "(typeid = " + networkinfo.getType() +
								 * "  typename = " + networkinfo.getTypeName() +
								 * "  subtypeid = " + networkinfo.getSubtype() +
								 * "  subtypename = " +
								 * networkinfo.getSubtypeName() + ")"
								 */;
					break;
				case TelephonyManager.NETWORK_TYPE_UNKNOWN:
				default:
					// unknow
					ret = "UNKNOW" /*
									 * + "(typeid = " + networkinfo.getType() +
									 * "  typename = " +
									 * networkinfo.getTypeName() +
									 * "  subtypeid = " +
									 * networkinfo.getSubtype() +
									 * "  subtypename = " +
									 * networkinfo.getSubtypeName() + ")"
									 */;
					break;
				}
			} else {
				ret = "UNKNOW" /*
								 * + "(typeid = " + networkinfo.getType() +
								 * "  typename = " + networkinfo.getTypeName() +
								 * ")"
								 */;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public static String getLocale(Context context) {
		return Locale.getDefault().getCountry();
	}

	/**
	 * 判断手机是否已ROOT
	 * 
	 * @return
	 */
	public static boolean findSu() {
		FileFilter suFilter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory()) {
					return false;
				}
				String fileName = pathname.getName();
                return fileName.equalsIgnoreCase("su");
            }
		};

		boolean ret = false;
		File xbinFile = new File("/system/xbin");
		File[] xbinSubFile = xbinFile.listFiles(suFilter);
		if (xbinSubFile != null && xbinSubFile.length > 0) {
			ret = true;
		}
		if (!ret) {
			File sbinFile = new File("/system/sbin");
			File[] sbinSubFile = sbinFile.listFiles(suFilter);
			if (sbinSubFile != null && sbinSubFile.length > 0) {
				ret = true;
			}

			File binFile = new File("/system/bin");
			File[] binSubFile = binFile.listFiles(suFilter);
			if (binSubFile != null && binSubFile.length > 0) {
				ret = true;
			}

			File suFile = new File("su");
			File[] suSubFile = suFile.listFiles(suFilter);
			if (suSubFile != null && suSubFile.length > 0) {
				ret = true;
			}

			File shFile = new File("sh");
			File[] shSubFile = shFile.listFiles(suFilter);
			if (shSubFile != null && shSubFile.length > 0) {
				ret = true;
			}
		}

		return ret;
	}

	/**
	 * 因为主题2.0新起进程，无法获取GoLauncher.getContext()， 所以重载此方法，以便主题2.0调用
	 * 
	 * @param context
	 * @return
	 */
	public static String getSimCountryIso(Context context) {
		String simCountryIso = "";
		try {
			// 从系统服务上获取了当前网络的MCC(移动国家号)，进而确定所处的国家和地区
			TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

			// SIM卡状态
			simCountryIso = manager.getSimCountryIso();

		} catch (Exception e) {
			// TODO: handle exception
		}
		if (simCountryIso == null || simCountryIso.trim().equals("")) {
			return getCountry(context);
		} else if (simCountryIso.contains(",")) {
			String[] simCountryIsoArray = simCountryIso.split(",");
			if (simCountryIsoArray != null && simCountryIsoArray.length > 1) {
				if (simCountryIsoArray[0] != null && !simCountryIsoArray[0].trim().equals("")) {
					return simCountryIsoArray[0];
				} else if (simCountryIsoArray[1] != null && !simCountryIsoArray[1].trim().equals("")) {
					return simCountryIsoArray[1];
				} else {
					return getCountry(context);
				}
			}
		}
		return simCountryIso;
	}
	/**
	 * 获取SD卡剩余空间
	 * 
	 * @return
	 */
	public static long getSDFreeSize() {
		// 取得SD卡文件路径
		File path = Environment.getExternalStorageDirectory();
		StatFs sf = new StatFs(path.getPath());
		long blockSize = sf.getBlockSize();
		// 空闲的数据块的数量
		long freeBlocks = sf.getAvailableBlocks();
		// 返回SD卡空闲大小
		return freeBlocks * blockSize; // 单位K
	}

	public static boolean isC8816() {
		return isPhone(C8816);
	}

	public static boolean isSONYC2305() {
		return isPhone(SONYC2305);
	}
	
	public static boolean isHT22I() {
		return isPhone(HT22I);
	}

	// 根据crash记录对LG定制的系统版本统计得：4.4版本的Rom名前缀为KRT16S,4.4.2版本的rom名的前缀为KVT89L和KOT49I。
	// 目前crash记录中只有这些系统信息所以先对这些内容进行处理
	/**
	 * <br>
	 * 功能简述:对LG定制的Rom版本进行检测，以处理edittext点击crash情况 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param checkLevel
	 *            :不同的检查级别检查的强度不一样
	 * @return
	 */
	public static boolean isLGAndOS4_4Up(int checkLevel) {
		boolean result = false;
		String display = Build.DISPLAY;
		String version = Build.VERSION.RELEASE;
		String brand = Build.BRAND;
		if (version == null || brand == null && display == null) {
			return result;
		}
		Pattern pattern = Pattern.compile("^K[A-Z]T");
		Matcher matcher = pattern.matcher(display);
		switch (checkLevel) {
		case LG_OS_CHECK_LEVEL1:
			for (String romName : LG_ROM_4_4_UP_PREFIX) {
				if (display.startsWith(romName)) {
					result = true;
				}
			}
			break;
		case LG_OS_CHECK_LEVEL2: // lg手机4.4以上系统
			if (brand.equalsIgnoreCase("lge") && version.startsWith("4.4")) {
				result = true;
			}
			break;
		case LG_OS_CHECK_LEVEL3: // 由LG版本系统命名规则看，其以后Rom版本名可能为K?T，清理范围最大的级别
			if (matcher.find()) {
				result = true;
			}
			break;
		case LG_OS_CHECK_LEVEL4: // LG定制ROM其他手机刷机情况有限，所以只对LG手机处理，且屏蔽掉其刷机情况，现在看来较理想的处理级别
		default:
			if (brand.equalsIgnoreCase("lge") && version.startsWith("4.4") && matcher.find()) {
				result = true;
			}
			break;
		}

		return result;
	}

	// 检测GPRS是否打开
	public static boolean isGprsEnable(Context context) {
		ConnectivityManager mCm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		Class mCmClass = mCm.getClass();
		Class[] argClasses = null;
		Object[] argObject = null;
		Boolean isOpen = false;
		try {

			Method method = mCmClass.getMethod("getMobileDataEnabled", argClasses);
			isOpen = (Boolean) method.invoke(mCm, argObject);
		} catch (Exception e) {
			Logcat.w("zhiping", e.toString());
		}

		return isOpen;
	}

	// 设置GPRS状态״̬
	public static void setGprsEnable(Context context, boolean isEnable) {
		ConnectivityManager mCm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		Class mCmClass = mCm.getClass();
		Class[] argClasses = new Class[1];
		argClasses[0] = boolean.class;
		try {
			Method method = mCmClass.getMethod("setMobileDataEnabled", argClasses);
			method.invoke(mCm, isEnable);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void setWifi(Context context, boolean onOff) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(onOff);
	}

	/**
	 * 获取GO天气EX程序的全局缓存目录<br>
	 * 优先在SD卡创建,如果不能在SD卡创建,则尝试可用的位置<br>
	 * @return file
	 */
	public static File getGoWeatherExExternalStorageDirectory() {
		final String cacheFolderName = "GOWeatherEX";
		// 优先使用SD位置
		File cacherFolder = new File(Environment.getExternalStorageDirectory(), cacheFolderName);
		if (!cacherFolder.exists()) {
			cacherFolder.mkdir();
		}
		if (cacherFolder.isDirectory()) {
			return cacherFolder;
		}
		// 部分机型在位置/mnt/emmc/存放资源
		cacherFolder = new File("/mnt/emmc/" + cacheFolderName);
		if (!cacherFolder.exists()) {
			cacherFolder.mkdir();
		}
		// TODO to be continue
		return cacherFolder;
	}
	
	/**
	 * 判断是否有sim卡 *
	 */
	public static boolean hasSIMCard(Context context) {
		TelephonyManager telMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		int simState = telMgr.getSimState();
		if (simState == TelephonyManager.SIM_STATE_READY) {
			return true;
		} else return simState != TelephonyManager.SIM_STATE_ABSENT;
	}

	public static boolean isXiaomi() {
		return Build.BRAND.toLowerCase().contains("xiaomi");
	}


	public static boolean isHuawei() {
		return Build.BRAND.toLowerCase().contains("huawei");
	}
	public static boolean isLG() {
		return Build.BRAND.toLowerCase().contains("lge");
	}

	public static boolean isOneplus() {
		return Build.BRAND.toLowerCase().contains("oneplus");
	}

	public static boolean isCoolpad() {
		return Build.BRAND.toLowerCase().contains("coolpad");
	}

	public static boolean isSamsung() {
		return Build.BRAND.toLowerCase().contains("samsung");
	}

	/**
	 * 获取设备DIP
	 * @param context
	 * @return
	 */
	public static int getDeviceDIP(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wMgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wMgr.getDefaultDisplay().getMetrics(dm);
		return dm != null ? dm.densityDpi : 0;
	}

	public static String getlocal(Context context) {
		return Locale.getDefault().getCountry();
	}

	/**
	 * 是否小米4<br>
	 *
	 * @return
	 */
	public static boolean isMi4() {
		return "MI 4LTE".equalsIgnoreCase(Build.MODEL);
	}

}
