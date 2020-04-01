package com.glt.magikoly.mvp;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.glt.magikoly.view.ProgressBarEvent;

import me.yokeyword.fragmentation.SupportFragment;

import static com.glt.magikoly.ext.BaseExtKt.postEvent;

public abstract class BaseSupportFragment<T extends AbsPresenter> extends SupportFragment implements IViewInterface<T>, View.OnClickListener {

    protected T mPresenter;

    public BaseSupportFragment() {
        mPresenter = createPresenter();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mPresenter.attachView(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mPresenter.detachView();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setOnClickListener(this);
    }

    protected void restoreInstanceState(Bundle outState) {

    }

    @Override
    public final boolean onBackPressedSupport() {
        ProgressBarEvent event = new ProgressBarEvent(ProgressBarEvent.EVENT_IS_SHOWN, "-1",true,false);
        postEvent(event);
        if (event.isShown()) {
            return true;
        }
        return doBackPressedSupport();
    }

    protected boolean doBackPressedSupport() {
        return false;
    }
}
