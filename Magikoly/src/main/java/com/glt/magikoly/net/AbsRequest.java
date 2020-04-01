package com.glt.magikoly.net;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;

import java.net.HttpURLConnection;
import java.util.Map;

/**
 * Created by yangjiacheng on 2018/4/17.
 * ...
 */
public abstract class AbsRequest<T> extends Request<T> {

    private Gson gson;

    private Map<String, String> params;
    private Map<String, String> headers;

    private RequestCallback<T> callback;
    protected ResponseDecoder decoder;

    AbsRequest(int method, String url, Map<String, String> headers, Map<String, String> params,
            Object tag, ResponseDecoder decoder, RequestCallback<T> callback) {
        super(method, url, callback);
        this.headers = headers;
        this.params = params;
        this.decoder = decoder;
        this.callback = callback;
        setRequestTag(tag);
    }

    @Override
    protected void deliverResponse(T response) {
        if (callback != null) {
            callback.onResponse(response);
        }
    }

    @Override
    protected Map<String, String> getParams() {
        return params;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    public void execute() {
        VolleyManager.getInstance().add(this);
    }

    private void setRequestTag(Object tag) {
        if (null == tag) {
            tag = VolleyManager.DEFAULT_REQUEST_TAG;
        }
        setTag(tag);
    }

    Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    @Override
    final protected Response<T> parseNetworkResponse(NetworkResponse response) {
        if (response.statusCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
            return Response.error(new VolleyError(response));
        } else {
            return doParseNetworkResponse(response);
        }
    }

    protected abstract Response<T> doParseNetworkResponse(NetworkResponse response);
}
