package com.glt.magikoly.function.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.TextView

class SecondaryItemView(context: Context, attrs: AttributeSet) : TextView(context, attrs) {

    var icon: Drawable? = null
        set(value) {
            field = value
            field?.setBounds(0, 0, field?.intrinsicWidth ?: 0, field?.intrinsicHeight ?: 0)
        }
    var highLight: Drawable? = null
        set(value) {
            field = value
            field?.setBounds(0, 0, field?.intrinsicWidth ?: 0, field?.intrinsicHeight ?: 0)
        }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        if (selected) {
            setCompoundDrawables(null, highLight, null, null)
        } else {
            setCompoundDrawables(null, icon, null, null)
        }
    }
}