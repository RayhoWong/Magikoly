package com.xfeng.cameralibrary.listener;

/**
 * 页面监听器
 */
public interface OnPageOperationListener {

    // 打开图库页面
    void onOpenGalleryPage();

    // 打开图片编辑页面
    void onOpenImageEditPage(String path);

    // 打开视频编辑页面
    void onOpenVideoEditPage(String path);


}
