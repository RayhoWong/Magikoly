package com.glt.magikoly.net;

import android.support.v4.util.ArrayMap;

import com.android.volley.Request;

import java.util.Map;

/**
 * Created by yangjiacheng on 2017/12/27.
 * ...
 */

public abstract class SuperRequestBuilder<T extends SuperRequestBuilder<T>> {

    protected abstract T getThis();

    public abstract Request build();

    String url;
    int method = Request.Method.GET;
    Map<String, String> headers = new ArrayMap<>(20);
    Map<String, String> params = new ArrayMap<>(20);
    Object tag;
    ResponseDecoder decoder;

    SuperRequestBuilder() {}

    public T method(int method) {
        this.method = method;
        return getThis();
    }

    public T url(String url) {
        this.url = url;
        return getThis();
    }

    public T headers(Map<String, String> headers) {
        this.headers.putAll(headers);
        return getThis();
    }

    public T addHeader(String headerName, String headerValue) {
        this.headers.put(headerName, headerValue);
        return getThis();
    }

    public T params(Map<String, String> params) {
        this.params.putAll(params);
        return getThis();
    }

    public T addParam(String paramName, String paramValue) {
        this.params.put(paramName, paramValue);
        return getThis();
    }

    public T setTag(Object tag) {
        this.tag = tag;
        return getThis();
    }

    public T decoder(ResponseDecoder decoder) {
        this.decoder = decoder;
        return getThis();
    }

    void checkUrlNull() {
        if (url == null) throw new IllegalStateException("url == null");
    }
}
