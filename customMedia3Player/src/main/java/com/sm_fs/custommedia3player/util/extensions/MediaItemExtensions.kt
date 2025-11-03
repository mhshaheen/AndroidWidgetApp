package com.sm_fs.custommedia3player.util.extensions

import android.util.Log
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.sm_fs.custommedia3player.domain.model.Track
import kotlinx.serialization.json.Json
import androidx.core.net.toUri
import com.sm_fs.custommedia3player.domain.model.TrackImpl

private const val TRACK_TAG = "SDK_TRACK"

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
}

fun Track.toMediaItem(): MediaItem {
    val metadata = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artist)
        .setArtworkUri(artworkUrl?.toUri())
        .setExtras(bundleOf(TRACK_TAG to this.toJsonString()))
        .build()

    return MediaItem.Builder()
        .setMediaId(contentId)
        .setUri(playbackUrl)
        .setMediaMetadata(metadata)
        .setTag(this)
        .build()
}

//fun MediaItem.toTrack(): Track? {
//    (this.localConfiguration?.tag as? Track)?.let { return it }
//    return this.mediaMetadata.extras?.getString(TRACK_TAG)?.toTrack()
//}

fun MediaItem.toTrack(): Track? {
    (this.localConfiguration?.tag as? Track)?.let { return it }

    val trackJson = this.mediaMetadata.extras?.getString(TRACK_TAG)
    return try {
        trackJson?.toTrack()
    } catch (e: Exception) {
        Log.e("MediaItemExtensions", "Failed to deserialize track", e)
        null
    }
}

private fun Track.toJsonString(): String {
    return json.encodeToString<TrackImpl?>(this as? TrackImpl)
}

private fun String.toTrack(): Track? {
    return json.decodeFromString<TrackImpl?>(this)
}
