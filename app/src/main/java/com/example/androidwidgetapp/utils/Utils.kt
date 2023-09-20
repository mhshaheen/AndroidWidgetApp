package com.example.androidwidgetapp.utils

import android.graphics.Typeface
import android.widget.TextView

fun TextView.asFontAwesome(isSolid: Boolean = true) {
    this.typeface =
        Typeface.createFromAsset(
            context.applicationContext.assets,
            if (isSolid) "fonts/fa_solid5.ttf" else "fonts/fa_regular5.ttf"
        )
}