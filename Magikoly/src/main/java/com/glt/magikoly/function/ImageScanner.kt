package com.glt.magikoly.function

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore
import android.text.TextUtils
import com.glt.magikoly.FaceAppState
import com.glt.magikoly.bean.FaceInfo
import com.glt.magikoly.bean.S3ImageInfo
import com.glt.magikoly.bean.net.EthnicityReportDTO
import com.glt.magikoly.data.operator.ImageInfoDataOperator
import com.glt.magikoly.data.table.ImageInfoTable
import com.glt.magikoly.ext.runAsyncInSingleThread
import com.glt.magikoly.ext.runMain
import com.glt.magikoly.function.facesdk.FaceSdkProxy
import com.glt.magikoly.function.main.album.ImageBean
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic
import com.glt.magikoly.statistic.Statistic103Constant
import com.glt.magikoly.subscribe.billing.BillingStatusManager
import com.glt.magikoly.utils.Duration
import com.glt.magikoly.utils.FileUtils
import com.glt.magikoly.utils.Logcat
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import magikoly.magiccamera.R
import java.io.File


object ImageScanner {

    const val TAG = "ImageScanner"
    private const val QUERY_LIMIT = 1000
    const val ACCEPT_FACE_AREA_PERCENT = 0.05f

    val faceImageList = ArrayList<FaceImageInfo>()
    private val faceImageSecondaryList = ArrayList<FaceImageInfo>()
    var isFaceImagesScanStarted = false
    var isFaceImagesScanFinish = false
    var isPendingIncrementScan = false
    private var scanTask: ScanTask? = null
    var demoListSize = 0
    private var imageLatestDate = 0L
    private val listeners: ArrayList<IScanListener> = ArrayList()
    private var imageDataOperator:ImageInfoDataOperator? = null

