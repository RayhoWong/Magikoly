package com.glt;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;

import com.glt.magikoly.function.facesdk.FaceSdkProxy;
import com.xfeng.beautyfacelib.view.GLImageView;

import magikoly.magiccamera.R;

/**
 * 包含人脸识别的测试类
 * 有个单词写错了 哎 江错就错吧 大家不要学我
 */
public class BeautyTestActivity extends AppCompatActivity {

    private GLImageView mGLImageView;
    private SeekBar m美肤强度;
    private SeekBar m磨皮调整;
    private SeekBar m大眼强度;
    private SeekBar m瘦脸强度;
    private int mWidth;
    private int mHeight;
    private Bitmap mBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shder_test);
        mGLImageView = findViewById(R.id.gl_iamgeview);
        m美肤强度 = findViewById(R.id.skin_strength);
        m磨皮调整 = findViewById(R.id.buffing);
        m大眼强度 = findViewById(R.id.light_eye);
        m瘦脸强度 = findViewById(R.id.light_tooth);

        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.demo1);
        mWidth = mBitmap.getWidth();
        mHeight = mBitmap.getHeight();
        FaceSdkProxy.detectFaceEyeInfo(mBitmap, mGLImageView, new FaceSdkProxy.OnDetectFinish() {
            @Override
            public void onFinished(boolean success) {

            }
        });

        initData();
    }

    private void initData() {
        mGLImageView.setFilter();
        mGLImageView.setBitmap(mBitmap);
        m美肤强度.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mGLImageView.setBeautyLevel(progress/100.00f);
                mGLImageView.UpdateView();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        m磨皮调整.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mGLImageView.setTenderSkinLevel(progress/100.00f);
                mGLImageView.UpdateView();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        m大眼强度.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mGLImageView.setBigEyeLevel(progress/100.00f);
                mGLImageView.UpdateView();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        m瘦脸强度.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mGLImageView.setFeatureLevel(progress/100.00f);
                mGLImageView.UpdateView();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
