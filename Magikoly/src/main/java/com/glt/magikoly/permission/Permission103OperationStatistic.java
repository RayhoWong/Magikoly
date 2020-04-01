package com.glt.magikoly.permission;

import android.content.Context;
import android.text.TextUtils;
import com.glt.magikoly.statistic.AbsBaseStatistic;
import com.glt.magikoly.utils.Logcat;

import java.util.HashMap;
import java.util.Map;

/**
 * 使用协议103的统计
 * 注:如果要统计应用安装,必须将安装统计代码定义在该类中,否则功能ID将获取不到(-1).
 *
 * @author zouguiquan
 */
public class Permission103OperationStatistic extends AbsBaseStatistic {
    // 操作结果---操作成功
    public final static int OPERATE_SUCCESS = 1;
    // 操作结果---操作失败
    public final static int OPERATE_FAIL = 0;


    //操作统计-日志序列
    public static final int OPERATION_LOG_SEQ = 103;

    public static final int PERMISSION_FUNCTION_ID = 391;

    public static final String PERMISSION_REQUEST_CODE = "per_get_f000";


    //存放操作代码对应的功能ID
    private static Map<String, Integer> sFunctionIdMap;

    public static void permissionStatistic(Context context, String permission, int entrance) {
       /* Logcat.d("xiaowu_permission_statistic", "permission:" + permission + "  entrance:" + entrance);
        uploadSqe103StatisticData(context, permission, PERMISSION_REQUEST_CODE, OPERATE_SUCCESS, entrance + "",
                "", "", "", "", "");*/
    }

    /**
     * 上传使用103协议的统计数据，因为不同协议要上传的数据可能不同，所以独立出来
     * 功能点ID||统计对象||操作代码||操作结果||入口||Tab分类||位置||关联对象||广告ID||备注
     *
     * @param context
     * @param sender        统计对象
     * @param optionCode    操作代码
     * @param optionResults 操作结果
     * @param entrance      入口
     * @param tabCategory   Tab分类
     * @param position      位置
     * @param associatedObj 关联对象
     * @param aId           广告ID
     * @param remark        备注
     */
    public static void uploadSqe103StatisticData(Context context, String sender, String optionCode, int optionResults,
                                                 String entrance, String tabCategory, String position, String associatedObj, String aId, String remark) {
        uploadSqe103StatisticData(context, getFunctionId(optionCode), sender, optionCode, optionResults, entrance, tabCategory, position, associatedObj, aId, remark);
    }

    /**
     * 上传使用103协议的统计数据，因为不同协议要上传的数据可能不同，所以独立出来
     * 功能点ID||统计对象||操作代码||操作结果||入口||Tab分类||位置||关联对象||广告ID||备注
     *
     * @param context
     * @param funId         功能ID
     * @param sender        统计对象
     * @param optionCode    操作代码
     * @param optionResults 操作结果
     * @param entrance      入口
     * @param tabCategory   Tab分类
     * @param position      位置
     * @param associatedObj 关联对象
     * @param aId           广告ID
     * @param remark        备注
     */
    public static void uploadSqe103StatisticData(final Context context, final int funId, final String sender, final String optionCode, final int optionResults,
                                                 final String entrance, final String tabCategory, final String position, final String associatedObj, final String aId, final String remark) {
        if (TextUtils.isEmpty(optionCode)) {
            return;
        }
        //防止未传入功能ID
        int newFunId = funId > 0 ? funId : getFunctionId(optionCode);
        StringBuffer buffer = new StringBuffer();
        //功能点ID
        buffer.append(newFunId);
        buffer.append(STATISTICS_DATA_SEPARATE_STRING);
        //统计对象(mapId)
        buffer.append(sender);
        buffer.append(STATISTICS_DATA_SEPARATE_STRING);
        //操作代码
        buffer.append(optionCode);
        buffer.append(STATISTICS_DATA_SEPARATE_STRING);
        //操作结果-----0:未成功,1:成功(默认成功)
        buffer.append(optionResults);
        buffer.append(STATISTICS_DATA_SEPARATE_STRING);
        //入口(本次需求为空)
        buffer.append(entrance);
        buffer.append(STATISTICS_DATA_SEPARATE_STRING);
        //Tab分类(本次需求为空)
        buffer.append(tabCategory);
        buffer.append(STATISTICS_DATA_SEPARATE_STRING);
        //位置--统计对象(AppID)的所在位置.-(本次需求为空)
        buffer.append(position);
        buffer.append(STATISTICS_DATA_SEPARATE_STRING);
        //关联对象(是否传值以特定的"操作代码"为准)
        buffer.append(associatedObj);
        buffer.append(STATISTICS_DATA_SEPARATE_STRING);
        //广告ID(是否传值以特定的”操作代码“为准)
        buffer.append(aId);
        buffer.append(STATISTICS_DATA_SEPARATE_STRING);
        //备注(是否传值以特定的”操作代码“为准)
        buffer.append(remark);
        //上传统计数据
        uploadStatisticData(context, OPERATION_LOG_SEQ, newFunId, buffer);
    }

    /**
     * 根据操作代码获取对应的功能ID
     *
     * @param optionCode 操作代码
     * @return 功能ID(201 : GO桌面屏幕操作统计 ; 202 : GO桌面DOCK栏操作统计 ; 203 : GO桌面功能表操作统计)
     */
    public synchronized static int getFunctionId(String optionCode) {
        if (sFunctionIdMap == null) {
            sFunctionIdMap = new HashMap<String, Integer>();
            sFunctionIdMap.put(PERMISSION_REQUEST_CODE, PERMISSION_FUNCTION_ID);
        }
        //获取功能ID
        Integer funId = !TextUtils.isEmpty(optionCode) ? sFunctionIdMap.get(optionCode) : null;
        return funId == null ? -1 : funId;
    }

}
