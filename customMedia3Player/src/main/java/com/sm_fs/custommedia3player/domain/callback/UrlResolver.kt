package com.sm_fs.custommedia3player.domain.callback

import com.sm_fs.custommedia3player.domain.model.Track

fun interface UrlResolver {
    suspend fun resolveUrl(track: Track): Result<String>
}