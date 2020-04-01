package com.glt.magikoly.ad.view;

import android.content.Context;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.FrameLayout;

import com.glt.magikoly.BuyChannelApiProxy;
import com.glt.magikoly.utils.Logcat;
import com.mopub.mobileads.MoPubView;

/**
 * Created by kingyang on 2017/5/26.
 */

public class MoPubViewWrapper extends FrameLayout {

    private boolean mIsAutoRefreshAvailable;
    private MoPubView mMoPubView;
    private boolean mIsOpenLockUser;

    public MoPubViewWrapper(Context context, MoPubView moPubView, boolean isAutoRefreshAvailable) {
        this(context, moPubView, isAutoRefreshAvailable, false);
    }

    public MoPubViewWrapper(Context context, MoPubView moPubView, boolean isAutoRefreshAvailable,
            boolean isOpenLockUser) {
        super(context);
        mMoPubView = moPubView;
        mIsAutoRefreshAvailable = isAutoRefreshAvailable;
        mIsOpenLockUser = isOpenLockUser;
        if (!mIsAutoRefreshAvailable) {
            addView(mMoPubView,
                    new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            mMoPubView.setAutorefreshEnabled(false);
        } else {
            if (!BuyChannelApiProxy.isBuyUser() && mIsOpenLockUser) {
                PowerManager powerManager =
                        (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
                boolean isScreenOn = powerManager.isScreenOn();
                if (isScreenOn) {
                    addView(mMoPubView,
                            new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                }
                handleScreenChange(isScreenOn);
            } else {
                addView(mMoPubView,
                        new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            }
        }
    }

    private void handleScreenChange(boolean isScreenOn) {
        if (mIsAutoRefreshAvailable) {
            if (isScreenOn) {
                mMoPubView.setAutorefreshEnabled(true);
                Logcat.i("MoPubViewWrapper", "打开广告自动刷新");
            } else {
                mMoPubView.setAutorefreshEnabled(false);
                Logcat.i("MoPubViewWrapper", "关闭广告自动刷新");
            }
        }
    }

    public void forceRefresh() {
        mMoPubView.forceRefresh();
    }

    public void setAutoRefreshAvailable(boolean enable) {
        mIsAutoRefreshAvailable = enable;
        mMoPubView.setAutorefreshEnabled(enable);
    }

    @Override
    public boolean performClick() {
        return mMoPubView.performClick();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        onDestroy();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        if (mIsAutoRefreshAvailable) {
            if (isShown()) {
                if (!mMoPubView.getAutorefreshEnabled()) {
                    mMoPubView.setAutorefreshEnabled(true);
                }
            } else {
                if (mMoPubView.getAutorefreshEnabled()) {
                    mMoPubView.setAutorefreshEnabled(false);
                }
            }
        }
    }

    public void onResume() {
        if (!BuyChannelApiProxy.isBuyUser() && mIsOpenLockUser) {
            handleScreenChange(true);
        }
    }

    public void onPause() {
        if (!BuyChannelApiProxy.isBuyUser() && mIsOpenLockUser) {
            handleScreenChange(false);
        }
    }

    public void onDestroy() {
        mMoPubView.setAutorefreshEnabled(false);
        mMoPubView.destroy();
    }

    public void setBannerAdListener(MoPubView.BannerAdListener listener) {
        mMoPubView.setBannerAdListener(listener);
    }
}
