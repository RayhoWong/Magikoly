package com.glt.magikoly.function.takephoto

import android.hardware.camera2.CameraDevice

class CameraDeviceCompat {
    companion object {
        fun closeCameraDevice(device: CameraDevice) {
            device.close()
        }
    }
}