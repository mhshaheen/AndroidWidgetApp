package com.example.androidwidgetapp.media3Player.coli3

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import coil3.size.Size
import coil3.transform.Transformation

class BrightnessTransformation(private val brightness: Float) : Transformation() {
    override val cacheKey: String = "BrightnessTransformation(brightness=$brightness)"

    override suspend fun transform(input: coil3.Bitmap, size: Size): coil3.Bitmap {
        val bmp = input.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bmp)
        val paint = Paint()
        val colorMatrix = ColorMatrix().apply {
            set(floatArrayOf(
                1f, 0f, 0f, 0f, brightness * 255,
                0f, 1f, 0f, 0f, brightness * 255,
                0f, 0f, 1f, 0f, brightness * 255,
                0f, 0f, 0f, 1f, 0f
            ))
        }
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bmp, 0f, 0f, paint)
        return bmp
    }
}