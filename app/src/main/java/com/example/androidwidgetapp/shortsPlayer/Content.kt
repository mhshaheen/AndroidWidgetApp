package com.example.androidwidgetapp.shortsPlayer

import androidx.annotation.Keep

@Keep
data class Content(
    var contentId: Int = -1,
    val streamingUrl: String = "",
)
