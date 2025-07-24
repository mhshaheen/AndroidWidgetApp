package com.example.androidwidgetapp.shortsPlayer

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

@OptIn(UnstableApi::class)
object ShortsCache {

    private const val MAX_CACHE_BYTES = 400L * 1024 * 1024 // 400 MB
    private val lock = Any()

    private var mediaSourceFactory: MediaSource.Factory? = null
    private var simpleCache: SimpleCache? = null
    private var lastCleanupTime = 0L

    fun getMediaSourceFactory(
        context: Context,
        interceptor: Interceptor
    ): MediaSource.Factory = synchronized(lock) {
        mediaSourceFactory ?: run {
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .followRedirects(true)
                .followSslRedirects(true)
                .connectionPool(ConnectionPool(8, 15, TimeUnit.SECONDS))
                .build()

            val okHttpDataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
                .setUserAgent("SM Shorts Player")

            val upstreamFactory = DefaultDataSource.Factory(context, okHttpDataSourceFactory)

            val cacheFactory = CacheDataSource.Factory()
                .setCache(getCache(context))
                .setUpstreamDataSourceFactory(upstreamFactory)
                .setFlags(
                    CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR or
                            CacheDataSource.FLAG_BLOCK_ON_CACHE
                )

            // DefaultMediaSourceFactory supports .mp3, .mp4, .m3u8 by default
            val defaultFactory = DefaultMediaSourceFactory(context)
                .setDataSourceFactory(cacheFactory)

            mediaSourceFactory = defaultFactory
            mediaSourceFactory!!
        }
    }

    private fun getCache(context: Context): SimpleCache = synchronized(lock) {
        simpleCache ?: run {
            val cacheDir = File(context.cacheDir, "media3_cache")
            val evictor = LeastRecentlyUsedCacheEvictor(MAX_CACHE_BYTES)
            val databaseProvider = StandaloneDatabaseProvider(context)
            simpleCache = SimpleCache(cacheDir, evictor, databaseProvider)
            simpleCache!!
        }
    }

    fun releaseCache() = synchronized(lock) {
        val now = System.currentTimeMillis()
        if (now - lastCleanupTime > 30_000) {
            lastCleanupTime = now
            simpleCache?.release()
            simpleCache = null
            mediaSourceFactory = null
        }
    }
}






