package com.glt.magikoly.permission;

import android.app.Activity;
import android.view.View;

import com.glt.magikoly.dialog.TipsDialog;
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic;
import com.glt.magikoly.statistic.Statistic103Constant;

import magikoly.magiccamera.R;

public class PermissionSaveController {

    public static void requestPermission(final Activity activity, final OnPermissionResult onPermissionResult, final String entrance) {
       /* PrivatePreference privatePreference = PrivatePreference.getPreference(activity);
        if (privatePreference.getBoolean(PrefConst.KEY_SAVE_FUNCTION_RESULT, true)) {
            PermissionHelper.requestWritePermission(activity, new OnPermissionResult() {
                @Override
                public void onPermissionGrant(String permission) {
                    if (onPermissionResult != null) {
                        onPermissionResult.onPermissionGrant(permission);
                    }
                    BaseSeq103OperationStatistic.uploadSqe103StatisticData(Statistic103Constant.PHOTOPERMISSION_OBTAINED, entrance);
                }

                @Override
                public void onPermissionDeny(String permission, boolean never) {
                    if (!never) {
                        showPermissionTipDialog(activity, onPermissionResult, entrance);
                    }
                }
            }, -1);
            privatePreference.putBoolean(PrefConst.KEY_SAVE_FUNCTION_RESULT, false);
            privatePreference.commit();
            BaseSeq103OperationStatistic.uploadSqe103StatisticData(Statistic103Constant.PHOTOPERMISSION_REQUEST, entrance);
        } else*/ if (!PermissionHelper.hasWriteStoragePermission(activity)){
            if (PermissionHelper.isPermissionGroupDeny(activity, Permissions.WRITE_EXTERNAL_STORAGE)) {
                //权限引导弹窗
                showPermissionDenyNeverDialog(activity);
            } else {
                PermissionHelper.requestWritePermission(activity, new OnPermissionResult() {
                    @Override
                    public void onPermissionGrant(String permission) {
                        if (onPermissionResult != null) {
                            onPermissionResult.onPermissionGrant(permission);
                        }
                        BaseSeq103OperationStatistic.uploadSqe103StatisticData(Statistic103Constant.PHOTO_PERMISSION_OBTAINED, entrance);
                    }

                    @Override
                    public void onPermissionDeny(String permission, boolean never) {
                        if (onPermissionResult != null) {
                            onPermissionResult.onPermissionDeny(permission, never);
                        }
                    }
                }, -1);
                BaseSeq103OperationStatistic.uploadSqe103StatisticData(Statistic103Constant.PHOTO_PERMISSION_REQUEST, entrance);
            }
        } else {
            if (onPermissionResult != null) {
                onPermissionResult.onPermissionGrant(Permissions.WRITE_EXTERNAL_STORAGE);
            }
        }

    }

    private static void showPermissionTipDialog(final Activity activity, final OnPermissionResult onPermissionResult, final String entrance) {
        final TipsDialog dialog = new TipsDialog(activity);
        dialog.setContent(R.string.permission_tip_write);
        dialog.setupOKButton(R.string.ok, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                PermissionHelper.requestWritePermission(activity, new OnPermissionResult() {
                    @Override
                    public void onPermissionGrant(String permission) {
                        if (onPermissionResult != null) {
                            onPermissionResult.onPermissionGrant(permission);
                        }
                        BaseSeq103OperationStatistic.uploadSqe103StatisticData(Statistic103Constant.PHOTO_PERMISSION_OBTAINED, entrance);
                    }

                    @Override
                    public void onPermissionDeny(String permission, boolean never) {
                        if (onPermissionResult != null) {
                            onPermissionResult.onPermissionDeny(permission, never);
                        }
                    }
                }, -1);
                BaseSeq103OperationStatistic.uploadSqe103StatisticData(Statistic103Constant.PHOTO_PERMISSION_REQUEST, entrance);
            }
        });
        dialog.show();
    }

    private static void showPermissionDenyNeverDialog(final Activity activity) {
        final TipsDialog dialog = new TipsDialog(activity);
        dialog.setContent(R.string.permission_tip_write_save_never);
        dialog.setupOKButton(R.string.ok, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                PermissionSettingPage.start(activity, false);
            }
        });
        dialog.show();
    }
}
