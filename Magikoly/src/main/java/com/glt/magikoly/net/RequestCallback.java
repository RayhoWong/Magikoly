package com.glt.magikoly.net;

import com.android.volley.Response;
import com.android.volley.VolleyError;

/**
 * Created by yangjiacheng on 2017/11/17.
 * ...
 */

public interface RequestCallback<T> extends Response.Listener<T>, Response.ErrorListener {

    @Override
    void onErrorResponse(VolleyError error);

    /**
     * Note ：默认情况下，Volley 会在 mainThread 回调此方法
     * @param response response
     */
    @Override
    void onResponse(T response);
}

