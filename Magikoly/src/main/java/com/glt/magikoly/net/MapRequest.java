package com.glt.magikoly.net;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by yangjiacheng on 2017/12/7.
 * <p>
 * 支持请求到的数据是多个并列 Json 对象并列的格式，映射为 Map 类型，以 <Map<String, T> 的格式回调
 * Note：并列 Json 对象需对应相同的 bean，只是key不同。
 * @see #dataKey 为并列 Json 对象的 Key
 * <p>
 *
 * {@link GsonRequest} 处理返回为单一的Json对象的请求
 *
 */

public class MapRequest<T> extends AbsRequest<Map<String, T>> {

    private final Class<T> clazz;
    private final String dataKey;

    MapRequest(Builder<T> builder) {
        super(builder.method, builder.url, builder.headers, builder.params, builder.tag,
                builder.decoder, builder.callback);
        this.clazz = builder.clazz;
        this.dataKey = builder.dataKey;
    }

    @Override
    protected Response<Map<String, T>> doParseNetworkResponse(NetworkResponse response) {
        Type typeOfHashMap = new TypeToken<Map<String, Object>>() {
        }.getType();
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            if (decoder != null) {
                json = decoder.decode(json);
            }
            JSONObject jsonObject = new JSONObject(json);
            String responseData = jsonObject.getString(dataKey);
            Map<String, Object> objectMap = getGson().fromJson(responseData, typeOfHashMap);
            Map<String, T> responseMap = new ArrayMap<>();
            for (String key : objectMap.keySet()) {
                String objectJson = new Gson().toJson(objectMap.get(key));
                T object = new Gson().fromJson(objectJson, clazz);
                responseMap.put(key, object);
            }
            return Response.success(responseMap, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            try {
                String json = new String(response.data);
                if (decoder != null) {
                    json = decoder.decode(json);
                }
                JSONObject jsonObject = new JSONObject(json);
                String responseData = jsonObject.getString(dataKey);
                Map<String, Object> objectMapMap = getGson().fromJson(responseData, typeOfHashMap);
                Map<String, T> responseMap = new ArrayMap<>();
                for (String key : objectMapMap.keySet()) {
                    String objectJson = new Gson().toJson(responseMap.get(key));
                    T object = new Gson().fromJson(objectJson, clazz);
                    responseMap.put(key, object);
                }
                return Response.success(responseMap, HttpHeaderParser.parseCacheHeaders(response));
            } catch (Exception e1) {
                return Response.error(new ParseError(e));
            }
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    public static class Builder<T> extends SuperRequestBuilder<Builder<T>> {

        Class<T> clazz;
        String dataKey;
        RequestCallback<Map<String, T>> callback;

        public Builder() {}

        public Builder<T> dataKey(@NonNull String dataKey) {
            this.dataKey = dataKey;
            return this;
        }

        public Builder<T> targetObject(@NonNull Class<T> clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder<T> callback(RequestCallback<Map<String, T>> callback) {
            this.callback = callback;
            return this;
        }

        @Override
        protected Builder<T> getThis() {
            return this;
        }

        @Override
        public MapRequest<T> build() {
            checkUrlNull();
            if (clazz == null) throw new IllegalStateException("targetObject == null");
            if (dataKey == null) throw new IllegalStateException("dataKey == null");
            return new MapRequest<>(this);
        }
    }
}
