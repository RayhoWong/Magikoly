package com.glt.libyuv;

public class YuvUtils {
    static {
        try {
            System.loadLibrary("yuv");
        } catch (Throwable t) {

        }
    }
    public native static void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height);
}
