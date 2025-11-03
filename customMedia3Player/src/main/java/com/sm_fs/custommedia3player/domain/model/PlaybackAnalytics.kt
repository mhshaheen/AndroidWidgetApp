package com.sm_fs.custommedia3player.domain.model

data class PlaybackAnalytics(
    val track: Track,
    val sessionId: String,
    val playedDurationMs: Long,
    val bufferingDurationMs: Long,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val wasSkipped: Boolean,
    val completionPercentage: Float,
    val averageBufferHealth: Float
)