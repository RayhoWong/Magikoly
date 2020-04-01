package com.xfeng.beautyfacelib.Filter.beautyFilter;

import android.content.Context;
import android.opengl.GLES30;

import com.xfeng.beautyfacelib.Filter.baseFilter.GLImageDrawElementsFilter;
import com.xfeng.beautyfacelib.bean.BeautyParam;
import com.xfeng.beautyfacelib.bean.IBeautify;
import com.xfeng.beautyfacelib.faceAI.LandmarkEngine;
import com.xfeng.beautyfacelib.utils.shaderUtils.OpenGLUtils;
import com.xfeng.beautyfacelib.utils.shaderUtils.TextureRotationUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * 大眼
 */
@Deprecated
public class GLImageBigEyeFilter extends GLImageDrawElementsFilter implements IBeautify {

    // 顶点坐标数组最大长度，这里主要用于复用缓冲
    private static final int MaxLength = 100;
    private float[] mVertices = new float[MaxLength];




    // 坐标缓冲
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mMaskTextureBuffer;



    private float mBrightEyeStrength;   // 亮眼程度
    private float mBeautyTeethStrength; // 美牙程度

    private int mProcessType = 0; // 处理类型
    private int mScaleRatio;
    private int mRadius;
    private int mLeftEyeCenterPosition;
    private int mRightEyeCenterPosition;
    private int mAspectRatio;

    public GLImageBigEyeFilter(Context context) {
        super(context, OpenGLUtils.getShaderFromAssets(context, "shader/beauty/big_eyes.vert"),
                OpenGLUtils.getShaderFromAssets(context, "shader/beauty/big_eyes.frag"));


        mBrightEyeStrength = 0;
        mBeautyTeethStrength = 0;
    }

    public void setBigEyeStrength(float v){
        mBrightEyeStrength=v;
    }

    public void setBeautyTeethStrength(float v){
        mBeautyTeethStrength=v;
    }

