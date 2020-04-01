package com.glt.magikoly.config;

import android.text.TextUtils;
import com.glt.magikoly.FaceAppState;
import com.glt.magikoly.FaceEnv;
import com.glt.magikoly.cache.CacheManager;
import com.glt.magikoly.cache.utils.RestoreUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kingyang on 2016/7/26.
 */
public abstract class AbsConfigBean {

    protected int mAbTestId = -1; //AB测试ID

    protected int mFilterId = -1;

    protected boolean mIsInited; //是否已经初始化过

    protected CacheManager mCacheManager = null;

    public AbsConfigBean() {
        mCacheManager = new CacheManager(RestoreUtil.getCacheImpl(FaceEnv.Path.ABCONFIG_CACHE,
                FaceEnv.InternalPath.getInnerFilePath(FaceAppState.getContext(), FaceEnv.InternalPath.ABTEST_DIR),
                getCacheKey()));
    }

    public void setAbTestId(int abTestId) {
        mAbTestId = abTestId;
        if (mAbTestId != -1) {
//            BaseSeq103OperationStatistic.uploadSqe103StatisticData(FaceAppState.getContext(),
//                    String.valueOf(mAbTestId), BaseSeq103OperationStatistic.SER_ABTEST,
//                    BaseSeq103OperationStatistic.OPERATE_SUCCESS, "", "", "", "", "", "");
        }
    }

    public int getAbTestId() {
        return mAbTestId;
    }

    public void setFilterId(int filterId) {
        mFilterId = filterId;
    }

    public int getFilterId() {
        return mFilterId;
    }

    public void readObjectByCache() {
        if (mIsInited) {
            return;
        }
        byte[] cache = mCacheManager.loadCache(getCacheKey());
        if (cache != null) {
            String dataJson = new String(cache);
            if (!TextUtils.isEmpty(dataJson)) {
                try {
                    extractData(new JSONObject(dataJson));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        mIsInited = true;
    }

    public void saveObjectToCache(JSONObject dataJson) {
        if (extractData(dataJson)) {
            mCacheManager.saveCacheAsync(getCacheKey(), dataJson.toString().getBytes(), null);
        }
        mIsInited = true;
    }

    private boolean extractData(JSONObject dataJson) {
        if (dataJson != null) {
            JSONObject infoJson = dataJson.optJSONObject("infos");
            if (null != infoJson) {
                try {
                    int abTestId = infoJson.getInt("abtest_id");
                    if (abTestId != -1) {
                        setAbTestId(abTestId);
                    }
                    int filterId = infoJson.getInt("filter_id");
                    if (filterId != -1) {
                        setFilterId(filterId);
                    }
                } catch (JSONException e) {
                    //do nothing
                }
                JSONArray cfgs = infoJson.optJSONArray("cfgs");
                if (cfgs != null && cfgs.length() > 0) {
                    readConfig(cfgs);
                    return true;
                } else {
                    restoreDefault();
                }
            }
        }
        return false;
    }

    protected abstract void readConfig(JSONArray jsonArray);

    public abstract String getCacheKey();

    protected abstract void restoreDefault();
}
