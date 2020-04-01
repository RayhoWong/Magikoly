package com.glt.magikoly.amazons3

import android.content.Context
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.S3ClientOptions
import com.amazonaws.services.s3.model.ObjectMetadata
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.bean.S3ImageInfo
import com.glt.magikoly.constants.ErrorCode.*
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.utils.Machine
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.math.BigInteger
import java.nio.channels.FileChannel
import java.security.MessageDigest


object AmazonS3Manger {

    private const val S3_BUCKET = "face-s3-data"
    private const val S3_REGION = "us-west-1"
    private const val IDENTITY_POOL_ID = "us-west-2:ddbac67d-ad75-4af4-a32a-2851740bc461"
    private const val IDENTITY_POOL_REGION = "us-west-2"
    private const val BASE_SERVER_URL = "http://faces3cdn.magikoly.com/"

    private val keyCreator = KeyCreator()
    private var credProvider: CognitoCachingCredentialsProvider? = null
    private var s3Client: AmazonS3Client? = null
    private var transferUtility: TransferUtility

    init {
        val context: Context = FaceAppState.getContext()
        transferUtility = TransferUtility.builder().context(context).s3Client(getS3Client(context)).build()
    }

    private fun getCredProvider(context: Context): CognitoCachingCredentialsProvider {
        if (credProvider == null) {
            credProvider = CognitoCachingCredentialsProvider(
                context.applicationContext,
                IDENTITY_POOL_ID,
                Regions.fromName(IDENTITY_POOL_REGION)
            )
        }
        return credProvider!!
    }

    private fun getS3Client(context: Context): AmazonS3Client {
        if (s3Client == null) {
            s3Client = AmazonS3Client(getCredProvider(context.applicationContext),
                    Region.getRegion(Regions.fromName(S3_REGION)))
            s3Client?.setS3ClientOptions(
                    S3ClientOptions.builder().setAccelerateModeEnabled(true).build())
        }
        return s3Client!!
    }

    fun uploadImage(uploadImageInfo: UploadImageInfo, listener: UploadListener?) {
        if (!Machine.isNetworkOK(FaceAppState.getContext())) {
            listener?.onUploadError(AMAZON_UPLOAD_FAIL_NETWORK_ERROR)
            return
        }
        if (uploadImageInfo.file == null || uploadImageInfo.file.isDirectory || !uploadImageInfo.file.exists()) {
            listener?.onUploadError(AMAZON_UPLOAD_FAIL_FILE_NO_EXISTS)
            return
        }
        val amazonKey = createFaceAmazonKey(uploadImageInfo.type)
        val eTag = getMd5ByFile(uploadImageInfo.file)
        val metadata = ObjectMetadata()
        metadata.contentType = "image/jpeg"
        val startTime = System.currentTimeMillis()
        val uploadObserver = transferUtility.upload(S3_BUCKET, amazonKey, uploadImageInfo.file, metadata)
        uploadObserver.setTransferListener(object : TransferListener {

            private var hasHandleError = false

            override fun onStateChanged(id: Int, state: TransferState) {
                if (TransferState.COMPLETED === state) {
                    BaseSeq103OperationStatistic.uploadSqe103StatisticData(Math.round(
                            (System.currentTimeMillis() - startTime) / 1000f).toString(),
                            Statistic103Constant.PHOTO_UPLOAD, "", "1", "")
                    val imageInfo = S3ImageInfo()
                    imageInfo.key = amazonKey
                    imageInfo.etag = eTag
                    imageInfo.imageWidth = uploadImageInfo.width
                    imageInfo.imageHeight = uploadImageInfo.height
                    listener?.onUploadCompleted(imageInfo, BASE_SERVER_URL + amazonKey)
                } else if (TransferState.FAILED == state) {
                    if (!hasHandleError) {
                        BaseSeq103OperationStatistic.uploadSqe103StatisticData(Math.round(
                                (System.currentTimeMillis() - startTime) / 1000f).toString(),
                                Statistic103Constant.PHOTO_UPLOAD, "", "2",
                                AMAZON_UPLOAD_FAIL_AMAZON.toString())
                        listener?.onUploadError(AMAZON_UPLOAD_FAIL_AMAZON)
                        hasHandleError = true
                    }
                }
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                val percentDoneF = bytesCurrent.toFloat() / bytesTotal.toFloat() * 100
                val percentDone = percentDoneF.toInt()
                listener?.onUploadProgress(percentDone)
            }

            override fun onError(id: Int, ex: Exception) {
                ex.printStackTrace()
                if (!hasHandleError) {
                    BaseSeq103OperationStatistic.uploadSqe103StatisticData(Math.round(
                            (System.currentTimeMillis() - startTime) / 1000f).toString(),
                            Statistic103Constant.PHOTO_UPLOAD, "", "2",
                            AMAZON_UPLOAD_FAIL_AMAZON.toString())
                    listener?.onUploadError(AMAZON_UPLOAD_FAIL_AMAZON)
                    hasHandleError = true
                }
            }

        })
    }

    /**
     * 获取文件的Md5值
     *
     * @param file
     * @return
     */
    private fun getMd5ByFile(file: File): String {
        var value = ""
        var inputStream: FileInputStream? = null
        try {
            inputStream = FileInputStream(file)
            val byteBuffer = inputStream.channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length())
            val md5 = MessageDigest.getInstance("MD5")
            md5.update(byteBuffer)
            val bi = BigInteger(1, md5.digest())
            value = bi.toString(16)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        return value
    }

    /**
     * 生成Key
     * @param type
     * @return
     */
    private fun createFaceAmazonKey(type: Int): String {
        return keyCreator.createAmazonKey(type)
    }

    interface UploadListener {
        fun onUploadCompleted(imageInfo: S3ImageInfo, imageUrl: String)
        fun onUploadProgress(percent: Int)
        fun onUploadError(errorCode: Int)
    }
}
