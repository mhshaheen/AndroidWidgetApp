package com.example.androidwidgetapp.media3Player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.media3.common.util.BitmapLoader
import androidx.media3.common.util.UnstableApi
import com.example.androidwidgetapp.media3Player.coli3.BrightnessTransformation
import com.example.androidwidgetapp.media3Player.coli3.Coil3ImageLoader
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.guava.future

@UnstableApi
@OptIn(DelicateCoroutinesApi::class)
class SMBitmapLoader(private val context: Context) : BitmapLoader {
    override fun supportsMimeType(mimeType: String): Boolean {
        return true
    }

    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> {
        return GlobalScope.future(Dispatchers.IO) {
            BitmapFactory.decodeByteArray(data, 0, data.size) ?: error("Could not decode image data")
        }
    }

    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> {
        return GlobalScope.future(Dispatchers.IO) {
            Coil3ImageLoader.loadImageThenReturnBitmapSuspend(
                context,
                uri,
                listOf(BrightnessTransformation(-0.2f),)
            )
        }
    }
}