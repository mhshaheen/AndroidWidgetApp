package com.sm_fs.custommedia3player.domain.model

import androidx.media3.common.Player

sealed class RepeatMode {
    object Off : RepeatMode()
    object One : RepeatMode()
    object All : RepeatMode()
    
    fun toExoPlayerMode(): Int {
        return when (this) {
            Off -> Player.REPEAT_MODE_OFF
            One -> Player.REPEAT_MODE_ONE
            All -> Player.REPEAT_MODE_ALL
        }
    }
    
    companion object {
        fun fromExoPlayerMode(mode: Int): RepeatMode {
            return when (mode) {
                Player.REPEAT_MODE_OFF -> Off
                Player.REPEAT_MODE_ONE -> One
                Player.REPEAT_MODE_ALL -> All
                else -> Off
            }
        }
    }
}