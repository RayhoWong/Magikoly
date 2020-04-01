package com.glt.magikoly.function.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import magikoly.magiccamera.R;

public class FaceCommonToolBar extends ConstraintLayout implements View.OnClickListener {
    private ImageView mBack;
    private TextView mTitle;
    private ImageView mMenu;
    private ImageView mMenuLeft;
    private OnTitleClickListener mOnTitleClickListener;

    public FaceCommonToolBar(Context context) {
        super(context);
    }

    public FaceCommonToolBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FaceCommonToolBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mBack = findViewById(R.id.img_back);
        mTitle = findViewById(R.id.tv_title);
        mMenu = findViewById(R.id.img_menu);
        mMenuLeft = findViewById(R.id.img_menu_left);
        setOnClickListener(this);
        mBack.setOnClickListener(this);
        mMenu.setOnClickListener(this);
        mMenuLeft.setOnClickListener(this);
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    public void setTitle(CharSequence title) {
        if (mTitle != null) {
            mTitle.setText(title);
        }
    }

    public void setTitleColor(int color) {
        if (mTitle != null) {
            mTitle.setTextColor(color);
        }
    }

    public void setTitleGravity(int gravity) {
        mTitle.setGravity(gravity);
    }

    public void setBackDrawable(Drawable drawable) {
        if (mBack != null) {
            if (drawable == null) {
                mBack.setVisibility(INVISIBLE);
            } else {
                mBack.setVisibility(VISIBLE);
                mBack.setImageDrawable(drawable);
            }
        }
    }

    public void setBackDrawable(@DrawableRes int id){
        if (mBack != null) {
            if (id == 0) {
                mBack.setVisibility(INVISIBLE);
            } else {
                mBack.setVisibility(VISIBLE);
                mBack.setImageResource(id);
            }
        }
    }

    public void setMenuDrawable(Drawable drawable) {
        if (mMenu != null) {
            if (drawable == null) {
                mMenu.setVisibility(INVISIBLE);
            } else {
                mMenu.setVisibility(VISIBLE);
                mMenu.setImageDrawable(drawable);
            }
        }
    }

    public void setMenuLeftDrawable(Drawable drawable) {
        if (mMenuLeft != null) {
            if (drawable == null) {
                mMenuLeft.setVisibility(INVISIBLE);
            } else {
                mMenuLeft.setVisibility(VISIBLE);
                mMenuLeft.setImageDrawable(drawable);
            }
        }
    }

    public void setOnTitleClickListener(OnTitleClickListener onTitleClickListener) {
        mOnTitleClickListener = onTitleClickListener;
    }

    @Override
    public void onClick(View v) {
        if (mOnTitleClickListener != null) {
            mOnTitleClickListener.onTitleClick(v, v == mBack);
        }
    }

    public void setItemColorFilter(int color) {
        if (mBack != null) {
            mBack.setColorFilter(color);
        }
        if (mMenu != null) {
            mMenu.setColorFilter(color);
        }
        if (mTitle != null) {
            mTitle.setTextColor(color);
        }
    }

    public interface OnTitleClickListener {
        void onTitleClick(View view, boolean back);
    }
}
