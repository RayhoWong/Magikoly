package com.xfeng.beautyfacelib.Filter.beautyFilter;

import android.content.Context;
import android.opengl.GLES30;

import com.xfeng.beautyfacelib.FirebaseFaceDataManage;
import com.xfeng.beautyfacelib.base.GLImageFilter;
import com.xfeng.beautyfacelib.bean.EyesInfo;
import com.xfeng.beautyfacelib.utils.shaderUtils.OpenGLUtils;

/**
 * 美肤滤镜
 */
public class GLFeatureFilter extends GLImageFilter {

    private static final float RATIO_FACTOR = 0.5f;

    private int mRadius;
    private int mAspectRatio;
    private float alpha = 0.5f * RATIO_FACTOR;
    private int mLeftContourPoints;
    private int mRightContourPoints;
    private int mDeltaArray;
    private int mArraySize;

    public GLFeatureFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context,
                "shader/beauty/Feature.frag"));
    }

    public GLFeatureFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mRadius = GLES30.glGetUniformLocation(mProgramHandle, "radius");
        mAspectRatio = GLES30.glGetUniformLocation(mProgramHandle, "aspectRatio");// 所处理图像的宽高比
        mLeftContourPoints = GLES30.glGetUniformLocation(mProgramHandle, "leftContourPoints");
        mRightContourPoints = GLES30.glGetUniformLocation(mProgramHandle, "rightContourPoints");
        mDeltaArray = GLES30.glGetUniformLocation(mProgramHandle, "deltaArray");
        mArraySize = GLES30.glGetUniformLocation(mProgramHandle, "arraySize");
    }



    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        EyesInfo eyesInfoData = FirebaseFaceDataManage.getInstance().getEyesInfoData();
        if(eyesInfoData==null)return;

        //TODO  别忘了设置
        setFloat(mRadius,0.08f*(1+alpha));
        setFloat(mAspectRatio, (float) ((mImageWidth*1.00)/(mDisplayHeight*1.00)));
        setFloatArray(mLeftContourPoints,eyesInfoData.getLeftFace());
        setFloatArray(mRightContourPoints,eyesInfoData.getRightFace());
        setFloatArray(mDeltaArray,eyesInfoData.getDeltaArray());
        setInteger(mArraySize,7);
    }

    @Override
    public void release() {
        super.release();
    }


    public void setFeatureStrength(float v) {
        alpha = v * RATIO_FACTOR;
    }
}
