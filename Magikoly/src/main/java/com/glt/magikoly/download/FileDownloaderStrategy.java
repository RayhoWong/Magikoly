package com.glt.magikoly.download;

import android.app.Application;
import android.content.Context;

import com.glt.magikoly.utils.Logcat;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.services.DownloadMgrInitialParams;
import com.liulishuo.filedownloader.util.FileDownloadHelper;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import java.io.File;


public class FileDownloaderStrategy implements DownloadStrategy {

    private static final String TAG = "FileDownloader";

    @Override
    public void init(Application application) {

        try {
            DownloadMgrInitialParams.InitCustomMaker maker = FileDownloader.setupOnApplicationOnCreate(application);

            maker.connectionCountAdapter(new FileDownloadHelper.ConnectionCountAdapter() {
                @Override
                public int determineConnectionCount(int downloadId, String url, String path, long totalLength) {
                    return 1;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void startDownload(Context context, String url, String baseFilePath, String fileName, final DownloadListener listener) {
        final DownloadTask downloadTask = new DownloadTask();


        String path = baseFilePath + File.separator + fileName;

        downloadTask.setTargetFilePath(path);
        downloadTask.setTempFilePath(FileDownloadUtils.getTempPath(path));

        byte status = FileDownloader.getImpl().getStatus(url, path);

        if (FileDownloadStatus.isIng(status)) {
            return;
        }

        int id = FileDownloader.getImpl().create(url).setPath(path).setListener(new FileDownloadListener() {

            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                downloadTask.setTotalLength(totalBytes);
                downloadTask.setCurrentProgress(soFarBytes);
                downloadTask.setDownloadSpeed(task.getSpeed());
                listener.pending(downloadTask);
            }

            @Override
            protected void started(BaseDownloadTask task) {
                super.started(task);
                listener.taskStart(downloadTask);
            }

            @Override
            protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int
                    totalBytes) {
                super.connected(task, etag, isContinue, soFarBytes, totalBytes);
                downloadTask.setTotalLength(totalBytes);
                downloadTask.setCurrentProgress(soFarBytes);
                downloadTask.setDownloadSpeed(task.getSpeed());

                listener.connectStart(downloadTask);
            }

            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                downloadTask.setTotalLength(totalBytes);
                downloadTask.setCurrentProgress(soFarBytes);
                downloadTask.setDownloadSpeed(task.getSpeed());
                listener.progress(downloadTask);
            }

            @Override
            protected void completed(final BaseDownloadTask task) {
                listener.completed(downloadTask);
            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                listener.paused(downloadTask);
            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                listener.error(downloadTask);
            }

            @Override
            protected void warn(BaseDownloadTask task) {
                Logcat.e(TAG, "FileDownloaderStrategy warn");
            }
        }).start();
        downloadTask.setId(id);
    }

    @Override
    public void pauseDownload(int taskId) {
        FileDownloader.getImpl().pause(taskId);
    }

    @Override
    public void cancelDownload(String url, String baseFilePath, String fileName) {
        String path = baseFilePath + File.separator + fileName;
        FileDownloader.getImpl().clear(FileDownloadUtils.generateId(url,path),path);
    }


    @Override
    public int getDownloadStatus(String url, String baseFilePath, String fileName) {
        String path = baseFilePath + File.separator + fileName;

        byte status = FileDownloader.getImpl().getStatus(url, path);

        int result;
        if (FileDownloadStatus.isIng(status)) {
            //downloading
            result = DownloadStatus.STATUS_DOWNLOADING;
        }else if(status == FileDownloadStatus.error){
            result = DownloadStatus.STATUS_ERROR;
        }else if(status == FileDownloadStatus.paused){
            result = DownloadStatus.STATUS_PAUSED;
        }else if(status == FileDownloadStatus.completed){
            result = DownloadStatus.STATUS_COMPLETED;
        }else{
            result = DownloadStatus.STATUS_NONE;
        }

        return result;
    }

    @Override
    public int getDownloadTaskId(String url, String baseFilePath, String fileName) {
        return FileDownloadUtils.generateId(url,baseFilePath + File.separator + fileName);
    }

    @Override
    public DownloadTask getDownloadProgress(String url, String baseFilePath, String fileName) {
        DownloadTask downloadTask = new DownloadTask();
        String path = baseFilePath + File.separator + fileName;
        int id = FileDownloadUtils.generateId(url, path);
        downloadTask.setId(id);
        downloadTask.setCurrentProgress( FileDownloader.getImpl().getSoFar(id));
        downloadTask.setTotalLength(FileDownloader.getImpl().getTotal(id));
        return downloadTask;
    }

    @Override
    public void pauseAll() {
        FileDownloader.getImpl().pauseAll();
    }


}
