package com.example.androidwidgetapp.media3Player

import android.annotation.SuppressLint
import android.net.Uri
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.BitmapLoader
import com.example.androidwidgetapp.media3Player.GenericTrack.Companion.safeJson
import kotlinx.serialization.json.Json

/**
 * Generic Track Model
 * Your app's track model should implement this interface
 */
interface GenericTrack {
    val id: String
    val title: String
    val artist: String?
    val artworkUrl: String?
    val playUrl: String
    val duration: Long // in seconds
    val currentDurationCursor: Long // in seconds (for resume position)
    var isCurrentlyPlaying: Boolean

    fun copy(): GenericTrack
    
    fun toMediaItem(): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setArtworkUri(artworkUrl?.toUri())
            .setExtras(bundleOf(
                GenericTrack.GENERIC_TRACK_TAG to safeJson.encodeToString<MyTrack?>(this as MyTrack)
            ))
            .build()
        
        return MediaItem.Builder()
            .setMediaId(id)
            .setUri(playUrl)
            .setMediaMetadata(metadata)
            .setTag(this)
            .build()
    }

    companion object {
        const val GENERIC_TRACK_TAG = "GenericTrack"
        val safeJson = Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
    }

}

/**
 * Extension function to extract GenericTrack from MediaItem
 */
fun MediaItem.getGenericTrack(): MyTrack? {
    return this.mediaMetadata.extras?.getString(GenericTrack.GENERIC_TRACK_TAG)?.toGenericTrack()
//    return this.localConfiguration?.tag as? GenericTrack
}

fun String.toGenericTrack(): MyTrack? {
    return safeJson.decodeFromString<MyTrack?>(this)
}

/**
 * Player State enum
 */
enum class GenericPlayerState {
    IDLE,
    BUFFERING,
    READY,
    PLAYING,
    NOT_PLAYING,
    ENDED
}

/**
 * Repeat Mode enum
 */
enum class GenericRepeatMode {
    NONE,
    ONE,
    ALL
}

/**
 * Service State enum
 */
enum class GenericServiceState {
    UNKNOWN,
    CONNECTING,
    CONNECTED,
    DISCONNECTED
}

/**
 * Analytics data for tracking playback
 */
data class GenericAnalytics(
    val track: GenericTrack,
    var playedMS: Long,
    var bufferingMS: Long,
    val startTime: Long,
    var lastPlayedMS: Long,
    var currentCursorPosition: Long,
    var isSkipped: Boolean,
    var duration: Long,
    var endTime: Long = 0L
)

/**
 * Configuration interface for the generic player
 */
@SuppressLint("UnsafeOptInUsageError")
interface GenericMediaConfig {
    val appName: String
    val notificationChannelId: String
    val notificationChannelName: String
    val notificationChannelDescription: String
    val notificationChannelNameRes: Int
    val notificationId: Int
    val notificationIconRes: Int?
    val seekForwardMs: Long
    val seekBackwardMs: Long
    val cacheMaxSizeBytes: Long
    val userAgent: String
    val deepLinkProvider: ((GenericTrack?) -> String)?
    val bitmapLoader: BitmapLoader?
}

/**
 * Default implementation of GenericMediaConfig
 */
data class DefaultMediaConfig(
    override val appName: String = "Media Player",
    override val notificationChannelId: String = "generic_media_notification",
    override val notificationChannelName: String = "Media",
    override val notificationChannelDescription: String = "Media Playback",
    override val notificationChannelNameRes: Int = android.R.string.unknownName,
    override val notificationId: Int = 1001,
    override val notificationIconRes: Int? = null,
    override val seekForwardMs: Long = 5000,
    override val seekBackwardMs: Long = 5000,
    override val cacheMaxSizeBytes: Long = 300 * 1024 * 1024L, // 300 MB
    override val userAgent: String = "Generic Media Player",
    override val deepLinkProvider: ((GenericTrack?) -> String)? = null,
    @UnstableApi
    override val bitmapLoader: BitmapLoader? = null
) : GenericMediaConfig

/**
 * Simple implementation of GenericTrack
 * You can use this or create your own implementation
 */
data class SimpleTrack(
    override val id: String,
    override val title: String,
    override val artist: String? = null,
    override val artworkUrl: String? = null,
    override val playUrl: String,
    override val duration: Long = 0L,
    override val currentDurationCursor: Long = 0L,
    override var isCurrentlyPlaying: Boolean = false
) : GenericTrack {
    override fun copy(): GenericTrack = this.copy()
}