package com.sm_fs.custommedia3player.util.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.media3.common.util.BitmapLoader
import androidx.media3.common.util.UnstableApi
import com.google.common.util.concurrent.ListenableFuture
import com.sm_fs.custommedia3player.util.extensions.coli3.BrightnessTransformation
import com.sm_fs.custommedia3player.util.extensions.coli3.Coil3ImageLoader
import com.sm_fs.custommedia3player.util.extensions.coli3.CustomBlurTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.guava.future

@UnstableApi
class CustomBitmapLoader(private val context: Context) : BitmapLoader {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun supportsMimeType(mimeType: String): Boolean = true

    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> {
        return scope.future {
            BitmapFactory.decodeByteArray(data, 0, data.size)
                ?: error("Could not decode image data")
        }
    }

    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> {
        return scope.future {
            Coil3ImageLoader.loadImageThenReturnBitmapSuspend(
                context,
                uri,
                transformations = listOf(
                    BrightnessTransformation(-0.3f),
                    CustomBlurTransformation(5)
                )
            )
        }
    }

    fun release() {
        scope.cancel()
    }
}