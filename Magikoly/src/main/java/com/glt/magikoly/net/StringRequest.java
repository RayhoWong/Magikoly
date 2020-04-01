package com.glt.magikoly.net;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

/**
 * Created by yangjiacheng on 2017/12/27.
 *
 * 处理服务器返回为 String 的请求
 */

public class StringRequest extends AbsRequest<String> {

    StringRequest(Builder builder) {
        super(builder.method, builder.url, builder.headers, builder.params, builder.tag,
                builder.decoder, builder.callback);
    }

    @Override
    protected Response<String> doParseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            if (decoder != null) {
                parsed = decoder.decode(parsed);
            }
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

    public static class Builder extends SuperRequestBuilder<Builder>{

        RequestCallback<String> callback;

        public Builder() {}

        public Builder callback(RequestCallback<String> callback) {
            this.callback = callback;
            return this;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public StringRequest build() {
            checkUrlNull();
            return new StringRequest(this);
        }
    }
}
