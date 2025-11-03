package com.sm_fs.custommedia3player.domain.model


interface Track {
    val contentId: String
    val contentType: String
    val title: String
    val artist: String?
    val artworkUrl: String?
    val playbackUrl: String
    val durationMs: Long
    val resumePositionMs: Long get() = 0L
    val metadata: Map<String, String> get() = emptyMap()
}