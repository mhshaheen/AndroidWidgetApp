package com.sm_fs.custommedia3player.domain.callback

sealed class PlayerError(val message: String, val cause: Throwable?) {
    class NetworkError(message: String, cause: Throwable? = null) : PlayerError(message, cause)
    class UrlResolutionError(message: String, cause: Throwable? = null) : PlayerError(message, cause)
    class PlaybackError(message: String, cause: Throwable? = null) : PlayerError(message, cause)
    class CacheError(message: String, cause: Throwable? = null) : PlayerError(message, cause)
}

enum class ErrorAction {
    RETRY,
    SKIP_TO_NEXT,
    STOP,
    CONTINUE
}

fun interface ErrorHandler {
    fun onError(error: PlayerError): ErrorAction
}