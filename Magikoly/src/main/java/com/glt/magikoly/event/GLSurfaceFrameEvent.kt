package com.glt.magikoly.event

class GLSurfaceFrameEvent(val entrance: Int) : BaseEvent() {
    companion object {
        const val CAMERA_ENTRANCE = 1
        const val IMAGE_ENTRANCE = 2

    }

    var initTime: Long = 0
}


