package com.glt.magikoly.ad.proxy

class NullAbsAdProxy(adUnit: String) : AbsAdProxy<Any>(adUnit) {

    override fun load() {
        onAdError(ERROR_NULL_AD_LOAD)
    }
}
