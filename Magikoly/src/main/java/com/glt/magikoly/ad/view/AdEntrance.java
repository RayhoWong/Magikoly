package com.glt.magikoly.ad.view;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({AdEntrance.LAUNCHER,
        AdEntrance.LOADING_PAGE})

@Retention(RetentionPolicy.SOURCE)
public @interface AdEntrance {
    int LAUNCHER = 1; //启动后全屏
    int LOADING_PAGE = 2; //扫描/加载页底部
}
