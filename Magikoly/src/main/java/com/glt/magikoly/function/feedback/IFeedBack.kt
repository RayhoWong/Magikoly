package com.glt.magikoly.function.feedback

import com.glt.magikoly.function.feedback.presenter.FeedbackPresenter
import com.glt.magikoly.mvp.IViewInterface

interface IFeedBack : IViewInterface<FeedbackPresenter> {
    fun onFeedbackStatus(success: Boolean)
}