    fun getLocalImageFromMediaStore(): List<ImageBean> {
        var localList = ArrayList<ImageBean>()

        val cursor = FaceAppState.getContext().contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID),
                "${MediaStore.Images.Media.MIME_TYPE}=? or ${MediaStore.Images.Media.MIME_TYPE}=? and ${MediaStore.Images.Media.DATE_MODIFIED}<?",
                arrayOf("image/jpeg", "image/png"),
                MediaStore.Images.Media.DEFAULT_SORT_ORDER
        )

        cursor?.use { cursor ->
            while (cursor.moveToNext()) {
                getImageBeanFromCursor(cursor)?.apply { localList.add(this) }
            }
        }
        return localList.filter { FileUtils.isValidFile(it.mPath, ReportNames.FACE_APP) }
    }

    private fun getImageBeanFromCursor(cursor: Cursor): ImageBean? {
        val id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID))
        val path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
        if (!TextUtils.isEmpty(path)) {
            return ImageBean(id, path)
        }
        return null
    }

    fun registerScanListener(listener: IScanListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun unregisterScanListener(listener: IScanListener) {
        listeners.remove(listener)
    }

    fun startScanFaceImages(context: Context, reload: Boolean) {
        if (isFaceImagesScanFinish && !reload) {
            return
        }
        if (imageDataOperator == null) {
            imageDataOperator = ImageInfoDataOperator(context)
        }
        if (!reload) {
            imageLatestDate = 0L
        }
        scanTask?.cancel()
        isFaceImagesScanStarted = true
        isFaceImagesScanFinish = false
        scanTask = ScanTask(context, reload).apply { start() }
    }

    fun startScanIncrementFaceImages(context: Context) {
        isPendingIncrementScan = false
        if (isFaceImagesScanFinish) {
            isFaceImagesScanStarted = true
            isFaceImagesScanFinish = false
            scanTask = ScanTask(context, false).apply {
                isIncrementScan = true
                start()
            }
        } else {
            isPendingIncrementScan = true
        }
    }

    fun initDemoData(): ArrayList<FaceImageInfo> {
        val demoList = ArrayList<FaceImageInfo>()

        //------------------demo1---------------------
        var ethnicityDTO = EthnicityReportDTO()
        ethnicityDTO.apply {
            asianScore = 68.64
            blackScore = 3.32
            caucasianScore = 9.16
            hispanicOrlatinoScore = 8.05
            middleEasternOrNorthAfrican = 8.75
            otherScore = 2.08
        }
        var faceInfo = FaceInfo()
        faceInfo.apply {
            top = 98
            left = 30
            width = 159
            height = 159
            gender = "F"
            age = 21
            ethnicity = 2
            glassFlag = false
        }
        var imageInfo = S3ImageInfo()
        imageInfo.apply {
            etag = "270500525a075bc13743e8a58532c6a3"
            key = "image/face/20190430/55f14c3682d2ed23/1556614076274OjXQuzAF.jpg"
            imageWidth = 220
            imageHeight = 298
        }
        var faceImageInfo = DemoFaceImageInfo(R.drawable.demo3, R.drawable.demo3_old,
                R.drawable.demo3_gender, R.drawable.demo3_child, "demo3.landmark", "019", ethnicityDTO, faceInfo, imageInfo)
        faceImageInfo.isFaceAreaAccepted = true
        demoList.add(faceImageInfo)

        //------------------demo2---------------------
        ethnicityDTO = EthnicityReportDTO()
        ethnicityDTO.apply {
            asianScore = 11.43
            blackScore = 11.76
            caucasianScore = 34.38
            hispanicOrlatinoScore = 19.27
            middleEasternOrNorthAfrican = 18.03
            otherScore = 5.13
        }
        faceInfo = FaceInfo()
        faceInfo.apply {
            top = 101
            left = 31
            width = 158
            height = 158
            gender = "M"
            age = 27
            ethnicity = 5
            glassFlag = false
        }
        imageInfo = S3ImageInfo()
        imageInfo.apply {
            etag = "e12cd48ef5345ae1a5b9567f08197e95"
            key = "image/face/20190430/55f14c3682d2ed23/1556613961838HBtU3MFd.jpg"
            imageWidth = 221
            imageHeight = 302
        }
        faceImageInfo = DemoFaceImageInfo(R.drawable.demo2, R.drawable.demo2_old,
                R.drawable.demo2_gender, R.drawable.demo2_child, "demo2.landmark", "002", ethnicityDTO, faceInfo, imageInfo)
        faceImageInfo.isFaceAreaAccepted = true
        demoList.add(faceImageInfo)

        //------------------demo3---------------------
        ethnicityDTO = EthnicityReportDTO()
        ethnicityDTO.apply {
            asianScore = 59.43
            blackScore = 9.94
            caucasianScore = 8.13
            hispanicOrlatinoScore = 8.01
            middleEasternOrNorthAfrican = 9.04
            otherScore = 5.45
        }
        faceInfo = FaceInfo()
        faceInfo.apply {
            top = 98
            left = 30
            width = 157
            height = 157
            gender = "F"
            age = 24
            ethnicity = 1
            glassFlag = false
        }
        imageInfo = S3ImageInfo()
        imageInfo.apply {
            etag = "2ce13185f9ac9e81a243f8e788edb45c"
            key = "image/face/20190430/55f14c3682d2ed23/1556613754792JFr7QWep.jpg"
            imageWidth = 220
            imageHeight = 297
        }
        faceImageInfo = DemoFaceImageInfo(R.drawable.demo1, R.drawable.demo1_old,
                R.drawable.demo1_gender, R.drawable.demo1_child, "demo1.landmark", "008", ethnicityDTO, faceInfo, imageInfo)
        faceImageInfo.isFaceAreaAccepted = true
        demoList.add(faceImageInfo)

        return demoList
    }

    private class Result {
        var hasMore = true
        var nextStartIndex = 0

        constructor(hasMore: Boolean, nextIndex: Int) {
            this.hasMore = hasMore
            this.nextStartIndex = nextIndex
        }
    }

    private class ScanTask(val context: Context, val reload: Boolean) : Runnable {

        var isCanceled = false
        var isIncrementScan = false
        private val lock = Object()
        private var faceImageCount = 0
        fun start() {
            runAsyncInSingleThread {
                Duration.setStart("ScanTask")
                run()
            }
        }

        fun cancel() {
            isCanceled = true
        }

        override fun run() {
            Logcat.i(TAG, "ScanTask start: ${this}")
            val startTime = System.currentTimeMillis()
            var result: Result? = null
            Duration.setStart("scanImage")
            if (!isIncrementScan) {
                if (!reload) {
                    faceImageList.clear()
                    if (!BillingStatusManager.getInstance().isVIP()) {
                        val demoList = initDemoData()
                        faceImageList.addAll(demoList)
                        demoListSize = demoList.size
                    }
                } else {
                    isIncrementScan = true
                }
                faceImageSecondaryList.clear()
                if (!isIncrementScan) {
                    runMain {
                        listeners.forEach { listener ->
                            listener.onFaceImagesLoadStart(faceImageList)
                        }
                    }
                    imageDataOperator?.updateAllImageInvalid()
                }
            }
            do {
                result = doFilterFaceImages(context, result?.nextStartIndex ?: 0)
            } while (result!!.hasMore && !isCanceled)
            if (!isCanceled && reload) {
                filterExistFiles()
            }
            imageDataOperator?.deleteAllInvalidImages()
            Duration.logDuration("scanImage")
            if (!isCanceled) {
                runMain {
                    isFaceImagesScanFinish = true
                    if (reload || !isIncrementScan) {
                        val duration = Math.round(
                                (System.currentTimeMillis() - startTime) / 1000.toDouble())
                        listeners.forEach { listener ->
                            if (faceImageList.size <= demoListSize && faceImageSecondaryList.isEmpty()) {
                                listener.onAllFaceImagesLoadFinish(faceImageList, true)
                                BaseSeq103OperationStatistic.uploadSqe103StatisticData("0",
                                        Statistic103Constant.FEATURED_SHOW, "", duration.toString())
                            } else {
                                var forceRefresh = false
                                if (faceImageList.size <= demoListSize) {
                                    if (faceImageSecondaryList.isNotEmpty()) {
                                        faceImageList.addAll(faceImageSecondaryList)
                                    }
                                    forceRefresh = true
                                }
                                listener.onAllFaceImagesLoadFinish(faceImageList, forceRefresh)
                                BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                                        faceImageList.size.toString(),
                                        Statistic103Constant.FEATURED_SHOW,
                                        "", duration.toString())
                            }
                        }
                    }
                    if (isPendingIncrementScan) {
                        startScanIncrementFaceImages(context)
                    }
                }
            }
            Logcat.i(TAG, "ScanTask finish: ${this} - Duration: ${Duration.getDuration(
                    "ScanTask")} - Face Image count: $faceImageCount")
        }


        private fun doFilterFaceImages(context: Context, startIndex: Int): Result {
            val imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val imageProjection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media.ORIENTATION)
            val cursor = context.contentResolver.query(imageUri, imageProjection,
                    "(${MediaStore.Images.Media.MIME_TYPE}=? or ${MediaStore.Images.Media.MIME_TYPE}=?) and ${MediaStore.Images.Media.DATE_ADDED}>?",
                    arrayOf("image/jpeg", "image/png", imageLatestDate.toString()),
                    MediaStore.Images.Media.DATE_ADDED + " desc limit $QUERY_LIMIT offset $startIndex")
            var nextStartIndex = startIndex
            if (cursor != null && cursor.count > 0) {
                cursor.use { cursor ->
                    Logcat.i(TAG, "cursor count: ${cursor.count}")
                    while (cursor.moveToNext() && !isCanceled) {
                        if (startIndex == 0 && cursor.isFirst) {
                            imageLatestDate = cursor.getLong(
                                    cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED))
                        }
                        nextStartIndex++
                        val imgId = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID))
                        val origPath = cursor.getString(
                                cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                        if (FileUtils.isValidFile(origPath, ReportNames.FACE_APP)) {
                            var faceImageInfo = imageDataOperator?.getImageInfo(imgId, origPath)
                            if (faceImageInfo != null) {
                                if (faceImageInfo.facePercent > ACCEPT_FACE_AREA_PERCENT) {
                                    faceImageInfo.isFaceAreaAccepted = true
                                }
                                imageDataOperator?.updateImageValid(imgId, true)
                                Logcat.i(TAG, "updateImageValid: $imgId  $origPath")
                                runMain {
                                    notifyRefresh(faceImageInfo!!)
                                }
                            } else {
                                val options = BitmapFactory.Options()
                                options.inPreferredConfig = Bitmap.Config.RGB_565
                                options.inJustDecodeBounds = true
                                BitmapFactory.decodeFile(origPath, options)
                                var longBound = 0
                                var shortBound = 0
                                if (options.outWidth < options.outHeight) {
                                    longBound = options.outHeight
                                    shortBound = options.outWidth
                                } else {
                                    longBound = options.outWidth
                                    shortBound = options.outHeight
                                }
                                val outWidth = options.outWidth
                                val outHeight = options.outHeight
                                if (longBound > 1280f && shortBound > 720f) {
                                    var sample = Math.max(Math.floor(longBound / 1280.toDouble()),
                                            Math.floor(shortBound / 720.toDouble()))
                                    var exponent = 0.toDouble()
                                    while (sample >= Math.pow(2.toDouble(), exponent)) {
                                        exponent++
                                    }
                                    options.inSampleSize = Math.pow(2.toDouble(),
                                            Math.max(exponent - 1, 0.toDouble())).toInt()
                                }
                                options.inJustDecodeBounds = false
                                val bitmap = BitmapFactory.decodeFile(origPath, options)
                                if (isBitmapValid(bitmap)) {
                                    val orientation = cursor.getInt(
                                            cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION))
                                    val bitmapRect = Rect(0,0,bitmap.width, bitmap.height)
                                    var needWait = true
                                    FaceSdkProxy.detectImage(true, true, bitmap, orientation,
                                            { faces ->
                                                faceImageInfo = createFaceImageInfo(imgId, origPath,
                                                        bitmapRect, faces)
                                                imageDataOperator?.addImageInfo(faceImageInfo!!)
                                                Logcat.i(TAG,
                                                        "addImageInfo: $imgId  $origPath $outWidth*$outHeight ${options.inSampleSize} ${bitmap?.width}*${bitmap?.height}, ${faceImageInfo?.faceCount} ${faceImageInfo?.facePercent}")
                                                notifyRefresh(faceImageInfo!!)
                                                synchronized(lock) {
                                                    needWait = false
                                                    lock.notifyAll()
                                                }
                                            },
                                            {
                                                Logcat.i(TAG, "Face detect failed: $origPath")
                                                synchronized(lock) {
                                                    needWait = false
                                                    lock.notifyAll()
                                                }
                                            })
                                    bitmap.recycle()
                                    synchronized(lock) {
                                        if (needWait) {
                                            try {
                                                lock.wait()
                                            } catch (e: Throwable) {

                                            }
                                        }
                                    }
                                    Logcat.i(TAG, "Thread continue: $origPath")
                                }
                            }
                        } else {
                            Logcat.i(TAG, "Invalid file: $origPath")
                        }
                    }
                }
                return Result(true, nextStartIndex)
            } else {
                cursor?.close()
                return Result(false, nextStartIndex)
            }
        }

        private fun isBitmapValid(bitmap: Bitmap?):Boolean {
            return bitmap != null && bitmap.width > 100 && bitmap.height > 100
        }

        private fun createFaceImageInfo(imgId: Int, path: String, bitmapRect: Rect,
                                        faces: List<FirebaseVisionFace>?): FaceImageInfo? {
            if (faces != null && faces.isNotEmpty()) {
                var isAccept = false
                var facePercent = 0f
                faces.forEach { face ->
                    val bitmapArea = (bitmapRect.width() * bitmapRect.height())
                    val rectArea = face.boundingBox.width() * face.boundingBox.height()
                    val percent = 1.0f * rectArea / bitmapArea
                    if (percent > facePercent) {
                        facePercent = percent
                    }
                    if (percent > ACCEPT_FACE_AREA_PERCENT) {
                        isAccept = true
                        return@forEach
                    }
                }
                return FaceImageInfo(imgId, path, faces.size, facePercent).apply {
                    isFaceAreaAccepted = isAccept
                }
            }
            return FaceImageInfo(imgId, path, 0, 0f)
        }

        private fun notifyRefresh(faceImageInfo: FaceImageInfo) {
            if (!isCanceled) {
                if (isIncrementScan) {
                    if (faceImageInfo.faceCount == 1 && faceImageInfo.isFaceAreaAccepted) {
                        runMain {
                            faceImageList.add(demoListSize, faceImageInfo)
                            faceImageCount++
                            listeners.forEach { listener ->
                                listener.onFaceImageFound(demoListSize, faceImageInfo,
                                        isIncrementScan
                                )
                            }
                        }
                    }
                } else {
                    if (faceImageInfo.faceCount > 0) {
                        if (faceImageInfo.faceCount == 1 && faceImageInfo.isFaceAreaAccepted) {
                            faceImageList.add(faceImageInfo)
                            faceImageCount++
                            listeners.forEach { listener ->
                                listener.onFaceImageFound(faceImageList.size - 1, faceImageInfo,
                                        isIncrementScan)
                            }
                        } else {
                            faceImageSecondaryList.add(faceImageInfo!!)
                        }
                    }
                }
            }
        }


        private fun filterExistFiles() {
            val newList = faceImageList.filter {
                if (!it.isDemo()) {
                    val file = File(it.imagePath)
                    file.exists()
                } else {
                    true
                }
            }
            faceImageList.clear()
            faceImageList.addAll(newList)

//            val removeList = faceImageList.filter {
//                if (!it.isDemo()) {
//                    val file = File(it.imagePath)
//                    !file.exists()
//                } else {
//                    false
//                }
//            }
//            removeList.forEach {
//                val index = faceImageList.indexOf(it)
//                faceImageList.removeAt(index)
//                runMain {
//                    listeners.forEach { listener ->
//                        listener.onFaceImageRemove(index)
//                    }
//                }
//            }
        }

