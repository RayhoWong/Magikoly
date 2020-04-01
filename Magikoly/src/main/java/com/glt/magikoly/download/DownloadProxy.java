package com.glt.magikoly.download;


import android.app.Application;
import android.content.Context;

public class DownloadProxy {

    private DownloadStrategy mStrategy;

    private static class SingletonHolder {
        private static final DownloadProxy INSTANCE = new DownloadProxy();
    }

    public static DownloadProxy getInstance() {
        if (SingletonHolder.INSTANCE.mStrategy == null) {
            throw new IllegalStateException("please invoke init()");
        }
        return SingletonHolder.INSTANCE;
    }

    public static void init(Application application, DownloadStrategy strategy) {
        SingletonHolder.INSTANCE.mStrategy = strategy;
        strategy.init(application);
    }

    public void startDownload(Context context, String url, String baseFilePath, String fileName, DownloadListener listener) {
        mStrategy.startDownload(context, url, baseFilePath, fileName, listener);
    }

    public void pauseDownload(int taskId) {
        mStrategy.pauseDownload(taskId);
    }

    public void pauseAll() {
        mStrategy.pauseAll();
    }

    public void cancelDownload(String url, String baseFilePath, String fileName) {
        mStrategy.cancelDownload(url, baseFilePath, fileName);
    }

    public int getDownloadStatus(String url, String baseFilePath, String fileName) {
        return mStrategy.getDownloadStatus(url, baseFilePath, fileName);
    }

    public DownloadTask getDownloadProgress(String url, String baseFilePath, String fileName) {
        return mStrategy.getDownloadProgress(url, baseFilePath, fileName);
    }

    public int getDownloadTaskId(String url, String baseFilePath, String fileName) {
        return mStrategy.getDownloadTaskId(url, baseFilePath, fileName);
    }



}
