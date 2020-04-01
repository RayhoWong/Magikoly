package com.glt.magikoly.download

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.glt.magikoly.utils.Logcat
import com.glt.magikoly.utils.ToastUtils
import magikoly.magiccamera.R
import kotlinx.android.synthetic.main.activity_test_download_activity.*

class TestDownloadActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "TestDownload"
    }

    private val url1 = "https://download.apkpure.com/b/apk/Y29tLm5jc29mdC5saW5lYWdlbTE5XzIxOF85NTJkNTAw?_fn=66as64uI7KeATV92MS40LjI3YV9hcGtwdXJlLmNvbS5hcGs&k=a261692eabde9f88ed84e536ca8f00895d3a9b1e&as=962dda648dea09b149be1f73709c5c695d37f896&_p=Y29tLm5jc29mdC5saW5lYWdlbTE5&c=2%7CGAME_ROLE_PLAYING%7CZGV2PU5DU09GVCUyMENvcnBvcmF0aW9uJnQ9YXBrJnM9NTMxNTIxOTUmdm49MS40LjI3YSZ2Yz0yMTg&hot=1"
    private val url2 = "https://download.apkpure.com/b/apk/Y29tLmxpbGl0aGdhbWUuaGdhbWUuZ3BfMjk2MF8xMjMzYmI5Mw?_fn=QUZLIEFyZW5hX3YxLjIxLjAyX2Fwa3B1cmUuY29tLmFwaw&k=af982cfc9278422b804879ab204ba57d5d3aa0c6&as=20d36182f290c9de23b05f56ac6f96a35d37fe3e&_p=Y29tLmxpbGl0aGdhbWUuaGdhbWUuZ3A&c=2%7CGAME_ROLE_PLAYING%7CZGV2PUxpbGl0aCUyMCUyMEdhbWVzJnQ9YXBrJnM9MTA2ODk0OTA0JnZuPTEuMjEuMDImdmM9Mjk2MA&hot=1"
    private val url3 = "https://download.apkpure.com/b/apk/anAuZ3VuZ2hvLnBhZFJhZGFyXzgwM18zYmI3ZWNlMg?_fn=UHV6emxlIERyYWdvbnMgUmFkYXJfdjMuMS42X2Fwa3B1cmUuY29tLmFwaw&k=1df881f95d016b8a769162e76b06bcbf5d3aa0e7&as=87fe254a1d5698c36541febd45e74ed35d37fe5f&_p=anAuZ3VuZ2hvLnBhZFJhZGFy&c=2%7CGAME_ADVENTURE%7CZGV2PUd1bmdIb09ubGluZUVudGVydGFpbm1lbnQmdD1hcGsmcz03MzUxOTQ0MSZ2bj0zLjEuNiZ2Yz04MDM&hot=1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_download_activity)

        val instance = DownloadManager.instance

        setProgress(progress1, instance.getDownloadProgress(url1, DownloadManager.sBaseFilePath, "1"))
        val downloadStatus1 = instance.getDownloadStatus(url1, DownloadManager.sBaseFilePath, "1")
        setStatus(tv_btn1, downloadStatus1)
        tv_btn1.setOnClickListener {
            configClick(it as TextView, url1, DownloadManager.sBaseFilePath, "1",downloadListener1)
        }
        if(downloadStatus1 == DownloadStatus.STATUS_DOWNLOADING){
            instance.setDownloadListener(url1,DownloadManager.sBaseFilePath, "1",downloadListener1)
        }



        setProgress(progress2, instance.getDownloadProgress(url2, DownloadManager.sBaseFilePath, "2"))
        val downloadStatus2 = instance.getDownloadStatus(url2, DownloadManager.sBaseFilePath, "2")
        setStatus(tv_btn2, downloadStatus2)
        tv_btn2.setOnClickListener {
            configClick(it as TextView, url2, DownloadManager.sBaseFilePath, "2",downloadListener2)
        }
        if(downloadStatus2 == DownloadStatus.STATUS_DOWNLOADING){
            instance.setDownloadListener(url2, DownloadManager.sBaseFilePath, "2",downloadListener2)
        }


        setProgress(progress3, instance.getDownloadProgress(url3, DownloadManager.sBaseFilePath, "3"))
        val downloadStatus3 = instance.getDownloadStatus(url3, DownloadManager.sBaseFilePath, "3")
        setStatus(tv_btn3, downloadStatus3)
        tv_btn3.setOnClickListener {
            configClick(it as TextView, url3, DownloadManager.sBaseFilePath, "3",downloadListener3)
        }
        if(downloadStatus3 == DownloadStatus.STATUS_DOWNLOADING){
            instance.setDownloadListener(url3,DownloadManager.sBaseFilePath, "3",downloadListener3)
        }


        tv_clear.setOnClickListener {

            DownloadManager.instance.cancelDownload(url3,DownloadManager.sBaseFilePath, "3")
            DownloadManager.instance.cancelDownload(url2,DownloadManager.sBaseFilePath, "2")
            DownloadManager.instance.cancelDownload(url1,DownloadManager.sBaseFilePath, "1")

        }

        tv_remove.setOnClickListener {

            DownloadManager.instance.removeDownloadListener(downloadListener1)

        }

        instance.setDownloadGroupListener(object :DownloadListener{
            override fun pending(task: DownloadTask) {
                Logcat.e(TAG, "pending " + task.id)
            }

            override fun taskStart(task: DownloadTask) {
                Logcat.e(TAG, "taskStart " + task.id)
            }

            override fun connectStart(task: DownloadTask) {
                Logcat.e(TAG, "connectStart " + task.id)
            }

            override fun progress(task: DownloadTask) {
                Logcat.e(TAG, "progress " + task.id)
            }

            override fun completed(task: DownloadTask) {
                Logcat.e(TAG, "completed " + task.id)
            }

            override fun paused(task: DownloadTask) {
                Logcat.e(TAG, "paused " + task.id)
            }

            override fun error(task: DownloadTask) {
                Logcat.e(TAG, "error " + task.id)
            }

        })
    }

    private fun configClick(tv: TextView, url: String, baseFilePath: String, fileName: String,downloadListener: DownloadListener) {
        when (tv.text.toString()) {
            "gone" -> {
                DownloadManager.instance.startDownload(url,baseFilePath,fileName,downloadListener)
                tv.text = "downloading"
            }
            "completed" -> {
                ToastUtils.showToast("completed",Toast.LENGTH_LONG)
            }
            "downloading" -> {
                DownloadManager.instance.pauseDownload(url, baseFilePath, fileName)
                tv.text = "pauseIng"
            }
            "error" -> {
                DownloadManager.instance.startDownload(url,baseFilePath,fileName,downloadListener)
                tv.text = "downloading"
            }
            "paused" -> {
                DownloadManager.instance.startDownload(url,baseFilePath,fileName,downloadListener)
                tv.text = "downloading"
            }

        }


    }

    private fun setStatus(tv: TextView, downloadStatus: Int) {
        if (downloadStatus == DownloadStatus.STATUS_NONE) {
            tv.text = "gone"
        } else if (downloadStatus == DownloadStatus.STATUS_COMPLETED) {
            tv.text = "completed"
        } else if (downloadStatus == DownloadStatus.STATUS_DOWNLOADING) {
            tv.text = "downloading"
        } else if (downloadStatus == DownloadStatus.STATUS_ERROR) {
            tv.text = "error"
        } else if (downloadStatus == DownloadStatus.STATUS_PAUSED) {
            tv.text = "paused"
        }
    }

    private fun setProgress(progress: ProgressBar, downloadTask: DownloadTask) {
        progress.max = 100
        if (downloadTask.totalLength == 0L) {
            progress.progress = 0
        } else {
            progress.progress = (downloadTask.currentProgress * 100f / downloadTask.totalLength).toInt()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        DownloadManager.instance.removeGroupAndUnderGroupListener()
    }

    private val downloadListener1 = object :DownloadListener{
        override fun pending(task: DownloadTask) {
            tv_btn1.text = "downloading"
            Logcat.d(TAG, "downloadListener1 pending")
        }

        override fun taskStart(task: DownloadTask) {
            tv_btn1.text = "downloading"
            Logcat.d(TAG, "downloadListener1 taskStart")
        }

        override fun connectStart(task: DownloadTask) {
            tv_btn1.text = "downloading"
            setProgress(progress1,task)
            Logcat.d(TAG, "downloadListener1 connectStart")
        }

        override fun progress(task: DownloadTask) {
            tv_btn1.text = "downloading"
            setProgress(progress1,task)
            Logcat.d(TAG, "downloadListener1 progress")
        }

        override fun completed(task: DownloadTask) {
            tv_btn1.text = "completed"
            Logcat.d(TAG, "downloadListener1 completed")
        }

        override fun paused(task: DownloadTask) {
            tv_btn1.text = "paused"
            Logcat.d(TAG, "downloadListener1 paused")
        }

        override fun error(task: DownloadTask) {
            tv_btn1.text = "error"
            Logcat.d(TAG, "downloadListener1 error")
        }
    }

    private val downloadListener2 = object :DownloadListener{
        override fun pending(task: DownloadTask) {
            tv_btn2.text = "downloading"
            Logcat.d(TAG, "downloadListener2 pending")
        }

        override fun taskStart(task: DownloadTask) {
            tv_btn2.text = "downloading"
            Logcat.d(TAG, "downloadListener2 taskStart")
        }

        override fun connectStart(task: DownloadTask) {
            tv_btn2.text = "downloading"
            setProgress(progress2,task)
            Logcat.d(TAG, "downloadListener2 connectStart")
        }

        override fun progress(task: DownloadTask) {
            tv_btn2.text = "downloading"
            setProgress(progress2,task)
            Logcat.d(TAG, "downloadListener2 progress")
        }

        override fun completed(task: DownloadTask) {
            tv_btn2.text = "completed"
            Logcat.d(TAG, "downloadListener2 completed")
        }

        override fun paused(task: DownloadTask) {
            tv_btn2.text = "paused"
            Logcat.d(TAG, "downloadListener2 paused")
        }

        override fun error(task: DownloadTask) {
            tv_btn2.text = "error"
            Logcat.d(TAG, "downloadListener2 error")
        }
    }

    private val downloadListener3 = object :DownloadListener{
        override fun pending(task: DownloadTask) {
            tv_btn3.text = "downloading"
            Logcat.d(TAG, "downloadListener3 pending")
        }

        override fun taskStart(task: DownloadTask) {
            tv_btn3.text = "downloading"
            Logcat.d(TAG, "downloadListener3 taskStart")
        }

        override fun connectStart(task: DownloadTask) {
            tv_btn3.text = "downloading"
            setProgress(progress3,task)
            Logcat.d(TAG, "downloadListener3 connectStart")
        }

        override fun progress(task: DownloadTask) {
            tv_btn3.text = "downloading"
            setProgress(progress3,task)
            Logcat.d(TAG, "downloadListener3 progress")
        }

        override fun completed(task: DownloadTask) {
            tv_btn3.text = "completed"
            Logcat.d(TAG, "downloadListener3 completed")
        }

        override fun paused(task: DownloadTask) {
            tv_btn3.text = "paused"
            Logcat.d(TAG, "downloadListener3 paused")
        }

        override fun error(task: DownloadTask) {
            tv_btn3.text = "error"
            Logcat.d(TAG, "downloadListener3 error")
        }
    }


}
