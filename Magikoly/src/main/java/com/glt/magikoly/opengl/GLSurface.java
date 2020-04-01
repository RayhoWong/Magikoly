package com.glt.magikoly.opengl;

import android.view.Surface;

import org.jetbrains.annotations.NotNull;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLSurface;

public class GLSurface {
    public static final int TYPE_WINDOW_SURFACE = 0;
    public static final int TYPE_PBUFFER_SURFACE = 1;
    public static final int TYPE_PIXMAP_SURFACE = 2;

    public final int type;
    public EGLSurface eglSurface = EGL10.EGL_NO_SURFACE;

    /**
     * 显示控件(支持SurfaceView、SurfaceHolder、Surface和SurfaceTexture)
     */
    public Object surface;
    public Viewport viewport = new Viewport();

    public GLSurface(int width, int height) {
        setViewport(0, 0, width, height);
        surface = null;
        type = TYPE_PBUFFER_SURFACE;
    }

    public GLSurface(Surface surface, int width, int height) {
        this(surface, 0, 0, width, height);
    }

    public GLSurface(Surface surface, int x, int y, int width, int height) {
        setViewport(x, y, width, height);
        this.surface = surface;
        type = TYPE_WINDOW_SURFACE;
    }

    public void setViewport(int x, int y, int width, int height) {
        viewport.x = x;
        viewport.y = y;
        viewport.width = width;
        viewport.height = height;
    }

    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public void set(@NotNull Surface surface, int width, int height) {

    }

    public static class Viewport {
        public int x;
        public int y;
        public int width;
        public int height;
    }
}