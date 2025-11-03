package com.sm_fs.custommedia3player.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TrackImpl(
        override val contentId: String,
        override val contentType: String,
        override val title: String,
        override val artist: String?,
        override val artworkUrl: String?,
        override val playbackUrl: String,
        override val durationMs: Long,
        override val resumePositionMs: Long = 0L,
        override val metadata: Map<String, String> = emptyMap(),
        var albumName: String = "",
        var genre: String = "",
) : Track