package com.example.androidwidgetapp.media3Player

import androidx.annotation.IntDef
import androidx.media3.common.Player

enum class SMServiceState{
    CONNECTING,
    CONNECTED,
    DISCONNECTED,
    UNKNOWN
}

enum class SMPlayerState{
    IDLE,
    BUFFERING,
    READY,
    ENDED,
    PLAYING,
    NOT_PLAYING
}

enum class SMRepeatMode{
    NONE,
    ONE,
    ALL
}