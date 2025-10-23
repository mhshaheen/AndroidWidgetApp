package com.example.androidwidgetapp.media3Player

import android.graphics.Bitmap
import coil3.transform.Transformation
import coil3.size.Size as CoilSize

class CustomBlurTransformation(private val radius: Int) : Transformation() {

    override val cacheKey: String = "CustomBlurTransformation(radius=$radius)"

    override suspend fun transform(input: Bitmap, size: CoilSize): Bitmap {
        return input.blur(radius)
    }

    private fun Bitmap.blur(blurRadius: Int): Bitmap {
        val width = this.width
        val height = this.height
        val bitmap = this.copy(config ?: Bitmap.Config.ARGB_8888, true)

        if (radius < 1) return bitmap

        val w = width
        val h = height

        val pix = IntArray(w * h)
        bitmap.getPixels(pix, 0, w, 0, 0, w, h)

        val div = blurRadius + blurRadius + 1
        val r = IntArray(w * h)
        val g = IntArray(w * h)
        val b = IntArray(w * h)

        val vmin = IntArray(maxOf(w, h))

        var yi = 0
        var yw = 0

        val dv = IntArray(256 * div)
        for (i in dv.indices) {
            dv[i] = i / div
        }

        for (y in 0 until h) {
            var rsum = 0
            var gsum = 0
            var bsum = 0
            for (i in -blurRadius..blurRadius) {
                val p = pix[yi + minOf(w - 1, maxOf(i, 0))]
                rsum += (p shr 16) and 0xFF
                gsum += (p shr 8) and 0xFF
                bsum += p and 0xFF
            }

            for (x in 0 until w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]

                if (y == 0) {
                    vmin[x] = minOf(x + blurRadius + 1, w - 1)
                }
                val p1 = pix[yw + vmin[x]]
                val p2 = pix[yw + maxOf(x - blurRadius, 0)]

                rsum += ((p1 shr 16) and 0xFF) - ((p2 shr 16) and 0xFF)
                gsum += ((p1 shr 8) and 0xFF) - ((p2 shr 8) and 0xFF)
                bsum += (p1 and 0xFF) - (p2 and 0xFF)

                yi++
            }
            yw += w
        }

        for (x in 0 until w) {
            var rsum = 0
            var gsum = 0
            var bsum = 0
            var yp = -blurRadius * w
            for (i in -blurRadius..blurRadius) {
                val yi2 = maxOf(0, yp) + x
                rsum += r[yi2]
                gsum += g[yi2]
                bsum += b[yi2]
                yp += w
            }

            var yi3 = x
            for (y in 0 until h) {
                pix[yi3] = (0xFF shl 24) or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]

                if (x == 0) {
                    vmin[y] = minOf(y + blurRadius + 1, h - 1) * w
                }
                val p1 = x + vmin[y]
                val p2 = x + maxOf(y - blurRadius, 0) * w

                rsum += r[p1] - r[p2]
                gsum += g[p1] - g[p2]
                bsum += b[p1] - b[p2]

                yi3 += w
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h)
        return bitmap
    }
}
