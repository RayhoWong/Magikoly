package com.glt.magikoly.net;

import com.android.volley.AuthFailureError;

import org.json.JSONObject;

/**
 * Created by yangjiacheng on 2018/4/17.
 * 需要 post 一个Json 字符串到服务器的请求
 * TODO 考虑到提交内容的ContentType有多种情况，不一定只是 “application/json”，后续需要对其他情况进行封装
 */
public class GsonPostRequest<T> extends GsonRequest<T> {

    private static final String PROTOCOL_CHARSET = "utf-8";

    private static final String PROTOCOL_CONTENT_TYPE =
            String.format("application/json; charset=%s", PROTOCOL_CHARSET);

    private final Object mRequestBody;

    GsonPostRequest(Builder<T> builder) {
        super(builder.method, builder.url, builder.headers, builder.params, builder.tag,
                builder.decoder, builder.callback, builder.clazz);
        mRequestBody = builder.requestBody;
    }

    @Override
    public String getBodyContentType() {
        if (mRequestBody == null) {
            return PROTOCOL_CHARSET;
        }
        return PROTOCOL_CONTENT_TYPE;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if (mRequestBody == null) {
            return super.getBody();
        }
        if (mRequestBody instanceof JSONObject) {
            return mRequestBody.toString().getBytes();
        } else if (mRequestBody instanceof String) {
            return ((String) mRequestBody).getBytes();
        }
        return null;
    }

    public static class Builder<T> extends SuperRequestBuilder<Builder<T>> {

        Class<T> clazz;
        RequestCallback<T> callback;
        Object requestBody;

        public Builder() {}

        public Builder<T> targetObject(Class<T> clazz) {
            this.clazz = clazz;
            return getThis();
        }

        public Builder<T> callback(RequestCallback<T> callback) {
            this.callback = callback;
            return getThis();
        }

        public Builder<T> requestBody(Object requestBody) {
            this.requestBody = requestBody;
            return getThis();
        }

        @Override
        protected Builder<T> getThis() {
            return this;
        }

        @Override
        public GsonPostRequest<T> build() {
            checkUrlNull();
            if (clazz == null) throw new IllegalStateException("targetObject == null");
            return new GsonPostRequest<>(this);
        }
    }

}
