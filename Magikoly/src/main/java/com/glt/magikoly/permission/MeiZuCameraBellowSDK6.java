package com.glt.magikoly.permission;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import com.glt.magikoly.thread.FaceThreadExecutorProxy;
import com.glt.magikoly.utils.Machine;

public class MeiZuCameraBellowSDK6 implements ISpecialPermission {
    private int permission = PackageManager.PERMISSION_DENIED;

    @Override
    public int checkPermission(Context context) {
        return permission;
    }

    @Override
    public void openPermissionPage(final Activity activity) {
        if (canUseCameraMeiZu(activity)) {
            permission = PackageManager.PERMISSION_GRANTED;
        } else {
            permission = PackageManager.PERMISSION_DENIED;
        }
        FaceThreadExecutorProxy.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                PermissionHelper.onActivityResult(activity, 1, 0, null);
            }
        });
    }

    private boolean canUseCameraMeiZu(Activity activity) {
        if (Build.MANUFACTURER.toLowerCase().contains("meizu") && !Machine.IS_SDK_ABOVE_6) {
            Camera open = null;
            try {
                open = Camera.open(0);
                open.getParameters();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                if (open != null) {
                    try {
                        open.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return ContextCompat.checkSelfPermission(activity, Permissions.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
}
