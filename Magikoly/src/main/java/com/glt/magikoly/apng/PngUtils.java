package com.glt.magikoly.apng;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import com.glt.magikoly.utils.BitmapUtils;
import com.glt.magikoly.utils.WindowController;

import net.ellerton.japng.argb8888.Argb8888Bitmap;
import net.ellerton.japng.argb8888.Argb8888Processor;
import net.ellerton.japng.error.PngException;
import net.ellerton.japng.reader.DefaultPngChunkReader;
import net.ellerton.japng.reader.PngReadHelper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Convenience functions to load PNGs for Android.
 */
public class PngUtils {

    public static Bitmap toBitmap(Argb8888Bitmap src) {
        int offset = 0;
        int stride = src.width;
        Bitmap srcBitmap =
                Bitmap.createBitmap(src.getPixelArray(), offset, stride, src.width, src.height,
                        Bitmap.Config.ARGB_8888);
        Bitmap dstBitmap = BitmapUtils
                .createScaledBitmap(srcBitmap, getScaledSize(srcBitmap.getWidth()),
                        getScaledSize(srcBitmap.getHeight()));
        if (srcBitmap != dstBitmap) {
            srcBitmap.recycle();
        }
        return dstBitmap;
    }

    public static Drawable readDrawable(Context context, InputStream is) throws PngException {
        Argb8888Processor<Drawable> processor =
                new Argb8888Processor<Drawable>(new PngViewBuilder(context));
        return PngReadHelper.read(is, new DefaultPngChunkReader<Drawable>(processor));
    }

    public static Drawable readDrawable(Context context, int id) throws PngException {
        final TypedValue value = new TypedValue();
        InputStream is = null;
        try {
            return readDrawable(context, context.getResources().openRawResource(id, value));
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private static float sScale = 0;

    public static int getScaledSize(int size) {
        if (sScale == 0) {
            int smallerBound = WindowController.getSmallerBound();
            int sampleBound = 1080;
            sScale = Math.min(1.0f * smallerBound / sampleBound, 1);
        }
        int scaledSize = (int) (size * sScale);
        return scaledSize;
    }
}
