package com.glt.magikoly;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AppsFlyerLib;
import com.glt.magikoly.utils.Logcat;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yangjiacheng on 2017/10/10.
 * ...
 */

public class AppsFlyProxy extends BroadcastReceiver {

    public static final String ONE_DAY_RETENTION = "1day_retention";
    public static final String SEVEN_DAY_RETENTION = "7day_retention";
    public static final String ENTER_WALLPAPER_STORE = "wp_store_enter";
    public static final String ENTER_THEME_STORE = "g001_load_new";
    public static final String SET_WALLPAPER = "wp_store_wp_i000";
    public static final String SET_THEME = "theme_i000";
    public static final String CLICK_AD = "ad_a000";
    public static final String PURCHASE_SERVICES = "p001";
    public static final String CLICK_PAY = "sub_a000";

    public static final String SUB_CLICK = "sub_click";//订阅页订阅点击事件
    public static final String SUB_SUCCESS = "sub_success";//订阅页订阅成功事件

    public static void trackEvent(final String eventName) {
        Log.e("trackEvent", eventName);
        Intent intent = new Intent(FaceAppState.getContext(), AppsFlyProxy.class);
        intent.setAction("com.glt.magikoly.AppsFlyProxy.TRACK_EVENT");
        intent.putExtra("eventName", eventName);
        FaceAppState.getContext().sendBroadcast(intent);
    }

    public static void trackPurchaseClick() {
        Map<String, Object> eventValue = new HashMap<String, Object>();
        // AF打点上报
        AppsFlyerLib.getInstance().trackEvent(FaceAppState.getContext(), "af_tap_subscription", eventValue);
        Logcat.d("AppsFlyProxy", "trackPurchaseClick price=");
    }

    public static void trackPurchase(long price, boolean isSub, String orderId, String currency) {
        Map<String, Object> eventValue = new HashMap<String, Object>();
        // 价格
        eventValue.put(AFInAppEventParameterName.REVENUE, price);
        // 订阅传 “sub”，内购传 “buy”
        if (isSub) {
            eventValue.put(AFInAppEventParameterName.CONTENT_TYPE, "sub");
        } else {
            eventValue.put(AFInAppEventParameterName.CONTENT_TYPE, "buy");
        }
        // 订单号
        eventValue.put(AFInAppEventParameterName.CONTENT_ID, orderId);
        // 货币单位
//        eventValue.put(AFInAppEventParameterName.CURRENCY, "USD");
        eventValue.put(AFInAppEventParameterName.CURRENCY, currency);
        // AF打点上报
        AppsFlyerLib.getInstance().trackEvent(FaceAppState.getContext(), "af_subcribe", eventValue);
        Logcat.d("AppsFlyProxy", "trackPurchase price=" + price + ",type=" + isSub + ",orderId=" + orderId + ",currency=" + currency);
    }

    public static void trackEvent(String eventName, Map<String, Object> eventValues) {
        AppsFlyerLib.getInstance().trackEvent(FaceAppState.getContext(), eventName, eventValues);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String eventName = intent.getExtras().getString("eventName");
        AppsFlyerLib.getInstance().trackEvent(FaceAppState.getContext(), eventName, new HashMap<String, Object>() {
        });
    }

    public static String getPriceWithoutCurrency(String price) {
        char[] chars = price.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (Character.isDigit(chars[i])) {
                return price.substring(i, price.length());
            }
        }
        return "0";
    }
}
