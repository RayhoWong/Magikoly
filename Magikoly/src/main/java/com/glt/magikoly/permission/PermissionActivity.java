package com.glt.magikoly.permission;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;

public abstract class PermissionActivity extends Activity {
    final static int REQUEST_CODE = 10001;
    private boolean mRequesting;
    @Override
    final public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            int length = permissions.length;
            setRequesting(false);
            for(int i = 0; i < length; i++) {
                PermissionHelper.onRequestPermissionsResult(this, permissions[i], i == length-1);
            }
        }
    }

    @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setRequesting(false);
        PermissionHelper.onActivityResult(this, requestCode, resultCode,data);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //当请求过程中，该发起请求的activity销毁，尝试重置当前请求状态
        if (mRequesting) {
            PermissionHelper.resetCurrentRequest();
        }
    }

    final void setRequesting(boolean requesting) {
        mRequesting = requesting;
    }
}
