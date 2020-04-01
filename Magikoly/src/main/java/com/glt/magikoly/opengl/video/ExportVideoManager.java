package com.glt.magikoly.opengl.video;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author rayhahah
 * @blog http://rayhahah.com
 * @time 2019/9/27
 * @tips 这个类是Object的子类
 * @fuction
 */
public class ExportVideoManager {

    private Context mContext;
    /**
     * 输出文件路径，带文件名
     */
    private String mOutputPath;
    private ExportVideoConfig.onExportVideoListener mListener;
    // 输出视频的宽度
    private static int mExportVideoWidth = 1920;
    // 输出视频的高度
    private static int mExportVideoHeight = 1080;
    // 输出视频的帧率FPS
    private static int mExportVideoFps = 8;
    // 输出视频的帧数
    private static int mExportFrameCount = 80;

    public ExportVideoManager() {
    }

    private static final String TAG = "ExportVideoService";

    // 输出视频的MIME类型
    private static final String EXPORT_VIDEO_MIME_TYPE = "video/avc";

    // 是否正在忙
    private boolean mIsBusy = false;

    // buffer
    private MediaCodec.BufferInfo mBufferInfo;
    // 编码器
    private MediaCodec mEncoder;
    // 输入的Surface
    private CodecInputSurface mInputSurface;
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

    /**
     * 在init 之前调用
     */
    public void setConfig(ExportVideoConfig exportVideoConfig) {
        mContext = exportVideoConfig.getContext();
        mOutputPath = exportVideoConfig.getOutputPath();
        mExportVideoWidth = exportVideoConfig.getOutputWidth();
        mExportVideoHeight = exportVideoConfig.getOutputHeight();
        mExportVideoFps = exportVideoConfig.getFps();
        mExportFrameCount = (int) (exportVideoConfig.getDuration() * mExportVideoFps);
        mListener = exportVideoConfig.getListener();
    }

    /**
     * 需要先调用setConfig
     * 初始化
     */
    public void init() {
        try {
            initVideoEncoder();
        } catch (Exception e) {
            e.printStackTrace();
            CrashReport.postCatchedException(e);
            if (mListener!=null) {
                mListener.onError(e);
            }
            releaseEncoder();
        }
    }

    /**
     * 停止合并
     */
    public void stopMerge() {
        mIsBusy = false;
    }

    /**
     * 开始合并
     */
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
//                    addAudioTrack();
                    addVideoData();
                    // send end-of-stream to encoder, and drain remaining output
                    drainEncoder(true);
//                    writeAudioData();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    File exportFile = new File(mOutputPath);
                    if (exportFile != null) {
                        exportFile.deleteOnExit();
                    }
                    if (mListener!=null) {
                        mListener.onError(throwable);
                    }
                } finally {
                    releaseEncoder();
                }
                mIsBusy = false;
            }
        }).start();
    }

    private void addVideoData() {
        mInputSurface.makeCurrent();
        mListener.onMakeCurrent(mExportVideoWidth,mExportVideoHeight);
        for (int i = 0; i < mExportFrameCount; i++) {
            Log.e(TAG, "-----------------start encode frame: frameIndex = " + i + "-----------------");
            // mEncoder从缓冲区取数据，然后交给mMuxer编码
            drainEncoder(false);
            // opengl绘制一帧
            generateSurfaceFrame(i);
            // 设置图像，发送给EGL的显示时间
            mInputSurface.setPresentationTime(computePresentationTimeNsec(i));
            // Submit it to the encoder
            mInputSurface.swapBuffers();
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


    private void initVideoEncoder() {
        // 创建一个buffer
        mBufferInfo = new MediaCodec.BufferInfo();

        /* 创建一个视频MediaFormat */
        // 设置MIME类型、宽高
        MediaFormat format = MediaFormat.createVideoFormat(EXPORT_VIDEO_MIME_TYPE,
                mExportVideoWidth, mExportVideoHeight);
        // 设置profile
        // BaseLine基本覆盖Android 4.x及以上版本
        format.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline);
        // 设置profile level(设置了profile，则一定要设置该值，不然报错)
        // L4.1基本覆盖Android 4.x及以上版本
        format.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel41);
        // 设置颜色编码格式
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        // 设置码率
        format.setInteger(MediaFormat.KEY_BIT_RATE, getBitRate(mExportVideoWidth, mExportVideoHeight));
        // 设置帧率FPS
        format.setInteger(MediaFormat.KEY_FRAME_RATE, mExportVideoFps);
        // 设置关键帧时间
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, getKeyFrameInterval(mExportVideoFps, mExportFrameCount));

        /* 创建编码器 */
        try {
            mEncoder = MediaCodec.createEncoderByType(EXPORT_VIDEO_MIME_TYPE);
            mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            mInputSurface = new CodecInputSurface(mEncoder.createInputSurface());
            mListener.onCreate();

            mEncoder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* 创建合成器 */
        try {
            mMuxer = new MediaMuxer(mOutputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mVideoTrackIndex = -1;
        mMuxerStarted = false;
    }

    /**
     * Releases encoder resources.  May be called after partial / failed initialization.
     * 释放资源
     */
    public void releaseEncoder() {
        try {
            if (mEncoder != null) {
                mEncoder.stop();
                mEncoder.release();
                mEncoder = null;
            }

            if (mListener!=null) {
                mListener.release();
                mListener=null;
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

    /**
     * Generates a frame of data using GL commands.
     */
    private void generateSurfaceFrame(int frameIndex) {
        float progress = frameIndex * 100f / mExportFrameCount;
        mListener.onDrawFrame(frameIndex, progress);
    }

    /**
     * Generates the presentation time for frame N, in nanoseconds.
     * 生成对应帧的时间(单位：纳秒。1秒=1000毫秒=1000000微秒=1000000000纳秒)
     */
    private static long computePresentationTimeNsec(int frameIndex) {
        final long ONE_BILLION = 1000000000;
        return frameIndex * (ONE_BILLION / mExportVideoFps);
    }

    private void addAudioTrack() throws Exception {
        mAudioExtractor = new MediaExtractor();
//        mAudioExtractor.setDataSource("/storage/emulated/0/YeAh/test/encoder/count_to_20.m4a");
        mAudioExtractor.setDataSource("/storage/emulated/0/YeAh/test/encoder/count_to_2.m4a");
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

    private long getDuration() {
        return 1000 / mExportVideoFps * mExportFrameCount;
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
        return (int) (width * heigh * 3.2);
    }

}
