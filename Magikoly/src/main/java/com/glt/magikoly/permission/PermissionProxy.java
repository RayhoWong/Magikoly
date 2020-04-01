package com.glt.magikoly.permission;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

public final class PermissionProxy extends Activity {
    final static String EXTRA_PERMISSION = "extra_permission";
    final static String EXTRA_SPECIAL_PERMISSION = "extra_special_permission";
    final static int REQUEST_CODE = 10001;
    private boolean mCallBack;
    private String mPermission = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        handlePermission(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handlePermission(intent);
    }

    private void handlePermission(Intent intent) {
        if (intent == null) {
            runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   finish();
               }
           });
            return;
        }
       mPermission = intent.getStringExtra(EXTRA_PERMISSION);
        if (mPermission == null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });
            return;
        }
        if (intent.getBooleanExtra(EXTRA_SPECIAL_PERMISSION, false)) {
            ISpecialPermission iSpecialPermission = Permissions.SPECIAL_PERMISSIONS.get(mPermission);
            if (iSpecialPermission != null) {
                iSpecialPermission.openPermissionPage(this);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{mPermission}, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            int length = permissions.length;
            mCallBack = true;
            for (int i = 0; i < length; i++) {
                PermissionHelper.onRequestPermissionsResult(this, permissions[i], i == length - 1);
            }
        }
    }


    static void requestPermission(Context context, String permission, boolean specialPermission) {
        Intent intent = new Intent(context, PermissionProxy.class);
        if (!(context instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(EXTRA_PERMISSION, permission);
        intent.putExtra(EXTRA_SPECIAL_PERMISSION, specialPermission);
        context.startActivity(intent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallBack = true;
        PermissionHelper.onActivityResult(this, requestCode, resultCode,data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mCallBack) {
            PermissionHelper.startNextRequestForProxy(this);
        }
    }
}
