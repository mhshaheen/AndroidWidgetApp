package com.example.androidwidgetapp.customMedia3PlayerImplementationDemo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.sm_fs.custommedia3player.core.player.GenericMediaPlayer
import com.sm_fs.custommedia3player.core.player.MediaPlayerProvider
import com.sm_fs.custommedia3player.domain.model.Track

//class MusicPlayerViewModel(private val app: Application) : AndroidViewModel(app) {
//
//    private val mediaPlayer: GenericMediaPlayer = MediaPlayerProvider.getInstance(app)
//
//    private val player by lazy { mediaPlayer }
//
//    val playerState by lazy { player.playerState.asLiveData() }
//    val currentTrack by lazy { player.currentTrack.asLiveData() }
//    val progress by lazy { player.playbackProgress.asLiveData() }
//    val playlist by lazy { player.playlist.asLiveData() }
//    val shuffleMode by lazy { player.shuffleMode.asLiveData() }
//    val repeatMode by lazy { player.repeatMode.asLiveData() }
//
//    fun playTracks(tracks: List<Track>) {
//        player.setPlaylist(tracks)
//    }
//
//    fun togglePlayPause() {
//        player.togglePlayPause()
//    }
//
//    fun next() = player.next()
//    fun previous() = player.previous()
//    fun seekTo(positionMs: Long) = player.seekTo(positionMs)
//    fun toggleShuffle() = player.toggleShuffle()
//    fun toggleRepeat() = player.toggleRepeatMode()
//    fun setSpeed(speed: Float) = player.setPlaybackSpeed(speed)
//    fun setSleepTimer(minutes: Int) = player.setSleepTimer(minutes * 60 * 1000L)
//
//    override fun onCleared() {
//        // Don't release here if you want music to continue in background
//        super.onCleared()
//    }
//}

class MusicPlayerViewModel(private val app: Application) : AndroidViewModel(app) {

    private val mediaPlayer: GenericMediaPlayer = MediaPlayerProvider.getInstance(app)

    val playerState = mediaPlayer.playerState.asLiveData()
    val currentTrack = mediaPlayer.currentTrack.asLiveData()
    val progress = mediaPlayer.playbackProgress.asLiveData()
    val playlist = mediaPlayer.playlist.asLiveData()
    val shuffleMode = mediaPlayer.shuffleMode.asLiveData()
    val repeatMode = mediaPlayer.repeatMode.asLiveData()

    fun playTracks(tracks: List<Track>) {
        mediaPlayer.setPlaylist(tracks)
    }

    fun togglePlayPause() = mediaPlayer.togglePlayPause()
    fun next() = mediaPlayer.next()
    fun previous() = mediaPlayer.previous()
    fun seekTo(positionMs: Long) = mediaPlayer.seekTo(positionMs)
    fun toggleShuffle() = mediaPlayer.toggleShuffle()
    fun toggleRepeat() = mediaPlayer.toggleRepeatMode()
    fun setSpeed(speed: Float) = mediaPlayer.setPlaybackSpeed(speed)
    fun setSleepTimer(minutes: Int) = mediaPlayer.setSleepTimer(minutes * 60 * 1000L)

    override fun onCleared() {
        // Don't release - player should survive ViewModel lifecycle
        // Only release when app is truly destroyed
        super.onCleared()
    }
}