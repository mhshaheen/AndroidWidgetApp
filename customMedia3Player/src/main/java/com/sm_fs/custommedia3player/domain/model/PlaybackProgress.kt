package com.sm_fs.custommedia3player.domain.model

data class PlaybackProgress(
    val currentPositionMs: Long,
    val durationMs: Long,
    val bufferedPercentage: Int
) {
    val progressPercentage: Float
        get() = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs) * 100 else 0f
}