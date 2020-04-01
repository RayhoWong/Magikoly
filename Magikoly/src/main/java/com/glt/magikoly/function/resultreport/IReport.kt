package com.glt.magikoly.function.resultreport

import com.glt.magikoly.function.main.ITabFragment
import com.glt.magikoly.mvp.IViewInterface
import me.yokeyword.fragmentation.ISupportFragment

interface IReport : IViewInterface<FaceReportPresenter> {
    fun showHideFragment(showFragment: ISupportFragment)
    fun refreshToolBar(showFragment: ITabFragment)
}