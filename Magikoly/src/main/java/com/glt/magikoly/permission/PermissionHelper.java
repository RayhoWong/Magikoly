package com.glt.magikoly.permission;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;

import com.glt.magikoly.event.PermissionEvent;
import com.glt.magikoly.thread.FaceThreadExecutorProxy;
import com.glt.magikoly.utils.Logcat;

import org.greenrobot.eventbus.EventBus;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class PermissionHelper {
    private static final String PERMISSION_SHARE_PREFERENCES = "permission_share_preferences";
    private static LinkedHashMap<String, PermissionResult> sRequestAction = new LinkedHashMap<>();
    private volatile static boolean sRequesting = false;
    private volatile static String sCurrentRequestPermission = "";

    public static void requestPermission(Context context, String permission, OnPermissionResult onPermissionResult, int entrance) {
        Logcat.d("xiaowu_permission", Log.getStackTraceString(new Throwable()));
        int flag = PermissionCheckCompat.checkPermission(context, permission);
        if (flag == PackageManager.PERMISSION_GRANTED) {
            if (onPermissionResult != null) {
                Logcat.d("xiaowu_permission", "onPermissionGrant permission:" + permission);
                onPermissionResult.onPermissionGrant(permission);
            }
        } else {
            addRequestAction(context, permission, onPermissionResult, entrance);
            startNextRequest(context);
        }
    }

    private static void addRequestAction(Context context, String permission, OnPermissionResult onPermissionResult, int entrance) {
        PermissionResult permissionResult = sRequestAction.get(permission);
        if (permissionResult == null) {
            permissionResult = new PermissionResult(permission);
            sRequestAction.put(permission, permissionResult);
        }
        permissionResult.setContext(context);
        permissionResult.setEntrance(entrance);
        if (onPermissionResult != null) {
            permissionResult.add(onPermissionResult);
        }
    }

    private static Context startNextRequest(Context context) {
        Context requestContext = null;
        if (!sRequesting) {
            Set<Map.Entry<String, PermissionResult>> entrySet = sRequestAction.entrySet();
            Iterator<Map.Entry<String, PermissionResult>> entryIterator = entrySet.iterator();
            if (entryIterator.hasNext()) {
                Map.Entry<String, PermissionResult> permissionResultEntry = entryIterator.next();
                String permission = permissionResultEntry.getKey();
                Context entryContext = permissionResultEntry.getValue().getContext();
                requestContext = context;
                if (entryContext != null) {
                    //优先获取发起请求权限的上下文
                    requestContext = entryContext;
                    if (entryContext instanceof PermissionActivity || entryContext instanceof PermissionFragmentActivity) {
                        if (((Activity) entryContext).isFinishing()) {
                            requestContext = context;
                        }
                    } else if (context instanceof PermissionProxy) {
                        requestContext = context;
                    }
                }
                ISpecialPermission iSpecialPermission = Permissions.SPECIAL_PERMISSIONS.get(permission);
                if (iSpecialPermission != null) {
                    if (requestContext instanceof PermissionActivity || entryContext instanceof PermissionFragmentActivity) {
                        iSpecialPermission.openPermissionPage((Activity) requestContext);
                    } else {
                        PermissionProxy.requestPermission(requestContext, permission, true);
                    }
                } else {
                    if (requestContext instanceof PermissionActivity) {
                        ((PermissionActivity) requestContext).setRequesting(true);
                        ActivityCompat.requestPermissions((PermissionActivity) requestContext, new String[]{permission}, PermissionActivity.REQUEST_CODE);
                    } else if (requestContext instanceof PermissionFragmentActivity) {
                        ActivityCompat.requestPermissions((PermissionFragmentActivity) requestContext, new String[]{permission}, PermissionFragmentActivity.REQUEST_CODE);
                        ((PermissionFragmentActivity) requestContext).setRequesting(true);
                    } else {
                        PermissionProxy.requestPermission(requestContext, permission, false);
                    }
                }
                sCurrentRequestPermission = permission;
                final int entrance = permissionResultEntry.getValue().getEntrance();
                final String permissionFinal = permission;
                final Context requestContextFinal = requestContext;
                FaceThreadExecutorProxy.execute(new Runnable() {
                    @Override
                    public void run() {
                        Permission103OperationStatistic.permissionStatistic(requestContextFinal,
                                permissionFinal, entrance);
                    }
                });
                sRequesting = true;
            }
        }
        return requestContext;
    }

    static void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (sRequesting) {
            ISpecialPermission iSpecialPermission = Permissions.SPECIAL_PERMISSIONS.get(sCurrentRequestPermission);
            PermissionResult remove = sRequestAction.remove(sCurrentRequestPermission);
            if (iSpecialPermission != null) {
                int flag = iSpecialPermission.checkPermission(activity);
                if (remove != null) {
                    if (flag == PackageManager.PERMISSION_GRANTED) {
                        for (OnPermissionResult onPermissionResult : remove.getOnPermissionResults()) {
                            Logcat.d("xiaowu_permission", "onPermissionGrant permission:" + sCurrentRequestPermission);
                            onPermissionResult.onPermissionGrant(sCurrentRequestPermission);
                        }
                    } else if (flag == PackageManager.PERMISSION_DENIED) {
                        for (OnPermissionResult onPermissionResult : remove.getOnPermissionResults()) {
                            onPermissionResult.onPermissionDeny(sCurrentRequestPermission, true);
                        }
                    }
                    if (!TextUtils.isEmpty(sCurrentRequestPermission)) {
                        EventBus.getDefault().post(new PermissionEvent(sCurrentRequestPermission));
                    }
                }

            }
            startNextRequestForProxy(activity);
        }

    }

    public static void resetCurrentRequest() {
        if (sRequesting) {
            sRequestAction.remove(sCurrentRequestPermission);
            sCurrentRequestPermission = "";
            sRequesting = false;
        }
    }

    static void startNextRequestForProxy(Activity activity) {
        sRequesting = false;
        sCurrentRequestPermission = "";
        Context lastContext = startNextRequest(activity);
        if (lastContext != activity && !(activity instanceof PermissionActivity) && !(activity instanceof PermissionFragmentActivity)) {
            activity.overridePendingTransition(0, 0);
            activity.finish();
        }
    }

    static void onRequestPermissionsResult(Activity activity, String permission, boolean last) {
        if (sRequesting) {
            int flag = PermissionCheckCompat.checkPermission(activity, permission);
            PermissionResult permissionResult = sRequestAction.remove(permission);
            if (permissionResult != null) {
                if (flag == PackageManager.PERMISSION_GRANTED) {
                    for (OnPermissionResult onPermissionResult : permissionResult.getOnPermissionResults()) {
                        Logcat.d("xiaowu_permission", "onPermissionGrant permission:" + permission);
                        onPermissionResult.onPermissionGrant(permission);
                    }
                    setPermissionDeny(activity, permission, false);
                } else {
                    boolean hasShow = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
                    Logcat.d("xiaowu_permission", "onPermissionDeny permission:" + permission + " never:" + !hasShow);
                    for (OnPermissionResult onPermissionResult : permissionResult.getOnPermissionResults()) {
                        onPermissionResult.onPermissionDeny(permission, !hasShow);
                    }
                    setPermissionDeny(activity, permission, !hasShow);
                }
                if (!TextUtils.isEmpty(permission)) {
                    EventBus.getDefault().post(new PermissionEvent(permission));
                }
            }
            if (last) {
                startNextRequestForProxy(activity);
            }
        }
    }

    public static void requestCameraPermission(Context context, OnPermissionResult onPermissionResult, int entrance) {
        requestPermission(context, Permissions.CAMERA, onPermissionResult, entrance);
    }

    public static void requestReadPermission(Context context, OnPermissionResult onPermissionResult, int entrance) {
        requestPermission(context, Permissions.READ_EXTERNAL_STORAGE, onPermissionResult, entrance);
    }

    public static void requestWritePermission(Context context, OnPermissionResult onPermissionResult, int entrance) {
        requestPermission(context, Permissions.WRITE_EXTERNAL_STORAGE, onPermissionResult, entrance);
    }

    public static void requestReadContactsPermission(Context context, OnPermissionResult onPermissionResult, int entrance) {
        requestPermission(context, Permissions.READ_CONTACTS, onPermissionResult, entrance);
    }

    public static void requestAccessCoarseLocationPermission(Context context, OnPermissionResult onPermissionResult, int entrance) {
        requestPermission(context, Permissions.ACCESS_COARSE_LOCATION, onPermissionResult, entrance);
    }

    public static void requestAccessFineLocationPermission(Context context, OnPermissionResult onPermissionResult, int entrance) {
        requestPermission(context, Permissions.ACCESS_FINE_LOCATION, onPermissionResult, entrance);
    }

    public static void requestGetAccountsPermission(Context context, OnPermissionResult onPermissionResult, int entrance) {
        requestPermission(context, Permissions.GET_ACCOUNTS, onPermissionResult, entrance);
    }

    public static boolean hasPermission(Context context, String permission) {
        return PermissionCheckCompat.checkPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }


    public static boolean hasReadStoragePermission(Context context) {
        return hasPermission(context, Permissions.READ_EXTERNAL_STORAGE);
    }

    public static boolean hasWriteStoragePermission(Context context) {
        return hasPermission(context, Permissions.WRITE_EXTERNAL_STORAGE);
    }

    public static boolean hasReadContactsPermission(Context context) {
        return hasPermission(context, Permissions.READ_CONTACTS);
    }

    public static boolean hasWriteContactsPermission(Context context) {
        return hasPermission(context, Permissions.WRITE_CONTACTS);
    }

    public static boolean hasAccessCoarseLocationPermission(Context context) {
        return hasPermission(context, Permissions.ACCESS_COARSE_LOCATION);
    }

    public static boolean hasReadSmsPermission(Context context) {
        return hasPermission(context, Permissions.READ_SMS);
    }

    public static boolean hasReadCallLogPermission(Context context) {
        return hasPermission(context, Permissions.READ_CALL_LOG);
    }

    public static boolean hasCameraPermission(Context context) {
        return hasPermission(context, Permissions.CAMERA);
    }

    public static boolean isPermissionGroupDeny(Context context, String permission) {
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(PERMISSION_SHARE_PREFERENCES, Context.MODE_PRIVATE);
        String key = Permissions.PERMISSION_GROUP.get(permission);
        if (key != null) {
            return sharedPreferences.getBoolean(key, false);
        }
        return false;
    }

    /**
     * 不可对包外提供
     *
     * @param context
     * @param permission
     * @param deny
     */
    private static void setPermissionDeny(Context context, String permission, boolean deny) {
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(PERMISSION_SHARE_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String key = Permissions.PERMISSION_GROUP.get(permission);
        if (key != null) {
            if (sharedPreferences.getBoolean(key, false) != deny) {
                editor.putBoolean(key, deny);
                editor.apply();
            }
        }

    }
}
