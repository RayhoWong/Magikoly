package com.glt.magikoly.ad.reward;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.glt.magikoly.ad.inner.InnerAdController;
import com.glt.magikoly.thread.FaceThreadExecutorProxy;

import org.jetbrains.annotations.Nullable;

import magikoly.magiccamera.R;


/**
 * @desc: 激励视频询问弹窗
 * @auther:duwei
 * @date:2019/1/17
 */
public class WatchVideoAskDialog extends Dialog implements View.OnClickListener {

    private Button mCancel, mConfirm;
    private IAskDialogListener mListener;
    private Context mContext;
    private TextView mTitle;
    private TextView mDesc;
    private ImageView mDialogCover;
    private View mPb;
    private ValueAnimator mPbAnimator;
    private boolean mIsConfirm = false;

    private int mModuleId;
    private String mTitleText;
    private String mDescText;
    private int mImgRes;
    private String mCancelText;

    public WatchVideoAskDialog(@NonNull Context context, int moduleId) {
        super(context, R.style.FaceDialog);
        mContext = context;
        mModuleId = moduleId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_watch_advideo);
        mCancel = findViewById(R.id.dialog_rate_cancel);
        mCancel.setOnClickListener(this);
        mConfirm = findViewById(R.id.dialog_rate_confirm);
        mConfirm.setOnClickListener(this);
        mTitle = findViewById(R.id.dialog_rate_title);
        mDesc = findViewById(R.id.dialog_rate_desc);
        mPb = findViewById(R.id.dialog_pb);
        mDialogCover = findViewById(R.id.dialog_cover);

        if (!TextUtils.isEmpty(mTitleText)) {
            mTitle.setText(mTitleText);
        }

        if (!TextUtils.isEmpty(mDescText)) {
            mDesc.setText(mDescText);
        }

        if (mImgRes > 0) {
            mDialogCover.setImageResource(mImgRes);
        }

        if (!TextUtils.isEmpty(mCancelText)) {
            mCancel.setText(mCancelText);
        }


        mIsConfirm = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_rate_cancel:
                if (mListener != null) {
                    mListener.onCancel();
                }
                break;
            case R.id.dialog_rate_confirm:
                if (mListener != null) {
                    mIsConfirm = true;
                    showAd();
                    mListener.onConfirm();
                }
                break;
            default:
                break;
        }
    }

    public void showAd() {
        showLoading();
        loadAdVideo();
    }

    private boolean mWindowHasFocus = false;
    private boolean mAdLoadUnprocessed = false;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mWindowHasFocus = hasFocus;

        if(hasFocus && mAdLoadUnprocessed){
            mAdLoadUnprocessed = false;
            loadAdVideo();
        }
    }

    private void loadAdVideo() {
        InnerAdController.AdLoadListener adListener = new InnerAdController.AdLoadListener() {

            @Override
            public void onAdLoadSuccess(@Nullable InnerAdController.AdBean adBean) {

                if (isShowing() && adBean != null) {
                    if (adBean.isInterstitialAd()) {

                        if (mWindowHasFocus) {
                            adBean.showInterstitialAd(this);
                        } else {
                            mAdLoadUnprocessed = true;
                        }

                    } else {
                        dismiss();
                        if (mListener != null) {
                            mListener.onAdFailed();
                        }
                    }
                }
            }

            @Override
            public void onAdLoadFail(int statusCode) {
                if (isShowing()) {
                    FaceThreadExecutorProxy.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isShowing()) {
                                dismiss();
                            }
                            if (mListener != null) {
                                mListener.onAdFailed();
                            }
                        }
                    });
                }
            }

            @Override
            public void onAdShowed() {
                if (isShowing()) {
                    FaceThreadExecutorProxy.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mListener != null) {
                                mListener.onVideoPlayStart();
                            }
                            if (isShowing()) {
                                dismiss();
                            }
                        }
                    });
                }
            }

            @Override
            public void onAdClicked() {
                if (mListener != null) {
                    mListener.onAdClicked();
                }
            }

            @Override
            public void onAdClosed() {
            }

            @Override
            public void onVideoPlayFinish(@Nullable InnerAdController.AdBean adBean) {
                if (mListener != null) {
                    mListener.onVideoPlayFinish();
                }
            }
        };
        InnerAdController.AdBean adBean =
                InnerAdController.Companion.getInstance().getPendingAdBean(mModuleId);
        if (adBean != null) {
            if (adBean.isInterstitialAd()) {
                adBean.showInterstitialAd(adListener);
            } else {
                dismiss();
                if (mListener != null) {
                    mListener.onAdFailed();
                }
            }
        } else {
            InnerAdController.Companion.getInstance().cancelLoad(mModuleId);
            InnerAdController.Companion.getInstance().loadAd(mContext, mModuleId, adListener);
        }
    }

    public void showLoading() {
        mCancel.setVisibility(View.INVISIBLE);
        mConfirm.setVisibility(View.INVISIBLE);
        mTitle.setVisibility(View.INVISIBLE);
        mDesc.setVisibility(View.INVISIBLE);
        mPb.setVisibility(View.VISIBLE);

        mPbAnimator = ValueAnimator.ofFloat(0f, 360f);
        mPbAnimator.setDuration(1000);
        mPbAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mPbAnimator.setInterpolator(new LinearInterpolator());
        mPbAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mPb.isAttachedToWindow()) {
                    mPb.setRotation((Float) animation.getAnimatedValue());
                    mPb.invalidate();
                }
            }
        });
        mPbAnimator.start();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mPbAnimator != null && mPb.getVisibility() == View.VISIBLE) {
            if (mPbAnimator.isRunning()) {
                mPbAnimator.cancel();
            }
        }
        if (!mIsConfirm) {
            if (mListener != null) {
                mListener.onNotClickDismiss(this);
            }
        }
    }


    public void show(String title, String desc, String cancelText, int imgRes) {
        this.mTitleText = title;
        this.mDescText = desc;
        this.mCancelText = cancelText;
        this.mImgRes = imgRes;
        super.show();
    }

    public void setListener(IAskDialogListener l) {
        mListener = l;
    }


    public static abstract class IAskDialogListener {

        public IAskDialogListener() {
        }

        public void onCancel() {

        }

        public void onConfirm() {
        }

        public void onVideoPlayFinish() {

        }

        public void onVideoPlayStart() {

        }

        public void onAdClicked() {

        }

        public void onAdFailed() {

        }

        public void onNotClickDismiss(WatchVideoAskDialog dialog) {

        }
    }
}
