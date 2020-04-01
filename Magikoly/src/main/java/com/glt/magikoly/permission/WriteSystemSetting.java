package com.glt.magikoly.permission;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import com.glt.magikoly.utils.Machine;

public class WriteSystemSetting implements ISpecialPermission {
    @Override
    public int checkPermission(Context context) {
        if (!Machine.IS_SDK_ABOVE_6 || Settings.System.canWrite(context)) {
            return PackageManager.PERMISSION_GRANTED;
        }
        return PackageManager.PERMISSION_DENIED;
    }

    @Override
    public void openPermissionPage(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + activity.getPackageName()));
        activity.startActivityForResult(intent, 20000);
    }

}