//        private fun detectByFacePlusPlus(imgId: Int, bitmap: Bitmap?, path: String,
//                                         orientation: Int): FaceImageInfo {
//            if (bitmap == null) {
//                return FaceImageInfo(imgId, path, 0, 0f)
//            }
//            val faces = FaceSdkProxy.detectImage(bitmap, orientation)
//            if (faces != null && faces.isNotEmpty()) {
//                var isAccept = false
//                val bitmapArea = bitmap.width * bitmap.height
//                var facePercent = 0f
//                faces.forEach { face ->
//                    val rectArea = face.rect.width() * face.rect.height()
//                    val percent = 1.0f * rectArea / bitmapArea
//                    if (percent > facePercent) {
//                        facePercent = percent
//                    }
//                    if (percent > ACCEPT_FACE_AREA_PERCENT) {
//                        isAccept = true
//                        return@forEach
//                    }
//                }
//                val imageInfo = FaceImageInfo(imgId, path, faces.size, facePercent)
//                imageInfo.isFaceAreaAccepted = isAccept
//                return imageInfo
//            }
//            return FaceImageInfo(imgId, path, 0, 0f)
//        }
    }
}

open class FaceImageInfo {

    var imgId = 0
    var imagePath: String = ""
    var faceCount = 0
    var facePercent = 0f
    var isFaceAreaAccepted = false

