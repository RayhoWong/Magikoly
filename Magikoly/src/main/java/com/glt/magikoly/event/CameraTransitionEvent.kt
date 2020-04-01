package com.glt.magikoly.event

class CameraTransitionEvent(val action: Int) : BaseEvent() {

    constructor(action: Int, cameraOpenSuccess: Boolean) : this(action) {
        this.cameraOpenSuccess = cameraOpenSuccess
    }

    var cameraOpenSuccess = true

    companion object {
        const val EVENT_SHOW = 1
        const val EVENT_HIDE = 2
    }
}