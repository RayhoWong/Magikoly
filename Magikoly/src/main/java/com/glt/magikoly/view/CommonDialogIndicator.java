package com.glt.magikoly.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.glt.magikoly.utils.DrawUtils;
import magikoly.magiccamera.R;


/**
 * Created by xiaojun on 2017/6/20.
 */

public class CommonDialogIndicator extends LinearLayout {
    private Context mContext;
    private int mCount = 3;
    private int mDrawableSelect = R.drawable.shape_circle_white;
    private int mDrawableNormal = R.drawable.shape_circle_half_transparent;
    private int mPadding = DrawUtils.dip2px(5);

    public CommonDialogIndicator(Context context) {
        this(context, null);
    }

    public CommonDialogIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
        selection(0);
    }

    public void initParams(int count, int drawableSelect, int drawableNormal, int padding) {
        mCount = count;
        if (drawableSelect != 0) {
            mDrawableSelect = drawableSelect;

        }
        if (drawableNormal != 0) {
            mDrawableNormal = drawableNormal;
        }
        if (padding != 0) {
            mPadding = padding;
        }
        removeAllViews();

        init();
    }

    private void init() {
        if (mCount <= 1) {
            return;
        }
        for (int i = 0; i < mCount; i++) {
            ImageView imageView = new ImageView(mContext);
            imageView.setImageResource(mDrawableNormal);
            imageView.setPadding(mPadding, 0, mPadding, 0);
            addView(imageView);
        }
    }

    public void selection(int position) {
        int childCount = getChildCount();
        if (position >= childCount) {
            return;
        }
        for (int i = 0; i < childCount; i++) {
            int resId = position == i ? mDrawableSelect : mDrawableNormal;
            View child = getChildAt(i);
            if (child instanceof ImageView) {
                ((ImageView) child).setImageResource(resId);
            }
        }
    }
}
