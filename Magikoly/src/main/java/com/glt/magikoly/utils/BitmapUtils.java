package com.glt.magikoly.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.IntRange;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatDrawableManager;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;

import com.glt.libyuv.YuvUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import static android.graphics.Bitmap.Config.ARGB_4444;
import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.Config.RGB_565;

/**
 * 类描述:bitmap处理工具类 功能详细描述:
 *
 * @author huyong
 * @date [2012-8-25]
 */
public class BitmapUtils {
    private static final String TAG = "BitmapUtility";

    /**
     * 功能简述:创建一张当前的view的bitmap截图 功能详细描述:根据指定的缩放比例，对当前view进行截图，并返回截图bitmap 注意:
     *
     * @param view  ：待画的view
     * @param scale ：缩放比例
     * @return：view的截图，若当前view为null或宽高<=0，则返回null。
     */
    public static final Bitmap createBitmap(View view, float scale) {
        Bitmap pRet = null;
        if (null == view) {
            Logcat.i(TAG, "create bitmap function param view is null");
            return pRet;
        }

        int scaleWidth = (int) (view.getWidth() * scale);
        int scaleHeight = (int) (view.getHeight() * scale);
        if (scaleWidth <= 0 || scaleHeight <= 0) {
            Logcat.i(TAG, "create bitmap function param view is not layout");
            return pRet;
        }

        boolean bViewDrawingCacheEnable = view.isDrawingCacheEnabled();
        if (!bViewDrawingCacheEnable) {
            view.setDrawingCacheEnabled(true);
        }
        try {
            Bitmap viewBmp = view.getDrawingCache(true);
            // 如果拿到的缓存为空
            if (viewBmp == null) {
                pRet = Bitmap.createBitmap(scaleWidth, scaleHeight,
                        view.isOpaque() ? Config.RGB_565 : Config.ARGB_8888);
                Canvas canvas = new Canvas(pRet);
                canvas.scale(scale, scale);
                view.draw(canvas);
                canvas = null;
            } else {
                pRet = Bitmap.createScaledBitmap(viewBmp, scaleWidth,
                        scaleHeight, true);
            }
            viewBmp = null;
        } catch (OutOfMemoryError e) {
            pRet = null;
            Logcat.i(TAG, "create bitmap out of memory");
        } catch (Exception e) {
            pRet = null;
            Logcat.i(TAG, "create bitmap exception");
        }
        if (!bViewDrawingCacheEnable) {
            view.setDrawingCacheEnabled(false);
        }

        return pRet;
    }

    /**
     * 功能简述:创建一张已有bmp居中显示的指定宽高的新Bitmap
     * 功能详细描述:需要传入已有bmp、新创建Bitmap的宽、高，三个条件，从而创建一张新的Bitmap
     * ，使得传入的bmp位于新Bitmap的居中显示。 注意:新创建的Bitmap的宽高，因不小于原有bmp的宽高。
     *
     * @param bmp       ：已有将要拿来居中显示的位图。
     * @param desWidth  ：新创建位图的宽度
     * @param desHeight ：新创建位图的高度
     * @return
     */
    public static final Bitmap createBitmap(Bitmap bmp, int desWidth,
                                            int desHeight) {
        Bitmap pRet = null;
        if (null == bmp) {
            Logcat.i(TAG, "create bitmap function param bmp is null");
            return pRet;
        }

        try {
            BitmapDrawable d = new BitmapDrawable(bmp);
            pRet = Bitmap.createBitmap(desWidth, desHeight, d.getOpacity() != PixelFormat.OPAQUE ?
                    ARGB_8888 : RGB_565);
            Canvas canvas = new Canvas(pRet);
            int left = (desWidth - bmp.getWidth()) / 2;
            int top = (desHeight - bmp.getHeight()) / 2;
            canvas.drawBitmap(bmp, left, top, null);
        } catch (OutOfMemoryError e) {
            pRet = null;
            Logcat.i(TAG, "create bitmap out of memory");
        } catch (Exception e) {
            pRet = null;
            Logcat.i(TAG, "create bitmap exception");
        }

        return pRet;
    }

    /**
     * 功能简述:创建一张已有bmp根据left,top值显示的指定宽高的新Bitmap
     *
     * @param bmp
     * @param desWidth
     * @param desHeight
     * @param left
     * @param top
     * @return
     */
    public static final Bitmap createBitmap(Bitmap bmp, int desWidth,
                                            int desHeight, int left, int top) {
        Bitmap pRet = null;
        if (null == bmp) {
            Logcat.i(TAG, "create bitmap function param bmp is null");
            return pRet;
        }

        try {
            BitmapDrawable d = new BitmapDrawable(bmp);
            pRet = Bitmap.createBitmap(desWidth, desHeight,
                    d.getOpacity() != PixelFormat.OPAQUE ? ARGB_8888 : RGB_565);
            Canvas canvas = new Canvas(pRet);
            canvas.drawBitmap(bmp, left, top, null);
        } catch (OutOfMemoryError e) {
            pRet = null;
            Logcat.i(TAG, "create bitmap out of memory");
        } catch (Exception e) {
            pRet = null;
            Logcat.i(TAG, "create bitmap exception");
        }

        return pRet;
    }