    constructor()
    constructor(imgId: Int, imagePath: String, faceCount: Int, facePercent: Float) {
        this.imgId = imgId
        this.imagePath = imagePath
        this.faceCount = faceCount
        this.facePercent = facePercent
    }

    fun toContentValues(): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(ImageInfoTable.IMG_ID, imgId)
        contentValues.put(ImageInfoTable.FACE_COUNT, faceCount)
        contentValues.put(ImageInfoTable.FACE_AREA_PERCENT, facePercent)
        contentValues.put(ImageInfoTable.IS_VALID, 1)
        return contentValues
    }

    open fun isDemo(): Boolean {
        return false
    }
}

class DemoFaceImageInfo :
        FaceImageInfo, Parcelable {
    var oldId = 0
    var genderId = 0
    var childId = 0
    lateinit var landmark: String
    lateinit var animal: String
    var ethnicityReportDTO: EthnicityReportDTO? = null
    var faceInfo: FaceInfo? = null
    var imageInfo: S3ImageInfo? = null

    constructor(demoId: Int, oldId: Int, genderId: Int, childId: Int, landmarkId: String,
                animal: String, ethnicityReportDTO: EthnicityReportDTO,
                faceInfo: FaceInfo, imageInfo: S3ImageInfo) : super(demoId, "demo", 1, 1f) {
        this.oldId = oldId
        this.genderId = genderId
        this.childId = childId
        this.landmark = landmarkId
        this.animal = animal
        this.ethnicityReportDTO = ethnicityReportDTO
        this.faceInfo = faceInfo
        this.imageInfo = imageInfo
    }

    constructor(parcel: Parcel) {
        imgId = parcel.readInt()
        imagePath = parcel.readString()
        faceCount = parcel.readInt()
        facePercent = parcel.readFloat()
        isFaceAreaAccepted = parcel.readInt() == 1
        oldId = parcel.readInt()
        genderId = parcel.readInt()
        childId = parcel.readInt()
        ethnicityReportDTO = parcel.readSerializable() as EthnicityReportDTO
        faceInfo = parcel.readSerializable() as FaceInfo
        imageInfo = parcel.readSerializable() as S3ImageInfo
    }

    override fun isDemo(): Boolean {
        return true
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(imgId)
        parcel.writeString(imagePath)
        parcel.writeInt(faceCount)
        parcel.writeFloat(facePercent)
        parcel.writeInt(if(isFaceAreaAccepted) 1 else 0)
        parcel.writeInt(oldId)
        parcel.writeInt(genderId)
        parcel.writeInt(childId)
        parcel.writeSerializable(ethnicityReportDTO)
        parcel.writeSerializable(faceInfo)
        parcel.writeSerializable(imageInfo)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DemoFaceImageInfo> {
        override fun createFromParcel(parcel: Parcel): DemoFaceImageInfo {
            return DemoFaceImageInfo(parcel)
        }

        override fun newArray(size: Int): Array<DemoFaceImageInfo?> {
            return arrayOfNulls(size)
        }
    }
}

interface IScanListener {
    fun onFaceImagesLoadStart(faceImageList: ArrayList<FaceImageInfo>)
    fun onFaceImageFound(position: Int, faceImageInfo: FaceImageInfo, isIncrement:Boolean)
    fun onFaceImageRemove(position: Int)
    fun onAllFaceImagesLoadFinish(faceImageList: ArrayList<FaceImageInfo>, forceRefresh: Boolean)
}