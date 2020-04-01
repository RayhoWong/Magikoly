package com.glt.magikoly.download;

public class DownloadTask {

    private long totalLength;//byte
    private long currentProgress;//byte
    private int downloadSpeed;//kb
    private int id;
    private String tempFilePath;


    public int getDownloadSpeed() {
        return downloadSpeed;
    }

    public void setDownloadSpeed(int downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    public int getId() {
        return id;
    }

    public String getTempFilePath() {
        return tempFilePath;
    }

    public String getTargetFilePath() {
        return targetFilePath;
    }

    private String targetFilePath;


    public long getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(long totalLength) {
        this.totalLength = totalLength;
    }

    public long getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(long currentProgress) {
        this.currentProgress = currentProgress;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTargetFilePath(String targetFilePath) {
        this.targetFilePath = targetFilePath;
    }

    public void setTempFilePath(String tempFilePath) {
        this.tempFilePath = tempFilePath;
    }
}
