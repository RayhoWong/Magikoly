package com.glt.magikoly.function.ethnicity.polygon;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.glt.magikoly.utils.DrawUtils;
import com.glt.magikoly.utils.Logcat;

import java.util.ArrayList;
import java.util.List;

import magikoly.magiccamera.R;

/**
 * @desc:多边形网状分析图 <p>
 * 1.边数可自定义
 * 2.顶点处的最大值可自定义
 * 3.顶点处的数值显示类型可自定义(百分比，还是整数)
 * </p>
 * @auther:duwei
 * @date:2019/1/14
 */
public class PolygonChartView extends View {
    private static final int DEFAULT_NUM_VERTEX = 6;
    private static final int DEFAULT_NUM_LAYER = 6;
    private static final float DEFAULT_RADIUS = DrawUtils.dip2px(50);
    private static final float DEFAULT_LINE_SPACE = DrawUtils.dip2px(70);

    /**
     * 顶点数
     */
    private int mNumVertex;
    /**
     * 多边形的个数（层）
     */
    private int mNumLayer;
    /**
     * 最大多边形的半径
     */
    private float mRadius;
    /**
     * 线段间的间隔
     */
    private float mLineaSpace;

    private int mLineColor = 0;
    private int mNetBackground = 0;
    private int mSelectedAreaColorFrom = 0;
    private int mSelectedAreaColorTo = 0;
    private int mSelectedPointColor = 0;


    private Path mCirclePath;
    private Paint mPolygonPaint;
    private Paint mSelectedAreaPaint;
    private Paint mValuePointPaint;
    private Paint mChartBackgroundPaint;
    private Paint mDashLinePaint;

    private PathMeasure mPathMeasure;
    private float mLengthPerSegment;
    private float[] mPositionPerSegment = new float[2];
    private float[] mTangentPerSegment = new float[2];
    private List<Path> mPolygonPaths;
    private ArrayList<Point> mPoints = new ArrayList<>();
    /**
     * 从最内层的环到最外层的环的连线的Path集合
     */
    private List<Path> mValuePercentPath;
    private List<Point> mOuterPoints;
    private List<Point> mInnerPoints;
    private List<Point> mTitleLocationPoints;
    private List<PolygonChartBean> mPolygonChartBeans;

    public PolygonChartView(Context context) {
        this(context, null);
    }

