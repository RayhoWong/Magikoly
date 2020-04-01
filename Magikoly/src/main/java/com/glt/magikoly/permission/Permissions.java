package com.glt.magikoly.permission;

import android.os.Build;

import com.glt.magikoly.utils.Machine;

import java.util.HashMap;

public final class Permissions {

    public static final String REQUEST_INSTALL_PACKAGES = "android.permission.REQUEST_INSTALL_PACKAGES"; // 8.0应用安装权限

    public static final String SYSTEM_ALERT_WINDOW = "android.permission.SYSTEM_ALERT_WINDOW"; // 6.0悬浮窗权限

    public static final String READ_CALENDAR = "android.permission.READ_CALENDAR"; // 读取日程提醒
    public static final String WRITE_CALENDAR = "android.permission.WRITE_CALENDAR"; // 写入日程提醒

    public static final String CAMERA = "android.permission.CAMERA"; // 拍照权限

    public static final String READ_CONTACTS = "android.permission.READ_CONTACTS"; // 读取联系人
    public static final String WRITE_CONTACTS = "android.permission.WRITE_CONTACTS"; // 写入联系人
    public static final String GET_ACCOUNTS = "android.permission.GET_ACCOUNTS"; // 访问账户列表

    public static final String ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION"; // 获取精确位置
    public static final String ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION"; // 获取粗略位置

    public static final String RECORD_AUDIO = "android.permission.RECORD_AUDIO"; // 录音权限

    public static final String READ_PHONE_STATE = "android.permission.READ_PHONE_STATE"; // 读取电话状态
    public static final String CALL_PHONE = "android.permission.CALL_PHONE"; // 拨打电话
    public static final String READ_CALL_LOG = "android.permission.READ_CALL_LOG"; // 读取通话记录
    public static final String WRITE_CALL_LOG = "android.permission.WRITE_CALL_LOG"; // 写入通话记录
    public static final String ADD_VOICEMAIL = "com.android.voicemail.permission.ADD_VOICEMAIL"; // 添加语音邮件
    public static final String USE_SIP = "android.permission.USE_SIP"; // 使用SIP视频
    public static final String PROCESS_OUTGOING_CALLS = "android.permission.PROCESS_OUTGOING_CALLS"; // 处理拨出电话

    public static final String BODY_SENSORS = "android.permission.BODY_SENSORS"; // 传感器

    public static final String SEND_SMS = "android.permission.SEND_SMS"; // 发送短信
    public static final String RECEIVE_SMS = "android.permission.RECEIVE_SMS"; // 接收短信
    public static final String READ_SMS = "android.permission.READ_SMS"; // 读取短信
    public static final String RECEIVE_WAP_PUSH = "android.permission.RECEIVE_WAP_PUSH"; // 接收WAP PUSH信息
    public static final String RECEIVE_MMS = "android.permission.RECEIVE_MMS"; // 接收彩信
    public static final String READ_CELL_BROADCASTS = "android.permission.READ_CELL_BROADCASTS";

    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE"; // 读取外部存储
    public static final String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE"; // 写入外部存储

    public static final String WRITE_SETTINGS = "android.permission.WRITE_SETTINGS";

    public static final String ACCESS_NOTIFICATION_POLICY = "android.permission.ACCESS_NOTIFICATION_POLICY";
    /**
     * 包内访问，不可以更改属性
     */
    static final HashMap<String, String> PERMISSION_GROUP = new HashMap<>();
    static final HashMap<String, ISpecialPermission> SPECIAL_PERMISSIONS = new HashMap<>();

    static {
        PERMISSION_GROUP.put(READ_CALENDAR, "android.permission-group.CALENDAR");
        PERMISSION_GROUP.put(WRITE_CALENDAR, "android.permission-group.CALENDAR");


        PERMISSION_GROUP.put(CAMERA, "android.permission-group.CAMERA");


        PERMISSION_GROUP.put(READ_CONTACTS, "android.permission-group.CONTACTS");
        PERMISSION_GROUP.put(WRITE_CONTACTS, "android.permission-group.CONTACTS");
        PERMISSION_GROUP.put(GET_ACCOUNTS, "android.permission-group.CONTACTS");


        PERMISSION_GROUP.put(ACCESS_FINE_LOCATION, "android.permission-group.LOCATION");
        PERMISSION_GROUP.put(ACCESS_COARSE_LOCATION, "android.permission-group.LOCATION");

        PERMISSION_GROUP.put(RECORD_AUDIO, "android.permission-group.MICROPHONE");


        PERMISSION_GROUP.put(READ_PHONE_STATE, "android.permission-group.PHONE");
        PERMISSION_GROUP.put(CALL_PHONE, "android.permission-group.PHONE");
        PERMISSION_GROUP.put(READ_CALL_LOG, "android.permission-group.PHONE");
        PERMISSION_GROUP.put(WRITE_CALL_LOG, "android.permission-group.PHONE");
        PERMISSION_GROUP.put(ADD_VOICEMAIL, "android.permission-group.PHONE");
        PERMISSION_GROUP.put(USE_SIP, "android.permission-group.PHONE");
        PERMISSION_GROUP.put(PROCESS_OUTGOING_CALLS, "android.permission-group.PHONE");


        PERMISSION_GROUP.put(BODY_SENSORS, "android.permission-group.SENSORS");


        PERMISSION_GROUP.put(SEND_SMS, "android.permission-group.SMS");
        PERMISSION_GROUP.put(RECEIVE_SMS, "android.permission-group.SMS");
        PERMISSION_GROUP.put(READ_SMS, "android.permission-group.SMS");
        PERMISSION_GROUP.put(RECEIVE_WAP_PUSH, "android.permission-group.SMS");
        PERMISSION_GROUP.put(RECEIVE_MMS, "android.permission-group.SMS");
        PERMISSION_GROUP.put(READ_CELL_BROADCASTS, "android.permission-group.SMS");


        PERMISSION_GROUP.put(READ_EXTERNAL_STORAGE, "android.permission-group.STORAGE");
        PERMISSION_GROUP.put(WRITE_EXTERNAL_STORAGE, "android.permission-group.STORAGE");


        /**
         * 非危险权限的特殊权限（指的是打开系统某个设置项才可以使用）
         */
        SPECIAL_PERMISSIONS.put(WRITE_SETTINGS, new WriteSystemSetting());
        SPECIAL_PERMISSIONS.put(ACCESS_NOTIFICATION_POLICY, new RindModeSetting());
        SPECIAL_PERMISSIONS.put(SYSTEM_ALERT_WINDOW, new FloatWindowSetting());

        if (Build.MANUFACTURER.toLowerCase().contains("meizu") && !Machine.IS_SDK_ABOVE_6) {
            SPECIAL_PERMISSIONS.put(CAMERA, new MeiZuCameraBellowSDK6());
        }
    }
}