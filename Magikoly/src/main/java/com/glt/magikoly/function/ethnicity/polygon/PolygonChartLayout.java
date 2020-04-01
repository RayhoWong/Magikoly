package com.glt.magikoly.function.ethnicity.polygon;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import magikoly.magiccamera.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @desc:
 * @auther:duwei
 * @date:2019/1/15
 */
public class PolygonChartLayout extends FrameLayout {
    private PolygonChartView mChartView;
    private List<View> mTitleViews;
    private List<PolygonChartView.Point> mTitleLocationPoints;
    private List<PolygonChartBean> mPolygonChartBeans;

    public PolygonChartLayout(@NonNull Context context) {
        this(context, null);
    }

    public PolygonChartLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolygonChartLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater inflater = LayoutInflater.from(context);
        mTitleViews = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            View v = inflater.inflate(R.layout.function_ethnicity_polygon_title, null);
            mTitleViews.add(v);
            addView(v);
        }
        mChartView = (PolygonChartView) inflater.inflate(R.layout.function_ethnicity_polygon, null);
        addView(mChartView);
    }

    public void setData(List<PolygonChartBean> polygonChartBeans) {
        mPolygonChartBeans = polygonChartBeans;
        mChartView.setData(mPolygonChartBeans);

        for (int i = 0; i < 6; i++) {
            View v = mTitleViews.get(i);
            ((ImageView) v.findViewById(R.id.polygon_title_icon))
                    .setImageResource(mPolygonChartBeans.get(i).getIcon());
            ((TextView) v.findViewById(R.id.polygon_title_name))
                    .setText(mPolygonChartBeans.get(i).getTitle());
            ((TextView) v.findViewById(R.id.polygon_title_value))
                    .setText(value2Percent(mPolygonChartBeans.get(i).getValue()));
        }

        invalidate();
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mChartView = findViewById(R.id.ethnicity_chart);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mTitleLocationPoints = mChartView.getTitleLocationPoints();

        for (int i = 0; i < 6; i++) {
            PolygonChartView.Point point = mTitleLocationPoints.get(i);
            View view = mTitleViews.get(i);

            int l = (int) (point.mX - view.getMeasuredWidth() / 2);
            int t = (int) (point.mY - view.getMeasuredHeight() / 2);
            int r = (int) (point.mX + view.getMeasuredWidth() / 2);
            int b = (int) (point.mY + view.getMeasuredHeight() / 2);

            view.layout(l, t, r, b);
        }

    }

    private String value2Percent(double value) {
        NumberFormat nt = NumberFormat.getPercentInstance();
        nt.setMinimumFractionDigits(2);
        return nt.format(value);
    }
}
