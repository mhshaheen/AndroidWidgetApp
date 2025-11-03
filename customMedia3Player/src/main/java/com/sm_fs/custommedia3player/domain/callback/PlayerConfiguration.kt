package com.sm_fs.custommedia3player.domain.callback

@ConsistentCopyVisibility
data class PlayerConfiguration internal constructor(
    val cacheSizeMb: Long,
    val seekIncrementMs: Long,
    val userAgent: String,
    val enableAnalytics: Boolean,
    val urlResolver: UrlResolver?,
    val customAnalyticsListener: CustomAnalyticsListener?,
    val errorHandler: ErrorHandler?
) {
    class Builder {
        var cacheSizeMb: Long = 300L
        var seekIncrementMs: Long = 5000L
        var userAgent: String = "GenericMedia3SDK/1.0"
        var enableAnalytics: Boolean = true
        var urlResolver: UrlResolver? = null
        var customAnalyticsListener: CustomAnalyticsListener? = null
        var errorHandler: ErrorHandler? = null

        fun build() = PlayerConfiguration(
            cacheSizeMb = cacheSizeMb,
            seekIncrementMs = seekIncrementMs,
            userAgent = userAgent,
            enableAnalytics = enableAnalytics,
            urlResolver = urlResolver,
            customAnalyticsListener = customAnalyticsListener,
            errorHandler = errorHandler
        )
    }
}

fun playerConfiguration(block: PlayerConfiguration.Builder.() -> Unit): PlayerConfiguration {
    return PlayerConfiguration.Builder().apply(block).build()
}