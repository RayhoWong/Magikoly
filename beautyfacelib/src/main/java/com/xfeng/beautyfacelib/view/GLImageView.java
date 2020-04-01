package com.xfeng.beautyfacelib.view;

/**
 * Created by xfengimacgomo
 * data 2019-05-21 17:41
 * email xfengv@yeah.net
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.xfeng.beautyfacelib.Filter.baseFilter.GLImageInputFilter;
import com.xfeng.beautyfacelib.Filter.beautyFilter.GLImageBeautyFilter;
import com.xfeng.beautyfacelib.base.GLImageFilter;
import com.xfeng.beautyfacelib.utils.shaderUtils.ConfigChooser;
import com.xfeng.beautyfacelib.utils.shaderUtils.OpenGLUtils;
import com.xfeng.beautyfacelib.utils.shaderUtils.TextureRotationUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 图片GL渲染视图
 * 注意:不建议相机渲染(视图缩放,效率不高,适合图片编辑)
 */
public class GLImageView extends GLSurfaceView implements GLSurfaceView.Renderer {

    // 输入纹理
    protected int mInputTexture = OpenGLUtils.GL_NOT_TEXTURE;
    // 图片输入滤镜
    protected GLImageInputFilter mInputFilter;
    // 美颜滤镜
    protected GLImageBeautyFilter mBeautyFilter;
    // 显示输出
    protected GLImageFilter mDisplayFilter;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;

    // 输入纹理大小
    protected int mTextureWidth;
    protected int mTextureHeight;
    // 控件视图大小
    protected int mViewWidth;
    protected int mViewHeight;

    // 输入图片
    private Bitmap mBitmap;

    // UI线程Handler，主要用于更新UI等
    protected Handler mMainHandler;

    boolean takePicture;

    CaptureCallback mCaptureCallback;

    private final Queue<Runnable> mRunOnDrawEnd;

    public GLImageView(Context context) {
        this(context, null);
    }

