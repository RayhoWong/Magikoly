package com.glt.magikoly.utils;

import android.graphics.*;

/**
 * 绘制圆角工具类
 * Created by chengyuen on 18-1-30.
 */

public class RoundCornerDrawHelper {

    private static final Paint sMaskPaint = new Paint();
    private static final Path sMaskPath = new Path();
    private static final RectF sMaskRectF = new RectF();

    static {
        sMaskPaint.setStyle(Paint.Style.FILL);
        // 取下层绘制非交集部分
        sMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        sMaskPaint.setAntiAlias(true);
        sMaskPaint.setColor(Color.BLACK);
    }

    /**
     * 用于在原来的绘制内容基础上绘制圆角
     * @param canvas
     * @param rx 圆角x半径
     * @param ry 圆角y半径
     * @param callback 原先的绘制操作
     */
    public static void drawRoundCorner(Canvas canvas, float rx, float ry, DrawCallback callback) {
        int save = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null,
                Canvas.ALL_SAVE_FLAG);
        // 绘制原先的内容
        if (callback != null) {
            callback.performDraw();
        }
        // 绘制圆角
        performDrawRoundCorner(canvas, null, rx, ry);
        canvas.restoreToCount(save);
    }

    public static void drawRoundCorner(Canvas canvas, RectF rect, float rx, float ry, DrawCallback callback) {
        int save = canvas.saveLayer(rect, null, Canvas.ALL_SAVE_FLAG);
        // 绘制原先的内容
        if (callback != null) {
            callback.performDraw();
        }
        // 绘制圆角
        performDrawRoundCorner(canvas, rect, rx, ry);
        canvas.restoreToCount(save);
    }

    private static void performDrawRoundCorner(Canvas canvas, RectF rect, float rx, float ry) {
        drawLeftTop(canvas, rect, rx, ry);
        drawLeftBottom(canvas, rect, rx, ry);
        drawRightTop(canvas, rect, rx, ry);
        drawRightBottom(canvas, rect, rx, ry);
    }

    private static void drawLeftTop(Canvas canvas, RectF rect, float rx, float ry) {
        float x = rect != null ? rect.left : 0f;
        float y = rect != null ? rect.top : 0f;
        sMaskPath.reset();
        sMaskPath.moveTo(x, ry);
        sMaskPath.lineTo(y, 0);
        sMaskPath.lineTo(rx, 0);
        sMaskRectF.set(x, y, x + rx * 2, y + ry * 2);
        sMaskPath.arcTo(sMaskRectF, -90, -90);
        sMaskPath.close();
        canvas.drawPath(sMaskPath, sMaskPaint);
    }

    private static void drawLeftBottom(Canvas canvas, RectF rect, float rx, float ry) {
        float x = rect != null ? rect.left : 0f;
        float y = rect != null ? rect.bottom : canvas.getHeight();
        sMaskPath.reset();
        sMaskPath.moveTo(x, y - ry);
        sMaskPath.lineTo(x, y);
        sMaskPath.lineTo(rx, y);
        sMaskRectF.set(x, y - ry * 2, x + rx * 2, y);
        sMaskPath.arcTo(sMaskRectF, 90, 90);
        sMaskPath.close();
        canvas.drawPath(sMaskPath, sMaskPaint);
    }

    private static void drawRightBottom(Canvas canvas, RectF rect, float rx, float ry) {
        float x = rect != null ? rect.right : canvas.getWidth();
        float y = rect != null ? rect.bottom : canvas.getHeight();
        sMaskPath.reset();
        sMaskPath.moveTo(x - rx, y);
        sMaskPath.lineTo(x, y);
        sMaskPath.lineTo(x, y - ry);
        sMaskRectF.set(x - rx * 2, y - ry * 2, x, y);
        sMaskPath.arcTo(sMaskRectF, 0, 90);
        sMaskPath.close();
        canvas.drawPath(sMaskPath, sMaskPaint);
    }

    private static void drawRightTop(Canvas canvas, RectF rect, float rx, float ry) {
        float x = rect != null ? rect.right : canvas.getWidth();
        float y = rect != null ? rect.top : 0;
        sMaskPath.reset();
        sMaskPath.moveTo(x, ry);
        sMaskPath.lineTo(x, 0);
        sMaskPath.lineTo(x - rx, 0);
        sMaskRectF.set(x - rx * 2, y, x, y + ry * 2);
        sMaskPath.arcTo(sMaskRectF, -90, 90);
        sMaskPath.close();
        canvas.drawPath(sMaskPath, sMaskPaint);
    }

    public interface DrawCallback {
        void performDraw();
    }
}
