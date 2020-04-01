package com.glt.magikoly.blur;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;

import com.glt.magikoly.FaceAppState;


public class BlurKit {

    public static final boolean USE_DYNAMIC_BLUR = false;
    public static final int MAX_BLUR_RADIUS = 10;
    private static boolean sIsDevicesSupport = true;

    private int mLastBitmapWidth = -1;
    private int mLastBitmapHeight = -1;
    private RenderScript mRenderScript;
    private ScriptIntrinsicBlur mBlurScript;
    private Allocation mOutAllocation;
    private Context mContext;

    static {
        //类初始化时做一次硬件支持测试
        try {
            RenderScript.create(FaceAppState.getContext());
        } catch (Throwable t) {
            sIsDevicesSupport = false;
        }
    }
    public BlurKit() {
        mContext = FaceAppState.getContext();
        try {
            mRenderScript = RenderScript.create(mContext);
            if (isBlurAvailable()) {
                mBlurScript = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));
            }
        } catch (Throwable t) {
            sIsDevicesSupport = false;
        }
    }

    public Bitmap blur(Bitmap src, float radius) {
        return blurByRenderScript(src, (int) radius);
    }

    private boolean canReuseAllocation(Bitmap bitmap) {
        return bitmap.getHeight() == mLastBitmapHeight && bitmap.getWidth() == mLastBitmapWidth;
    }

    private Bitmap blurByRenderScript(Bitmap bitmap, int radius) {
        if (isBlurAvailable() && mBlurScript != null && radius > 0 && radius <= 25) {
            Allocation inAllocation = Allocation.createFromBitmap(mRenderScript, bitmap);

            if (!canReuseAllocation(bitmap)) {
                if (mOutAllocation != null) {
                    mOutAllocation.destroy();
                }
                mOutAllocation = Allocation.createTyped(mRenderScript, inAllocation.getType());
                mLastBitmapWidth = bitmap.getWidth();
                mLastBitmapHeight = bitmap.getHeight();
            }

            mBlurScript.setRadius(radius);
            mBlurScript.setInput(inAllocation);
            //do not use inAllocation in forEach. it will cause visual artifacts on blurred Bitmap
            mBlurScript.forEach(mOutAllocation);
            mOutAllocation.copyTo(bitmap);

            inAllocation.destroy();
            return bitmap;
        }
        return bitmap;
    }

    public Bitmap blur(View src, int radius) {
        Bitmap bitmap = getBitmapForView(src, 1f);
        return blur(bitmap, radius);
    }

    public Bitmap fastBlur(View src, int radius, float downscaleFactor) {
        Bitmap bitmap = getBitmapForView(src, downscaleFactor);
        return blur(bitmap, radius);
    }

    public Bitmap getBitmapForView(View src, float downscaleFactor) {
        Bitmap bitmap = Bitmap.createBitmap(
                (int) (src.getWidth() * downscaleFactor),
                (int) (src.getHeight() * downscaleFactor),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);
        Matrix matrix = new Matrix();
        matrix.preScale(downscaleFactor, downscaleFactor);
        canvas.setMatrix(matrix);
        src.draw(canvas);

        return bitmap;
    }

    public static final boolean isBlurAvailable() {
        return android.os.Build.VERSION.SDK_INT >= 17 && sIsDevicesSupport;
//        return false;
    }
}
