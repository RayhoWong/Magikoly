package com.glt.magikoly.mvp;

/**
 * Created by yangguanxiang on 2018/3/1.
 */

public interface IViewInterface<T extends AbsPresenter> {
    T createPresenter();
}