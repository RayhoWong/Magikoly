package com.glt.magikoly;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.glt.magikoly.utils.AppUtils;
import com.glt.magikoly.utils.Logcat;

import java.io.File;

import static com.glt.magikoly.constants.PackageName.PACKAGE_NAME;

public class FaceEnv {
    public static boolean sSIT;
    public static final String PROCESS_NAME = PACKAGE_NAME;
    public static final String PROCESS_DAEMON_ASSISTANT = PROCESS_NAME + ":daemonAssistant";
    public static String sChannelId = "230";
    public static boolean sABTestDebug;
    public static boolean sSubscribeSdkTestServer;

    public static void loadConfig(Context context) {
        sSIT = !AppUtils.isProductionMode(context);
        sChannelId = AppUtils.getChannel(context);
        sABTestDebug = sSIT && !TextUtils.isEmpty(AppUtils.getABTestUser(context));
    }

    public static final class Path {
        /**
         * sdcard head
         */
        private final static String SDCARD = Environment.getExternalStorageDirectory().getPath();

        // 存储路径
        public final static String FACE_DIR = SDCARD + "/magikoly";
        public static final String LOG_DIR = FACE_DIR + "/logs/";

        /**
         * 服务器AB测试缓存路径
         */
        public static final String ABCONFIG_CACHE = FACE_DIR + "/abconfig/cache";

        public static final String HOTWORD_CACHE = FACE_DIR + "/hotword/cache";
    }

    public static class InternalPath {

        public static final String BITMAP_CACHE_DIR = "bitmapCache";
        public static final String ABTEST_DIR = "abconfig";
        public static final String HOTWORD_DIR = "hotword";
        public static final String PHOTO_CROP_DIR = "crop_dir" + File.separator;
        public static final String ANIMAL_DIR = "animal"  + File.separator;

        //通过该方法获取内置路径，属于该程序私有目录，其他程序无法访问
        public final static String getInnerFilePath(Context context, String targetFilePath) {
            String root = targetFilePath;
            String target = "";
            if (targetFilePath.contains(File.separator)) {
                String[] split = targetFilePath.split(File.separator, 2);
                root = split[0];
                target = split[1];
            }
            String absolutePath = new File(context.getDir(root, Context.MODE_PRIVATE), target).getAbsolutePath();
            Logcat.d("xiaowu_inner_path:", absolutePath);
            return absolutePath;
        }

        //通过该方法获取路径，在内置内存不足的情况下，有可能被系统回收内存
        public final static String getCacheInnerFilePath(Context context, String targetFilePath) {
            Logcat.d("xiaowu_inner_path:", targetFilePath);
            String absolutePath = new File(context.getCacheDir(), targetFilePath).getAbsolutePath();
            Logcat.d("xiaowu_inner_path:", absolutePath);
            return absolutePath;
        }
    }
}
