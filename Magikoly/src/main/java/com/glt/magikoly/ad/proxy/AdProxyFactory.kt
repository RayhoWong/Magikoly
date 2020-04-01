package com.glt.magikoly.ad.proxy


object AdProxyFactory {
    private const val AD_APPLOVIN = 50
    private const val AD_FB_NATIVE = 2

    fun getAdProxy(type: Int, ad: String): AbsAdProxy<*> {
        when (type) {
            AD_APPLOVIN -> return AppLovinInterstitialProxy(ad)
        }
        return NullAbsAdProxy(ad)
    }
}
