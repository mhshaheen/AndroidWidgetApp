package com.example.androidwidgetapp.shortsPlayer.extension

import androidx.compose.runtime.compositionLocalOf

val LocalFragmentVisibility = compositionLocalOf<()->Boolean> { { true } }