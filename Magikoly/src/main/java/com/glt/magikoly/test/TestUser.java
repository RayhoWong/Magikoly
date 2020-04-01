package com.glt.magikoly.test;

/**
 * AB测试用户类
 *
 * @author wushuangshuang
 */
public enum TestUser {
    ///////////////////////////////////////////////新用户////////////////////////////////////////////////////////////////////////////////
    USER_DEFAULT(TestConstant.ABTEST_TESTUSER_DEFAULT, true, true, true, true, TestConstant.ABTEST_OLDS_DEFAULT, true, true), // 默认用户

    USER_PNM("pnm", false, false, false),
    USER_PMD("pmd", false, false, false);

//    USER_A300("a300", false, false, false),
//    USER_A301("a301", true, false, false),
//    USER_B301("b301", true, false, false),
//    USER_A310("a310", false, false, true),
//    USER_B310("b310", false, false, true),

    /////////////////////////////////////////////屏蔽用户//////////////////////////////////////////////////////////////////////////////
//    USER_B251("b251", false),
//    USER_A249("a249", false),
//    USER_B249("b249", false);

    ///////////////////////////////////////////老用户///////////////////////////////////////////////////////////////////////////////
   // USER_C252_OLD("c252", true, false, false, true, TestConstant.ABTEST_OLDS_DEFAULT, true, false);
//	USER_DAN("dan", true, true, false, false, TestConstant.ABTEST_OLDS_DEFAULT, true, false);
//	USER_BBZ("bbz", true, true, false, false, TestConstant.ABTEST_OLDS_DEFAULT, true, false);
//	USER_CCZ("ccz", true, true, false, false, TestConstant.ABTEST_OLDS_DEFAULT, true, false);
    /////////////////////////////////////////////////////////////构造和取值////////////////////////////////////////////////////////////////////////////////
    private String mValue; // 测试方案的值
    private boolean mIsDefaultUser; // 是否是默认用户
    private boolean mIsTestUser; // 是否是测试用户, 默认是测试用户
    private boolean mIsFirstTime; // 是否在第一段有效时间内
    private boolean mIsSecondTime; // 是否在第二段有效时间内
    private boolean mIsThirdTime; // 是否在第三段有效时间内
    private boolean mIsNewUser; // 是否是老用户
    private int mOlds; // AB方案出现的概率

    /**
     * 描述：true,是测试用户，false,不是测试用户
     * 其余参数是默认值(TestConstant)
     *
     * @param isTestUser
     */
    TestUser(String value, boolean isTestUser) {
        this.mValue = value;
        this.mIsTestUser = isTestUser;
        this.mIsDefaultUser = TestConstant.ABTEST_ISDEFAULTUSER_DEFAULT;
        this.mIsFirstTime = TestConstant.ABTEST_ISFISTTIME_DEFAULT;
        this.mIsSecondTime = TestConstant.ABTEST_ISSECONDTIME_DEFAULT;
        this.mIsThirdTime = TestConstant.ABTEST_ISTHIRDTIME_DEFAULT;
        this.mOlds = TestConstant.ABTEST_OLDS_DEFAULT;
        this.mIsNewUser = TestConstant.ABTEST_ISNEWUSER_DEFAULT;
    }

    /**
     * @param value：AB方案值
     * @param isFirstTime：在第一段时间内是否有效
     * @param olds:出现频率               其余参数默认值
     */
    TestUser(String value, boolean isFirstTime, int olds) {
        this.mValue = value;
        this.mIsFirstTime = isFirstTime;
        this.mOlds = olds;
        this.mIsTestUser = TestConstant.ABTEST_ISTESTUSER_DEFAULT;
        this.mIsDefaultUser = TestConstant.ABTEST_ISDEFAULTUSER_DEFAULT;
        this.mIsSecondTime = TestConstant.ABTEST_ISSECONDTIME_DEFAULT;
        this.mIsThirdTime = TestConstant.ABTEST_ISTHIRDTIME_DEFAULT;
        this.mIsNewUser = TestConstant.ABTEST_ISNEWUSER_DEFAULT;
    }

    /**
     * @param value        AB方案值
     * @param isFirstTime  是否在第一段时间内有效
     * @param isSecondTime 是否在第二段时间内有效
     *                     其余参数默认值
     */
    TestUser(String value, boolean isFirstTime, boolean isSecondTime) {
        this.mValue = value;
        this.mIsFirstTime = isFirstTime;
        this.mIsSecondTime = isSecondTime;
        this.mIsTestUser = TestConstant.ABTEST_ISTESTUSER_DEFAULT;
        this.mIsDefaultUser = TestConstant.ABTEST_ISDEFAULTUSER_DEFAULT;
        this.mIsThirdTime = TestConstant.ABTEST_ISTHIRDTIME_DEFAULT;
        this.mOlds = TestConstant.ABTEST_OLDS_DEFAULT;
        this.mIsNewUser = TestConstant.ABTEST_ISNEWUSER_DEFAULT;
    }

    /**
     * @param value：方案值
     * @param isFirstTime：是否在第一段时间内有效
     * @param isSecondTime：是否在第二段时间内有效
     * @param olds：出现频率                其余参数默认值
     */
    TestUser(String value, boolean isFirstTime, boolean isSecondTime, int olds) {
        this.mValue = value;
        this.mIsFirstTime = isFirstTime;
        this.mIsSecondTime = isSecondTime;
        this.mOlds = olds;
        this.mIsTestUser = TestConstant.ABTEST_ISTESTUSER_DEFAULT;
        this.mIsDefaultUser = TestConstant.ABTEST_ISDEFAULTUSER_DEFAULT;
        this.mIsThirdTime = TestConstant.ABTEST_ISTHIRDTIME_DEFAULT;
        this.mIsNewUser = TestConstant.ABTEST_ISNEWUSER_DEFAULT;
    }

