package com.glt.magikoly.mvp;

import java.lang.ref.WeakReference;

/**
 * Created by yangguanxiang on 2018/3/1.
 */

public abstract class AbsPresenter<T extends IViewInterface> {

    private WeakReference<T> mViewRef;

    public void attachView(T view) {
        mViewRef = new WeakReference<>(view);
    }

    protected T getView() {
        if (mViewRef == null) {
            return null;
        }
        return mViewRef.get();
    }

    public void detachView() {
        if (mViewRef != null) {
            mViewRef.clear();
            mViewRef = null;
        }
    }
}
