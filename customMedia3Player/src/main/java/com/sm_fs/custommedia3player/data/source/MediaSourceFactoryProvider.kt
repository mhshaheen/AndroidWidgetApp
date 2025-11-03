package com.sm_fs.custommedia3player.data.source

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.ExtractorsFactory
import androidx.media3.extractor.mp3.Mp3Extractor
import okhttp3.OkHttpClient
import androidx.media3.datasource.okhttp.OkHttpDataSource
import com.sm_fs.custommedia3player.data.cache.CacheManager
import com.sm_fs.custommedia3player.data.interceptor.UrlInterceptor
import com.sm_fs.custommedia3player.domain.callback.PlayerConfiguration
import java.util.concurrent.TimeUnit

@OptIn(UnstableApi::class)
class MediaSourceFactoryProvider(
    private val context: Context,
    private val configuration: PlayerConfiguration
) {
    
    fun create(urlInterceptor: UrlInterceptor?): DefaultMediaSourceFactory {
        val okHttpClient = createOkHttpClient(urlInterceptor)
        val dataSourceFactory = createDataSourceFactory(okHttpClient)
        val extractorsFactory = createExtractorsFactory()
        
        return DefaultMediaSourceFactory(dataSourceFactory, extractorsFactory)
    }
    
    private fun createOkHttpClient(urlInterceptor: UrlInterceptor?): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
        
        urlInterceptor?.let { builder.addInterceptor(it) }
        
        return builder.build()
    }
    
    private fun createDataSourceFactory(okHttpClient: OkHttpClient): CacheDataSource.Factory {
        val networkFactory = OkHttpDataSource.Factory(okHttpClient)
        val baseFactory = DefaultDataSource.Factory(context, networkFactory)
        val cache = CacheManager.getCache(context, configuration.cacheSizeMb)
        
        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(baseFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }
    
    private fun createExtractorsFactory(): ExtractorsFactory {
        return ExtractorsFactory { arrayOf(Mp3Extractor()) }
    }
}