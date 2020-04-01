package com.glt.magikoly.net;

import android.support.annotation.NonNull;
import com.glt.magikoly.utils.Logcat;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Locale;

/**
 * Created by yangjiacheng on 2017/11/20.
 * ...
 * OkHTTP 网络请求 日志格式化打印
 */

public class LogInterceptor implements Interceptor {

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        long t1 = System.nanoTime();
        Logcat.d(VolleyManager.LOG_TAG + " header", String.format(Locale.getDefault(), "Sending request %s on %s%n%s",
                request.url(), chain.connection(), request.headers()));

        Response response = chain.proceed(request);
        long t2 = System.nanoTime();
        Logcat.d(VolleyManager.LOG_TAG + " header", String.format(Locale.getDefault(), "Received response for %s in %.1fms%n%s",
                response.request().url(), (t2 - t1) / 1e6d, response.headers()));
        return response;
    }

}
