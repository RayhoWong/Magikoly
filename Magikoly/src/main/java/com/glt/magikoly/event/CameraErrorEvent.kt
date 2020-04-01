package com.glt.magikoly.event

/**
 * @desc:
 * @auther:duwei
 * @date:2019/3/18
 */
class CameraErrorEvent(val code: Int) : BaseEvent() {
    companion object {
        /**
         * 摄像头链接不可用
         */
        const val ERROR_CODE_ACCESS_EXCEPTION = 1
        /**
         * 没有找到摄像头
         */
        const val ERROR_CODE_NO_CAMERA = 2
        /**
         * 一些状态信息出错
         */
        const val ERROR_CODE_STATE_EXCEPTION = 3
        /**
         * 系统API回调出错的问题
         */
        const val ERROR_CODE_CALLBACK_FAILE = 4
    }

}