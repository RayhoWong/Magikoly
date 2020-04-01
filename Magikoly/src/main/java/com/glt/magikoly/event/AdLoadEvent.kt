package com.glt.magikoly.event

import com.glt.magikoly.ad.inner.InnerAdController

class AdLoadEvent(val adBean: InnerAdController.AdBean) : BaseEvent() {
}