package com.glt.magikoly.function.takephoto

import android.os.Build
import android.support.annotation.RequiresApi
import android.transition.ChangeBounds
import android.transition.ChangeImageTransform
import android.transition.ChangeTransform
import android.transition.TransitionSet

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class EnterTransition : TransitionSet() {

    init {
        ordering = ORDERING_TOGETHER
        addTransition(ChangeBounds())
                .addTransition(ChangeTransform())
                .addTransition(ChangeImageTransform())
    }


}