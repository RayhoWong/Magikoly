package com.glt.magikoly.function.main.videocourse;

import android.view.animation.Interpolator;

public class DecelerateAccelerateInterpolator implements Interpolator {

    public DecelerateAccelerateInterpolator() {
    }

    public float getInterpolation(float x) {
        return (float) (Math.tan((x-0.5) * 2) / Math.PI  + 0.5);
    }
}
