package com.xfeng.beautyfacelib.utils.commonUtils;

import android.opengl.EGL14;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Surface;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * Create by Billin on 2019/7/26
 */
public class EGLEnvironment {

    private static final String TAG = "EGLEnvironment";

    private EGL10 egl;

    private EGLDisplay eglDisplay;

    private EGLConfig eglConfig;

    private EGLContext eglContext;

    private EGLSurface eglSurface;

    private int[] tmpInt = new int[1];

    public void initialize() {
        egl = (EGL10) EGLContext.getEGL();

        /*
         * Get to the default display.
         */
        eglDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        if (eglDisplay == EGL10.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed");
        }

        /*
         * We can now initialize EGL for that display
         */
        int[] version = new int[2];
        if (!egl.eglInitialize(eglDisplay, version)) {
            throw new RuntimeException("eglInitialize failed");
        }

        eglConfig = chooseConfig(egl, eglDisplay);
        eglContext = createContext(egl, eglDisplay, eglConfig);

        if (eglContext == null || eglContext == EGL10.EGL_NO_CONTEXT) {
            eglContext = null;
            throw new RuntimeException("createContext failed");
        }

        eglSurface = null;
    }

    private EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig config) {
        int[] attrList = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            attrList = new int[]{
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL10.EGL_NONE
            };
        }

        return egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, attrList);
    }

    private void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
        if (!egl.eglDestroyContext(display, context)) {
            throw new RuntimeException("destroyContext failed");
        }
    }

    public void bindSurface(int width, int height, @Nullable Surface windowSurface) {
        /*
         * Check preconditions.
         */
        if (egl == null) {
            throw new RuntimeException("egl not initialized");
        }
        if (eglDisplay == null) {
            throw new RuntimeException("eglDisplay not initialized");
        }
        if (eglConfig == null) {
            throw new RuntimeException("eglConfig not initialized");
        }

        /*
         *  The window size has changed, so we need to create a new
         *  surface.
         */
        destroySurface();

        try {
            // if window surface is null, we create PbufferSurface.
            if (windowSurface != null) {
                int[] surfaceAttr = {
                        EGL10.EGL_NONE
                };

                eglSurface = egl.eglCreateWindowSurface(eglDisplay, eglConfig, windowSurface, surfaceAttr);
            } else {
                int[] surfaceAttr = {
                        EGL10.EGL_WIDTH, width,
                        EGL10.EGL_HEIGHT, height,
                        EGL10.EGL_NONE
                };

                eglSurface = egl.eglCreatePbufferSurface(eglDisplay, eglConfig, surfaceAttr);
            }

            if (eglSurface == EGL10.EGL_NO_SURFACE) {
                throw new RuntimeException("eglSurface create failed");
            }
        } catch (IllegalArgumentException e) {
            // This exception indicates that the surface flinger surface
            // is not valid. This can happen if the surface flinger surface has
            // been torn down, but the application has not yet been
            // notified via SurfaceHolder.Callback.surfaceDestroyed.
            // In theory the application should be notified first,
            // but in practice sometimes it is not. See b/4588890
            Log.e(TAG, "eglCreateWindowSurface", e);
        }

        /*
         * Before we can issue GL commands, we need to make sure
         * the context is current and bound to a surface. It also bound
         * current thread with context.
         */
        if (!egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            /*
             * Could not make the context current, probably because the underlying
             * SurfaceView surface has been destroyed.
             */
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }

    private void destroySurface() {
        if (eglSurface != null && eglSurface != EGL10.EGL_NO_SURFACE) {
            egl.eglMakeCurrent(eglDisplay,
                    EGL10.EGL_NO_SURFACE,
                    EGL10.EGL_NO_SURFACE,
                    EGL10.EGL_NO_CONTEXT);

            egl.eglDestroySurface(eglDisplay, eglSurface);
            eglSurface = null;
        }
    }

    private EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
        int redSize = 8;
        int greenSize = 8;
        int blueSize = 8;
        int alphaSize = 8;
        int depthSize = 16;
        int stencilSize = 0;

        int[] numConfigs = new int[1];
        int[] configSpec = {
                EGL10.EGL_RED_SIZE, redSize,
                EGL10.EGL_GREEN_SIZE, greenSize,
                EGL10.EGL_BLUE_SIZE, blueSize,
                EGL10.EGL_ALPHA_SIZE, alphaSize,
                EGL10.EGL_DEPTH_SIZE, depthSize,
                EGL10.EGL_STENCIL_SIZE, stencilSize,
                EGL10.EGL_NONE
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            int len = configSpec.length;
            int[] newConfigAttributes = new int[len + 2];
            System.arraycopy(configSpec, 0, newConfigAttributes, 0, len);
            newConfigAttributes[len - 1] = EGL10.EGL_RENDERABLE_TYPE;
            newConfigAttributes[len] = EGL14.EGL_OPENGL_ES2_BIT;
            newConfigAttributes[len + 1] = EGL10.EGL_NONE;
            configSpec = newConfigAttributes;
        }

        // 获取该设备 OpenGL 支持的配置并检测返回值是否成功
        if (!egl.eglChooseConfig(display, configSpec, null, 0, numConfigs)) {
            throw new RuntimeException("eglChooseConfig failed");
        }
        // 该设备不支持设定 spec 配置
        if (numConfigs[0] < 0) {
            throw new RuntimeException("Unable to find any matching EGL config");
        }

        // 获取支持的配置列表并选择其中一个配置
        EGLConfig[] configs = new EGLConfig[numConfigs[0]];
        if (!egl.eglChooseConfig(display, configSpec, configs, numConfigs[0], numConfigs)) {
            throw new IllegalArgumentException("eglChooseConfig#2 failed");
        }

        for (EGLConfig config : configs) {
            int d = findConfigAttr(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0);
            int s = findConfigAttr(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0);
            if ((d >= depthSize) && (s >= stencilSize)) {
                int r = findConfigAttr(egl, display, config, EGL10.EGL_RED_SIZE, 0);
                int g = findConfigAttr(egl, display, config, EGL10.EGL_GREEN_SIZE, 0);
                int b = findConfigAttr(egl, display, config, EGL10.EGL_BLUE_SIZE, 0);
                int a = findConfigAttr(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0);
                if ((r == redSize) && (g == greenSize) && (b == blueSize) && (a == alphaSize)) {
                    return config;
                }
            }
        }

        return null;
    }

    private int findConfigAttr(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {
        if (egl.eglGetConfigAttrib(display, config, attribute, tmpInt)) {
            return tmpInt[0];
        }
        return defaultValue;
    }

    public int swap() {
        if (!egl.eglSwapBuffers(eglDisplay, eglSurface)) {
            return egl.eglGetError();
        }
        return EGL10.EGL_SUCCESS;
    }

    public void release() {
        destroySurface();
        if (eglContext != null) {
            destroyContext(egl, eglDisplay, eglContext);
            eglContext = null;
        }

        if (eglDisplay != null) {
            egl.eglTerminate(eglDisplay);
            eglDisplay = null;
        }

        egl = null;
    }
}
