package com.glt.magikoly.opengl.fbo;

import android.content.Context;

import com.glt.magikoly.opengl.util.OpenGLUtils;


/**
 * @author rayhahah
 * @blog http://rayhahah.com
 * @time 2019/9/24
 * @tips 这个类是Object的子类
 * @fuction
 */
public class WaterMaskFrameBuffer extends AbsFrameBuffer {

    private float verticesData[] = {
            -1f, -1f,
            -1f, 1f,
            1f, 1f,
            -1f, -1f,
            1f, 1f,
            1f, -1f
    };

    /**
     * bitmap的原点在左上角，需要对其上下颠倒坐标
     */
    float textureData[] = {
            0f, 1f,
            0f, 0f,
            1f, 0f,
            0f, 1f,
            1f, 0f,
            1f, 1f
    };
    public boolean mIsFirstDraw = true;


    public void setPosition(float widthPercent, float ratio, float marginWidthPercent) {
        float transX = (1 - widthPercent) * 2;
        float heightPercent = widthPercent / ratio;
        float transY = -((1 - heightPercent) * 2);

        float transMarginX = -2 * marginWidthPercent;
        float transMarginY = -transMarginX;

        float leftTopX = -1f + transX + transMarginX;
        float leftTopY = 1f + transY + transMarginY;
        float leftBottomX = -1f + transX + transMarginX;
        float leftBottomY = -1f + transMarginY;
        float rightTopX = 1f + transMarginX;
        float rightTopY = 1f + transY + transMarginY;
        float rightBottomX = 1f + transMarginX;
        float rightBottomY = -1f + transMarginY;

        verticesData = new float[]{
                leftBottomX, leftBottomY,
                leftTopX, leftTopY,
                rightTopX, rightTopY,
                leftBottomX, leftBottomY,
                rightTopX, rightTopY,
                rightBottomX, rightBottomY
        };
        mVertexBuffer = OpenGLUtils.createFloatBuffer(verticesData);
        mTextureBuffer = OpenGLUtils.createFloatBuffer(textureData);
        mVertexCount = verticesData.length / mCoordsPerVertex;
    }


    public WaterMaskFrameBuffer(Context context) {
        super(context,
                OpenGLUtils.getShaderFromAssets(context, "shader/fbo_water_mask.vert"),
                OpenGLUtils.getShaderFromAssets(context, "shader/fbo_water_mask.frag"));
        mIsMatrix = false;
        mClearColor = new float[]{1, 1, 1, 1f};
        mVertexBuffer = OpenGLUtils.createFloatBuffer(verticesData);
        mTextureBuffer = OpenGLUtils.createFloatBuffer(textureData);
        mVertexCount = verticesData.length / mCoordsPerVertex;
    }

    @Override
    protected void initProgramHandle(int programHandle) {
        super.initProgramHandle(programHandle);
    }

    @Override
    protected void onDrawFrame() {
        super.onDrawFrame();
    }

    public void setClearColor(float[] clearColor) {
        mClearColor = clearColor;
    }

}
