package com.sm_fs.custommedia3player.core.analytics

import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.analytics.AnalyticsListener
import com.sm_fs.custommedia3player.domain.callback.CustomAnalyticsListener
import com.sm_fs.custommedia3player.domain.model.PlaybackAnalytics
import com.sm_fs.custommedia3player.domain.model.Track
import com.sm_fs.custommedia3player.util.extensions.toTrack
import java.util.UUID

@OptIn(UnstableApi::class)
class AnalyticsCollector(
    private val customAnalyticsListener: CustomAnalyticsListener?
) : AnalyticsListener {
    
    companion object {
        private const val TAG = "AnalyticsCollector"
        private const val SKIP_THRESHOLD_MS = 5000L
    }
    
    private var currentSession: AnalyticsSession? = null
    private var lastPosition = 0L
    
    override fun onEvents(player: Player, events: AnalyticsListener.Events) {
        if (events.contains(AnalyticsListener.EVENT_IS_PLAYING_CHANGED)) {
            handlePlayingChanged(player)
        }
        
        if (events.contains(AnalyticsListener.EVENT_POSITION_DISCONTINUITY)) {
            handlePositionDiscontinuity(player)
        }
        
        if (events.contains(AnalyticsListener.EVENT_MEDIA_ITEM_TRANSITION)) {
            handleMediaItemTransition(player)
        }
    }
    
    private fun handlePlayingChanged(player: Player) {
        if (player.isPlaying) {
            if (currentSession == null) {
                player.currentMediaItem?.toTrack()?.let { track ->
                    currentSession = AnalyticsSession(
                        track = track,
                        sessionId = UUID.randomUUID().toString(),
                        startTime = System.currentTimeMillis()
                    )
                }
            }
            currentSession?.resume()
        } else {
            currentSession?.pause()
        }
    }
    
    private fun handlePositionDiscontinuity(player: Player) {
        currentSession?.let { session ->
            val currentPos = player.currentPosition
            if (currentPos - lastPosition > SKIP_THRESHOLD_MS) {
                session.markSkipped()
            }
            lastPosition = currentPos
        }
    }
    
    private fun handleMediaItemTransition(player: Player) {
//        currentSession?.let { session ->
//            session.end()
//            val analytics = session.toAnalytics()
//            customAnalyticsListener?.onAnalyticsEvent(analytics)
//            Log.d(TAG, "Analytics: ${analytics.track.title} - Played: ${analytics.playedDurationMs}ms")
//        }

        currentSession?.let { session ->
            session.end()
            val analytics = session.toAnalytics()
            try {
                customAnalyticsListener?.onAnalyticsEvent(analytics)
                Log.d(TAG, "Analytics: ${analytics.track.title} - Played: ${analytics.playedDurationMs}ms")
            } catch (e: Exception) {
                Log.e(TAG, "Error reporting analytics", e)
            }
        }
        
        player.currentMediaItem?.toTrack()?.let { track ->
            currentSession = AnalyticsSession(
                track = track,
                sessionId = UUID.randomUUID().toString(),
                startTime = System.currentTimeMillis()
            )
        } ?: run {
            currentSession = null
        }
        
        lastPosition = player.currentPosition
    }

    private data class AnalyticsSession(
        val track: Track,
        val sessionId: String,
        val startTime: Long,
        var playedDuration: Long = 0L,
        var bufferingDuration: Long = 0L,
        var lastResumeTime: Long = 0L,
        var wasSkipped: Boolean = false,
        var endTime: Long = 0L
    ) {
        fun resume() {
            if (lastResumeTime == 0L) {
                lastResumeTime = System.currentTimeMillis()
            }
        }
        
        fun pause() {
            if (lastResumeTime > 0L) {
                playedDuration += System.currentTimeMillis() - lastResumeTime
                lastResumeTime = 0L
            }
        }
        
        fun markSkipped() {
            wasSkipped = true
        }
        
        fun end() {
            pause()
            endTime = System.currentTimeMillis()
        }
        
        fun toAnalytics(): PlaybackAnalytics {
            val totalDuration = track.durationMs
            val completion = if (totalDuration > 0) {
                (playedDuration.toFloat() / totalDuration) * 100
            } else 0f
            
            return PlaybackAnalytics(
                track = track,
                sessionId = sessionId,
                playedDurationMs = playedDuration,
                bufferingDurationMs = bufferingDuration,
                startTimestamp = startTime,
                endTimestamp = endTime,
                wasSkipped = wasSkipped,
                completionPercentage = completion,
                averageBufferHealth = 90f // Simplified
            )
        }
    }
}