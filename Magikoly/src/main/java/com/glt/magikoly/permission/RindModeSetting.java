package com.glt.magikoly.permission;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import com.glt.magikoly.utils.Machine;

public class RindModeSetting implements ISpecialPermission {
    @Override
    public int checkPermission(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (!Machine.IS_SDK_ABOVE_6 || (notificationManager != null && notificationManager.isNotificationPolicyAccessGranted())) {
            return PackageManager.PERMISSION_GRANTED;
        }
        return PackageManager.PERMISSION_DENIED;
    }


    @Override
    public void openPermissionPage(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        activity.startActivityForResult(intent, 200002);
    }
}
