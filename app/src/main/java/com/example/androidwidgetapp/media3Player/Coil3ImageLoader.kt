package com.example.androidwidgetapp.media3Player

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import coil3.Bitmap
import coil3.ImageLoader
import coil3.asDrawable
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.gif.GifDecoder
import coil3.imageLoader
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import coil3.request.transformations
import coil3.size.Scale
import coil3.svg.SvgDecoder
import coil3.toBitmap
import coil3.transform.CircleCropTransformation
import coil3.transform.Transformation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File

object Coil3ImageLoader {

    private var imageLoaderInstance: ImageLoader? = null
    fun getOrCreateImageLoader(context: Context): ImageLoader {
        return imageLoaderInstance ?: synchronized(this) {
            imageLoaderInstance ?: ImageLoader.Builder(context.applicationContext)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .memoryCache {
                    MemoryCache.Builder()
                        .maxSizePercent(context, 0.05)
                        .strongReferencesEnabled(true)
                        .build()
                }
                .diskCachePolicy(CachePolicy.ENABLED)
                .diskCache {
                    DiskCache.Builder()
                        .directory(File(context.cacheDir, "coil_image_cache"))
                        .maxSizePercent(0.03)
                        .build()
                }
                .networkCachePolicy(CachePolicy.ENABLED)
                .components {
                    add(GifDecoder.Factory())
                    add(SvgDecoder.Factory())
                }
                .build().also { imageLoaderInstance = it }
        }
    }

    fun load(
        attachImageView: ImageView,
        imageUrl: Any?,
        placeholderDrawable: Drawable? = null,
        @DrawableRes placeholderRes: Int? = null,
        @DrawableRes errorRes: Int? = null,
        scaleType: Scale = Scale.FIT,
        crossfade: Boolean = true,
        crossfadeDuration: Int = 500,
        transformations: List<Transformation> = emptyList()
    ) {
        val context = attachImageView.context
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .apply{
                crossfade(crossfadeDuration)
                crossfade(crossfade)
                if(placeholderRes != null) {
                    placeholder(placeholderRes)
                } else if(placeholderDrawable != null) {
                    placeholder(placeholderDrawable)
                }
                if(errorRes != null) {
                    error(errorRes)
                }
                if (transformations.isNotEmpty()){
                    transformations(transformations)
                }
            }
            .target(
                onStart = { placeholder ->
                    attachImageView.setImageDrawable(placeholder?.asDrawable(context.resources))
                },
                onSuccess = { result ->
                    attachImageView.setImageDrawable(result.asDrawable(context.resources))
                },
                onError = { error ->  }
            )
            .build()
        context.imageLoader.enqueue(request)
    }


    fun loadImageThenReturnBitmap(
        context: Context,
        imageUrl: Any?,
        bitmap: (Bitmap) -> Unit,
    ){
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .apply{
                crossfade(500)
                crossfade(true)
                allowHardware(false)
                transformations(CustomBlurTransformation( 25))
            }
            .target(
                onStart = { placeholder ->
                    placeholder?.toBitmap()?.let {
                        bitmap.invoke(it)
                    }
                },
                onSuccess = { result ->
                    bitmap.invoke(result.toBitmap())
                },
                onError = { error ->  }
            )
            .build()
        context.imageLoader.enqueue(request)
    }


    suspend fun loadImageThenReturnBitmapSuspend(
        context: Context,
        imageUrl: Any?,
        transformations: List<Transformation> = listOf(CustomBlurTransformation(15)),
    ): Bitmap = suspendCancellableCoroutine { continuation ->

        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .apply {
                allowHardware(false)
                crossfade(true)
                if (transformations.isNotEmpty()) transformations(transformations)
            }
            .target(
                onStart = { placeholder ->
                    placeholder?.toBitmap()?.let {
                        if (continuation.isActive) continuation.resume(it) { cause, _, _ -> }
                    }
                },
                onSuccess = { result ->
                    if (continuation.isActive) continuation.resume(result.toBitmap()) { cause, _, _ -> }
                },
                onError = { error ->
                }
            )
            .build()

        context.imageLoader.enqueue(request)
    }


    val circleCropTransform get() = listOf(CircleCropTransformation())
}