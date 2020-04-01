package com.glt.magikoly.net;

import com.android.volley.AuthFailureError;
import com.android.volley.Header;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.glt.magikoly.utils.Logcat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by yangjiacheng on 2017/11/16.
 * ...
 * 为 Volley 创建的 OkHttp3 网络请求 Client，并规避过时API，不使用 HttpUrlConnection
 */

public class OkHttpNetwork implements Network {

    OkHttpNetwork() {
    }

    private static void setRequestMethod(okhttp3.Request.Builder builder, Request<?> request)
            throws AuthFailureError {
        switch (request.getMethod()) {
            case Request.Method.DEPRECATED_GET_OR_POST:
                byte[] postBody = request.getBody();
                if (postBody != null) {
                    builder.post(RequestBody
                            .create(MediaType.parse(request.getBodyContentType()), postBody));
                }
                break;
            case Request.Method.GET:
                builder.get();
                break;
            case Request.Method.DELETE:
                builder.delete();
                break;
            case Request.Method.POST:
                builder.post(createRequestBody(request));
                break;
            case Request.Method.PUT:
                builder.put(createRequestBody(request));
                break;
            case Request.Method.HEAD:
                builder.head();
                break;
            case Request.Method.OPTIONS:
                builder.method("OPTIONS", null);
                break;
            case Request.Method.TRACE:
                builder.method("TRACE", null);
                break;
            case Request.Method.PATCH:
                builder.patch(createRequestBody(request));
                break;
            default:
                throw new IllegalStateException("Unknown method type.");
        }
    }

    private static RequestBody createRequestBody(Request r) throws AuthFailureError {
        final byte[] body = r.getBody();
        if (body == null) return null;

        return RequestBody.create(MediaType.parse(r.getBodyContentType()), body);
    }

    @Override
    public NetworkResponse performRequest(Request<?> request) throws VolleyError {

        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        Map<String, String> headers = request.getHeaders();
        for (final String name : headers.keySet()) {
            builder.addHeader(name, headers.get(name));
        }
        builder.url(request.getUrl());
        setRequestMethod(builder, request);
        builder.tag(request.getTag());
        okhttp3.Request okHttpRequest = builder.build();

        Response okHttpResponse = null;
        try {
            okHttpResponse =
                    VolleyManager.getInstance().getDefaultOkHttpClient().newCall(okHttpRequest)
                            .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        NetworkResponse response = null;
        if (okHttpResponse != null) {
            try {
                byte[] bytes = null;
                if (okHttpResponse.body() != null) {
                    bytes = okHttpResponse.body().bytes();
                    debugLogResponse(bytes);
                }
                ArrayList<Header> headerArrayList = new ArrayList<>();
                for (int i = 0; i < okHttpResponse.headers().toMultimap().size(); i++) {
                    Header header = new Header(okHttpResponse.headers().name(i),
                            okHttpResponse.headers().value(i));
                    headerArrayList.add(header);
                }
                response = new NetworkResponse(okHttpResponse.code(), bytes,
                        okHttpResponse.code() == 304,
                        okHttpResponse.receivedResponseAtMillis() -
                                okHttpResponse.sentRequestAtMillis(), headerArrayList);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (response == null) {
            response = new NetworkResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, null, false, 0,
                    null);
        }
        return response;
    }

    private byte[] obtainByteArray(final InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int read;
        byte[] data = new byte[16384];

        while ((read = input.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, read);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    private void debugLogResponse(byte[] response) {
        if (response != null) {
            Logcat.d(VolleyManager.LOG_TAG + " Response", new String(response));
        }
    }
}