    /**
     * 功能简述:创建缩放处理后的新图，若缩放后大小与原图大小相同，则直接返回原图。 功能详细描述:
     * 注意:若缩放目标尺寸与原图尺寸相等，则直接返回原图，不再创建新的bitmap
     *
     * @param bmp         ：待处理bmp
     * @param scaleWidth  ：缩放目标宽
     * @param scaleHeight ：缩放目标高
     * @return
     */
    public static final Bitmap createScaledBitmap(Bitmap bmp, int scaleWidth,
                                                  int scaleHeight) {
        Bitmap pRet = null;
        if (null == bmp) {
            Logcat.i(TAG, "create scale bitmap function param bmp is null");
            return pRet;
        }
        // 这里有待改进，这里直接返回原图，有可能原图会在后面recycle，导致创建出来的都会被recycle
        if (scaleWidth == bmp.getWidth() && scaleHeight == bmp.getHeight()) {
            return bmp;
        }

        try {
            pRet = Bitmap
                    .createScaledBitmap(bmp, scaleWidth, scaleHeight, true);
        } catch (OutOfMemoryError e) {
            pRet = null;
            Logcat.i(TAG, "create scale bitmap out of memory");
        } catch (Exception e) {
            pRet = null;
            e.printStackTrace();
        }

        return pRet;
    }

    /**
     * 功能简述:将位图保存为指定文件名的文件。 功能详细描述: 注意:若已存在同名文件，则首先删除原有文件，若删除失败，则直接退出，保存失败
     *
     * @param bmp      ：待保存位图
     * @param filePath ：保存位图内容的目标文件路径
     * @return true for 保存成功，false for 保存失败。
     */
    public static final boolean saveBitmap(Bitmap bmp, String filePath) {
        return saveBitmap(bmp, filePath, Bitmap.CompressFormat.PNG);
    }

    public static final boolean saveBitmap(Bitmap bmp, String filePath,
                                           CompressFormat format) {
        return saveBitmap(bmp, filePath, 100, format);
    }

    public static final boolean saveBitmap(Bitmap bmp, String filePath,
                                           int quality, CompressFormat format) {
        if (null == bmp) {
            Logcat.i(TAG, "save bitmap to file bmp is null");
            return false;
        }
        FileOutputStream stream = null;
        try {
            File file = new File(filePath);
            if (file.exists()) {
                boolean bDel = file.delete();
                if (!bDel) {
                    Logcat.i(TAG, "delete src file fail");
                    return false;
                }
            } else {
                File parent = file.getParentFile();
                if (null == parent) {
                    Logcat.i(TAG, "get bmpName parent file fail");
                    return false;
                }
                if (!parent.exists()) {
                    boolean bDir = parent.mkdirs();
                    if (!bDir) {
                        Logcat.i(TAG, "make dir fail");
                        return false;
                    }
                }
            }
            boolean bCreate = file.createNewFile();
            if (!bCreate) {
                Logcat.i(TAG, "create file fail");
                return false;
            }
            stream = new FileOutputStream(file);
            boolean bOk = bmp.compress(format, quality, stream);

            if (!bOk) {
                Logcat.i(TAG, "bitmap compress file fail");
                return false;
            }
        } catch (Exception e) {
            Logcat.i(TAG, e.toString());
            return false;
        } finally {
            if (null != stream) {
                try {
                    stream.close();
                } catch (Exception e2) {
                    Logcat.i(TAG, "close stream " + e2.toString());
                }
            }
        }
        return true;
    }

    /**
     * 功能简述:根据指定的图片文件的uri，创建图片。 功能详细描述: 注意:
     *
     * @param context
     * @param uri     ：目标图片文件的uri
     * @return
     */
    public static Bitmap loadBitmap(Context context, Uri uri, int simpleSize) {
        Bitmap pRet = null;
        if (null == context) {
            Logcat.i(TAG, "load bitmap context is null");
            return pRet;
        }
        if (null == uri) {
            Logcat.i(TAG, "load bitmap uri is null");
            return pRet;
        }

        InputStream is = null;
        int sampleSize = simpleSize;
        Options opt = new Options();

        boolean bool = true;
        while (bool) {
            try {
                is = context.getContentResolver().openInputStream(uri);
                opt.inSampleSize = sampleSize;
                pRet = null;
                pRet = BitmapFactory.decodeStream(is, null, opt);
                bool = false;
            } catch (OutOfMemoryError e) {
                sampleSize *= 2;
                if (sampleSize > (1 << 10)) {
                    bool = false;
                }
            } catch (Throwable e) {
                bool = false;
                Logcat.i(TAG, e.getMessage());
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (Exception e2) {
                    Logcat.i(TAG, e2.getMessage());
                    Logcat.i(TAG, "load bitmap close uri stream exception");
                }
            }
        }

        return pRet;
    }

