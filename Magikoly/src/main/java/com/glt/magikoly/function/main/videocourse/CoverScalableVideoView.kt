package com.glt.magikoly.function.main.videocourse

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.support.annotation.RawRes
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.TextureView
import android.widget.FrameLayout
import android.widget.ImageView
import com.glt.magikoly.utils.DrawUtils
import com.yqritc.scalablevideoview.ScalableType
import com.yqritc.scalablevideoview.ScalableVideoView
import java.io.FileDescriptor
import java.io.IOException

class CoverScalableVideoView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), TextureView.SurfaceTextureListener {
    private val scalableVideoView: ScalableVideoView = ScalableVideoView(context)
    private val coverImageView = ImageView(context)
    private lateinit var startClipCircle: Circle
    private lateinit var endClipCircle: Circle
    private var circleFraction = 1f
    private val clipPath = Path()
    private val circleEvaluator = CircleEvaluator()

    init {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        scalableVideoView.surfaceTextureListener = this
        addView(scalableVideoView, params)
        addView(coverImageView, params)
        coverImageView.scaleType = ImageView.ScaleType.FIT_XY
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        startClipCircle = Circle(w * 1.0f / 2, h * 1.0f / 2, (Math.sqrt(Math.pow(w.toDouble(), 2.toDouble()) + Math.pow(h.toDouble(), 2.toDouble())) / 2).toFloat())
        endClipCircle = Circle(w * 1.0f / 2, h * 1.0f / 2, DrawUtils.dip2px(28f).toFloat())
    }

    fun updateClipCircleFraction(fraction: Float) {
        circleFraction = fraction
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean =  true


    override fun dispatchDraw(canvas: Canvas?) {
        clipPath.reset()
        val evaluate = circleEvaluator.evaluate(circleFraction, endClipCircle, startClipCircle)
        clipPath.addCircle(evaluate.x, evaluate.y, evaluate.radius, Path.Direction.CCW)
        canvas?.clipPath(clipPath)
        super.dispatchDraw(canvas)
    }

    private var mCoverBitmap: Bitmap? = null
    fun setCoverBitmap(bitmap: Bitmap?) {
        mCoverBitmap = bitmap
        coverImageView.setImageBitmap(bitmap)
    }

    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
        scalableVideoView.onSurfaceTextureAvailable(surfaceTexture, width, height)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        scalableVideoView.onSurfaceTextureSizeChanged(surface, width, height)
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return scalableVideoView.onSurfaceTextureDestroyed(surface)
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        scalableVideoView.onSurfaceTextureUpdated(surface)
    }

    @Throws(IOException::class)
    fun setRawData(@RawRes id: Int) {
        scalableVideoView.setRawData(id)
    }

    @Throws(IOException::class)
    fun setAssetData(assetName: String) {
        scalableVideoView.setAssetData(assetName)
    }


    @Throws(IOException::class)
    fun setDataSource(path: String) {
        scalableVideoView.setDataSource(path)
    }

    @Throws(IOException::class)
    fun setDataSource(context: Context, uri: Uri,
                      headers: Map<String, String>?) {
        scalableVideoView.setDataSource(context, uri, headers)
    }

    @Throws(IOException::class)
    fun setDataSource(context: Context, uri: Uri) {
        scalableVideoView.setDataSource(context, uri)
    }

    @Throws(IOException::class)
    fun setDataSource(fd: FileDescriptor, offset: Long, length: Long) {
        scalableVideoView.setDataSource(fd, offset, length)
    }

    @Throws(IOException::class)
    fun setDataSource(fd: FileDescriptor) {
        scalableVideoView.setDataSource(fd)
    }

    fun setScalableType(scalableType: ScalableType) {
        scalableVideoView.setScalableType(scalableType)
        coverImageView.imageMatrix = scalableVideoView.getTransform(null)
    }

    @Throws(IOException::class, IllegalStateException::class)
    fun prepare(listener: MediaPlayer.OnPreparedListener?) {
        scalableVideoView.prepare(listener)
    }

    @Throws(IllegalStateException::class)
    fun prepareAsync(listener: MediaPlayer.OnPreparedListener?) {
        scalableVideoView.prepareAsync(listener)
    }

    @Throws(IOException::class, IllegalStateException::class)
    fun prepare() {
        prepare(null)
    }

    @Throws(IllegalStateException::class)
    fun prepareAsync() {
        prepareAsync(null)
    }

    fun setOnErrorListener(listener: MediaPlayer.OnErrorListener?) {
        scalableVideoView.setOnErrorListener(listener)
    }

    fun setOnCompletionListener(listener: MediaPlayer.OnCompletionListener?) {
        scalableVideoView.setOnCompletionListener(listener)
    }

    fun setOnInfoListener(listener: MediaPlayer.OnInfoListener?) {
        scalableVideoView.setOnInfoListener(listener)
    }

    fun getCurrentPosition(): Int {
        return scalableVideoView.currentPosition
    }

    fun getDuration(): Int {
        return scalableVideoView.duration
    }

    fun getVideoHeight(): Int {
        return scalableVideoView.videoHeight
    }

    fun getVideoWidth(): Int {
        return scalableVideoView.videoWidth
    }

    fun isLooping(): Boolean {
        return scalableVideoView.isLooping
    }

    fun isPlaying(): Boolean {
        return scalableVideoView.isPlaying
    }

    fun pause() = scalableVideoView.pause()

    fun seekTo(msec: Int) {
        scalableVideoView.seekTo(msec)
    }

    fun setLooping(looping: Boolean) {
        scalableVideoView.isLooping = looping
    }

    fun setVolume(leftVolume: Float, rightVolume: Float) {
        scalableVideoView.setVolume(leftVolume, rightVolume)
    }

    fun start() {
        scalableVideoView.start()
    }

    fun stop() {
        scalableVideoView.stop()
    }

    fun reset() {
        scalableVideoView.reset()
    }

    fun release() {
        reset()
        scalableVideoView.release()
    }
}