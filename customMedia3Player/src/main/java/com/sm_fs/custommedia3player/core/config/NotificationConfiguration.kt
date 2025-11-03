package com.sm_fs.custommedia3player.core.config

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

data class NotificationConfiguration(
    val channelId: String = "my_music_channel",
    val channelName: String = "Media Playback",
    val channelDescription: String = "Controls for media playback",
    val notificationId: Int = 1001,
    @DrawableRes val smallIconRes: Int? = null,
    @ColorRes val colorRes: Int? = null
)