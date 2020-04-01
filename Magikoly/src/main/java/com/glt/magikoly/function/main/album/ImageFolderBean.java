package com.glt.magikoly.function.main.album;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Locale;

/**
 * @desc:
 * @auther:duwei
 * @date:2019/4/4
 */
public class ImageFolderBean implements Parcelable, Comparable<ImageFolderBean> {
    private static Collator sCollator = Collator.getInstance(Locale.getDefault());
    public ArrayList<ImageBean> mImageList;
    public String mFolderName;
    public ImageFolderBean(){}

    protected ImageFolderBean(Parcel in) {
        mImageList = in.createTypedArrayList(ImageBean.CREATOR);
        mFolderName = in.readString();
    }

    public static final Creator<ImageFolderBean> CREATOR = new Creator<ImageFolderBean>() {
        @Override
        public ImageFolderBean createFromParcel(Parcel in) {
            return new ImageFolderBean(in);
        }

        @Override
        public ImageFolderBean[] newArray(int size) {
            return new ImageFolderBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mImageList);
        dest.writeString(mFolderName);
    }

    @Override
    public int compareTo(@NonNull ImageFolderBean o) {
        return sCollator.getCollationKey(mFolderName).compareTo(sCollator.getCollationKey(o.mFolderName));
    }

}
