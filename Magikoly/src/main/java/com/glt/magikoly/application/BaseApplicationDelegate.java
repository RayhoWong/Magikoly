package com.glt.magikoly.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.cs.bd.ad.AdSdkApi;
import com.cs.bd.ad.params.ClientParams;
import com.cs.bd.buychannel.BuyChannelApi;
import com.cs.bd.buychannel.IBuyChannelUpdateListener;
import com.cs.statistic.ICrashReporter;
import com.cs.statistic.StatisticsManager;
import com.glt.magikoly.BuyChannelApiProxy;
import com.glt.magikoly.FaceAppState;
import com.glt.magikoly.FaceEnv;
import com.glt.magikoly.ad.proxy.AppLovinInterstitialProxy;
import com.glt.magikoly.net.VolleyManager;
import com.glt.magikoly.pref.PrefConst;
import com.glt.magikoly.pref.PrivatePreference;
import com.glt.magikoly.thread.FaceThreadExecutorProxy;
import com.glt.magikoly.utils.AppUtils;
import com.glt.magikoly.utils.DrawUtils;
import com.glt.magikoly.utils.Duration;
import com.glt.magikoly.utils.Logcat;
import com.glt.magikoly.utils.WindowController;
import com.glt.magikoly.version.VersionController;
import com.tencent.bugly.crashreport.CrashReport;

import static com.glt.magikoly.ProductionInfo.ip1;
import static com.glt.magikoly.ProductionInfo.ip2;
import static com.glt.magikoly.ProductionInfo.port;

/**
 * App基本逻辑委托类
 *
 * @author yangguanxiang
 */
public class BaseApplicationDelegate {

    private Application mApplication;

//    Locale mCurrentLocale;

    BaseApplicationDelegate(Application app) {
        mApplication = app;
        FaceAppState.init(mApplication);
    }

    public void onCreate() {
//        initUmSdk();
        initBugly();
//        CrashReport crashReport = new CrashReport();
//        crashReport.start(mApplication);
        DrawUtils.resetDensity(mApplication);
//        Configuration configuration = mApplication.getResources().getConfiguration();
//        mCurrentLocale = configuration.locale;
        VolleyManager.initContext(mApplication);
        VolleyManager.getInstance().start();
        WindowController.init(mApplication);
        FaceThreadExecutorProxy.init();
        initStatisticsManager();
        if (mApplication instanceof IApplication && ((IApplication) mApplication).isMainProcess()) {
            VersionController.init();
        }
        BuyChannelApiProxy.init(FaceAppState.getApplicationProxy());
    }

    public void onConfigurationChanged(Configuration newConfig) {
//        if ((null != mCurrentLocale) && !newConfig.locale.equals(mCurrentLocale)) {
//            mCurrentLocale = newConfig.locale;
//            FaceAppState.restart();
//        }
    }

    public Context getApplicationContext(Context baseContext) {
        Context retval = baseContext;
        if (retval == null) {
            retval = mApplication;
        }
        return retval;
    }

    public Resources getResources(Resources baseResource) {
        return baseResource;
    }

    private void initStatisticsManager() {
        // 初始化统计
        StatisticsManager.initBasicInfo(FaceEnv.PROCESS_NAME, FaceEnv.sChannelId, new String[]{
                        ip1 + ":" + port, ip2 + ":" + port},
                "topdata." + mApplication.getPackageName());
        Class<? extends Activity>[] excludeActivities = new Class[]{
//				ChargeBatteryProxyActivity.class,
//				StandarProxyActivity.class,
//				MoPubActivity.class,
//				MraidActivity.class,
//				MoPubBrowser.class,
//				MraidVideoPlayerActivity.class,
//				AdUrlPreParseLoadingActivity.class,
//				H5AdActivity.class,
//				AdActivity.class,
//				InterstitialAd.class,
//				com.facebook.ads.InterstitialAd.class,
//				AppwallActivity.class,
        };
        StatisticsManager.enableApplicationStateStatistic(FaceAppState.getApplicationProxy(), null, excludeActivities);
        StatisticsManager.registerCrashReporter(new ICrashReporter() {
            @Override
            public void report(Throwable t) {
                CrashReport.postCatchedException(t);
            }
        });
        StatisticsManager statisticsManager = StatisticsManager.getInstance(mApplication);
        statisticsManager.enableLog(FaceEnv.sSIT);
        statisticsManager.setJobSchedulerEnable(true);
    }

