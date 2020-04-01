package com.glt.magikoly.permission;

import android.content.Context;

import java.lang.ref.WeakReference;
import java.util.concurrent.CopyOnWriteArrayList;

class PermissionResult {
    private String mPermission;
    private int mEntrance = -1;
    private WeakReference<Context> mContextWeakReference;
    private CopyOnWriteArrayList<OnPermissionResult> mOnPermissionResults;

    PermissionResult(String permission) {
        mPermission = permission;
        mOnPermissionResults = new CopyOnWriteArrayList<>();
    }

    void setContext(Context context) {
        mContextWeakReference = new WeakReference<>(context);
    }

    Context getContext() {
        return mContextWeakReference.get();
    }

     void add(OnPermissionResult onPermissionResult) {
        mOnPermissionResults.add(onPermissionResult);
    }

     String getPermission() {
        return mPermission;
    }

    int getEntrance() {
        return mEntrance;
    }

    void setEntrance(int entrance) {
        mEntrance = entrance;
    }

     CopyOnWriteArrayList<OnPermissionResult> getOnPermissionResults() {
        return mOnPermissionResults;
    }
}
