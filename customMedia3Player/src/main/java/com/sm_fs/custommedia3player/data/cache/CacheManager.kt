package com.sm_fs.custommedia3player.data.cache

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@OptIn(UnstableApi::class)
object CacheManager {
    @Volatile
    private var instance: SimpleCache? = null

    fun getCache(context: Context, maxSizeMb: Long): SimpleCache {
        return instance ?: synchronized(this) {
            instance ?: createCache(context, maxSizeMb).also { instance = it }
        }
    }

    private fun createCache(context: Context, maxSizeMb: Long): SimpleCache {
        val cacheDir = File(context.cacheDir, "media_cache")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }

        val maxSizeBytes = maxSizeMb * 1024 * 1024
        val evictor = LeastRecentlyUsedCacheEvictor(maxSizeBytes)
        val databaseProvider = StandaloneDatabaseProvider(context)

        return SimpleCache(cacheDir, evictor, databaseProvider)
    }

    fun release() {
        synchronized(this) {
            instance?.release()
            instance = null
        }
    }
}