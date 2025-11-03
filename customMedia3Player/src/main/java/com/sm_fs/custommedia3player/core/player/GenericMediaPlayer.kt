package com.sm_fs.custommedia3player.core.player

import android.content.Context
import com.sm_fs.custommedia3player.core.config.NotificationConfiguration
import com.sm_fs.custommedia3player.domain.callback.PlayerConfiguration
import com.sm_fs.custommedia3player.domain.model.PlaybackProgress
import com.sm_fs.custommedia3player.domain.model.PlayerState
import com.sm_fs.custommedia3player.domain.model.RepeatMode
import com.sm_fs.custommedia3player.domain.model.ShuffleMode
import com.sm_fs.custommedia3player.domain.model.Track
import kotlinx.coroutines.flow.StateFlow

interface GenericMediaPlayer {
    
    // State Observables
    val playerState: StateFlow<PlayerState>
    val currentTrack: StateFlow<Track?>
    val playlist: StateFlow<List<Track>>
    val playbackProgress: StateFlow<PlaybackProgress>
    val repeatMode: StateFlow<RepeatMode>
    val shuffleMode: StateFlow<ShuffleMode>
    val playbackSpeed: StateFlow<Float>
    val sleepTimerRemaining: StateFlow<Long>
    
    // Playback Control
    fun play()
    fun pause()
    fun togglePlayPause()
    fun next()
    fun previous()
    fun stop()
    fun seekTo(positionMs: Long)
    fun setPlaybackSpeed(speed: Float)
    
    // Queue Management
    fun setPlaylist(tracks: List<Track>, startIndex: Int = 0, startPositionMs: Long = 0L)
    fun addToQueue(tracks: List<Track>)
    fun removeFromQueue(index: Int)
    fun moveInQueue(fromIndex: Int, toIndex: Int)
    fun clearQueue()
    fun playTrackAt(index: Int)
    
    // Mode Control
    fun setRepeatMode(mode: RepeatMode)
    fun toggleRepeatMode()
    fun setShuffleMode(enabled: Boolean)
    fun toggleShuffle()
    
    // Advanced Features
    fun setSleepTimer(durationMs: Long)
    fun cancelSleepTimer()
    
    // Query
    fun isPlaying(): Boolean
    fun getCurrentPosition(): Int
    
    // Lifecycle
    fun release()
    
    companion object {
        fun create(
            context: Context,
            configuration: PlayerConfiguration,
            notificationConfig: NotificationConfiguration
        ): GenericMediaPlayer {
            return GenericMediaPlayerImpl(context, configuration, notificationConfig)
        }
    }
}