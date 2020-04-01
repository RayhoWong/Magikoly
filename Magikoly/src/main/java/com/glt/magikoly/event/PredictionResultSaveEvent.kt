package com.glt.magikoly.event

/**
 * @desc:
 * @auther:duwei
 * @date:2019/1/21
 */
class PredictionResultSaveEvent(var tabId: Int = -1, var success: Boolean,var showToast : Boolean?= true) : BaseEvent(){
    companion object {
        const val FROM_AGING = 1
        const val FROM_BABY = 2
        const val FROM_ETHNICITY = 3
        const val FROM_GENDER = 4
        const val FROM_TUNING = 5
        const val FROM_ART_FILTER = 6
    }
}
