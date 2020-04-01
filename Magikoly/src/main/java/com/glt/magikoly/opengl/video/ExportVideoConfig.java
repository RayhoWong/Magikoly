package com.glt.magikoly.opengl.video;

import android.content.Context;

/**
 * @author rayhahah
 * @blog http://rayhahah.com
 * @time 2019/9/27
 * @tips 这个类是Object的子类
 * @fuction
 */
public class ExportVideoConfig {

    private float duration;
    private Context context;
    private String outputPath;

    private int outputWidth;
    private int outputHeight;
    private int fps;

    private onExportVideoListener listener;

    private ExportVideoConfig(Builder builder) {
        duration = builder.duration;
        context = builder.context;
        outputPath = builder.outputPath;
        outputWidth = builder.outputWidth;
        outputHeight = builder.outputHeight;
        fps = builder.fps;
        listener = builder.listener;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public interface onExportVideoListener {
        /**
         * 添加到屏幕前
         *
         * @param width
         * @param height
         */
        void onMakeCurrent(int width, int height);

        /**
         * 绘制每一帧，处于子线程
         *
         * @param frame    当前帧数
         * @param progress
         */
        void onDrawFrame(int frame, float progress);

        /**
         * 初始化
         */
        void onCreate();

        /**
         * 释放资源
         */
        void release();

        /**
         * 报错
         */
        void onError(Throwable throwable);
    }

    public float getDuration() {
        return duration;
    }

    public Context getContext() {
        return context;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public int getOutputWidth() {
        return outputWidth;
    }

    public int getOutputHeight() {
        return outputHeight;
    }

    public int getFps() {
        return fps;
    }

    public onExportVideoListener getListener() {
        return listener;
    }


    public static final class Builder {
        private float duration;
        private Context context;
        private String outputPath;
        private int outputWidth;
        private int outputHeight;
        private int fps;
        private onExportVideoListener listener;

        private Builder() {
        }

        /**
         * Second
         *
         * @param duration Second
         */
        public Builder setDuration(float duration) {
            this.duration = duration;
            return this;
        }

        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        public Builder setOutputPath(String outputPath) {
            this.outputPath = outputPath;
            return this;
        }

        public Builder setOutputWidth(int outputWidth) {
            this.outputWidth = outputWidth;
            return this;
        }

        public Builder setOutputHeight(int outputHeight) {
            this.outputHeight = outputHeight;
            return this;
        }

        public Builder setFps(int fps) {
            this.fps = fps;
            return this;
        }

        public Builder setListener(onExportVideoListener listener) {
            this.listener = listener;
            return this;
        }

        public ExportVideoConfig build() {
            return new ExportVideoConfig(this);
        }
    }
}
