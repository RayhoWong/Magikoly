package com.xfeng.beautyfacelib.Filter.beautyFilter;

import android.content.Context;
import android.opengl.GLES30;

import com.xfeng.beautyfacelib.FirebaseFaceDataManage;
import com.xfeng.beautyfacelib.base.GLImageFilter;
import com.xfeng.beautyfacelib.bean.EyesInfo;
import com.xfeng.beautyfacelib.utils.shaderUtils.OpenGLUtils;

import java.util.List;

/**
 * 美肤滤镜
 */
public class GLBigEyesFilter extends GLImageFilter {

    private static final float RATIO_FACTOR = 0.25f;
    /**
     * 缩放系数，0：无缩放，大于0则放大
     */
    private int mScaleRatio;
    /**
     * 缩放算法的作用半径
     */
    private int mRadius;
    /**
     * 左眼控制点
     */
    private int mLeftEyeCenterPosition;
    /**
     * 右眼控制点
     */
    private int mRightEyeCenterPosition;
    /**
     * 屏幕比例
     */
    private int mAspectRatio;

    private float alpha = 0.5f * RATIO_FACTOR;

    public GLBigEyesFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context,
                "shader/beauty/big_eyes.frag"));
    }

    public GLBigEyesFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mScaleRatio = GLES30.glGetUniformLocation(mProgramHandle, "scaleRatio");
        mRadius = GLES30.glGetUniformLocation(mProgramHandle, "radius");
        mLeftEyeCenterPosition = GLES30.glGetUniformLocation(mProgramHandle, "leftEyeCenterPosition");
        mRightEyeCenterPosition = GLES30.glGetUniformLocation(mProgramHandle, "rightEyeCenterPosition");
        mAspectRatio = GLES30.glGetUniformLocation(mProgramHandle, "aspectRatio");// 所处理图像的宽高比
    }



    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        EyesInfo eyesInfoData = FirebaseFaceDataManage.getInstance().getEyesInfoData();
        if (eyesInfoData == null) return;
        setFloat(mScaleRatio, alpha);
        List<EyesInfo.Position> leftEyesPoints = eyesInfoData.getLeftEyesPoint();
        if (leftEyesPoints != null && !leftEyesPoints.isEmpty()) {
            setFloat(mRadius,
                    ((leftEyesPoints.get(8).getX() - leftEyesPoints.get(0).getX()) / mImageWidth) *
                            (1 + alpha));
        }
        setFloatVec2(mLeftEyeCenterPosition, new float[]{
                eyesInfoData.getLeftEyesPosition().getX() / mImageWidth,
                1 - eyesInfoData.getLeftEyesPosition().getY() / mImageHeight});
        setFloatVec2(mRightEyeCenterPosition, new float[]{
                eyesInfoData.getRightEyesPosition().getX() / mImageWidth,
                1 - eyesInfoData.getRightEyesPosition().getY() / mImageHeight});
        setFloat(mAspectRatio,
                (float) ((mImageWidth * 1.00) / (mImageHeight * 1.00)));
    }

    @Override
    public void release() {
        super.release();
    }


    public void setBigEyeStrength(float v) {
        alpha = v * RATIO_FACTOR;
    }
}
