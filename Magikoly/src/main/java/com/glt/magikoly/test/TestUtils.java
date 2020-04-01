package com.glt.magikoly.test;

import com.glt.magikoly.FaceAppState;
import com.glt.magikoly.pref.PrivatePreference;
import com.glt.magikoly.utils.AppUtils;
import com.glt.magikoly.version.VersionController;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * 
 * @author wushuangshuang
 *
 */
public class TestUtils {

	// 获取字符串数据
	public static synchronized String getTestUserString(String key, String... defValue) {
		PrivatePreference pref = PrivatePreference.getPreference(FaceAppState.getContext());
		if (null != defValue && defValue.length > 0) {
			return pref.getString(key, defValue[0]);
		} else {
			return pref.getString(key, "");
		}
	}

	// 保存字符串数据
	public static synchronized void saveTestUserString(String key, String value) {
		PrivatePreference pref = PrivatePreference.getPreference(FaceAppState.getContext());
		if (value == null) {
			pref.putString(key, "");
		} else {
			pref.putString(key, value);
		}
		pref.commit();
	}

	// 方便测试：读取key
	public static String getTestUser() {
		return AppUtils.getABTestUser(FaceAppState.getContext());
	}

	public static boolean isTimeValid(String start) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		try {
			Date before = df.parse(start);
			Date now = df.parse(df.format(new Date()));
			if (now.after(before)) {
				return true;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean isValid(String start, String end) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		try {
			Date before = df.parse(start);
			Date now = df.parse(df.format(new Date()));
			Date after = df.parse(end);
			if (now.before(after) && now.after(before)) {
				return true;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean isValidVersion() {
		float version = Float.parseFloat(getVersionName());
        return version > TestConstant.SMALL_VERSIONNAME
                && version < TestConstant.MAX_VERSIONNAME;

    }

	public static boolean isFistRandom() {
		PrivatePreference pref = PrivatePreference.getPreference(FaceAppState.getContext());
		return pref.getInt(TestConstant.RANDOM_COUNT, -1) == 1;
	}

	public static void setRandomCount() {
		PrivatePreference pref = PrivatePreference.getPreference(FaceAppState.getContext());
		pref.putInt(TestConstant.RANDOM_COUNT, 1);
	}

	private static String getVersionName() {
		String str = AppUtils
				.getVersionNameByPkgName(FaceAppState.getContext(),
						FaceAppState.getContext().getPackageName());
		char[] cha = str.toCharArray();
		for (int i = 0; i < cha.length; i++) {
			if (Character.isLetter(cha[i])) {
				return str.substring(0, i);
			}
		}

		return str;
	}

	/**
	 * 是否符合老用户测试
	 * @return
	 */
	public static boolean isTallyOldUserTest() {
        return VersionController.isNewVersionFirstRun() && !VersionController.isNewUser()
                && TestConstant.IS_OPEN_OLDUSER_TEST;

    }
}