    @Override
    protected void initBuffers() {
        releaseBuffers();
        mVertexBuffer = ByteBuffer.allocateDirect(MaxLength * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertexBuffer.position(0);

        mMaskTextureBuffer = ByteBuffer.allocateDirect(MaxLength * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mMaskTextureBuffer.position(0);

        mIndexBuffer = ByteBuffer.allocateDirect(MaxLength * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer();
        mIndexBuffer.position(0);
    }

    @Override
    protected void releaseBuffers() {
        super.releaseBuffers();
        if (mVertexBuffer != null) {
            mVertexBuffer.clear();
            mVertexBuffer = null;
        }
        if (mMaskTextureBuffer != null) {
            mMaskTextureBuffer.clear();
            mMaskTextureBuffer = null;
        }
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mScaleRatio = GLES30.glGetUniformLocation(mProgramHandle, "scaleRatio");
            mRadius = GLES30.glGetUniformLocation(mProgramHandle, "radius");
            mLeftEyeCenterPosition = GLES30.glGetUniformLocation(mProgramHandle, "leftEyeCenterPosition");
            mRightEyeCenterPosition = GLES30.glGetUniformLocation(mProgramHandle, "rightEyeCenterPosition");
            mAspectRatio = GLES30.glGetUniformLocation(mProgramHandle, "aspectRatio");// 所处理图像的宽高比
        }
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
    }

    @Override
    public void onDisplaySizeChanged(int width, int height) {
        super.onDisplaySizeChanged(width, height);

    }


    @Override
    public int drawFrameBuffer(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        // 先将原图图像绘制到FBO中
        updateBuffer(0, -1);
        super.drawFrameBuffer(textureId, vertexBuffer, textureBuffer);

        if (LandmarkEngine.getInstance().hasFace()) {

            // 逐个人脸进行亮眼、美牙、消除法令纹、消除卧蚕和眼袋等处理
            for (int faceIndex = 0; faceIndex < LandmarkEngine.getInstance().getFaceSize(); faceIndex++) {
                // 1、亮眼处理
                if (mBrightEyeStrength != 0.0) {
                    updateBuffer(1, 0);
                    //setInteger(mProcessTypeHandle, 1);
                    //setFloat(mBrightEyeStrengthHandle, mBrightEyeStrength);
                    super.drawFrameBuffer(textureId, mVertexBuffer, mMaskTextureBuffer);
                }

                // 2、美牙处理
                if (mBeautyTeethStrength != 0.0) {
                    updateBuffer(2, 0);
                    //setInteger(mProcessTypeHandle, 2);
                    //(mTeethStrengthHandle, mBeautyTeethStrength);
                    super.drawFrameBuffer(textureId, mVertexBuffer, mMaskTextureBuffer);
                }

            }

        }

        return mFrameBufferTextures[0];

    }

    /**
     * 更新缓冲
     * @param type 索引类型，0表示原图，1表示亮眼，2表示美牙，3表示消除法令纹，4表示消除卧蚕眼袋
     * @param faceIndex 人脸索引
     */
    private void updateBuffer(int type, int faceIndex) {
        mProcessType = type;
        switch (type) {
            case 1: // 亮眼
                // 更新眼睛顶点坐标
                LandmarkEngine.getInstance().getBrightEyeVertices(mVertices, faceIndex);
                mVertexBuffer.clear();
                mVertexBuffer.put(mVertices);
                mVertexBuffer.position(0);
                // 更新眼睛遮罩纹理坐标
                mMaskTextureBuffer.clear();
                mMaskTextureBuffer.put(mEyeMaskTextureVertices);
                mMaskTextureBuffer.position(0);
                // 更新眼睛索引
                mIndexBuffer.clear();
                mIndexBuffer.put(mEyeIndices);
                mIndexBuffer.position(0);
                mIndexLength = mEyeIndices.length;
                break;

            case 2: // 美牙
                // 更新美牙顶点坐标
                LandmarkEngine.getInstance().getBeautyTeethVertices(mVertices, faceIndex);
                mVertexBuffer.clear();
                mVertexBuffer.put(mVertices);
                mVertexBuffer.position(0);
                // 更新美牙遮罩纹理坐标
                mMaskTextureBuffer.clear();
                mMaskTextureBuffer.put(mTeethMaskTextureVertices);
                mMaskTextureBuffer.position(0);
                // 更新美牙索引
                mIndexBuffer.clear();
                mIndexBuffer.put(mTeethIndices);
                mIndexBuffer.position(0);
                mIndexLength = mTeethIndices.length;
                break;

            case 0: // 原图
            default:    // 其他类型也是原图
                mIndexBuffer.clear();
                mIndexBuffer.put(TextureRotationUtils.Indices);
                mIndexBuffer.position(0);
                mIndexLength = 6;
                break;
        }
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
    }


    @Override
    public void initFrameBuffer(int width, int height) {
        super.initFrameBuffer(width, height);

    }

    @Override
    public void destroyFrameBuffer() {
        super.destroyFrameBuffer();

    }

    @Override
    public void release() {
        super.release();

    }

    @Override
    public void onBeauty(BeautyParam beauty) {
        if (beauty != null) {
            mBrightEyeStrength = clamp(beauty.eyeBrightIntensity, 0.0f, 1.0f);
            mBeautyTeethStrength = clamp(beauty.teethBeautyIntensity, 0.0f, 1.0f);
        }
    }


    /**
     * 眼睛部分索引
     */
    private static final short[] mEyeIndices = new short[] {
            0, 5, 1,
            1, 5, 12,
            12, 5, 13,
            12, 13, 4,
            12, 4, 2,
            2, 4, 3,

            6, 7, 11,
            7, 11, 14,
            14, 11, 15,
            14, 15, 10,
            14, 10, 8,
            8, 10, 9
    };

    /**
     * 眼睛遮罩纹理坐标
     */
    private static final float[] mEyeMaskTextureVertices = new float[] {
            0.102757f, 0.465517f,
            0.175439f, 0.301724f,
            0.370927f, 0.310345f,
            0.446115f, 0.603448f,
            0.353383f, 0.732759f,
            0.197995f, 0.689655f,

            0.566416f, 0.629310f,
            0.659148f, 0.336207f,
            0.802005f, 0.318966f,
            0.884712f, 0.465517f,
            0.812030f, 0.681034f,
            0.681704f, 0.750023f,

            0.273183f, 0.241379f,
            0.275689f, 0.758620f,

            0.721805f, 0.275862f,
            0.739348f, 0.758621f,
    };

    /**
     * 美牙索引
     */
    private static final short[] mTeethIndices = new short[] {
            0, 11, 1,
            1, 11, 10,
            1, 10, 2,
            2, 10, 3,
            3, 10, 9,
            3, 9, 8,
            3, 8, 4,
            4, 8, 5,
            5, 8, 7,
            5, 7, 6,
    };

    /**
     * 美牙遮罩纹理坐标
     */
    private static final float[] mTeethMaskTextureVertices = new float[] {
            0.154639f, 0.378788f,
            0.295533f, 0.287879f,
            0.398625f, 0.196970f,
            0.512027f, 0.287879f,
            0.611684f, 0.212121f,
            0.728523f, 0.287879f,
            0.872852f, 0.378788f,
            0.742268f, 0.704546f,
            0.639176f, 0.848485f,
            0.522337f, 0.636364f,
            0.398625f, 0.833333f,
            0.240550f, 0.651515f,
    };
}
