package com.glt.magikoly.permission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.UserHandle;
import android.provider.Settings;

import java.lang.reflect.Method;
import java.util.List;

/**
 *    desc   : 权限设置页（支持大部分国产手机）
 */
public final class PermissionSettingPage {

    private static final String MARK = Build.MANUFACTURER.toLowerCase();

    /**
     * 跳转到应用权限设置页面
     *
     * @param context 上下文对象
     * @param newTask 是否使用新的任务栈启动
     */
    public static void start(Context context, boolean newTask) {

        Intent intent;
        if (MARK.contains("huawei")) {
            intent = huawei(context);
        } else if (MARK.contains("xiaomi")) {
            intent = xiaomi(context);
        } else if (MARK.contains("oppo")) {
            intent = oppo(context);
        } else if (MARK.contains("vivo")) {
            intent = vivo(context);
        } else if (MARK.contains("meizu")) {
            intent = meizu(context);
        } else if (MARK.contains("Sony")){
          intent =   sony(context);
        } else if (MARK.contains("LG")) {
            intent = LG(context);
        } else if (MARK.contains("360")) {
            intent = phone360(context);
        } else {
            intent = google(context);
        }

       if (null != intent) {
           if (newTask || !(context instanceof Activity)) {
               intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
           }

           try {
               context.startActivity(intent);
           } catch (Exception e) {
               intent = google(context);
               context.startActivity(intent);
           }
       }
    }

    /**
     * 通过包名和权限名授予运行时权限
     *
     * @param packageName 包名
     * @param permission  权限名
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void grantPermission(Context context, String packageName, String permission) {
        try {
            PackageManager packageManager = context.getPackageManager();
            Method method = packageManager
                    .getClass()
                    .getMethod("grantRuntimePermission", String.class, String.class, UserHandle.class);
            method.setAccessible(true);
            method.invoke(packageManager, packageName, permission, android.os.Process.myUserHandle());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过包名和权限名取消运行时权限的授予
     *
     * @param packageName 包名
     * @param permission  权限名
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void revokePermission(Context context, String packageName, String permission) {
        try {
            PackageManager packageManager = context.getPackageManager();
            Method method = packageManager
                    .getClass()
                    .getMethod("revokeRuntimePermission", String.class, String.class, UserHandle.class);
            method.setAccessible(true);
            method.invoke(packageManager, packageName, permission, android.os.Process.myUserHandle());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static Intent google(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        return intent;
    }

    private static Intent huawei(Context context) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity"));
        if (hasIntent(context, intent)) return intent;
        intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.addviewmonitor.AddViewMonitorActivity"));
        if (hasIntent(context, intent)) return intent;
        intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.notificationmanager.ui.NotificationManagmentActivity"));
        return intent;
    }

    private static Intent xiaomi(Context context) {
        Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
        intent.putExtra("extra_pkgname", context.getPackageName());
        if (hasIntent(context, intent)) return intent;

        intent.setPackage("com.miui.securitycenter");
        if (hasIntent(context, intent)) return intent;

        intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
        if (hasIntent(context, intent)) return intent;

        intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
        return intent;
    }

    private static Intent oppo(Context context) {
        Intent intent = new Intent();
        intent.putExtra("packageName", context.getPackageName());
        intent.setClassName("com.color.safecenter", "com.color.safecenter.permission.floatwindow.FloatWindowListActivity");
        if (hasIntent(context, intent)) return intent;

        intent.setClassName("com.coloros.safecenter", "com.coloros.safecenter.sysfloatwindow.FloatWindowListActivity");
        if (hasIntent(context, intent)) return intent;

        intent.setClassName("com.oppo.safe", "com.oppo.safe.permission.PermissionAppListActivity");
        return intent;
    }

    private static Intent vivo(Context context) {
        Intent intent = new Intent();
        intent.setClassName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.FloatWindowManager");
        intent.putExtra("packagename", context.getPackageName());
        if (hasIntent(context, intent)) return intent;

        intent.setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.safeguard.SoftPermissionDetailActivity"));
        return intent;
    }

    private static Intent meizu(Context context) {
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        intent.putExtra("packageName", context.getPackageName());
        intent.setComponent(new ComponentName("com.meizu.safe", "com.meizu.safe.security.AppSecActivity"));
        return intent;
    }

    private static Intent sony(Context context) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("packageName",context.getPackageName());
        ComponentName comp = new ComponentName("com.sonymobile.cta", "com.sonymobile.cta.SomcCTAMainActivity");
        intent.setComponent(comp);
        return intent;
    }

    private static Intent LG(Context context) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("packageName", context.getPackageName());
        ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.Settings$AccessLockSummaryActivity");
        intent.setComponent(comp);
        return intent;
    }

    private static Intent phone360(Context context) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("packageName", context.getPackageName());
        ComponentName comp = new ComponentName("com.qihoo360.mobilesafe", "com.qihoo360.mobilesafe.ui.index.AppEnterActivity");
        intent.setComponent(comp);
        return intent;
    }

    private static boolean hasIntent(Context context, Intent intent) {
        return context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
    }

    private Intent doStartApplicationWithPackageName(Context context, String packagename) {
        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = context.getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            return null;
        }
        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);
        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = context.getPackageManager()
                .queryIntentActivities(resolveIntent, 0);
        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            // packageName参数2 = 参数 packname
            String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packageName参数2.mainActivityname]
            String className = resolveinfo.activityInfo.name;
            // LAUNCHER Intent
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            // 设置ComponentName参数1:packageName参数2:MainActivity路径
            ComponentName cn = new ComponentName(packageName, className);
            intent.setComponent(cn);
            return intent;
        }
        return null;
    }
}
