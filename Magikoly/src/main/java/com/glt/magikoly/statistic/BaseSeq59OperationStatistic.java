package com.glt.magikoly.statistic;

import android.content.Context;
import com.cs.statistic.StatisticsManager;
import com.cs.statistic.utiltool.UtilTool;
import com.glt.magikoly.FaceAppState;
import com.glt.magikoly.FaceEnv;
import com.glt.magikoly.utils.AppUtils;
import com.glt.magikoly.utils.Machine;

import static com.glt.magikoly.ProductionInfo.STATISTIC_59_FUN_ID;

/**
 * Created by cjh94 on 2016/12/15.
 */

public class BaseSeq59OperationStatistic extends AbsBaseStatistic {

    private static final int LOG_SEQ = 59;
    public static final String PURCHASE_VISUAL = "f000";


    /**
     * @param productId 产品id
     * @param entrance  入口
     * @param tab       样式
     */
    public static void uploadPurchaseVisual(String productId, int entrance, String tab) {
        uploadOperationStatisticData(FaceAppState.getContext(),
                productId,
                PURCHASE_VISUAL, entrance, tab, "", "", 0, "", "","",tab);
    }

    /**
     * @param productId 产品id
     * @param entrance  入口
     * @param tab       样式
     */
    public static void uploadPurchaseClick(String productId, int entrance, String tab) {
        uploadOperationStatisticData(FaceAppState.getContext(),
                productId,
                "j005", entrance, tab, "", "", 0, "", "","",tab);
    }

    /**
     * @param productId 产品id
     * @param entrance  入口
     * @param tab       样式
     */
    public static void uploadPurchaseSuccess(String productId, int entrance, String tab,String orderID) {
        uploadOperationStatisticData(FaceAppState.getContext(),
                productId,
                "j005", entrance, tab, orderID, "", 1, "", "","",tab);
    }

    /**
     * @param productId 产品id
     * @param entrance  入口
     * @param tab       样式
     */
    public static void uploadPurchaseSync(String productId, int entrance, String tab,String orderID) {
        uploadOperationStatisticData(FaceAppState.getContext(),
                productId,
                "p001", entrance, tab, orderID, "", 0, "", "","",tab);
    }


    /**
     * 上传操作统计数据
     *
     * @param context
     * @param productID     购买产品id
     * @param optionCode    操作代码
     * @param entrance      入口
     * @param orderID       位置
     * @param associatedObj 关联对象
     * @param purchaseType  订单类型
     * @param googleId      谷歌广告id
     */
    public static void uploadOperationStatisticData(Context context, String productID,
                                                    String optionCode, int entrance,
                                                    String tab, String orderID,
                                                    String associatedObj, int resultCode,
                                                    String purchaseType, String googleId,String markI,String markII) {
        StringBuffer data = new StringBuffer();
        //日志序列
        data.append(LOG_SEQ);
        data.append(STATISTICS_DATA_SEPARATE_STRING);
        //Android ID
        data.append(Machine.getAndroidId(context));
        data.append(STATISTICS_DATA_SEPARATE_STRING);
        // 日志打印时间
        data.append(UtilTool.getBeiJinTime(System.currentTimeMillis()));
        data.append(STATISTICS_DATA_SEPARATE_STRING);
        //功能ID
        data.append(STATISTIC_59_FUN_ID);
        data.append(STATISTICS_DATA_SEPARATE_STRING);
        //统计对象 当前所支付资源的sku id
        data.append(productID);
        data.append(STATISTICS_DATA_SEPARATE_STRING);
        // 操作代码
        data.append(optionCode);
        data.append(STATISTICS_DATA_SEPARATE_STRING);
        // 操作结果-----0:点击,1:成功
        data.append(resultCode);
        data.append(STATISTICS_DATA_SEPARATE_STRING);

        //国家(取SIM卡里面的国家代码；无SIM卡时，取手动系统语言里面 的国家代码，报表显示 cn)
        data.append(Machine.getSimCountryIso(context));
        data.append(STATISTICS_DATA_SEPARATE_STRING);
        //渠道
        data.append(FaceEnv.sChannelId);
        data.append(STATISTICS_DATA_SEPARATE_STRING);
        //版本号
        data.append(AppUtils.getVersionCodeByPkgName(context, context.getPackageName())); // 版本号
        data.append(STATISTICS_DATA_SEPARATE_STRING);
        //版本名
        data.append(AppUtils.getVersionNameByPkgName(context, context.getPackageName()));
        data.append(STATISTICS_DATA_SEPARATE_STRING);
        // 入口(本次需求为空)
        data.append(entrance);
        data.append(STATISTICS_DATA_SEPARATE_STRING);

        // Tab分类(本次需求为空)
        data.append(tab);
        data.append(STATISTICS_DATA_SEPARATE_STRING);


        // 位置--购买成功时，上传订单编号
        data.append(orderID);
        data.append(STATISTICS_DATA_SEPARATE_STRING);

        // imei 随 机生成,引用go桌面的同 一套编码只有桌面主题才上传该字段，其他产品填0
        data.append("0");
        data.append(STATISTICS_DATA_SEPARATE_STRING);

        // goid  用户在GO系列产品中的唯一id，用于产品间的用户关联。用户在不同GO系列产品间使用同一goid
        data.append(StatisticsManager.getUserId(context));
        data.append(STATISTICS_DATA_SEPARATE_STRING);

        // 关联对象(价格:币种)
        data.append(associatedObj);
        data.append(STATISTICS_DATA_SEPARATE_STRING);

        //GLive 相关业务，上传内部订单号，其他产品忽略
//        data.append("");
//        data.append(STATISTICS_DATA_SEPARATE_STRING);

        //备注
        data.append("");
        data.append(STATISTICS_DATA_SEPARATE_STRING);
        // 上传订单类型（1：普通内购，2：订阅）
        data.append(purchaseType);
        data.append(STATISTICS_DATA_SEPARATE_STRING);

        // 谷歌广告ID
        data.append(googleId);
        data.append(STATISTICS_DATA_SEPARATE_STRING);
        // 备注1
        data.append(markI);
        data.append(STATISTICS_DATA_SEPARATE_STRING);
        // 备注2
        data.append(markII);
        data.append(STATISTICS_DATA_SEPARATE_STRING);

        // 备注(用户当前使用的gmail账号（部分支付方式，例如fortumo，取不到用户相关的帐号信息，则此字段为空）)
//        String gmail = com.jiubang.golauncher.utils.Machine.getGmail(context);
//        if (!TextUtils.isEmpty(gmail)) { // 备注
//            data.append(gmail);
//        } else {
//            data.append("");
//        }
        StatisticsManager.getInstance(context).upLoadStaticData(data.toString());
    }
}