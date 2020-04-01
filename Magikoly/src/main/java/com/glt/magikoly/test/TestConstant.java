package com.glt.magikoly.test;

import com.glt.magikoly.FaceEnv;

/**
 * AB常量类
 * @author wushuangshuang
 *
 */
public class TestConstant {
	////////////////////////////AB默认值///////////////////////////////////
	public static final String ABTEST_TESTUSER_DEFAULT = "000"; // 默认的用户值
	public static final boolean ABTEST_ISDEFAULTUSER_DEFAULT = false; // 是否是默认测试用户
	public static final boolean ABTEST_ISTESTUSER_DEFAULT = true; // 是否是测试用户
	public static final boolean ABTEST_ISFISTTIME_DEFAULT = false; // 是否在第一段时间内有效
	public static final boolean ABTEST_ISSECONDTIME_DEFAULT = false; // 是否在第二段时间内有效
	public static final boolean ABTEST_ISTHIRDTIME_DEFAULT = false; // 是否在第三段时间内有效
	public static final boolean ABTEST_ISNEWUSER_DEFAULT = true; // 是否是新用户
	public static final int ABTEST_OLDS_DEFAULT = 1; // ab方案出现概率
	
	
	///////////////////////////////常量值//////////////////////////////////////
	public static final boolean IS_OPEN_OLDUSER_TEST = true; // 是否开启老用户测试
	public static final String SAVE_SP_KEY = "abtest";
	public static final String ABTEST_KEY = "abtest.txt";
	public static final String SAVE_ABTEST_KEY_PATH = FaceEnv.Path.FACE_DIR + "/abtest";
	public static final String RANDOM_COUNT = "randomcount";
	public static final float SMALL_VERSIONNAME = 1.01f;
	public static final float MAX_VERSIONNAME = 1.02f;
}
