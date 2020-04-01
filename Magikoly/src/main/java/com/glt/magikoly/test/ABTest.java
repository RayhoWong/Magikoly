package com.glt.magikoly.test;

import android.text.TextUtils;
import com.glt.magikoly.FaceEnv;
import com.glt.magikoly.utils.Logcat;
import com.glt.magikoly.version.VersionController;

import java.util.ArrayList;
import java.util.List;

/**
 * ABTEST测试
 * @author wushuangshuang
 *
 */
public class ABTest {
	private static final String KEY_USER = "user";
	private static ABTest sInstance;
	private String mUser; // 测试用户

	private ABTest() {
		init();
	}

	public synchronized static ABTest getInstance() {
		if (null == sInstance) {
			sInstance = new ABTest();
		}
		return sInstance;
	}

	private void init() {
		String user;
		if (FaceEnv.sABTestDebug) {
			user = null;
		} else {
			user = TestUtils.getTestUserString(KEY_USER, "");
		}

		if (user != null && TestUtils.isTallyOldUserTest()) {
			user = null;
		}
		
		if (TextUtils.isEmpty(user)) {
			if (FaceEnv.sABTestDebug) {
				user = TestUtils.getTestUser();
			} else {
				user = genUser();
				if (user == null) {
					user = getDefaultUser();
				}
				TestUtils.saveTestUserString(KEY_USER, user);
			}
		}
		Logcat.i("Test", "AB TestUser: " + user);
		mUser = user;
	}
	
	/**
	 * 是否是测试用户 isTestUser(这里用一句话描述这个方法的作用) (这里描述这个方法适用条件 – 可选)
	 * 
	 *            {@link TestUser}
	 * @return boolean
	 * @exception
	 * @since 1.0.0
	 */
	public boolean isTestUser(TestUser user) {
		String value = user.getValue();
		if (!TextUtils.isEmpty(value)) {
			return value.equals(getUser());
		}
		return false;
	}

	/**
	 * 获取当前测试用户 getUser(这里用一句话描述这个方法的作用) (这里描述这个方法适用条件 – 可选)
	 * 
	 * @return String
	 * @exception
	 * @since 1.0.0
	 */
	public String getUser() {
		if (TextUtils.isEmpty(mUser)) {
			if (FaceEnv.sABTestDebug) {
				mUser = TestUtils.getTestUser();
			} else {
				mUser = TestUtils.getTestUserString(KEY_USER, genUser());
			}
		}
		return mUser;
	}
	
	private String genUser() {
		if (VersionController.isNewUser()) {
			return genNewUser();
		} else {
			return genOldUser();
		}
	}

	private String genNewUser() {
		try {
			List<TestUser> userList = getNewUserList(TestUtils.isValid(
					TestTime.STARTTIME.getTimeValue(),
					TestTime.FISTTIME.getTimeValue()), TestUtils.isValid(
					TestTime.FISTTIME.getTimeValue(),
					TestTime.SECONDTIME.getTimeValue()), TestUtils.isValid(
					TestTime.SECONDTIME.getTimeValue(),
					TestTime.ENDTIME.getTimeValue()));
			
			if (userList == null || userList.size() == 0) {
				return null;
			}
			int index = (int) (Math.random() * userList.size());
			TestUser user = userList.get(index);
			return user.getValue();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	private List<TestUser> getNewUserList(boolean firstTimeValid,
                                          boolean secondTimeValid, boolean thirdTimeValid) {
		TestUser[] users = TestUser.values();
		if (users == null) {
			throw new RuntimeException("not find test user");
		}

		List<TestUser> list = new ArrayList<TestUser>();
		for (int i = 0; i < users.length; i++) {
			TestUser user = users[i];
			if (user.isTestUser() && user.isNewUser()) {
				if ((firstTimeValid && user.isFirstTimeValid())
						|| (secondTimeValid && user.isSecondTimeValid())
						|| (thirdTimeValid && user.isThirdTimeValid())) {
					for (int j = 0; j < user.getOlds(); j++) {
						list.add(user);
					}
				}
			}
		}

		return list;
	}

	private String getDefaultUser() {
		TestUser[] users = TestUser.values();
		if (users == null || users.length == 0) {
			throw new RuntimeException("not find test user");
		}

		for (TestUser user : users) {
			try {
				if (VersionController.isNewUser() && user.isDefaultUser()
						&& user.isNewUser()) {

					return user.getValue();

				} else if (VersionController.isNewUser()
						&& user.isDefaultUser() && !user.isNewUser()) {
					return user.getValue();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	private String genOldUser() {
		try {
			List<TestUser> userList = getOldUserList(TestUtils.isValid(
					TestTime.OLDSTARTTIME.getTimeValue(),
					TestTime.OLDFISTTIME.getTimeValue()), TestUtils.isValid(
					TestTime.OLDFISTTIME.getTimeValue(),
					TestTime.OLDSECONDTIME.getTimeValue()), TestUtils.isValid(
					TestTime.OLDSECONDTIME.getTimeValue(),
					TestTime.OLDENDTIME.getTimeValue()));
			if (userList == null || userList.size() == 0) {
				return null;
			}
			int index = (int) (Math.random() * userList.size());
			if (userList.size() > index) {
				TestUser user = userList.get(index);
				return user.getValue();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private List<TestUser> getOldUserList(boolean firstTimeValid,
                                          boolean secondTimeValid, boolean thirdTimeValid) {
		TestUser[] users = TestUser.values();
		if (users == null) {
			throw new RuntimeException("not find test user");
		}

		List<TestUser> list = new ArrayList<TestUser>();
		for (int i = 0; i < users.length; i++) {
			TestUser user = users[i];
			if (user.isTestUser() && !user.isNewUser()) {
				if ((firstTimeValid && user.isFirstTimeValid())
						|| (secondTimeValid && user.isSecondTimeValid())
						|| (thirdTimeValid && user.isThirdTimeValid())) {
					for (int j = 0; j < user.getOlds(); j++) {
						list.add(user);
					}
				}
			}
		}

		return list;
	}

	public void setTestUser(TestUser user) {
		mUser = user.getValue();
		TestUtils.saveTestUserString(KEY_USER, mUser);
	}

}
