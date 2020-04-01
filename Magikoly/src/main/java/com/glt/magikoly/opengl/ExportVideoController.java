package com.glt.magikoly.opengl;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.opengl.GLES20;
import android.util.Log;

import com.cs.bd.commerce.util.io.FileUtil;
import com.glt.magikoly.opengl.egl.CodecInputSurface;
import com.glt.magikoly.opengl.egl.EGLEnvironmentZ;
import com.glt.magikoly.opengl.egl.IEGLEnvironment;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;

import magikoly.magiccamera.R;

/**
 * @author rayhahah
 * @blog http://rayhahah.com
 * @time 2019/9/23
 * @tips 这个类是Object的子类
 * @fuction
 */
public class ExportVideoController {


    private Context mContext;
    private GLRenderer mRenderer;
    private EGLEnvironmentZ mEGLZ;

    public ExportVideoController(Context context, GLRenderer glRenderer) {
        mContext = context;
        mRenderer = glRenderer;
    }

    private static final String TAG = "ExportVideoService";

    // 输出视频的MIME类型
    private static final String EXPORT_VIDEO_MIME_TYPE = "video/avc";
    // 输出视频的宽度
    private static final int EXPORT_VIDEO_WIDTH = 1080;
    // 输出视频的高度
    private static final int EXPORT_VIDEO_HEIGHT = 608;
    // 输出视频的帧率FPS
    private static final int EXPORT_VIDEO_FPS = 24;
    // 输出视频的帧数
    private static final int EXPORT_FRAME_COUNT = 80;

    // 输出的文件路径
    private String mExportFilePath;

    // 是否正在忙
    private boolean mIsBusy = false;

    // buffer
    private MediaCodec.BufferInfo mBufferInfo;
    // 编码器
    private MediaCodec mEncoder;
    // 输入的Surface
    private CodecInputSurface mInputSurface;
    //EGL环境
    private IEGLEnvironment ieglEnvironment;
    // 合成器
    private MediaMuxer mMuxer;

    // MediaMuxer中的视轨索引
    private int mVideoTrackIndex;
    private boolean mMuxerStarted;

    // 音轨解析器
    private MediaExtractor mAudioExtractor;
    // 音轨在解析器中的索引
    private int mAudioExtractorIndex = -1;
    // MediaMuxer中的音轨索引
    private int mAudioTrackIndex;


    public void init() {
        try {
            initVideoEncoder();
        } catch (Exception e) {
            e.printStackTrace();
            releaseEncoder();
        }
    }

    private long getDuration() {
        return 1000 / EXPORT_VIDEO_FPS * EXPORT_FRAME_COUNT;
    }

    private int getKeyFrameInterval(int fps, int frameCount) {
        int videoDuration = 1000 / fps * frameCount;
        if (videoDuration < 10 * 1000) {
            return 1;
        } else {
            return 5;
        }
    }

    private int getBitRate(int width, int heigh) {
        return (int) (width * heigh * 1.6);
    }

    private void initVideoEncoder() {
        // 创建一个buffer
        mBufferInfo = new MediaCodec.BufferInfo();

        /* 创建一个视频MediaFormat */
        // 设置MIME类型、宽高
        MediaFormat format = MediaFormat.createVideoFormat(EXPORT_VIDEO_MIME_TYPE,
                EXPORT_VIDEO_WIDTH, EXPORT_VIDEO_HEIGHT);
        // 设置profile
        // BaseLine基本覆盖Android 4.x及以上版本
        format.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline);
        // 设置profile level(设置了profile，则一定要设置该值，不然报错)
        // L4.1基本覆盖Android 4.x及以上版本
        format.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel41);
        // 设置颜色编码格式
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        // 设置码率
        format.setInteger(MediaFormat.KEY_BIT_RATE, getBitRate(EXPORT_VIDEO_WIDTH, EXPORT_VIDEO_HEIGHT));
        // 设置帧率FPS
        format.setInteger(MediaFormat.KEY_FRAME_RATE, EXPORT_VIDEO_FPS);
        // 设置关键帧时间
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, getKeyFrameInterval(EXPORT_VIDEO_FPS, EXPORT_FRAME_COUNT));

        /* 创建编码器 */
        try {
            mEncoder = MediaCodec.createEncoderByType(EXPORT_VIDEO_MIME_TYPE);
            mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);


            mEGLZ = new EGLEnvironmentZ(mEncoder.createInputSurface());
