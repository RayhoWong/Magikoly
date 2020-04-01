package com.glt.magikoly.net;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;


/**
 * Created by yangjiacheng on 2017/12/9.
 * 直接从请求中读出响应 bytes
 */

public class BytesRequest extends AbsRequest<byte[]> {

    BytesRequest(Builder builder) {
        super(builder.method, builder.url, builder.headers, builder.params, builder.tag,
                builder.decoder, builder.callback);
    }

    @Override
    protected Response<byte[]> doParseNetworkResponse(NetworkResponse response) {
        return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
    }

    public static class Builder extends SuperRequestBuilder<Builder> {

        RequestCallback<byte[]> callback;

        Builder() {}

        Builder callback(RequestCallback<byte[]> callback) {
            this.callback = callback;
            return this;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public BytesRequest build() {
            checkUrlNull();
            return new BytesRequest(this);
        }
    }
}