package com.glt.magikoly.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.glt.magikoly.utils.DrawUtils;

import magikoly.magiccamera.R;

/**
 * Created by xfengimacgomo
 * data 2019-06-03 12:50
 * email xfengv@yeah.net
 */
public class CameraCircleView extends View {

    private Paint mPaint;
    public final int FACE_CIRCLE_MARGIN_LEFT_RIGHT = DrawUtils.dip2px(21);

    private Bitmap mCircleMaskBitmap = null;
    private Canvas mCircleMaskCanvas = null;

    public CameraCircleView(Context context) {
        super(context);
        init();
    }

    public CameraCircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(getResources().getColor(R.color.black_mask));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mCircleMaskBitmap == null) {
            mCircleMaskBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_4444);
        }

        if (mCircleMaskCanvas == null) {
            mCircleMaskCanvas = new Canvas(mCircleMaskBitmap);
        }

//        Bitmap bitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_4444);
//        Canvas newCanvas = new Canvas(bitmap);

        mCircleMaskCanvas.save();
        mCircleMaskCanvas.drawColor(Color.TRANSPARENT);
        mCircleMaskCanvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        mCircleMaskCanvas.drawCircle(getMeasuredWidth() / 2,
                DrawUtils.dip2px(96) + getMeasuredWidth() / 2 - FACE_CIRCLE_MARGIN_LEFT_RIGHT,
                getMeasuredWidth() / 2 - FACE_CIRCLE_MARGIN_LEFT_RIGHT, mPaint);
        mPaint.setXfermode(null);
        mCircleMaskCanvas.restore();

        mPaint.reset();
        mPaint.setColor(getContext().getResources().getColor(R.color.take_photo_fragment_cover));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(6);
        mPaint.setPathEffect(new DashPathEffect(new float[]{15, 15}, 0));
        mCircleMaskCanvas.drawCircle(getMeasuredWidth() / 2,
                DrawUtils.dip2px(96) + getMeasuredWidth() / 2 - FACE_CIRCLE_MARGIN_LEFT_RIGHT,
                getMeasuredWidth() / 2 - FACE_CIRCLE_MARGIN_LEFT_RIGHT - DrawUtils.dip2px(3), mPaint);

        mPaint.reset();
        mPaint.setColor(getContext().getResources().getColor(R.color.black_mask));
        canvas.drawBitmap(mCircleMaskBitmap, 0, 0, mPaint);
//        bitmap.recycle();
    }
}
