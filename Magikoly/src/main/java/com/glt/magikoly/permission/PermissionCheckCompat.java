package com.glt.magikoly.permission;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import com.glt.magikoly.utils.Machine;

public class PermissionCheckCompat {
    static int checkPermission(Context context, String permission) {
        ISpecialPermission iSpecialPermission = Permissions.SPECIAL_PERMISSIONS.get(permission);
        if (iSpecialPermission != null) {
            return iSpecialPermission.checkPermission(context);
        }
        return ContextCompat.checkSelfPermission(context, permission);
    }

}
