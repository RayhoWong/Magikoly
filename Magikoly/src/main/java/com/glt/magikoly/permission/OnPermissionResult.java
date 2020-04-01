package com.glt.magikoly.permission;

public interface OnPermissionResult {

    void onPermissionGrant(String permission);

    void onPermissionDeny(String permission, boolean never);

}
