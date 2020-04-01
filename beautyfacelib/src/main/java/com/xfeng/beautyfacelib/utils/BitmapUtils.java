package com.xfeng.beautyfacelib.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;

import java.io.ByteArrayOutputStream;

/**
 * Created by xfengimacgomo
 * data 2019-05-30 19:18
 * email xfengv@yeah.net
 */
public class BitmapUtils {
    public static Bitmap getBitmapFromFrameData(RenderScript rs, ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic, byte[] data, int width, int height) {
        Type.Builder yuvType = null, rgbaType;
        Allocation in = null, out = null;
        try {

            final int w = width;  //宽度
            final int h = height;
            Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (yuvType == null) {
                    yuvType = new Type.Builder(rs, Element.U8(rs)).setX(data.length);
                    in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);
                    rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(w).setY(h);
                    out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
                }

                in.copyFrom(data);
                yuvToRgbIntrinsic.setInput(in);
                yuvToRgbIntrinsic.forEach(out);
                bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                out.copyTo(bitmap);

            } else {
                ByteArrayOutputStream baos;
                byte[] rawImage;
                //处理data
                BitmapFactory.Options newOpts = new BitmapFactory.Options();
                newOpts.inJustDecodeBounds = true;
                YuvImage yuvimage = new YuvImage(
                        data,
                        ImageFormat.NV21,
                        w,
                        h,
                        null);
                baos = new ByteArrayOutputStream();
                yuvimage.compressToJpeg(new Rect(0, 0, w, h), 100, baos);// 80--JPG图片的质量[0-100],100最高
                rawImage = baos.toByteArray();
                //将rawImage转换成bitmap
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                bitmap = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length, options);
            }
            in = null;
            out = null;
            return bitmap;
        } catch (Throwable e) {
            in = null;
            out = null;
            return null;
        }
    }
}
