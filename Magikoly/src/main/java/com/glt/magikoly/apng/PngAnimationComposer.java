package com.glt.magikoly.apng;

import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import magikoly.magiccamera.R;
import net.ellerton.japng.argb8888.Argb8888Bitmap;
import net.ellerton.japng.argb8888.Argb8888ScanlineProcessor;
import net.ellerton.japng.chunks.PngAnimationControl;
import net.ellerton.japng.chunks.PngFrameControl;
import net.ellerton.japng.chunks.PngHeader;

import java.util.ArrayList;
import java.util.List;

/**
 * Takes loaded PNG frames and composes them into an android AnimationDrawable.
 */
public class PngAnimationComposer {
    private Resources resources;
    private Canvas canvas;
    private PngHeader header;
    private Bitmap canvasBitmap;
    private Argb8888ScanlineProcessor scanlineProcessor;
    private PngAnimationControl animationControl;
    private PngFrameControl currentFrame;
    private List<Frame> frames;
    private int durationScale = 1;
    private Paint srcModePaint;
    private final Paint clearModePaint;

    /**
     * Keep a 1x1 transparent image around as reference for creating a scaled starting bitmap.
     * Considering this because of some reported OutOfMemory errors, and this post:
     *
     * http://stackoverflow.com/a/8527745/963195
     *
     * Specifically: "NEVER use Bitmap.createBitmap(width, height, Config.ARGB_8888). I mean NEVER!"
     *
     * Instead the 1x1 image (68 bytes of resources) is scaled up to the needed size.
     * Whether or not this fixes the OOM problems is TBD...
     */
    static Bitmap referenceImage = null;

    public static Bitmap getReferenceImage(Resources resources) {
        if (referenceImage==null) {
            referenceImage = BitmapFactory.decodeResource(resources, R.drawable.onepxtransparent);
        }
        return referenceImage;
    }

    public PngAnimationComposer(Resources resources, PngHeader header, Argb8888ScanlineProcessor scanlineProcessor, PngAnimationControl animationControl) {
        this.resources = resources;
        this.header = header;
        this.scanlineProcessor = scanlineProcessor;
        this.animationControl = animationControl;

        //this.canvasBitmap = Bitmap.createBitmap(this.header.width, this.header.height, Bitmap.Config.ARGB_8888);
        this.canvasBitmap = Bitmap.createScaledBitmap(getReferenceImage(resources),
                PngUtils.getScaledSize(this.header.width),
                PngUtils.getScaledSize(this.header.height), false);
        this.canvas = new Canvas(this.canvasBitmap);
        this.frames = new ArrayList<>(animationControl.numFrames);
        this.srcModePaint = new Paint();
        this.srcModePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        this.clearModePaint = new Paint();
        this.clearModePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    public int getDurationScale() {
        return durationScale;
    }

    public void setDurationScale(int durationScale) {
        this.durationScale = durationScale;
    }

    public boolean isSingleFrame() {
        return 1 == animationControl.numFrames;
    }

//    public int getNumFrames() {
//        return animationControl.numFrames;
//    }

    public ImageView buildInto(ImageView view) {
        if (isSingleFrame()) {
            Argb8888Bitmap bitmap = scanlineProcessor.getBitmap(); // TODO: ok?
            view.setImageBitmap(PngUtils.toBitmap(bitmap)); // TODO: ok?
        } else {
            view.setBackgroundDrawable(assemble());
        }
        return view;
    }

    public AnimationDrawable assemble() {
        // TODO: handle special case of one frame animation as a plain ImageView
        boolean isFinite = !animationControl.loopForever();
        AnimationDrawable ad = new AnimationDrawable();
        ad.setOneShot(isFinite);

        // The AnimationDrawable doesn't support a repeat count so add
        // frames as required. At least the frames can re-use drawables.
        int repeatSequenceCount = isFinite ? animationControl.numPlays : 1;

        for (int i = 0; i < repeatSequenceCount; i++) {
            for (Frame frame : frames) {
                ad.addFrame(frame.drawable, frame.control.getDelayMilliseconds() * durationScale);
            }
        }
        return ad;
    }

    public Argb8888ScanlineProcessor beginFrame(PngFrameControl frameControl) {
        currentFrame = frameControl;
        return scanlineProcessor.cloneWithSharedBitmap(header.adjustFor(currentFrame));
        //return scanlineProcessor.cloneWithNewBitmap(header.adjustFor(currentFrame));
    }

    public void completeFrame(Argb8888Bitmap frameImage) {

        Bitmap frame = PngUtils.toBitmap(frameImage);
        Paint paint = null;
        Drawable d;
        Bitmap previous=null;
        int xOffset = PngUtils.getScaledSize(currentFrame.xOffset);
        int yOffset = PngUtils.getScaledSize(currentFrame.yOffset);
        int width = PngUtils.getScaledSize(currentFrame.width);
        int height = PngUtils.getScaledSize(currentFrame.height);
        // Capture the current bitmap region IF it needs to be reverted after rendering
        if (2 == currentFrame.disposeOp) {
            previous = Bitmap.createBitmap(canvasBitmap, xOffset, yOffset, width, height);
        }

        if (0 == currentFrame.blendOp) { // SRC_OVER, not blend (for blend, leave paint null)
            paint = srcModePaint;
        }

        // Draw the new frame into place
        canvas.drawBitmap(frame, xOffset, yOffset, paint);

        // Extract a drawable from the canvas. Have to copy the current bitmap.
        d = new BitmapDrawable(resources, canvasBitmap.copy(Bitmap.Config.ARGB_8888, false));

        // Store the drawable in the sequence of frames
        frames.add(new Frame(currentFrame, d));

        switch (currentFrame.disposeOp) {
            case 1: // APNG_DISPOSE_OP_BACKGROUND
                Rect region = new Rect(xOffset, yOffset, xOffset + width, yOffset + height);
                canvas.drawRect(region, clearModePaint);
                break;
            case 2: // APNG_DISPOSE_OP_PREVIOUS
                // Put the original section back
                if (null != previous) {
                    paint = srcModePaint;
                    canvas.drawBitmap(previous, xOffset, yOffset, paint);
                    previous.recycle();
                } else {
                    System.out.println("  Huh, no previous?");
                }
                break;

            case 0: // APNG_DISPOSE_OP_NONE
            default: // Default should never happen
                // do nothing
                break;
        }

        currentFrame = null;
    }

    public static class Frame {
        public final PngFrameControl control;
        public final Drawable drawable;

        public Frame(PngFrameControl control, Drawable drawable) {
            this.control = control;
            this.drawable = drawable;
        }
    }
}