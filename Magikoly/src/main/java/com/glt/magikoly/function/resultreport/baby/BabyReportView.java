package com.glt.magikoly.function.resultreport.baby;

import com.glt.magikoly.mvp.IViewInterface;

/**
 * @desc:
 * @auther:duwei
 * @date:2019/4/18
 */
public interface BabyReportView extends IViewInterface<BabyReportPresenter> {

    void showBabyImage(String url);

    void showErrorDialog(int errorCode);

    void showLoading(String entrance);

    void hideLoading();

}
