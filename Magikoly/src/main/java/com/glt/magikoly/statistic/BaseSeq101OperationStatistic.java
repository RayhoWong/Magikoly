package com.glt.magikoly.statistic;

import android.content.Context;
import android.text.TextUtils;

import com.cs.statistic.StatisticsManager;
import com.cs.statistic.beans.OptionBean;

import static com.glt.magikoly.ProductionInfo.STATISTIC_101_FUN_ID;


/**
 * GoLauncher操作统计类
 *
 * @author caoyaming
 */
public class BaseSeq101OperationStatistic extends AbsBaseStatistic {

    private static final int OPERATION_LOG_SEQ = 101;

    /**
     * 上传操作统计数据
     *
     * @param context
     * @param sender     统计对象
     * @param optionCode 操作代码
     * @param entrance   入口
     */
    public static void uploadOperationStatisticData(Context context, String sender, String optionCode, String entrance) {
        uploadOperationStatisticData(context, sender, optionCode, OPERATE_SUCCESS, entrance, "", "", "", "");
    }


    /**
     * 上传操作统计数据
     *
     * @param context
     * @param sender        统计对象
     * @param optionCode    操作代码
     * @param optionResults 操作结果
     * @param entrance      入口
     * @param tabCategory   Tab分类
     * @param position      位置
     * @param associatedObj 关联对象
     * @param remark        备注
     */
    public static void uploadOperationStatisticData(final Context context,  final String sender,
                                                    final String optionCode, final int optionResults,
                                                    final String entrance, final String tabCategory,
                                                    final String position, final String associatedObj,
                                                    final String remark) {
        if (TextUtils.isEmpty(optionCode)) {
            return;
        }
        int newFunId = STATISTIC_101_FUN_ID;
        StringBuffer buffer = createDataBuffer(STATISTIC_101_FUN_ID,
                sender,
                optionCode,
                optionResults,
                entrance,
                tabCategory,
                position,
                associatedObj,
                remark);
        // 上传统计数据
        uploadStatisticData(context, OPERATION_LOG_SEQ, newFunId, buffer);
    }

    /*
     * for option , see the method above
     */
    public static void uploadOperationStatisticData(final Context context, final String sender,
                                                    final String optionCode, final int optionResults,
                                                    final String entrance, final String tabCategory,
                                                    final String position, final String associatedObj,
                                                    final String remark, final OptionBean... optionBeans) {
        if (TextUtils.isEmpty(optionCode)) {
            return;
        }
        int newFunId = STATISTIC_101_FUN_ID;
        StringBuffer buffer = createDataBuffer(newFunId, sender, optionCode, optionResults, entrance, tabCategory, position, associatedObj, remark);

        // 上传统计数据
        StatisticsManager.getInstance(context).uploadStaticDataForOptions(
                OPERATION_LOG_SEQ, newFunId, buffer.toString(), null, optionBeans);
    }

    private static StringBuffer createDataBuffer(Object... dataItems) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < dataItems.length - 1; i++) {
            buffer.append(dataItems[i]);
            buffer.append(STATISTICS_DATA_SEPARATE_STRING);
        }
        buffer.append(dataItems[dataItems.length - 1]);
        return buffer;
    }


}