    public PolygonChartView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolygonChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.PolygonChartView, defStyleAttr, R.style.Widget_PolygonChartView);
        mNumVertex = typedArray.getInteger(R.styleable.PolygonChartView_numVertex, DEFAULT_NUM_VERTEX);
        mNumLayer = typedArray.getInteger(R.styleable.PolygonChartView_numLayers, DEFAULT_NUM_LAYER);
        mRadius = typedArray.getDimension(R.styleable.PolygonChartView_radius, DEFAULT_RADIUS);
        mLineaSpace = typedArray.getDimension(R.styleable.PolygonChartView_lineSpace, DEFAULT_LINE_SPACE);
        mLineColor = typedArray.getColor(R.styleable.PolygonChartView_lineColor,
                context.getResources().getColor(R.color.polygon_lineColor));
        mNetBackground = typedArray.getColor(R.styleable.PolygonChartView_netBackground,
                context.getResources().getColor(R.color.polygon_netBackground));
        mSelectedAreaColorFrom = typedArray.getColor(R.styleable.PolygonChartView_selectedAreaColorFrom,
                context.getResources().getColor(R.color.polygon_selectedAreaColorFrom));
        mSelectedAreaColorTo = typedArray.getColor(R.styleable.PolygonChartView_selectedAreaColorTo,
                context.getResources().getColor(R.color.polygon_selectedAreaColorTo));
        mSelectedPointColor = typedArray.getColor(R.styleable.PolygonChartView_selectedPointColor,
                context.getResources().getColor(R.color.polygon_selectedPointColor));

        typedArray.recycle();

        mCirclePath = new Path();
        mPathMeasure = new PathMeasure();

        mPolygonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPolygonPaint.setStyle(Paint.Style.STROKE);
        mPolygonPaint.setStrokeWidth(3);
        mPolygonPaint.setColor(mLineColor);

        mSelectedAreaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSelectedAreaPaint.setStyle(Paint.Style.FILL);

        mValuePointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mValuePointPaint.setStyle(Paint.Style.FILL);
        mValuePointPaint.setColor(mSelectedPointColor);
        mValuePointPaint.setStrokeWidth(18);

        mChartBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mChartBackgroundPaint.setStyle(Paint.Style.FILL);
        mChartBackgroundPaint.setColor(mNetBackground);
        mChartBackgroundPaint.setStrokeWidth(0.1f);

        mDashLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDashLinePaint.setStyle(Paint.Style.STROKE);
        mDashLinePaint.setStrokeWidth(2f);
        mDashLinePaint.setColor(Color.parseColor("#40491e83"));
        mDashLinePaint.setPathEffect(new DashPathEffect(new float[]{15, 15}, 0));

        mPolygonPaths = new ArrayList<>(mNumLayer);
        mValuePercentPath = new ArrayList<>(mNumVertex);
        mOuterPoints = new ArrayList<>(mNumVertex);
        mInnerPoints = new ArrayList<>(mNumVertex);
        mTitleLocationPoints = new ArrayList<>(mNumVertex);
    }

    public void setData(List<PolygonChartBean> pData) {
        mPolygonChartBeans = pData;
        invalidate();
    }

    public List<PolygonChartBean> getPolygonChartBeans() {
        return mPolygonChartBeans;
    }

    public List<Point> getTitleLocationPoints() {
        return mTitleLocationPoints;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Logcat.d("wdw", "PolygonChartView::onSizeChanged");
        resetAllCollection();

        generateTitleLocation();

        for (int i = 0; i < mNumLayer; i++) {
            mCirclePath.addCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2,
                    mRadius - mLineaSpace * i, Path.Direction.CCW);

            mPathMeasure.setPath(mCirclePath, false);
            mLengthPerSegment = mPathMeasure.getLength() / mNumVertex;

            Path polygonPath = new Path();
            for (int j = 0; j <= mNumVertex; j++) {
                mPathMeasure.getPosTan(
                        (mPathMeasure.getLength() / 4 + mLengthPerSegment * j) % mPathMeasure.getLength(),
                        mPositionPerSegment,
                        mTangentPerSegment);
                float x = mPositionPerSegment[0];
                float y = mPositionPerSegment[1];
//                if (j == 0) {
//                    polygonPath.moveTo(x, y);
//                } else {
//                    polygonPath.lineTo(x, y);
//                }
                if (i == 0) {//只需要存最外面的的多边形的点
                    Point point = new Point(x, y);
                    mOuterPoints.add(point);
                }
                if (i == mNumLayer - 1) {
                    //内部点集合
                    Point point = new Point(x, y);
                    mInnerPoints.add(point);
                    //内点到外点的Path集合
                    Path linePath = new Path();
                    linePath.moveTo(x, y);
                    Point outerPoint = mOuterPoints.get(j);
                    linePath.lineTo(outerPoint.mX, outerPoint.mY);
                    mValuePercentPath.add(linePath);
                }
            }
            polygonPath.set(mCirclePath);
            mPolygonPaths.add(polygonPath);
            mCirclePath.reset();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int viewWidth = (int) mRadius + this.getPaddingLeft() + this.getPaddingRight();
        int viewHeight = (int) mRadius + this.getPaddingTop() + this.getPaddingBottom();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);//获取宽度的模式
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);//获取宽度的尺寸
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);//高度的模式
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);//高端的尺寸
        int width, height;

        if (widthMode == MeasureSpec.EXACTLY) {//指定了确定的尺寸：100dp,match_parent
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {//wrap_content类型
            width = Math.min(viewWidth, widthSize);
        } else {
            width = viewWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(viewHeight, heightSize);
        } else {
            height = viewHeight;
        }
        setMeasuredDimension(width, height);
    }

    private void resetAllCollection() {
        mTitleLocationPoints.clear();
        mOuterPoints.clear();
        mInnerPoints.clear();
        mValuePercentPath.clear();
    }

    private void generateTitleLocation() {
        mCirclePath.addCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2,
                mRadius + 2 * mLineaSpace, Path.Direction.CCW);

        mPathMeasure.setPath(mCirclePath, false);
        mLengthPerSegment = mPathMeasure.getLength() / mNumVertex;

        for (int j = 0; j <= mNumVertex; j++) {
            mPathMeasure.getPosTan(
                    (mPathMeasure.getLength() / 4 + mLengthPerSegment * j) % mPathMeasure.getLength(),
                    mPositionPerSegment,
                    mTangentPerSegment);
            float x = mPositionPerSegment[0];
            float y = mPositionPerSegment[1];
            Point point = new Point(x, y);
            mTitleLocationPoints.add(point);
        }
        mCirclePath.reset();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawPolygon(canvas);
        drawDashLine(canvas);
        drawDataPath(canvas);
    }

    private void drawDataPath(Canvas pCanvas) {
        if (mPolygonChartBeans == null || mPolygonChartBeans.isEmpty()) {
            return;
        }
        Path path = new Path();
        Point point0 = null;
        Point point3 = null;
        for (int i = 0; i < mNumVertex; i++) {
            PolygonChartBean bean = mPolygonChartBeans.get(i);
            if (bean != null) {
                double value = bean.getValue();
                Path line = mValuePercentPath.get(i);
                mPathMeasure.setPath(line, false);
                mPathMeasure.getPosTan((float) (mPathMeasure.getLength() * value),
                        mPositionPerSegment, mTangentPerSegment);

                mPoints.add(new Point(mPositionPerSegment[0], mPositionPerSegment[1]));
//                pCanvas.drawCircle(mPositionPerSegment[0], mPositionPerSegment[1],
//                        DrawUtils.dip2px(3), mValuePointPaint);

                if (i == 0) {
                    point0 = new Point(mPositionPerSegment[0], mPositionPerSegment[1]);
                    path.moveTo(mPositionPerSegment[0], mPositionPerSegment[1]);
                } else {
                    if (i == 3) {
                        point3 = new Point(mPositionPerSegment[0], mPositionPerSegment[1]);
                    }
                    path.lineTo(mPositionPerSegment[0], mPositionPerSegment[1]);
                }
            }

        }
        path.close();
        mSelectedAreaPaint.setShader(new LinearGradient(point0.mX, point0.mY, point3.mX, point3.mY,
                mSelectedAreaColorFrom, mSelectedAreaColorTo, Shader.TileMode.CLAMP));
        pCanvas.drawPath(path, mSelectedAreaPaint);
        for (Point point : mPoints) {
            pCanvas.drawCircle(point.mX, point.mY, DrawUtils.dip2px(3), mValuePointPaint);
        }
        mPoints.clear();
        mSelectedAreaPaint.reset();
        mSelectedAreaPaint.setStyle(Paint.Style.STROKE);
        mSelectedAreaPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mSelectedAreaPaint.setStrokeWidth(DrawUtils.dip2px(1));
        mSelectedAreaPaint.setColor(Color.parseColor("#5e6be1"));
        pCanvas.drawPath(path,mSelectedAreaPaint);
    }

    private void drawPolygon(Canvas pCanvas) {
        for (int i = 0; i < mNumLayer; i++) {
//            if (i == 0) {
                pCanvas.drawPath(mPolygonPaths.get(i), mChartBackgroundPaint);
//            }
//            pCanvas.drawPath(mPolygonPaths.get(i), mPolygonPaint);
        }
    }

    private void drawDashLine(Canvas canvas) {
        for (int i = 0; i < mNumVertex; i++) {
            canvas.drawPath(mValuePercentPath.get(i),mDashLinePaint);
        }
    }

