package com.glt.magikoly.net;

import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.glt.magikoly.utils.Logcat;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by yangjiacheng on 2017/11/17.
 *
 * 处理服务器返回为单一的Json对象的请求
 */

public class GsonRequest<T> extends AbsRequest<T> {

    private final Class<T> clazz;

    GsonRequest(Builder<T> builder) {
        this(builder.method, builder.url, builder.headers, builder.params, builder.tag,
                builder.decoder, builder.callback, builder.clazz);
    }

    GsonRequest(int method, String url, Map<String, String> headers, Map<String, String> params,
            Object tag, ResponseDecoder decoder, RequestCallback<T> callback, Class<T> clazz) {
        super(method, url, headers, params, tag, decoder, callback);
        this.clazz = clazz;
    }

    @Override
    protected Response<T> doParseNetworkResponse(NetworkResponse response) {
        String isEncrypt = response.headers.get("Server-Encrypt");
        try {
            String json =
                    new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            Logcat.i("GsonRequest", "json: " + json);
            if (json.contains("403 Forbidden")) {
                response = new NetworkResponse(403, response.data, response.notModified,
                        response.networkTimeMs, response.allHeaders);
                return Response.error(new NetworkError(response));
            }
            if ("true".equals(isEncrypt) && decoder != null) {
                try {
                    json = decoder.decode(json);
                } catch (Exception e1) {
                    return Response.error(new ParseError(e1));
                }
            }
            return Response.success(getGson().fromJson(json, clazz),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e2) {
            try {
                String json = new String(response.data);
                if ("true".equals(isEncrypt) && decoder != null) {
                    try {
                        json = decoder.decode(json);
                    } catch (Exception e3) {
                        return Response.error(new ParseError(e3));
                    }
                }
                return Response.success(getGson().fromJson(json, clazz), HttpHeaderParser.parseCacheHeaders(response));
            } catch (JsonSyntaxException e4) {
                return Response.error(new ParseError(e4));
            }
        } catch (JsonSyntaxException e5) {
            return Response.error(new ParseError(e5));
        }
    }

    public static class Builder<T> extends SuperRequestBuilder<Builder<T>> {

        Class<T> clazz;
        RequestCallback<T> callback;

        public Builder() {
        }

        public Builder<T> targetObject(Class<T> clazz) {
            this.clazz = clazz;
            return getThis();
        }

        public Builder<T> callback(RequestCallback<T> callback) {
            this.callback = callback;
            return getThis();
        }

        @Override
        protected Builder<T> getThis() {
            return this;
        }

        @Override
        public GsonRequest<T> build() {
            checkUrlNull();
            if (clazz == null) throw new IllegalStateException("targetObject == null");
            return new GsonRequest<>(this);
        }
    }
}