//            ieglEnvironment = new EGLEnvironment();
//            ieglEnvironment.initialize();
//            ieglEnvironment.bindSurface(1080,608,mEncoder.createInputSurface());
//            mInputSurface = new CodecInputSurface(mEncoder.createInputSurface());
//            mInputSurface.makeCurrent();
//            mRenderer.onCreated();
            mEncoder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


        /* 创建合成器 */
        Calendar calendar = Calendar.getInstance();
        mExportFilePath = mContext.getFilesDir().getAbsolutePath() + File.separator + "GifMaker" + File.separator
                + (calendar.get(Calendar.MONTH) + 1) + "_" + calendar.get(Calendar.DAY_OF_MONTH) + "_"
                + calendar.get(Calendar.HOUR_OF_DAY) + "_" + calendar.get(Calendar.MINUTE) + "_"
                + calendar.get(Calendar.SECOND)
                + ".mp4";
        FileUtil.createNewFile(mExportFilePath,false);
        try {
            mMuxer = new MediaMuxer(mExportFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mVideoTrackIndex = -1;
        mMuxerStarted = false;
    }


    public void stopMerge() {
        mIsBusy = false;
    }

    public void startMerge() {
        if (mIsBusy) {
            Log.e(TAG, "busy...");
            return;
        }

        mIsBusy = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    addAudioTrack();
                    addVideoData();
                    // send end-of-stream to encoder, and drain remaining output
                    drainEncoder(true);
                    writeAudioData();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    File exportFile = new File(mExportFilePath);
                    exportFile.deleteOnExit();
                } finally {
                    releaseEncoder();
                }
                mIsBusy = false;
//                stopSelf();
            }
        }).start();
    }

    private void addVideoData() {
//        mInputSurface.makeCurrent();
        for (int i = 0; i < EXPORT_FRAME_COUNT; i++) {
            Log.e(TAG, "-----------------start encode frame: frameIndex = " + i + "-----------------");
            // mEncoder从缓冲区取数据，然后交给mMuxer编码
            drainEncoder(false);
            // opengl绘制一帧
            generateSurfaceFrame(i);
            // 设置图像，发送给EGL的显示时间
//            mInputSurface.setPresentationTime(computePresentationTimeNsec(i));
            // Submit it to the encoder
//            mInputSurface.swapBuffers();
            mEGLZ.swap();
            if (ieglEnvironment != null) {
                ieglEnvironment.swap();
            }
            Log.e(TAG, "-----------------end encode frame: frameIndex = " + i + "-----------------");

        }
    }

    /**
     * mEncoder从缓冲区取数据，然后交给mMuxer编码
     *
     * @param endOfStream 是否停止录制
     */
    private void drainEncoder(boolean endOfStream) {
        final int TIMEOUT_USEC = 10000;

        // 停止录制
        if (endOfStream) {
            mEncoder.signalEndOfInputStream();
        }
        // 拿到输出缓冲区,用于取到编码后的数据
        ByteBuffer encoderOutputBuffer;
        int lastOutputBufferIndex = Integer.MIN_VALUE;
        while (mIsBusy) {
            // 拿到输出缓冲区的索引
            int outputBufferIndex = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (lastOutputBufferIndex != outputBufferIndex) {
                Log.d(TAG, "outputBufferIndex = " + outputBufferIndex);
            }
            lastOutputBufferIndex = outputBufferIndex;
            switch (outputBufferIndex) {
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    if (!endOfStream) {
                        Log.d(TAG, "no output available yet");
                        endOfStream = true;
                    }
                    break;

                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    // should happen before receiving buffers, and should only happen once
                    if (mMuxerStarted) {
                        throw new RuntimeException("format changed twice");
                    }
                    //
                    MediaFormat newFormat = mEncoder.getOutputFormat();
                    Log.e(TAG, "222222222222: newFormat = " + newFormat);
                    // now that we have the Magic Goodies, start the muxer
                    mVideoTrackIndex = mMuxer.addTrack(newFormat);
                    //
                    mMuxer.start();
                    mMuxerStarted = true;
                    break;

                default:
                    Log.d(TAG, "drainEncoder: ");
                    // 获取编码后的数据
                    encoderOutputBuffer = mEncoder.getOutputBuffer(outputBufferIndex);
                    if (encoderOutputBuffer == null) {
                        throw new RuntimeException("encoderOutputBuffer " + outputBufferIndex +
                                " was null");
                    }
                    //
                    if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        mBufferInfo.size = 0;
                    }
                    //
                    if (mBufferInfo.size != 0) {
                        if (!mMuxerStarted) {
                            throw new RuntimeException("muxer hasn't started");
                        }
                        // adjust the ByteBuffer values to match BufferInfo (not needed?)
                        encoderOutputBuffer.position(mBufferInfo.offset);
                        encoderOutputBuffer.limit(mBufferInfo.offset + mBufferInfo.size);
                        // 写入混合器，核心！！
                        mMuxer.writeSampleData(mVideoTrackIndex, encoderOutputBuffer, mBufferInfo);
                    }
                    // 释放资源
                    mEncoder.releaseOutputBuffer(outputBufferIndex, false);

                    if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        if (!endOfStream) {
                            Log.d(TAG, "reached end of stream unexpectedly");
                        }
                        endOfStream = true;
                        break;
                    }
                    break;
            }
            if (endOfStream) {
                Log.d(TAG, "-----endOfStream=" + endOfStream);
            }
            if (endOfStream) {
                break;
            }
        }
    }

    // RGB color values for generated frames
    private static final int TEST_R0 = 0;
    private static final int TEST_G0 = 136;
    private static final int TEST_B0 = 0;
    //
    private static final int TEST_R1 = 236;
    private static final int TEST_G1 = 50;
    private static final int TEST_B1 = 186;

    /**
     * Generates a frame of data using GL commands.  We have an 8-frame animation
     * sequence that wraps around.  It looks like this:
     * <pre>
     *   0 1 2 3
     *   7 6 5 4
     * </pre>
     * We draw one of the eight rectangles and leave the rest set to the clear color.
     */
    private void generateSurfaceFrame(int frameIndex) {
//        mRenderer.onDrawFrame();

        int startX, startY;
        if (frameIndex < 4) {
            // (0,0) is bottom-left in GL
            startX = frameIndex * (EXPORT_VIDEO_WIDTH / 4);
            startY = EXPORT_VIDEO_HEIGHT / 2;
        } else {
            startX = (7 - frameIndex) * (EXPORT_VIDEO_WIDTH / 4);
            startY = 0;
        }

        GLES20.glClearColor(TEST_R0 / 255.0f, TEST_G0 / 255.0f, TEST_B0 / 255.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(startX, startY, EXPORT_VIDEO_WIDTH / 4, EXPORT_VIDEO_HEIGHT / 2);
        GLES20.glClearColor(TEST_R1 / 255.0f, TEST_G1 / 255.0f, TEST_B1 / 255.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
    }

    private void addAudioTrack() throws Exception {
        mAudioExtractor = new MediaExtractor();
        Uri videoUri = Uri.parse("android.resource://"
                + mContext.getPackageName() + "/"
                + R.raw.count_to_2);
        mAudioExtractor.setDataSource(mContext, videoUri, null);
        mAudioExtractorIndex = 0;
        for (; mAudioExtractorIndex < mAudioExtractor.getTrackCount(); mAudioExtractorIndex++) {
            Log.e(TAG, "track" + mAudioExtractorIndex + " = " + mAudioExtractor.getTrackFormat(mAudioExtractorIndex));
            if (mAudioExtractor.getTrackFormat(mAudioExtractorIndex).getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                mAudioExtractor.selectTrack(mAudioExtractorIndex);
                break;
            }
        }
        mAudioTrackIndex = mMuxer.addTrack(mAudioExtractor.getTrackFormat(mAudioExtractorIndex));

    }

    private void writeAudioData() {
        if (!mMuxerStarted) {
            mMuxer.start();
        }
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);

        /************************************ 获取帧之间的间隔时间 ************************************/
        long stampTime;
        mAudioExtractor.readSampleData(byteBuffer, 0);
        if (mAudioExtractor.getSampleFlags() == mAudioExtractor.SAMPLE_FLAG_SYNC) {
            mAudioExtractor.advance();
        }
        mAudioExtractor.readSampleData(byteBuffer, 0);
        long secondTime = mAudioExtractor.getSampleTime();
        mAudioExtractor.advance();
        mAudioExtractor.readSampleData(byteBuffer, 0);
        long thirdTime = mAudioExtractor.getSampleTime();
        stampTime = Math.abs(thirdTime - secondTime);
        Log.e(TAG, "stampTime = " + stampTime);
        /************************************ 获取帧之间的间隔时间 ************************************/

        mAudioExtractor.unselectTrack(mAudioExtractorIndex);
        mAudioExtractor.selectTrack(mAudioExtractorIndex);

        long videoDurationUs = getDuration() * 1000;
        long presentationTimeUs = 0;
        while (true) {
            int readSampleSize = mAudioExtractor.readSampleData(byteBuffer, 0);
//            Log.e(TAG, "readSampleSize = " + readSampleSize);
            if (readSampleSize < 0) {
                /* 音轨比视轨短，就循环写入音轨数据 */
//                mAudioExtractor.advance();
                mAudioExtractor.unselectTrack(mAudioExtractorIndex);
                mAudioExtractor.selectTrack(mAudioExtractorIndex);
                Log.e(TAG, "presentationTimeMs = " + (presentationTimeUs / 1000));
                continue;
            }
            mAudioExtractor.advance();

            bufferInfo.size = readSampleSize;
            bufferInfo.flags = mAudioExtractor.getSampleFlags();
            bufferInfo.offset = 0;
            bufferInfo.presentationTimeUs = (presentationTimeUs += stampTime);

            if (presentationTimeUs < videoDurationUs) {
                mMuxer.writeSampleData(mAudioTrackIndex, byteBuffer, bufferInfo);
            } else {
                break;
            }
        }

        mAudioExtractor.release();
    }

    /**
     * Releases encoder resources.  May be called after partial / failed initialization.
     * 释放资源
     */
    private void releaseEncoder() {
        try {
            if (mEncoder != null) {
                mEncoder.stop();
                mEncoder.release();
                mEncoder = null;
            }
            mEGLZ.release();
            if (ieglEnvironment != null) {
                ieglEnvironment.release();
                ieglEnvironment = null;
            }
            if (mInputSurface != null) {
                mInputSurface.release();
                mInputSurface = null;
            }
            if (mMuxer != null) {
                if (mMuxerStarted) {
                    mMuxer.stop();
                }
                mMuxer.release();
                mMuxer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "e = " + e.toString());
        }
    }
}
