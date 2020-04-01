package com.glt.magikoly.config;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import com.cpcphone.abtestcenter.AbtestCenterService;
import com.cs.utils.net.request.THttpRequest;
import com.glt.magikoly.BuyChannelApiProxy;
import com.glt.magikoly.FaceAppState;
import com.glt.magikoly.ProductionInfo;
import com.glt.magikoly.application.IApplication;
import com.glt.magikoly.constants.ICustomAction;
import com.glt.magikoly.utils.AppUtils;
import com.glt.magikoly.utils.Logcat;
import com.glt.magikoly.utils.Machine;
import com.glt.magikoly.version.VersionController;
import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务器AB测试信息管理类
 * @author kingyang
 */
public class ConfigManager {

    private final static String TAG = "ConfigManager";

    private static final int CID = Integer.parseInt(ProductionInfo.CID);

    private static final int CID2 = ProductionInfo.STATISTIC_19_CID;

    private static ConfigManager sInstance;

    private ConcurrentHashMap<Integer, AbsConfigBean> mConfigMap = new ConcurrentHashMap<>();

    private ConfigManager() {
        IApplication app = (IApplication) FaceAppState.getApplication();
        if (!app.isMainProcess()) {
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (ICustomAction.ACTION_CONFIG_BEAN_CHANGE.equals(action)) {
                        int sid = intent.getIntExtra("sid", -1);
                        AbsConfigBean bean = getConfigBean(sid);
                        bean.mIsInited = false;
                    }
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ICustomAction.ACTION_CONFIG_BEAN_CHANGE);
            FaceAppState.getApplication().registerReceiver(receiver, intentFilter);
        }
    }

    public static ConfigManager getInstance() {
        if (sInstance == null) {
            sInstance = new ConfigManager();
        }
        return sInstance;
    }
    /**
     *
     */
    public interface HttpCallback {

        void success(AbsConfigBean configBean);

        void error();
    }

    /**
     * 根据不同的业务id获取配置信息
     * @param sid
     * @return
     */
    public AbsConfigBean getConfigBean(int sid) {
        AbsConfigBean configBean = mConfigMap.get(sid);
        if (configBean == null) {
            configBean = ConfigBeanFactory.getConfigBean(sid);
            if (configBean != null) {
                mConfigMap.put(sid, configBean);
            }
        }
        if (configBean != null) {
            configBean.readObjectByCache();
        }
        return configBean;
    }

    public void requestConfig(final Context context, final int sid, final HttpCallback callback) {
        doRequestConfig(context, sid, callback);
    }

    private void doRequestConfig(final Context context, final int sid, final HttpCallback callback) {
        AbtestCenterService service = new AbtestCenterService.Builder()
                .sid(new int[]{sid})// 业务ID
                .cid(CID)     // 产品ID
                .cid2(CID2) //统计协议使用的产品ID, GO桌面为1
                .cversion(AppUtils.getVersionCodeByPkgName(context,
                        context.getPackageName())) // 客户端版本号，必须大于0
                .local(Machine.getCountry(context).toUpperCase()) //国家
                .utm_source(BuyChannelApiProxy.getBuyChannel()) //买量渠道
                .user_from(BuyChannelApiProxy.getSecondUserType()) // 买量SDK用户类型
                .entrance(
                        AbtestCenterService.Builder.Entrance.MAIN_PACKAGE)    //业务请求入口
                .cdays(AppUtils.getCdays(context))       //客户端安装天数，必须大于0
                .aid(Machine.getAndroidId(context)) //客户端安卓ID
                .isupgrade(VersionController.isNewUser() ? 2 : 1).build(context); //是否升级用户： 1是，2否

        Logcat.i(TAG, "sid: " + sid + " cid: " + CID + " cid2: " + CID2 + " versionCode: " +
                AppUtils.getVersionCodeByPkgName(context, context.getPackageName()) +
                " locale: " +
                Machine.getCountry(context) + " buyChannel: " +
                BuyChannelApiProxy.getBuyChannel() + " cdays: " + AppUtils.getCdays(context) +
                " androidID: " + Machine.getAndroidId(context) + " isNewUser: " +
                VersionController.isNewUser() + " userFrom: " + BuyChannelApiProxy.getSecondUserType());
        try {
            service.send(new AbtestCenterService.ResultCallback() {

                @Override
                public void onResponse(String response) {
                    AbsConfigBean configBean = getConfigBean(sid);
                    Logcat.i(TAG, response);
                    JSONObject json = getDataJson(response);
                    if (json != null) {
                        configBean.saveObjectToCache(json);
                        AbtestCenterService
                                .retentionStatics(context, CID2, sid, configBean.getAbTestId(),
                                        configBean.getFilterId());
                        if (callback != null) {
                            callback.success(configBean);
                        }

                        Intent intent = new Intent(ICustomAction.ACTION_CONFIG_BEAN_CHANGE);
                        intent.putExtra("sid", sid);
                        FaceAppState.getContext().sendBroadcast(intent);
                    } else {
                        if (callback != null) {
                            callback.error();
                        }
                    }
                }

                @Override
                public void onException(THttpRequest request, String errorMsg, int code) {
                    if (callback != null) {
                        callback.error();
                    }
                }

                @Override
                public void onException(THttpRequest request, int code) {
                    if (callback != null) {
                        callback.error();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) {
                callback.error();
            }
        }
    }


    /**
     * get info
     */
    private static JSONObject getDataJson(String json) {
        if (!TextUtils.isEmpty(json)) {
            try {
                JSONObject jsonObject = new JSONObject(json);
                boolean value = jsonObject.optBoolean("success");
                if (value) {
                    JSONObject dataJson = jsonObject.optJSONObject("datas");
                    return dataJson;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}