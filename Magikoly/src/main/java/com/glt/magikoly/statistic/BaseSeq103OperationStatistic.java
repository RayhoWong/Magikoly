package com.glt.magikoly.statistic;

import android.content.Context;
import android.text.TextUtils;

import com.glt.magikoly.FaceAppState;

import static com.glt.magikoly.ProductionInfo.STATISTIC_103_FUN_ID;


/**
 * 使用协议103的统计
 * 注:如果要统计应用安装,必须将安装统计代码定义在该类中,否则功能ID将获取不到(-1).
 *
 * @author zouguiquan
 */
public class BaseSeq103OperationStatistic extends AbsBaseStatistic {
    //操作统计-日志序列
    private static final int OPERATION_LOG_SEQ = 103;

    public static void uploadSqe103StatisticData(final String optionCode, final String entrance) {
        uploadSqe103StatisticData("", optionCode, entrance, "");

    }

    public static void uploadSqe103StatisticData(final String sender, final String optionCode,
                                                 final String entrance, final String tabCategory,String position) {
        uploadSqe103StatisticData(FaceAppState.getContext(),
                sender,
                optionCode,
                OPERATE_SUCCESS,
                entrance,
                tabCategory,
                position,
                "",
                "",
                "");
    }

    public static void uploadSqe103StatisticData(final String obj, final String optionCode,
                                                 final String entrance, final String tabCategory,String position,String assObj) {
        uploadSqe103StatisticData(FaceAppState.getContext(),
                obj,
                optionCode,
                OPERATE_SUCCESS,
                entrance,
                tabCategory,
                position,
                assObj,
                "",
                "");
    }

    public static void uploadSqe103StatisticData(final String obj, final String optionCode,
                                                 final String entrance, final String tabCategory) {
        uploadSqe103StatisticData(FaceAppState.getContext(),
                obj,
                optionCode,
                OPERATE_SUCCESS,
                entrance,
                tabCategory,
                "",
                "",
                "",
                "");
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
    public static void uploadSqe103StatisticData(final Context context, final String sender, final String optionCode, final int optionResults,
                                                 final String entrance, final String tabCategory, final String position, final String associatedObj,
                                                 final String aId, final String remark) {
        if (TextUtils.isEmpty(optionCode)) {
            return;
        }
        StringBuffer buffer = new StringBuffer();
        //功能点ID
        buffer.append(STATISTIC_103_FUN_ID);
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
        uploadStatisticData(context, OPERATION_LOG_SEQ, STATISTIC_103_FUN_ID, buffer);
    }

}
