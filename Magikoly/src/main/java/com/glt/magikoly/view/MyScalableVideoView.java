package com.glt.magikoly.view;

import android.content.Context;
import android.util.AttributeSet;

import com.yqritc.scalablevideoview.ScalableVideoView;

/**
 * @author rayhahah
 * @blog http://rayhahah.com
 * @time 2019/3/20
 * @tips 这个类是Object的子类
 * @fuction
 */
public class MyScalableVideoView extends ScalableVideoView {
    public MyScalableVideoView(Context context) {
        this(context, null);
    }

    public MyScalableVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyScalableVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void releaseSafety() {
        if (mMediaPlayer == null) {
            return;
        }
        if (isPlaying()) {
            stop();
        }
        release();
    }
}