    public GLImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setZOrderMediaOverlay(true);
        setEGLContextClientVersion(2);
        setEGLConfigChooser(new ConfigChooser());
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mRunOnDrawEnd = new LinkedList<>();
        mMainHandler = new Handler(Looper.getMainLooper());
        mVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
    }

    @Override
    protected void finalize() throws Throwable {
        if (mCaptureCallback != null) {
            mCaptureCallback = null;
        }
        super.finalize();
    }

    @Override
    public void onPause() {
        super.onPause();
        mInputTexture = OpenGLUtils.GL_NOT_TEXTURE;
        mBeautyFilter = null;
        mDisplayFilter = null;
        mInputFilter = null;
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES30.glDisable(GL10.GL_DITHER);
        GLES30.glClearColor(0, 0, 0, 0);
        GLES30.glEnable(GL10.GL_CULL_FACE);
        GLES30.glEnable(GL10.GL_DEPTH_TEST);
        initFilters();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mViewWidth = width;
        mViewHeight = height;
        GLES30.glViewport(0, 0, width, height);
        if (mInputTexture == OpenGLUtils.GL_NOT_TEXTURE) {
            mInputTexture = OpenGLUtils.createTexture(mBitmap, mInputTexture);
        }
        // Note: 如果此时显示输出滤镜对象为空，则表示调用了onPause方法销毁了所有GL对象资源，需要重新初始化滤镜
        if (mDisplayFilter == null) {
            initFilters();
        }
        onFilterSizeChanged();
    }

    /**
     * 初始化滤镜
     */
    private void initFilters() {
        if (mBitmap != null) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    calculateViewSize();
                }
            });
        }


        if (mInputFilter == null) {
            mInputFilter = new GLImageInputFilter(getContext());
        } else {
            mInputFilter.initProgramHandle();
        }
        if (mBeautyFilter == null) {
            createColorFilter();
        } else if (mBeautyFilter != null) {
            mBeautyFilter.initProgramHandle();
        }
        if (mDisplayFilter == null) {
            mDisplayFilter = new GLImageFilter(getContext());
        } else {
            mDisplayFilter.initProgramHandle();
        }


    }

    /**
     * 滤镜大小发生变化
     */
    private void onFilterSizeChanged() {
        if (mInputFilter != null) {
            mInputFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            mInputFilter.initFrameBuffer(mTextureWidth, mTextureHeight);
            mInputFilter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        }
        if (mBeautyFilter != null) {
            mBeautyFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            mBeautyFilter.initFrameBuffer(mTextureWidth, mTextureHeight);
            mBeautyFilter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        }
        if (mDisplayFilter != null) {
            mDisplayFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            mDisplayFilter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClearColor(0, 0, 0, 0);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        if (mDisplayFilter == null) {
            return;
        }
        int currentTexture = mInputTexture;
        if (mInputFilter != null) {
            currentTexture = mInputFilter.drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer);
        }
        if (mBeautyFilter != null) {
            currentTexture = mBeautyFilter.drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer);
        }
        mDisplayFilter.drawFrame(currentTexture, mVertexBuffer, mTextureBuffer);
        if (takePicture) {
            int width = getWidth();
            int height = getHeight();
            ByteBuffer buf = ByteBuffer.allocateDirect(width * height * 4);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            GLES30.glReadPixels(0, 0, width, height,
                    GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, buf);
            OpenGLUtils.checkGlError("glReadPixels");
            buf.rewind();
            takePicture = false;
            if (mCaptureCallback != null) {
                mCaptureCallback.onCapture(buf, width, height);
            }
        }
        runEnd();
    }

    /**
     * 设置滤镜
     */
    public void setFilter() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mBeautyFilter != null) {
                    mBeautyFilter.release();
                    mBeautyFilter = null;
                }
                createColorFilter();
                onFilterSizeChanged();
                requestRender();
            }
        });
    }

    public void UpdateView() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                requestRender();
            }
        });

    }

    public void setBeautyLevel(float level) {
        //mBeautyFilter为空可能是界面已经销毁,已经在其他地方更改,尽量避免
        if (mBeautyFilter != null) {
            mBeautyFilter.setBeattyLevel(level);
        }
    }

    public void setTenderSkinLevel(float level) {
        if (mBeautyFilter != null) {
            mBeautyFilter.setTenderSkinLevel(level);
        }
    }

    public void setBigEyeLevel(float level) {
        if (mBeautyFilter != null) {
            mBeautyFilter.setBigEyeLevel(level);
        }
    }

    public void setFeatureLevel(float level) {
        if (mBeautyFilter != null) {
            mBeautyFilter.setFeatureLevel(level);
        }
    }



    /**
     * 截屏回调
     *
     * @param captureCallback
     */
    public void setCaptureCallback(CaptureCallback captureCallback) {
        this.mCaptureCallback = captureCallback;
    }

    /**
     * 拍照
     */
    public synchronized void getCaptureFrame() {
        if (takePicture) {
            Toast.makeText(getContext(), "正在保存图片", Toast.LENGTH_SHORT).show();
            return;
        }
        takePicture = true;
        requestRender();
    }

    /**
     * 创建颜色滤镜
     */
    private void createColorFilter() {
        try {
            mBeautyFilter = new GLImageBeautyFilter(getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置滤镜
     *
     * @param bitmap
     */
    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        mTextureWidth = mBitmap.getWidth();
        mTextureHeight = mBitmap.getHeight();
        requestRender();
    }

    /**
     * 计算视图大小
     */
    private void calculateViewSize() {
        if (mTextureWidth == 0 || mTextureHeight == 0) {
            return;
        }
        if (mViewWidth == 0 || mViewHeight == 0) {
            mViewWidth = getWidth();
            mViewHeight = getHeight();
        }
        float ratio = mTextureWidth * 1.0f / mTextureHeight;
        double viewAspectRatio = (double) mViewWidth / mViewHeight;
        if (ratio < viewAspectRatio) {
            mViewWidth = (int) (mViewHeight * ratio);
        } else {
            mViewHeight = (int) (mViewWidth / ratio);
        }
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.width = mViewWidth;
        layoutParams.height = mViewHeight;
        setLayoutParams(layoutParams);
    }

    /**
     * 截帧回调
     */
    public interface CaptureCallback {
        void onCapture(ByteBuffer buffer, int width, int height);
    }

    public void runOnGLThread(Runnable runnable) {
        synchronized (mRunOnDrawEnd) {
            mRunOnDrawEnd.add(runnable);
        }
    }

    private void runEnd() {
        synchronized (mRunOnDrawEnd) {
            while (!mRunOnDrawEnd.isEmpty()) {
                mRunOnDrawEnd.poll().run();
            }
        }
    }
}