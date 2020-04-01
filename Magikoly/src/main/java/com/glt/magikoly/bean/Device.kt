package com.glt.magikoly.bean

import com.google.gson.annotations.SerializedName

class Device {
    var pkgname: String? = null
    var cversion: Int = 0
    var country: String? = null
    @SerializedName("zone_id")
    var zoneId: String? = null
    var lang: String? = null
    var did: String? = null
    var platform: Int = 1
}
