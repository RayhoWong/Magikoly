package com.glt.magikoly.permission;

import android.app.Activity;
import android.content.Context;

public interface ISpecialPermission {
    int checkPermission(Context context);

    //一定要以startActivityForResult的方式启动
    void openPermissionPage(Activity activity);
}
