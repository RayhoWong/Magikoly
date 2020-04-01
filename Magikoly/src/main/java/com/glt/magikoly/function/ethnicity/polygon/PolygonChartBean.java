package com.glt.magikoly.function.ethnicity.polygon;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

/**
 * @desc: 种族分析图中的单个数据bean
 * @auther:duwei
 * @date:2019/1/14
 */
public class PolygonChartBean implements Parcelable {

    @DrawableRes
    private int mIcon;
    @StringRes
    private int mTitle;

    private double mValue;

    public PolygonChartBean(@DrawableRes int icon,
                            @StringRes int title,
                            double pValue) {
        mIcon = icon;
        mTitle = title;
        mValue = pValue;
    }

    public PolygonChartBean(Parcel source) {
        mIcon = source.readInt();
        mTitle = source.readInt();
        mValue = source.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mIcon);
        dest.writeInt(mTitle);
        dest.writeDouble(mValue);
    }

    @DrawableRes
    public int getIcon() {
        return mIcon;
    }

    public void setIcon(@DrawableRes int pIcon) {
        mIcon = pIcon;
    }

    @StringRes
    public int getTitle() {
        return mTitle;
    }

    public void setTitle(@StringRes int pTitle) {
        mTitle = pTitle;
    }

    public double getValue() {
        return mValue;
    }

    public void setValue(double pValue) {
        mValue = pValue;
    }

    public static final Creator<PolygonChartBean> CREATOR = new Creator<PolygonChartBean>() {
        @Override
        public PolygonChartBean createFromParcel(Parcel in) {
            return new PolygonChartBean(in);
        }

        @Override
        public PolygonChartBean[] newArray(int size) {
            return new PolygonChartBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

}
