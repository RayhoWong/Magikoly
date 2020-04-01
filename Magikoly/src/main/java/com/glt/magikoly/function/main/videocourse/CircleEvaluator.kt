package com.glt.magikoly.function.main.videocourse

import android.animation.TypeEvaluator

class CircleEvaluator : TypeEvaluator<Circle> {
    private val tempCircle = Circle(0f, 0f, 0f)
    override fun evaluate(fraction: Float, startValue: Circle?, endValue: Circle?): Circle {
        tempCircle.x = startValue!!.x + (endValue!!.x - startValue.x) * fraction
        tempCircle.y = startValue.y + (endValue.y - startValue.y) * fraction
        tempCircle.radius = startValue.radius + (endValue.radius - startValue.radius) * fraction
        return tempCircle
    }
}