    /**
     * 功能简述:对指定drawable进行指定的高宽缩放后，创建一张新的BitmapDrawable。 功能详细描述: 注意:
     *
     * @param context
     * @param drawable ：待处理的drawable
     * @param w        :期望缩放后的BitmapDrawable的宽
     * @param h        ：期望缩放后的BitmapDrawable的高
     * @return 经缩放处理后的新的BitmapDrawable
     */
    public static BitmapDrawable zoomDrawable(Context context,
                                              Drawable drawable, int w, int h) {
        if (drawable != null) {
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            Bitmap oldbmp = null;
            // drawable 转换成 bitmap
            if (drawable instanceof BitmapDrawable) {
                // 如果传入的drawable是BitmapDrawable,就不必要生成新的bitmap
                oldbmp = ((BitmapDrawable) drawable).getBitmap();
            } else {
                oldbmp = createBitmapFromDrawable(drawable);
            }

            int bw = oldbmp.getWidth();
            int bh = oldbmp.getHeight();

            Matrix matrix = new Matrix(); // 创建操作图片用的 Matrix 对象
            float scaleWidth = (float) w / width; // 计算缩放比例
            float scaleHeight = (float) h / height;
            matrix.postScale(scaleWidth, scaleHeight); // 设置缩放比例

            // 建立新的bitmap，其内容是对原bitmap的缩放后的图
            Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, bw, bh, matrix,
                    true);
            matrix = null;

            // 把bitmap转换成drawable并返回
            return new BitmapDrawable(context.getResources(), newbmp);
        }
        return null;
    }

    /**
     * 功能简述: 功能详细描述: 注意:
     *
     * @param drawable
     * @param wScale
     * @param hScale
     * @param res
     * @return
     */
    public static BitmapDrawable zoomDrawable(Drawable drawable, float wScale,
                                              float hScale, Resources res) {
        if (drawable != null) {
            Bitmap oldbmp = null;
            // drawable 转换成 bitmap
            if (drawable instanceof BitmapDrawable) {
                // 如果传入的drawable是BitmapDrawable,就不必要生成新的bitmap
                oldbmp = ((BitmapDrawable) drawable).getBitmap();
            } else {
                oldbmp = createBitmapFromDrawable(drawable);
            }

            int bw = oldbmp.getWidth();
            int bh = oldbmp.getHeight();

            Matrix matrix = new Matrix(); // 创建操作图片用的 Matrix 对象
            matrix.postScale(wScale, hScale); // 设置缩放比例

            // 建立新的bitmap，其内容是对原bitmap的缩放后的图
            Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, bw, bh, matrix,
                    true);
            matrix = null;

            // 把 bitmap 转换成 drawable 并返回
            return new BitmapDrawable(res, newbmp);
        }
        return null;
    }

    /**
     * 功能简述: 功能详细描述: 注意:
     *
     * @param drawable
     * @param w
     * @param h
     * @param res
     * @return
     */
    public static BitmapDrawable clipDrawable(BitmapDrawable drawable, int w,
                                              int h, Resources res) {
        if (drawable != null) {
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            if (width < w) {
                w = width;
            }
            if (height < h) {
                h = height;
            }
            int x = (width - w) >> 1;
            int y = (height - h) >> 1;
            Matrix matrix = new Matrix(); // 创建操作图片用的 Matrix 对象

            // 建立新的bitmap，其内容是对原bitmap的缩放后的图
            Bitmap newbmp = Bitmap.createBitmap(drawable.getBitmap(), x, y, w,
                    h, matrix, true);
            matrix = null;
            // 把 bitmap 转换成 drawable 并返回
            return new BitmapDrawable(res, newbmp);
        }
        return null;
    }

    public static Drawable composeDrawableTextExpend(Context context,
                                                     Drawable src, String text, int textSize, int padding) {
        if (src == null) {
            return null;
        }
        if (text == null) {
            return src;
        }
        try {
            if (!(src instanceof BitmapDrawable)) {
                Paint paint = new Paint();
                paint.setTextSize(textSize);
                paint.setStyle(Style.FILL_AND_STROKE);
                paint.setColor(Color.WHITE);
                paint.setAntiAlias(true); // 抗锯齿
                paint.setTextAlign(Paint.Align.CENTER);
                int length = (int) paint.measureText(text);

                int width = src.getIntrinsicWidth();
                int height = src.getIntrinsicHeight();

                if (width < length + padding * 2) {
                    width = length + padding * 2;
                }
                src.setBounds(0, 0, width, height);

                Bitmap temp = Bitmap.createBitmap(width, height,
                        Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(temp);
                src.draw(canvas);
                Drawable drawable = new BitmapDrawable(context.getResources(),
                        temp);
                return composeDrawableText(context, drawable, text, textSize);
            } else {
                return composeDrawableText(context, src, text, textSize);
            }

        } catch (Exception e) {
        }
        return null;
    }

    public static Drawable composeDrawableText(Context context, Drawable src,
                                               String text, int textSize) {
        if (src == null) {
            return null;
        }
        if (text == null) {
            return src;
        }
        try {
            Bitmap srcBitmap = null;
            if (src instanceof BitmapDrawable) {
                srcBitmap = ((BitmapDrawable) src).getBitmap();
            } else {
                srcBitmap = createBitmapFromDrawable(src);
            }
            if (srcBitmap == null) {
                return null;
            }
            int width = srcBitmap.getWidth();
            int height = srcBitmap.getHeight();
            Bitmap temp = Bitmap.createBitmap(width, height,
                    src.getOpacity() != PixelFormat.OPAQUE ? ARGB_8888 : RGB_565);
            Canvas canvas = new Canvas(temp);
            canvas.drawBitmap(srcBitmap, 0, 0, null);

            Paint paint = new Paint();
            paint.setTextSize(textSize);
            paint.setStyle(Style.FILL_AND_STROKE);
            paint.setColor(Color.WHITE);
            paint.setAntiAlias(true); // 抗锯齿
            paint.setTextAlign(Paint.Align.CENTER);
            int size = text.length();
            int length = (int) paint.measureText(text);
            int center = length / size / 2;
            int offX = width / 2;
            int offY = height / 2 + center + 1;
            canvas.drawText(text, offX, offY, paint);

            return new BitmapDrawable(context.getResources(), temp);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    /**
     * 将图片设为72*72
     *
     * @param drawable
     * @return
     */
    public static BitmapDrawable convertLePhoneIcon(Context context,
                                                    BitmapDrawable drawable) {
        int width = drawable.getBitmap().getWidth();
        int height = drawable.getBitmap().getHeight();
        int newWidth = 72;
        int newHeight = 72;

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        // create the new Bitmap object
        Bitmap resizedBitmap = Bitmap.createBitmap(drawable.getBitmap(), 0, 0,
                width, height, matrix, true);
        BitmapDrawable bmd = new BitmapDrawable(context.getResources(),
                resizedBitmap);
        return bmd;
    }

    /**
     * 功能简述: 功能详细描述: 注意:
     *
     * @param bitmap
     * @param wScale
     * @param hScale
     * @return
     */
    public static Bitmap zoomBitmap(Bitmap bitmap, float wScale, float hScale) {
        if (bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            Matrix matrix = new Matrix(); // 创建操作图片用的 Matrix 对象
            matrix.postScale(wScale, hScale); // 设置缩放比例

            // 建立新的bitmap，其内容是对原bitmap的缩放后的图
            Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height,
                    matrix, true);
            matrix = null;

            return newbmp;
        }
        return null;
    }

    public static Bitmap createBitmapFromDrawable(final Drawable drawable) {

        if (drawable == null) {
            return null;
        }

        Bitmap bitmap = null;
        final int intrinsicWidth = drawable.getIntrinsicWidth();
        final int intrinsicHeight = drawable.getIntrinsicHeight();
        if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
            return null;
        }

        try {
            Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                    : Bitmap.Config.RGB_565;
            bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight,
                    config);
        } catch (OutOfMemoryError e) {
            return null;
        }
        if (bitmap == null) {
            return null;
        }

        Canvas canvas = new Canvas(bitmap);
        // canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
        drawable.draw(canvas);
        canvas = null;
        return bitmap;
    }

    public static Bitmap createBitmapFromDrawable(final Drawable drawable,
                                                  int intrinsicWidth, int intrinsicHeight) {

        if (drawable == null) {
            return null;
        }

        Bitmap bitmap = null;
        if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
            return null;
        }

        try {
            Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                    : Bitmap.Config.RGB_565;
            bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight,
                    config);
        } catch (OutOfMemoryError e) {
            return null;
        }
        if (bitmap == null) {
            return null;
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
        drawable.draw(canvas);
        canvas = null;
        return bitmap;
    }

    public static BitmapDrawable createBitmapDrawableFromDrawable(
            final Drawable drawable, Context context) {
        Bitmap bitmap = createBitmapFromDrawable(drawable);
        if (bitmap == null) {
            return null;
        }

        BitmapDrawable bitmapDrawable = new BitmapDrawable(
                context.getResources(), bitmap);
        return bitmapDrawable;
    }

    public static Bitmap clipDrawableInCenter(Drawable drawable, int width,
                                              int height, int offsetX, int offsetY) {

        if (drawable == null || width <= 0 || height <= 0) {
            return null;
        }

        Bitmap bitmap = null;
        try {
            bitmap = Bitmap.createBitmap(width, height,
                    drawable.getOpacity() != PixelFormat.OPAQUE ? ARGB_8888 : RGB_565);
        } catch (OutOfMemoryError e) {
            return null;
        }

        int dwidth = drawable.getIntrinsicWidth();
        int dheight = drawable.getIntrinsicHeight();
        drawable.setBounds(0, 0, dwidth, dheight);

        Canvas canvas = new Canvas(bitmap);
        int saveCount = canvas.save();
        Matrix matrix = new Matrix();

        float scale;
        float dx = 0, dy = 0;

        if (dwidth * height > width * dheight) {
            scale = (float) height / (float) dheight;
            dx = (width - dwidth * scale) * 0.5f;
        } else {
            scale = (float) width / (float) dwidth;
            dy = (height - dheight * scale) * 0.5f;
        }

        matrix.setScale(scale, scale);
        matrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
        canvas.clipRect(0, 0, width, height);

        canvas.concat(matrix);
        drawable.draw(canvas);
        canvas.restoreToCount(saveCount);
        return bitmap;
    }

    /**
     * 合成图片
     *
     * @param pictures ： 待合成的图片
     * @param width    ： 合成图片宽
     * @param height   ： 合成图片高
     * @return 合成过后的图片
     * @author liwenxue
     */
    public static Bitmap composeBitmaps(Bitmap[] pictures, int width, int height) {
        if (pictures != null && pictures.length > 0) {
            // 创建一张新的图片
            Bitmap newBitmap = Bitmap.createBitmap(width, height,
                    Config.ARGB_8888);
            if (newBitmap != null) {
                Canvas cv = new Canvas(newBitmap);
                for (int i = 0; i < pictures.length; i++) {
                    // draw 缩放图片
                    Matrix matrix = new Matrix();
                    int picWidth = pictures[i].getWidth();
                    int picHeight = pictures[i].getHeight();
                    matrix.postScale((float) width / picWidth, (float) height
                            / picHeight);
                    cv.drawBitmap(pictures[i], matrix, null);
                }
                return newBitmap;
            }
        }
        return null;
    }

    /**
     * <br>
     * 功能简述: 按指定大小从文件解析bitmap <br>
     * 功能详细描述: <br>
     * 注意:
     *
     * @param filename
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapFromFile(String filename,
                                                     int reqWidth, int reqHeight) {
        if (reqWidth == 0 || reqHeight == 0) {
            return null;
        }
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }

    /**
     * 根据指定的位图最大宽度和高度生成位图
     *
     * @param res       资源
     * @param resId     资源ID
     * @param maxWidth  最大宽度
     * @param maxHeight 最大高度
     * @return 根据指定的位图最大宽度和高度生成位图
     */
    public static Bitmap getBitmap(Resources res, int resId, int maxWidth,
                                   int maxHeight) {
        if (resId == 0) {
            return null;
        }
        Bitmap retBitmap = null;
        int sampleSize = 1;
        BitmapFactory.Options options = new BitmapFactory.Options();

        // 取像sampleSize的初始值
        options.inJustDecodeBounds = true;
        retBitmap = BitmapFactory.decodeResource(res, resId, options);

        // 宽度比
        int ratioWidth = (int) ((double) options.outWidth / maxWidth + 0.5);

        // 高度比
        int ratioHeight = (int) ((double) options.outHeight / maxHeight + 0.5);

        int max = ratioHeight > ratioWidth ? ratioHeight : ratioWidth;

        if (max <= 1) {
            sampleSize = 1;
        } else {
            sampleSize = max;
        }

        options.inJustDecodeBounds = false;

        try {
            // 背景图片像素的宽和高取原来的1/sampleSize
            options.inSampleSize = sampleSize;
            retBitmap = BitmapFactory.decodeResource(res, resId, options);
        } catch (OutOfMemoryError e) {
            // BgDataEx.add(BgDataEx.ERROR_OUT_OF_MEMMORY);
            return null;
        } catch (Exception e) {
            Logcat.e(TAG, "Exception: " + e.getLocalizedMessage());
            return null;
        }

        return retBitmap;
    }

    /**
     * 根据指定的位图最大宽度和高度生成位图，获取图片有异常的时候认为是下载的图片有误可能不是一张完整的图片将其删除
     *
     * @param filepath 原图的路径
     * @return 根据指定的位图最大宽度和高度生成位图
     */
    public static Bitmap getCompleteBitmap(String filepath) {
        Bitmap retBitmap = null;
        try {
            retBitmap = BitmapFactory.decodeFile(filepath);
            if (retBitmap == null) {
                File file = new File(filepath);
                file.delete();
            }
        } catch (OutOfMemoryError e) {
            // BgDataEx.add(BgDataEx.ERROR_OUT_OF_MEMMORY);
        } catch (Exception e) {
            File file = new File(filepath);
            file.delete();
            return null;
        }
        return retBitmap;
    }

    /**
     * 自定义图片角落圆角
     *
     * @param source      出来的图片
     * @param leftTop     左上角角度
     * @param rightTop    右上角角度
     * @param leftBottom  左下角角度
     * @param rightBottom 右下角角度
     * @return
     */
    public static Bitmap getAutoRoundBitmap(Bitmap source, int leftTop, int rightTop, int leftBottom,
                                            int rightBottom) {
        BitmapDrawable d = new BitmapDrawable(source);
        Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(),
                d.getOpacity() != PixelFormat.OPAQUE ? ARGB_8888 : ARGB_4444);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color.WHITE);
        gradientDrawable.setBounds(new Rect(0, 0, source.getWidth(), source.getHeight()));
        gradientDrawable.setCornerRadii(
                new float[]{leftTop, leftTop, rightTop, rightTop, rightBottom, rightBottom, leftBottom, leftBottom});
        gradientDrawable.draw(canvas);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(source, 0, 0, paint);
        return output;
    }

    public static Bitmap getWhileBitmap(Bitmap source) {
        if (source == null) {
            return null;
        }
        ColorDrawable colorDrawable = new ColorDrawable(Color.WHITE);

        BitmapDrawable d = new BitmapDrawable(source);
        Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(),
                d.getOpacity() != PixelFormat.OPAQUE ? ARGB_8888 : ARGB_4444);
        Canvas canvas = new Canvas(output);
        colorDrawable.draw(canvas);
        return output;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;

    }

    public static Bitmap adjustPhotoRotation(Bitmap bm, int orientationDegree, boolean recycleSource) {

        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        float targetX, targetY;
        if (orientationDegree == 90) {
            targetX = bm.getHeight();
            targetY = 0;
        } else if (orientationDegree == -90) {
            targetX = 0;
            targetY = bm.getWidth();
        } else {
            targetX = bm.getWidth();
            targetY = bm.getHeight();
        }

        final float[] values = new float[9];
        m.getValues(values);

        float x1 = values[Matrix.MTRANS_X];
        float y1 = values[Matrix.MTRANS_Y];

        m.postTranslate(targetX - x1, targetY - y1);

        Bitmap bm1 = Bitmap.createBitmap(bm.getHeight(), bm.getWidth(), Config.ARGB_4444);

        Paint paint = new Paint();
        Canvas canvas = new Canvas(bm1);
        canvas.drawBitmap(bm, m, paint);
        if (recycleSource && bm1 != bm) {
            bm.recycle();
        }
        return bm1;
    }

