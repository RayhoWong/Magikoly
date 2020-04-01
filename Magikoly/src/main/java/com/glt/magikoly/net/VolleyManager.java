package com.glt.magikoly.net;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.NoCache;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.OkHttpClient;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by yangjiacheng on 2017/11/16.
 * ...
 */

public class VolleyManager {

    public static final String LOG_TAG = "Network Log";

    public static final String DEFAULT_REQUEST_TAG = VolleyManager.class.getSimpleName();

    private static VolleyManager sInstance;

    private static Context sContext;

    private RequestQueue mRequestQueue;

    public OkHttpClient getDefaultOkHttpClient() {
        return mOkHttpClient;
    }

    private OkHttpClient mOkHttpClient;

    private VolleyManager() {
        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .cache(obtainOkHttpCache())
                .addInterceptor(new LogInterceptor())
                .build();
        //使用 OkHttp 缓存，Volley 不缓存
        mRequestQueue = new RequestQueue(new NoCache(), new OkHttpNetwork());
    }

    public static void initContext(Context context) {
        sContext = context;
    }

    public synchronized static VolleyManager getInstance() {
        if (sContext == null) {
            throw new IllegalStateException("please call method initContext first");
        }
        if (sInstance == null) {
            sInstance = new VolleyManager();
        }
        return sInstance;
    }

    public <T> Request<T> add(Request<T> request) {
        return mRequestQueue.add(request);
    }

    public void start() {
        mRequestQueue.start();
    }

    public RequestQueue getRequestQueue() {
        return this.mRequestQueue;
    }

    public void clearNetworkCache() {
        if (mRequestQueue != null) {
            mRequestQueue.getCache().clear();
            //TODO 清除 OkHttp 缓存
        }
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
            if (mOkHttpClient != null) {
                for (Call call : mOkHttpClient.dispatcher().queuedCalls()) {
                    if (call.request().tag().equals(tag))
                        call.cancel();
                }
                for (Call call : mOkHttpClient.dispatcher().runningCalls()) {
                    if (call.request().tag().equals(tag))
                        call.cancel();
                }
            }
        }
    }

    private Cache obtainOkHttpCache() {
        int cacheSize = 20 * 1024 * 1024;
        File cacheFile = new File(sContext.getExternalCacheDir(), "NetworkCache");
        return new Cache(cacheFile, cacheSize);
    }

    public static final CacheControl FORCE_NETWORK = new CacheControl.Builder().noCache().build();

}
