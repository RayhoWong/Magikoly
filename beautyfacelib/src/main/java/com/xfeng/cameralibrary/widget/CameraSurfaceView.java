package com.xfeng.cameralibrary.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.xfeng.beautyfacelib.R;


/**
 * 注意:此渲染效率相比现在用的效率没有那么高   但是支持渲染模式要多
 * 考虑到目前相机仅仅只有磨皮和美白功能  暂时不使用这个类
 * 后期可以参考使用
 */
@Deprecated
public class CameraSurfaceView extends SurfaceView {

    private static final String TAG = "CainSurfaceView";
    private static final boolean VERBOSE = false;

    // 单击时点击的位置
    private float mTouchX = 0;
    private float mTouchY = 0;

    // 对焦动画
    private ValueAnimator mFocusAnimator;
    // 对焦图片
    private ImageView mFocusImageView;
    // 滑动事件监听
    private OnTouchScroller mScroller;
    // 单双击事件监听
    private OnMultiClickListener mMultiClickListener;

    // 手势监听器
    private GestureDetectorCompat mGestureDetector;

    public CameraSurfaceView(Context context) {
        super(context);
        init();
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setClickable(true);
        mGestureDetector = new GestureDetectorCompat(getContext(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                if (VERBOSE) {
                    Log.d(TAG, "onDown: ");
                }

                //测试贴纸触摸
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {
                if (VERBOSE) {
                    Log.d(TAG, "onShowPress: ");
                }
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (VERBOSE) {
                    Log.d(TAG, "onSingleTapUp: ");
                }
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (VERBOSE) {
                    Log.d(TAG, "onScroll: ");
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (VERBOSE) {
                    Log.d(TAG, "onLongPress: ");
                }
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (VERBOSE) {
                    Log.d(TAG, "onFling: ");
                }

                // 快速左右滑动是
                if (Math.abs(velocityX) > Math.abs(velocityY) * 1.5) {
                    if (velocityX < 0) {
                        if (mScroller != null) {
                            mScroller.swipeBack();
                        }
                    } else {
                        if (mScroller != null) {
                            mScroller.swipeFrontal();
                        }
                    }
                }
                return false;
            }
        });
        mGestureDetector.setOnDoubleTapListener(mDoubleTapListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    /**
     * 添加对焦动画
     */
    public void showFocusAnimation() {
        if (mFocusAnimator == null) {
            mFocusImageView = new ImageView(getContext());
            mFocusImageView.setImageResource(R.drawable.video_focus);
            mFocusImageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mFocusImageView.measure(0, 0);
            mFocusImageView.setX(mTouchX - mFocusImageView.getMeasuredWidth() / 2);
            mFocusImageView.setY(mTouchY - mFocusImageView.getMeasuredHeight() / 2);
            final ViewGroup parent = (ViewGroup) getParent();
            parent.addView(mFocusImageView);

            mFocusAnimator = ValueAnimator.ofFloat(0, 1).setDuration(500);
            mFocusAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if(mFocusImageView != null) {
                        float value = (float) animation.getAnimatedValue();
                        if (value <= 0.5f) {
                            mFocusImageView.setScaleX(1 + value);
                            mFocusImageView.setScaleY(1 + value);
                        } else {
                            mFocusImageView.setScaleX(2 - value);
                            mFocusImageView.setScaleY(2 - value);
                        }
                    }
                }
            });
            mFocusAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if(mFocusImageView != null) {
                        parent.removeView(mFocusImageView);
                        mFocusAnimator = null;
                    }
                }
            });
            mFocusAnimator.start();
        }
    }

    /**
     * 双击监听器
     */
    private final GestureDetector.OnDoubleTapListener mDoubleTapListener = new GestureDetector.OnDoubleTapListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (VERBOSE) {
                Log.d(TAG, "onSingleTapConfirmed: ");
            }
            mTouchX = e.getX();
            mTouchY = e.getY();
            if (mMultiClickListener != null) {
                mMultiClickListener.onSurfaceSingleClick(e.getX(), e.getY());
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (VERBOSE) {
                Log.d(TAG, "onDoubleTap: ");
            }
            if (mMultiClickListener != null) {
                mMultiClickListener.onSurfaceDoubleClick(e.getX(), e.getY());
            }
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            if (VERBOSE) {
                Log.d(TAG, "onDoubleTapEvent: ");
            }
            return true;
        }
    };


    /**
     * 添加滑动回调
     * @param scroller
     */
    public void addOnTouchScroller(OnTouchScroller scroller) {
        mScroller = scroller;
    }

    /**
     * 滑动监听器
     */
    public interface OnTouchScroller {
        void swipeBack();
        void swipeFrontal();
        void swipeUpper(boolean startInLeft, float distance);
        void swipeDown(boolean startInLeft, float distance);
    }

    /**
     * 添加点击事件回调
     * @param listener
     */
    public void addMultiClickListener(OnMultiClickListener listener) {
        mMultiClickListener = listener;
    }

    /**
     * 点击事件监听器
     */
    public interface OnMultiClickListener {

        // 单击
        void onSurfaceSingleClick(float x, float y);

        // 双击
        void onSurfaceDoubleClick(float x, float y);
    }
}