package com.sm_fs.custommedia3player.domain.model

sealed class PlayerState {
    object Idle : PlayerState()
    object Buffering : PlayerState()
    object Ready : PlayerState()
    data class Playing(val track: Track) : PlayerState()
    data class Paused(val track: Track) : PlayerState()
    object Ended : PlayerState()
    data class Error(val exception: Throwable) : PlayerState()
}