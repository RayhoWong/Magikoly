package com.glt.magikoly.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.glt.magikoly.utils.DrawUtils;

public class PointProgressBar extends View {
    private static int POINT_SIZE = DrawUtils.dip2px(18);
    private static float POINT_RADIUS = DrawUtils.dip2px(7);
    private static int START_COLOR = Color.parseColor("#3000aa");
    private static int END_COLOR = Color.parseColor("#af00d2");
    private static final int DEFAULT_WIDTH = POINT_SIZE * 6;
    private static final int DEFAULT_HEIGHT = POINT_SIZE * 4;
    private ArgbEvaluator mEvaluator = new ArgbEvaluator();
    private Paint mPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Point[] mPoints = new Point[5];
    private ValueAnimator mAnim;
    private int mCurrentX;
    private boolean mReverse = false;
    int averageLength;
    float mFraction;

    public PointProgressBar(Context context) {
        this(context, null);
    }

    public PointProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PointProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPointPaint.setStyle(Paint.Style.FILL);
        mPointPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        } else if (widthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(DEFAULT_WIDTH, heightSize);
        } else if (heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSize, DEFAULT_HEIGHT);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        averageLength = w / mPoints.length;
//        mPointPaint.setStrokeWidth(averageLength * 0.7f);
        for (int i = 0; i < mPoints.length; i++) {
            Point point = new Point();
            point.y = h / 2;
            point.x = averageLength / 2 + averageLength * i;
            mPoints[i] = point;
        }
        initAnim();
    }

    private void initAnim() {
        mAnim = ValueAnimator.ofInt(mPoints[0].x, mPoints[mPoints.length - 1].x);
        mAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentX = (int) animation.getAnimatedValue();
                mFraction = animation.getAnimatedFraction();
                invalidate();
            }

        });
        mAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                mReverse = !mReverse;
            }
        });
        mAnim.setDuration(1500);
        mAnim.setRepeatCount(ValueAnimator.INFINITE);
        mAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnim.setRepeatMode(ValueAnimator.REVERSE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int lastIndex = 0;
        int nextIndex = mPoints.length - 1;
        for (int i = 0; i < mPoints.length; i++) {
            Point point = mPoints[i];
            if (point.x > mCurrentX) {
                nextIndex = i;
                break;
            } else {
                lastIndex = i;
            }
        }

        int centerX = mCurrentX;
        int centerY = getHeight() / 2;

//        int x = mPoints[nextIndex].x - (mCurrentX - mPoints[lastIndex].x);
//        double y = Math.sqrt(Math.pow(averageLength / 2f, 2) - Math.pow(x - centerX, 2)) * (mReverse ? 1 : -1) + centerY;
        float radius = mPoints[nextIndex].x - mPoints[lastIndex].x;
        double angle = (mCurrentX - mPoints[lastIndex].x) / radius * 180;
        double x = centerX + radius * Math.cos(angle * 3.14 / 180 * (mReverse ? 1 : -1));
        double y = centerY + radius * Math.sin(angle * 3.14 / 180 * (mReverse ? 1 : -1));
        int evaluate = (int) mEvaluator.evaluate(mCurrentX * 1f / getWidth(), START_COLOR, END_COLOR);
        mPointPaint.setColor(evaluate);
        canvas.drawCircle((float) x, (float) y, POINT_RADIUS, mPointPaint);
        canvas.drawCircle(mCurrentX, getHeight() / 2f, POINT_RADIUS, mPointPaint);


        for (int i = lastIndex - 1; i >= 0; i--) {
            Point point = mPoints[i];
            evaluate = (int) mEvaluator.evaluate(point.x * 1f / getWidth(), START_COLOR, END_COLOR);
            mPointPaint.setColor(evaluate);
            canvas.drawCircle(point.x, point.y, POINT_RADIUS, mPointPaint);
        }

        for (int i = nextIndex + 1; i < mPoints.length; i++) {
            Point point = mPoints[i];
            evaluate = (int) mEvaluator.evaluate(point.x * 1f / getWidth(), START_COLOR, END_COLOR);
            mPointPaint.setColor(evaluate);
            canvas.drawCircle(point.x, point.y, POINT_RADIUS, mPointPaint);
        }

    }

    public void startAnim() {
        if (mAnim != null && !mAnim.isRunning()) {
            mAnim.start();
            mReverse = false;
        }
    }

    public void stopAnim() {
        if (mAnim != null) {
            mAnim.cancel();
        }
    }
}
