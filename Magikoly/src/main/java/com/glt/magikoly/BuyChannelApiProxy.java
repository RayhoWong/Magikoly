package com.glt.magikoly;

import android.app.Application;
import android.text.TextUtils;

import com.appsflyer.AppsFlyerLib;
import com.cs.bd.buychannel.BuyChannelApi;
import com.cs.bd.buychannel.BuySdkInitParams;
import com.cs.bd.buychannel.IBuyChannelUpdateListener;
import com.cs.bd.buychannel.buyChannel.bean.BuyChannelBean;
import com.cs.bd.commerce.util.LogUtils;
import com.glt.magikoly.application.IApplication;
import com.glt.magikoly.permission.OnPermissionResult;
import com.glt.magikoly.permission.PermissionHelper;
import com.glt.magikoly.permission.Permissions;
import com.glt.magikoly.statistic.BaseSeq19OperationStatistic;
import com.glt.magikoly.utils.AppUtils;
import com.glt.magikoly.utils.Logcat;

import static com.glt.magikoly.ProductionInfo.ACCESS_KEY;
import static com.glt.magikoly.ProductionInfo.PRODUCT_KEY;


/**
 * Created by kingyang on 2017/2/16.
 */

public class BuyChannelApiProxy {

    private static final String UNKNOWN_BUY_CHANNEL = "unknown_buychannel";

    private static Application sContext;

    public static void preInit(Application app) {
        if (FaceEnv.sSIT) {
            BuyChannelApi.setDebugMode();
        }
        BuyChannelApi.preInit(true, app);
    }

    public static void init(Application application) {
        sContext = application;
        LogUtils.setShowLog(FaceEnv.sSIT);

        AppsFlyerLib.getInstance().setOutOfStore(AppUtils.getStore(sContext)); //"xxx"为对应的商店名
        AppsFlyerLib.getInstance().setCollectIMEI(true); //设置收集设备IMEI用于追踪用户；
        AppsFlyerLib.getInstance().setCollectAndroidID(true); //设置收集设备AndroidId；
        AppsFlyerLib.getInstance().setMinTimeBetweenSessions(2); //通过该API设置两次Session上报的时间间隔。Activity onResume()会有首次Session的上报，用户授权后获取IMEI，会有第二次Session的上报，如果两次Session上报的时间间隔小于设定的值，则第二次Session会被block掉。


        BuySdkInitParams.Builder builder = new BuySdkInitParams.Builder(FaceEnv.sChannelId,
                ProductionInfo.STATISTIC_45_FUN_ID, ProductionInfo.BUY_CHANNEL_CID, null, false,
                PRODUCT_KEY, ACCESS_KEY);
        BuyChannelApi.init(application, builder.build());
        registerBuyChannelUpdateListener(new IBuyChannelUpdateListener() {
            @Override
            public void onBuyChannelUpdate(String buyChannel) {
                Logcat.i("buychannelsdk", "BuyChannel: " + buyChannel);
                IApplication iApplication = ((IApplication) FaceAppState.getApplication());
                if (iApplication != null && iApplication.isMainProcess()) {
                    if (isBuyChannelFetched()) {
                        BaseSeq19OperationStatistic.uploadBasicInfo();
                    }
                }
            }
        });
    }

    public static void onResume() {
        if (!PermissionHelper.hasPermission(sContext, Permissions.READ_PHONE_STATE)) {
            PermissionHelper.requestPermission(sContext, Permissions.READ_PHONE_STATE,
                    new OnPermissionResult() {
                        @Override
                        public void onPermissionGrant(String permission) {
                            AppsFlyerLib.getInstance().setOutOfStore(AppUtils.getStore(sContext)); //"xxx"为对应的商店名
                            AppsFlyerLib.getInstance().setCollectIMEI(true); //再次设置收集设备IMEI用于追踪用户；
                            AppsFlyerLib.getInstance().setCollectAndroidID(true); //再次设置收集设备AndroidId；
                            AppsFlyerLib.getInstance().reportTrackSession(sContext); //触发再次上报
                        }

                        @Override
                        public void onPermissionDeny(String permission, boolean never) {

                        }
                    }, -1);
        }
    }

    public static String getBuyChannel() {
        return BuyChannelApi.getBuyChannelBean(sContext).getBuyChannel();
    }

    public static int getSecondUserType() {
        return BuyChannelApi.getBuyChannelBean(sContext).getSecondUserType();
    }

    public static boolean isBuyUser() {
        return BuyChannelApi.getBuyChannelBean(sContext).isUserBuy();
    }

    public static void registerBuyChannelUpdateListener(IBuyChannelUpdateListener listener) {
        BuyChannelApi.registerBuyChannelListener(sContext, listener);
    }

    public static boolean isBuyChannelFetched() {
        String buyChannel = getBuyChannel();
        if (TextUtils.isEmpty(buyChannel)) {
            return false;
        }
        return !UNKNOWN_BUY_CHANNEL.equals(buyChannel);
    }

    public static String getCampaign() {
        BuyChannelBean bean = BuyChannelApi.getBuyChannelBean(sContext);
        String campaign = bean.getCampaign();
        Logcat.i("campaign", "campaign: " + campaign);
        Logcat.i("campaign", bean.toJsonStr());
        return campaign;
//        return "cart";
//        return "child";
    }
}
