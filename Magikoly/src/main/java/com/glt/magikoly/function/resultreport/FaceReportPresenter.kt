package com.glt.magikoly.function.resultreport

import com.glt.magikoly.event.PredictionResultSaveEvent.Companion.FROM_AGING
import com.glt.magikoly.event.PredictionResultSaveEvent.Companion.FROM_ART_FILTER
import com.glt.magikoly.event.PredictionResultSaveEvent.Companion.FROM_ETHNICITY
import com.glt.magikoly.event.PredictionResultSaveEvent.Companion.FROM_GENDER
import com.glt.magikoly.event.PredictionResultSaveEvent.Companion.FROM_TUNING
import com.glt.magikoly.function.resultreport.TabInfo.CREATOR.SUB_TAB_AGING
import com.glt.magikoly.function.resultreport.TabInfo.CREATOR.SUB_TAB_ART_FILTER
import com.glt.magikoly.function.resultreport.TabInfo.CREATOR.SUB_TAB_ETHNICITY
import com.glt.magikoly.function.resultreport.TabInfo.CREATOR.SUB_TAB_GENDER
import com.glt.magikoly.function.resultreport.TabInfo.CREATOR.SUB_TAB_TUNING
import com.glt.magikoly.mvp.AbsPresenter

class FaceReportPresenter : AbsPresenter<IReport>() {

    /**
     * 通过tabID获取统计用的tab分类
     */
    fun getCurrentTabCategory(mCurrentTabId: Int): Int {
        when (mCurrentTabId) {
            SUB_TAB_AGING -> {
                return FROM_AGING
            }
            SUB_TAB_GENDER -> {
                return FROM_GENDER
            }
            SUB_TAB_ETHNICITY -> {
                return FROM_ETHNICITY
            }
            SUB_TAB_TUNING -> {
                return FROM_TUNING
            }
            SUB_TAB_ART_FILTER -> {
                return FROM_ART_FILTER
            }
        }
        return -1
    }
}