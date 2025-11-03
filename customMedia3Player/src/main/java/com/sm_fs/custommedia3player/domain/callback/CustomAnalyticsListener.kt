package com.sm_fs.custommedia3player.domain.callback

import com.sm_fs.custommedia3player.domain.model.PlaybackAnalytics

fun interface CustomAnalyticsListener {
    fun onAnalyticsEvent(analytics: PlaybackAnalytics)
}