    /**
     * @param value
     * @param isFirstTime
     * @param isSecondTime
     * @param isThirdTime
     */
    TestUser(String value, boolean isFirstTime, boolean isSecondTime,
             boolean isThirdTime) {
        this.mValue = value;
        this.mIsFirstTime = isFirstTime;
        this.mIsSecondTime = isSecondTime;
        this.mIsThirdTime = isThirdTime;
        this.mIsTestUser = TestConstant.ABTEST_ISTESTUSER_DEFAULT;
        this.mIsDefaultUser = TestConstant.ABTEST_ISDEFAULTUSER_DEFAULT;
        this.mOlds = TestConstant.ABTEST_OLDS_DEFAULT;
        this.mIsNewUser = TestConstant.ABTEST_ISNEWUSER_DEFAULT;
    }

    /**
     * @param value
     * @param isFirstTime
     * @param isSecondTime
     * @param isThirdTime
     * @param olds
     */
    TestUser(String value, boolean isFirstTime, boolean isSecondTime,
             boolean isThirdTime, int olds) {
        this.mValue = value;
        this.mIsFirstTime = isFirstTime;
        this.mIsSecondTime = isSecondTime;
        this.mIsThirdTime = isThirdTime;
        this.mOlds = olds;
        this.mIsTestUser = TestConstant.ABTEST_ISTESTUSER_DEFAULT;
        this.mIsDefaultUser = TestConstant.ABTEST_ISDEFAULTUSER_DEFAULT;
        this.mIsNewUser = TestConstant.ABTEST_ISNEWUSER_DEFAULT;
    }

    /**
     * @param value
     * @param isDefaultUser
     * @param isFirstTime
     * @param isSecondTime
     * @param isThirdTime
     */
    TestUser(String value, boolean isDefaultUser, boolean isFirstTime,
             boolean isSecondTime, boolean isThirdTime) {
        this.mValue = value;
        this.mIsFirstTime = isFirstTime;
        this.mIsSecondTime = isSecondTime;
        this.mIsThirdTime = isThirdTime;
        this.mIsDefaultUser = isDefaultUser;
        this.mIsTestUser = TestConstant.ABTEST_ISTESTUSER_DEFAULT;
        this.mOlds = TestConstant.ABTEST_OLDS_DEFAULT;
        this.mIsNewUser = TestConstant.ABTEST_ISNEWUSER_DEFAULT;
    }

    /**
     * @param value
     * @param isDefaultUser
     * @param isFirstTime
     * @param isSecondTime
     * @param isThirdTime
     * @param olds
     */
    TestUser(String value, boolean isDefaultUser, boolean isFirstTime,
             boolean isSecondTime, boolean isThirdTime, int olds) {
        this.mValue = value;
        this.mIsDefaultUser = isDefaultUser;
        this.mIsFirstTime = isFirstTime;
        this.mIsSecondTime = isSecondTime;
        this.mIsThirdTime = isThirdTime;
        this.mOlds = olds;
        this.mIsTestUser = TestConstant.ABTEST_ISTESTUSER_DEFAULT;
        this.mIsNewUser = TestConstant.ABTEST_ISNEWUSER_DEFAULT;
    }

    /**
     * @param value
     * @param isDefaultUser
     * @param isFirstTime
     * @param isSecondTime
     * @param isThirdTime
     * @param olds
     * @param isTestUser
     */
    TestUser(String value, boolean isDefaultUser, boolean isFirstTime,
             boolean isSecondTime, boolean isThirdTime, int olds,
             boolean isTestUser) {
        this.mValue = value;
        this.mIsDefaultUser = isDefaultUser;
        this.mIsFirstTime = isFirstTime;
        this.mIsSecondTime = isSecondTime;
        this.mIsThirdTime = isThirdTime;
        this.mOlds = olds;
        this.mIsTestUser = isTestUser;
        this.mIsNewUser = TestConstant.ABTEST_ISNEWUSER_DEFAULT;
    }

    /**
     * @param value
     * @param isDefaultUser
     * @param isFirstTime
     * @param isSecondTime
     * @param isThirdTime
     * @param olds
     * @param isTestUser
     * @param isNewUser
     */
    TestUser(String value, boolean isDefaultUser, boolean isFirstTime,
             boolean isSecondTime, boolean isThirdTime, int olds,
             boolean isTestUser, boolean isNewUser) {
        this.mValue = value;
        this.mIsDefaultUser = isDefaultUser;
        this.mIsFirstTime = isFirstTime;
        this.mIsSecondTime = isSecondTime;
        this.mIsThirdTime = isThirdTime;
        this.mOlds = olds;
        this.mIsTestUser = isTestUser;
        this.mIsNewUser = isNewUser;
    }

    /**
     * @return
     */
    public String getValue() {
        return mValue;
    }

    /**
     * @return
     */
    public boolean isDefaultUser() {
        return mIsDefaultUser;
    }

    /**
     * @return
     */
    public boolean isFirstTimeValid() {
        return mIsFirstTime;
    }

    /**
     * @return
     */
    public boolean isSecondTimeValid() {
        return mIsSecondTime;
    }

    /**
     * @return
     */
    public boolean isThirdTimeValid() {
        return mIsThirdTime;
    }

    /**
     * @return
     */
    public boolean isTestUser() {
        return mIsTestUser;
    }

    /**
     * 是否是新用户
     *
     * @return
     */
    public boolean isNewUser() {
        return mIsNewUser;
    }

    /**
     * @return
     */
    public int getOlds() {
        return mOlds;
    }
}
