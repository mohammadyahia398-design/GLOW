package com.glow.app.ui.common

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.glow.app.media.Track
import kotlin.math.abs
import kotlin.math.min

/** ألوان مستخرجة من غلاف المقطع: لون رئيسي، ولون ثانٍ مختلف الدرجة، وآخر هو المتوسط. */
@Immutable
data class ArtPalette(val primary: Color, val secondary: Color, val tertiary: Color) {
    companion object {
        val Default = ArtPalette(Color(0xFF06B6D4), Color(0xFF67E8F9), Color(0xFF12303D))
    }
}

private fun hueDistance(a: Float, b: Float): Float {
    val d = abs(a - b)
    return min(d, 360f - d)
}

private fun hsvColor(h: Float, s: Float, v: Float): Color =
    Color(android.graphics.Color.HSVToColor(floatArrayOf(((h % 360f) + 360f) % 360f, s, v)))

/** يستخرج لوناً رئيسياً حيوياً ولوناً ثانياً من الصورة. يعيد الألوان الافتراضية عند أي فشل. */
fun Bitmap.artPalette(): ArtPalette = runCatching {
    val src = if (config == Bitmap.Config.HARDWARE) {
        copy(Bitmap.Config.ARGB_8888, false) ?: this
    } else {
        this
    }
    val n = 24
    val small = Bitmap.createScaledBitmap(src, n, n, true)
    val hsv = FloatArray(3)
    val pixels = ArrayList<FloatArray>(n * n)
    var r = 0L
    var g = 0L
    var b = 0L
    for (y in 0 until n) {
        for (x in 0 until n) {
            val c = small.getPixel(x, y)
            r += android.graphics.Color.red(c)
            g += android.graphics.Color.green(c)
            b += android.graphics.Color.blue(c)
            android.graphics.Color.colorToHSV(c, hsv)
            // h, s, v, score: نفضّل الألوان المشبعة الساطعة
            pixels += floatArrayOf(hsv[0], hsv[1], hsv[2], hsv[1] * (0.35f + hsv[2]))
        }
    }
    val count = n * n
    val average = Color(
        red = (r / count).toInt(),
        green = (g / count).toInt(),
        blue = (b / count).toInt(),
    )

    val top = pixels.maxByOrNull { it[3] }
    if (top == null || top[1] < 0.15f) {
        // غلاف رمادي تقريباً: نبقي ألوان الهوية
        ArtPalette.Default.copy(tertiary = average)
    } else {
        val primary = hsvColor(top[0], top[1].coerceIn(0.45f, 1f), top[2].coerceIn(0.6f, 0.95f))
        val other = pixels
            .filter { hueDistance(it[0], top[0]) > 35f && it[1] > 0.2f }
            .maxByOrNull { it[3] }
        val secondary = if (other != null) {
            hsvColor(other[0], other[1].coerceIn(0.3f, 1f), other[2].coerceIn(0.7f, 1f))
        } else {
            hsvColor(top[0] + 25f, (top[1] * 0.6f).coerceIn(0.3f, 1f), 1f)
        }
        ArtPalette(primary, secondary, average)
    }
}.getOrDefault(ArtPalette.Default)

/** ألوان غلاف المقطع الحالي، تُحسب مرة لكل غلاف. */
@Composable
fun rememberArtPalette(track: Track?): ArtPalette {
    val bmp = rememberArt(track, 160)
    return remember(bmp) { bmp?.artPalette() ?: ArtPalette.Default }
}
