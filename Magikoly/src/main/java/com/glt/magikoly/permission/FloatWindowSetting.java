package com.glt.magikoly.permission;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import com.glt.magikoly.utils.Machine;

public class FloatWindowSetting implements ISpecialPermission {
    @Override
    public int checkPermission(Context context) {
        //权限判断
        if (!Machine.IS_SDK_ABOVE_6 || Settings.canDrawOverlays(context)) {
            return PackageManager.PERMISSION_GRANTED;
        } else {
            return PackageManager.PERMISSION_DENIED;
        }
    }

    @Override
    public void openPermissionPage(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
        activity.startActivityForResult(intent, 20002);
    }
}