    protected void initAdSDK() {
        PrivatePreference pref = PrivatePreference.getPreference(FaceAppState.getContext());
        long firstRunTime = pref.getLong(PrefConst.KEY_FIRST_RUN_TIME, 0);
        ClientParams params = new ClientParams(BuyChannelApiProxy.getBuyChannel(), firstRunTime,
                !VersionController.isNewUser());
        AdSdkApi.setClientParams(mApplication, params);
        AdSdkApi.setEnableLog(FaceEnv.sSIT);
        /*AdSdkApi.setTestServer(true);*/
        AdSdkApi.initSDK(mApplication,
                FaceEnv.PROCESS_NAME,
                StatisticsManager.getUserId(mApplication),
                AppUtils.getGoogleAdvertisingId(),
                FaceEnv.sChannelId,
                null);
        BuyChannelApiProxy.registerBuyChannelUpdateListener(new IBuyChannelUpdateListener() {
            @Override
            public void onBuyChannelUpdate(String s) {
                FaceThreadExecutorProxy.execute(new Runnable() {
                    @Override
                    public void run() {
                        PrivatePreference pref = PrivatePreference.getPreference(FaceAppState.getContext());
                        long firstRunTime = pref.getLong(PrefConst.KEY_FIRST_RUN_TIME, 0);
                        ClientParams params = new ClientParams(BuyChannelApiProxy.getBuyChannel(), firstRunTime, !VersionController.isNewUser());
                        params.setUseFrom(BuyChannelApi.getBuyChannelBean(FaceAppState.getContext()).getSecondUserType() + "");
                        AdSdkApi.setClientParams(FaceAppState.getContext(), params);
                    }
                });
            }
        });
        AppLovinInterstitialProxy.Companion.initialize();



//
        TTAdSdk.init(FaceAppState.getContext(),
                new TTAdConfig.Builder()
                        .appId("5001121")
                        .useTextureView(false) //使用TextureView控件播放视频,默认为SurfaceView,当有SurfaceView冲突的场景，可以使用TextureView
                        .appName("神奇相机")
                        .titleBarTheme(TTAdConstant.TITLE_BAR_THEME_DARK)
                        .allowShowNotify(true) //是否允许sdk展示通知栏提示
                        .allowShowPageWhenScreenLock(true) //是否在锁屏场景支持展示广告落地页
                        .debug(true) //测试阶段打开，可以通过日志排查问题，上线时去除该调用
                        .directDownloadNetworkType(TTAdConstant.NETWORK_STATE_WIFI, TTAdConstant.NETWORK_STATE_3G) //允许直接下载的网络状态集合
                        .supportMultiProcess(false) //是否支持多进程，true支持
                        //.httpStack(new MyOkStack3())//自定义网络库，demo中给出了okhttp3版本的样例，其余请自行开发或者咨询工作人员。
                        .build());




        Logcat.i("Test", "initAdSDK---------" + Duration.getDuration("initAdSDK") + AppUtils.getCurProcessName(mApplication));
    }

//    private void initUmSdk() {
//        String channel = TextUtils.isEmpty(AppUtils.getStore(mApplication)) ? "none" :
//                AppUtils.getStore(mApplication) + "_" + AppUtils.getChannel(mApplication);
//        UMConfigure.setLogEnabled(FaceEnv.sSIT);
//        UMConfigure.init(mApplication, AppUtils.getUmengAppKey(mApplication), channel,
//                UMConfigure.DEVICE_TYPE_PHONE, null);
//        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
//    }

    private void initBugly() {
        CrashReport.initCrashReport(mApplication, AppUtils.getBuglyAppId(mApplication), false);
        CrashReport.setAppChannel(mApplication, AppUtils.getChannel(mApplication));
    }

    protected void initGoogleAdvertisingId(final OnGoogleAdvertisingIdInitListener l) {
        new Thread() {
            public void run() {
                String googleId = AppUtils.initGoogleAdvertisingId(mApplication);
                if (l != null) {
                    l.onGoogleAdvertisingIdInited(googleId);
                }
            }

        }.start();
    }

    /**
     * @author yangguanxiang
     */
    public interface OnGoogleAdvertisingIdInitListener {
        void onGoogleAdvertisingIdInited(String googleId);
    }

}