//    /**
//     * @param bitmap
//     * @param format ImageConverter format
//     * @return
//     */
//    public static byte[] getBitmapData(Bitmap bitmap, int format) {
//        if (bitmap == null) {
//            return null;
//        }
//        byte[] data = new byte[bitmap.getWidth() * bitmap.getHeight() * 3 / 2];
//        ImageConverter convert = new ImageConverter();
//        convert.initial(bitmap.getWidth(), bitmap.getHeight(), format);
//        if (convert.convert(bitmap, data)) {
//            Log.d(TAG, "convert success!");
//        } else {
//            Log.d(TAG, "convert failed!");
//            data = null;
//        }
//        convert.destroy();
//        return data;
//    }

    public static byte[] getNV21Data(int inputWidth, int inputHeight, Bitmap scaled) {

        int[] argb = new int[inputWidth * inputHeight];

        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

        byte[] yuv = new byte[inputWidth * inputHeight * 3 / 2];
        try {
            YuvUtils.encodeYUV420SP(yuv, argb, inputWidth, inputHeight);
        } catch (Throwable t) {
            encodeYUV420SP(yuv, argb, inputWidth, inputHeight);
        }

        return yuv;
    }

    private static void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        final int frameSize = width * height;
        int yIndex = 0;
        int uvIndex = frameSize;
        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;
                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                    yuv420sp[uvIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                }
                index++;
            }
        }
    }

    /**
     * 截取原图某个矩形区域
     *
     * @param src
     * @param rect
     * @return
     */
    public static Bitmap clipBitmap(Bitmap src, Rect rect) {
        int left = rect.left;
        int top = rect.top;
        int width = rect.width();
        int height = rect.height();
        if (left < 0) {
            left = 0;
        }
        if (top < 0) {
            top = 0;
        }
        if (left + width > src.getWidth()) {
            width = src.getWidth() - left;
        }
        if (top + height > src.getHeight()) {
            height = src.getHeight() - top;
        }
        return Bitmap.createBitmap(src, left, top, width, height);
    }

    /**
     * 把target绘制到base上
     *
     * @param base
     * @param target
     * @param left
     * @param top
     * @param width
     * @param height
     * @return
     */
    public static Bitmap composeBitmap(Bitmap base, Bitmap target, int left, int top, int width,
                                       int height) {
        Canvas canvas;
        try {
            canvas = new Canvas(base);
        } catch (IllegalStateException e) {
            base = base.copy(Bitmap.Config.RGB_565, true);
            canvas = new Canvas(base);
        }
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setFilterBitmap(true);
        canvas.drawBitmap(target, null, new Rect(left, top, left + width, top + height), paint);
        return base;
    }

    public static boolean isBase64Img(String imgurl) {
        return !TextUtils.isEmpty(imgurl) && (imgurl.startsWith("data:image/png;base64,")
                || imgurl.startsWith("data:image/jpeg;base64,")
                || imgurl.startsWith("data:image/*;base64,")
                || imgurl.startsWith("data:image/jpg;base64,")
        );
    }

    public static byte[] getByteForBase64Url(String url) {
        byte[] decode = null;
        if (isBase64Img(url)) {
            url = url.split(",")[1];
            decode = Base64.decode(url, Base64.DEFAULT);
        }
        return decode;
    }

    public static Bitmap fixBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width % 2 != 0) {
            width--;
        }
        if (height % 2 != 0) {
            height--;
        }
        Bitmap newBitmap = bitmap;
        if (width != bitmap.getWidth() || height != bitmap.getHeight()) {
            newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        }
        return newBitmap;
    }

    /**
     * 等比缩放图片至maxValue和minValue之间
     *
     * @param bitmap
     * @return
     */
    public static Bitmap scaleForDisplay(Bitmap bitmap, int displayHeight, int displayWidth) {
        float height = bitmap.getHeight();
        float width = bitmap.getWidth();
        if (height < displayHeight && width < displayWidth) {
            return bitmap;
        }
        float mViewRadio = displayHeight * 1.0f / displayWidth;
        float mBitmapRadio = height * 1.0f / width;
        float rectWidth = displayWidth;
        float rectHeight = displayHeight;
        if (mViewRadio > mBitmapRadio) {
            rectHeight = rectWidth * mBitmapRadio;
        } else {
            rectWidth = rectHeight / mBitmapRadio;
        }
        return createScaledBitmap(bitmap, (int) rectWidth, (int) rectHeight);
    }

    public static Bitmap getShadowBitmap(Bitmap source, Bitmap mask, Bitmap shadow) {
        return getShadowBitmap(source, mask, shadow, ImageView.ScaleType.CENTER_CROP);
    }

    public static Bitmap getShadowBitmap(Bitmap source, Bitmap mask, Bitmap shadow, ImageView.ScaleType scaleType) {
        Rect maskRect = new Rect(0, 0, mask.getWidth(), mask.getHeight());
        Bitmap bitmap = Bitmap.createBitmap(maskRect.width(), maskRect.height(), ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        int save = canvas.saveLayer(new RectF(0, 0, shadow.getWidth(), shadow.getHeight()), null, Canvas.ALL_SAVE_FLAG);
        Rect rect = new Rect(0, 0, source.getWidth(), source.getHeight());
        float mViewRadio = shadow.getHeight() * 1.0f / shadow.getWidth();
        float mBitmapRadio = source.getHeight() * 1.0f / source.getWidth();
        float rectWidth = shadow.getWidth();
        float rectHeight = shadow.getHeight();
        if (scaleType == ImageView.ScaleType.CENTER_INSIDE) {
            if (mViewRadio < mBitmapRadio) {
                rectWidth = rectHeight / mBitmapRadio;
            } else {
                rectHeight = rectWidth * mBitmapRadio;
            }

        } else if (scaleType == ImageView.ScaleType.CENTER_CROP) {
            if (mViewRadio < mBitmapRadio) {
                rectHeight = rectWidth * mBitmapRadio;
            } else {
                rectWidth = rectHeight / mBitmapRadio;
            }
        }
        rect.left = (int) ((shadow.getWidth() - rectWidth) / 2);
        rect.top = (int) ((shadow.getHeight() - rectHeight) / 2);
        rect.right = (int) (rect.left + rectWidth);
        rect.bottom = (int) (rect.top + rectHeight);
        canvas.drawBitmap(mask, null, maskRect, null);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawRect(maskRect, paint);
        canvas.drawBitmap(source, null, rect, paint);
        canvas.restoreToCount(save);
        canvas.drawBitmap(shadow, null, maskRect, null);
        return bitmap;
    }

    public static Bitmap getBitmapForView(View src, float downscaleFactor) {
        Bitmap bitmap = Bitmap.createBitmap(
                (int) (src.getWidth() * downscaleFactor),
                (int) (src.getHeight() * downscaleFactor),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);
        Matrix matrix = new Matrix();
        matrix.preScale(downscaleFactor, downscaleFactor);
        canvas.setMatrix(matrix);
        src.draw(canvas);

        return bitmap;
    }

    public static Bitmap centerScaleBitmapForViewSize(Bitmap src, float viewWidth, float viewHeight) {
        float srcW = src.getWidth();
        float srcH = src.getHeight();
        float targetW;
        float targetH;
        float scale;
        if (viewWidth / viewHeight > srcW / srcH) {
            targetW = viewWidth;
            scale = targetW / srcW;
            targetH = srcH * scale;
        } else {
            targetH = viewHeight;
            scale = targetH / srcH;
            targetW = srcW * scale;
        }
        return BitmapUtils.createScaledBitmap(src, (int) targetW, (int) targetH);
    }

    public static byte[] toByteArray(Bitmap bmp) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap toBitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private static boolean isEmptyBitmap(final Bitmap src) {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0;
    }

    /**
     * Return the scaled bitmap.
     *
     * @param src       The source of bitmap.
     * @param newWidth  The new width.
     * @param newHeight The new height.
     * @return the scaled bitmap
     */
    public static Bitmap scale(final Bitmap src, final int newWidth, final int newHeight) {
        return scale(src, newWidth, newHeight, false);
    }

    /**
     * Return the scaled bitmap.
     *
     * @param src       The source of bitmap.
     * @param newWidth  The new width.
     * @param newHeight The new height.
     * @param recycle   True to recycle the source of bitmap, false otherwise.
     * @return the scaled bitmap
     */
    public static Bitmap scale(final Bitmap src,
                               final int newWidth,
                               final int newHeight,
                               final boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        Bitmap ret = Bitmap.createScaledBitmap(src, newWidth, newHeight, true);
        if (recycle && !src.isRecycled() && ret != src) src.recycle();
        return ret;
    }

    /**
     * Return the scaled bitmap
     *
     * @param src         The source of bitmap.
     * @param scaleWidth  The scale of width.
     * @param scaleHeight The scale of height.
     * @return the scaled bitmap
     */
    public static Bitmap scale(final Bitmap src, final float scaleWidth, final float scaleHeight) {
        return scale(src, scaleWidth, scaleHeight, false);
    }

    /**
     * Return the scaled bitmap
     *
     * @param src         The source of bitmap.
     * @param scaleWidth  The scale of width.
     * @param scaleHeight The scale of height.
     * @param recycle     True to recycle the source of bitmap, false otherwise.
     * @return the scaled bitmap
     */
    public static Bitmap scale(final Bitmap src,
                               final float scaleWidth,
                               final float scaleHeight,
                               final boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        Matrix matrix = new Matrix();
        matrix.setScale(scaleWidth, scaleHeight);
        Bitmap ret = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        if (recycle && !src.isRecycled() && ret != src) src.recycle();
        return ret;
    }


    ///////////////////////////////////////////////////////////////////////////
    // about compress
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Return the compressed bitmap using scale.
     *
     * @param src       The source of bitmap.
     * @param newWidth  The new width.
     * @param newHeight The new height.
     * @return the compressed bitmap
     */
    public static Bitmap compressByScale(final Bitmap src,
                                         final int newWidth,
                                         final int newHeight) {
        return scale(src, newWidth, newHeight, false);
    }

    /**
     * Return the compressed bitmap using scale.
     *
     * @param src       The source of bitmap.
     * @param newWidth  The new width.
     * @param newHeight The new height.
     * @param recycle   True to recycle the source of bitmap, false otherwise.
     * @return the compressed bitmap
     */
    public static Bitmap compressByScale(final Bitmap src,
                                         final int newWidth,
                                         final int newHeight,
                                         final boolean recycle) {
        return scale(src, newWidth, newHeight, recycle);
    }

    /**
     * Return the compressed bitmap using scale.
     *
     * @param src         The source of bitmap.
     * @param scaleWidth  The scale of width.
     * @param scaleHeight The scale of height.
     * @return the compressed bitmap
     */
    public static Bitmap compressByScale(final Bitmap src,
                                         final float scaleWidth,
                                         final float scaleHeight) {
        return scale(src, scaleWidth, scaleHeight, false);
    }

    /**
     * Return the compressed bitmap using scale.
     *
     * @param src         The source of bitmap.
     * @param scaleWidth  The scale of width.
     * @param scaleHeight The scale of height.
     * @param recycle     True to recycle the source of bitmap, false otherwise.
     * @return he compressed bitmap
     */
    public static Bitmap compressByScale(final Bitmap src,
                                         final float scaleWidth,
                                         final float scaleHeight,
                                         final boolean recycle) {
        return scale(src, scaleWidth, scaleHeight, recycle);
    }

    /**
     * Return the compressed bitmap using quality.
     *
     * @param src     The source of bitmap.
     * @param quality The quality.
     * @return the compressed bitmap
     */
    public static Bitmap compressByQuality(final Bitmap src,
                                           @IntRange(from = 0, to = 100) final int quality) {
        return compressByQuality(src, quality, false);
    }

    /**
     * Return the compressed bitmap using quality.
     *
     * @param src     The source of bitmap.
     * @param quality The quality.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the compressed bitmap
     */
    public static Bitmap compressByQuality(final Bitmap src,
                                           @IntRange(from = 0, to = 100) final int quality,
                                           final boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        src.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        byte[] bytes = baos.toByteArray();
        if (recycle && !src.isRecycled()) src.recycle();
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * Return the compressed bitmap using quality.
     *
     * @param src         The source of bitmap.
     * @param maxByteSize The maximum size of byte.
     * @return the compressed bitmap
     */
    public static Bitmap compressByQuality(final Bitmap src, final long maxByteSize) {
        return compressByQuality(src, maxByteSize, false);
    }

    /**
     * Return the compressed bitmap using quality.
     *
     * @param src         The source of bitmap.
     * @param maxByteSize The maximum size of byte.
     * @param recycle     True to recycle the source of bitmap, false otherwise.
     * @return the compressed bitmap
     */
    public static Bitmap compressByQuality(final Bitmap src,
                                           final long maxByteSize,
                                           final boolean recycle) {
        if (isEmptyBitmap(src) || maxByteSize <= 0) return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        src.compress(CompressFormat.JPEG, 100, baos);
        byte[] bytes;
        if (baos.size() <= maxByteSize) {
            bytes = baos.toByteArray();
        } else {
            baos.reset();
            src.compress(CompressFormat.JPEG, 0, baos);
            if (baos.size() >= maxByteSize) {
                bytes = baos.toByteArray();
            } else {
                // find the best quality using binary search
                int st = 0;
                int end = 100;
                int mid = 0;
                while (st < end) {
                    mid = (st + end) / 2;
                    baos.reset();
                    src.compress(CompressFormat.JPEG, mid, baos);
                    int len = baos.size();
                    if (len == maxByteSize) {
                        break;
                    } else if (len > maxByteSize) {
                        end = mid - 1;
                    } else {
                        st = mid + 1;
                    }
                }
                if (end == mid - 1) {
                    baos.reset();
                    src.compress(CompressFormat.JPEG, st, baos);
                }
                bytes = baos.toByteArray();
            }
        }
        if (recycle && !src.isRecycled()) src.recycle();
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * Return the compressed bitmap using sample size.
     *
     * @param src        The source of bitmap.
     * @param sampleSize The sample size.
     * @return the compressed bitmap
     */

    public static Bitmap compressBySampleSize(final Bitmap src, final int sampleSize) {
        return compressBySampleSize(src, sampleSize, false);
    }

    /**
     * Return the compressed bitmap using sample size.
     *
     * @param src        The source of bitmap.
     * @param sampleSize The sample size.
     * @param recycle    True to recycle the source of bitmap, false otherwise.
     * @return the compressed bitmap
     */
    public static Bitmap compressBySampleSize(final Bitmap src,
                                              final int sampleSize,
                                              final boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        src.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        if (recycle && !src.isRecycled()) src.recycle();
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    /**
     * Return the compressed bitmap using sample size.
     *
     * @param src       The source of bitmap.
     * @param maxWidth  The maximum width.
     * @param maxHeight The maximum height.
     * @return the compressed bitmap
     */
    public static Bitmap compressBySampleSize(final Bitmap src,
                                              final int maxWidth,
                                              final int maxHeight) {
        return compressBySampleSize(src, maxWidth, maxHeight, false);
    }

    /**
     * Return the compressed bitmap using sample size.
     *
     * @param src       The source of bitmap.
     * @param maxWidth  The maximum width.
     * @param maxHeight The maximum height.
     * @param recycle   True to recycle the source of bitmap, false otherwise.
     * @return the compressed bitmap
     */
    public static Bitmap compressBySampleSize(final Bitmap src,
                                              final int maxWidth,
                                              final int maxHeight,
                                              final boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        src.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
        options.inJustDecodeBounds = false;
        if (recycle && !src.isRecycled()) src.recycle();
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    /**
     * Return the size of bitmap.
     *
     * @param file The file.
     * @return the size of bitmap
     */
    public static int[] getSize(File file) {
        if (file == null) return new int[]{0, 0};
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
        return new int[]{opts.outWidth, opts.outHeight};
    }

    /**
     * Return the sample size.
     *
     * @param options   The options.
     * @param maxWidth  The maximum width.
     * @param maxHeight The maximum height.
     * @return the sample size
     */
    private static int calculateInSampleSize(final BitmapFactory.Options options,
                                             final int maxWidth,
                                             final int maxHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        while (height > maxHeight || width > maxWidth) {
            height >>= 1;
            width >>= 1;
            inSampleSize <<= 1;
        }
        return inSampleSize;
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId, int width, int height) {
        Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
