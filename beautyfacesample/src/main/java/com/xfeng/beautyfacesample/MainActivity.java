package com.xfeng.beautyfacesample;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;

import com.xfeng.beautyfacelib.view.GLImageView;

public class MainActivity extends AppCompatActivity {

    private GLImageView mGLImageView;
    private SeekBar m美肤强度;
    private SeekBar m磨皮调整;
    private SeekBar m大眼强度;
    private SeekBar m瘦脸强度;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGLImageView = findViewById(R.id.gl_iamgeview);
        m美肤强度 = findViewById(R.id.skin_strength);
        m磨皮调整 = findViewById(R.id.buffing);
        m大眼强度 = findViewById(R.id.light_eye);
        m瘦脸强度 = findViewById(R.id.light_tooth);
        initData();
    }

    private void initData() {
        mGLImageView.setFilter();
        mGLImageView.setBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.wuye));
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
                //mGLImageView.设置美牙强度(progress/100.00f);
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
