package com.glt.magikoly.download;

import android.support.annotation.NonNull;

public interface DownloadListener {



    void pending(@NonNull DownloadTask task);

    void taskStart(@NonNull DownloadTask task);

    void connectStart(@NonNull DownloadTask task);

    void progress(@NonNull DownloadTask task);

    void completed(@NonNull DownloadTask task);

    void paused(@NonNull DownloadTask task);

    void error(@NonNull DownloadTask task);
}
