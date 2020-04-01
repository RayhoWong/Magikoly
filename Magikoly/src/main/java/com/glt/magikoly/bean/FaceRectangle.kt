package com.glt.magikoly.bean

class FaceRectangle {
    /**
     * 1 top  Integer	非空	人脸矩形框左上角像素点的纵坐标
     * 2 left	Integer	非空	人脸矩形框左上角像素点的横坐标
     * 3 width	Integer	非空	人脸矩形框的宽度
     * 4 height	Integer	非空	人脸矩形框的高度
     */
    var top: Int = 0
    var left: Int = 0
    var width: Int = 0
    var height: Int = 0

    constructor(){}

    constructor(left: Int, top: Int, width: Int, height: Int){
        this.left = left
        this.top = top
        this.width = width
        this.height = height
    }

//    constructor(left: Int, top: Int, right: Int, bottom: Int) {
//        this.left = left
//        this.top = top
//        this.width = right - left
//        this.height = bottom - top
//    }
}