//    private void drawPointsForDebug(Canvas canvas) {
//        for (int i = 0; i < mOuterPoints.size(); i++) {
//            mPolygonPaint.reset();
//            mPolygonPaint.setStrokeWidth(15);
//            mPolygonPaint.setStyle(Paint.Style.FILL);
//            if (i == 1) {
//                mPolygonPaint.setColor(Color.BLACK);
//            } else if (i == 2) {
//                mPolygonPaint.setColor(Color.BLUE);
//            } else {
//                mPolygonPaint.setColor(Color.RED);
//            }
//            Point point = mOuterPoints.get(i);
//            canvas.drawPoint(point.mX, point.mY, mPolygonPaint);
//        }
//
//    }
//
//    private void drawPointsInnerForDebug(Canvas pCanvas) {
//        for (int i = 0; i < mInnerPoints.size(); i++) {
//            mPolygonPaint.reset();
//            mPolygonPaint.setStrokeWidth(15);
//            mPolygonPaint.setStyle(Paint.Style.FILL);
//            if (i == 1) {
//                mPolygonPaint.setColor(Color.BLACK);
//            } else if (i == 2) {
//                mPolygonPaint.setColor(Color.BLUE);
//            } else {
//                mPolygonPaint.setColor(Color.RED);
//            }
//            Point point = mInnerPoints.get(i);
//            pCanvas.drawPoint(point.mX, point.mY, mPolygonPaint);
//        }
//
//    }
//
//    private void drawLinePathForDebug(Canvas pCanvas) {
//        for (int i = 0; i < mNumVertex; i++) {
//            mPolygonPaint.reset();
//            mPolygonPaint.setStrokeWidth(5);
//            mPolygonPaint.setStyle(Paint.Style.STROKE);
//            if (i == 0) {
//                mPolygonPaint.setColor(Color.BLACK);
//            } else if (i == 1) {
//                mPolygonPaint.setColor(Color.BLUE);
//            } else {
//                mPolygonPaint.setColor(Color.RED);
//            }
//            Path path = mValuePercentPath.get(i);
//            pCanvas.drawPath(path, mPolygonPaint);
//        }
//    }
//
//    private void drawTitleLocationForDebug(Canvas pCanvas) {
//        for (Point point : mTitleLocationPoints) {
//            pCanvas.drawPoint(point.mX, point.mY, mValuePointPaint);
//        }
//    }

    public static final class Point {
        public float mX;
        public float mY;

        public Point(float pX, float pY) {
            mX = pX;
            mY = pY;
        }
    }

}
