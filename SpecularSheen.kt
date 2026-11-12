package com.glow.app.ui.glass.lighting

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import com.glow.app.ui.glass.core.GlassStyle
import kotlin.math.abs

/** إزاحة بؤرة اللمعان عن المركز نحو الضوء، كنسبة من أبعاد الشكل. */
private const val HOTSPOT_SHIFT = 0.30f

/** نصف قطر البؤرة كنسبة من أكبر بُعد في الشكل. */
private const val HOTSPOT_RADIUS = 0.65f

/** مواضع شريط اللمعان على محور الضوء (0 = الحافة المواجهة للضوء). */
private const val STREAK_FADE_IN = 0.06f
private const val STREAK_PEAK = 0.14f
private const val STREAK_FADE_OUT = 0.24f

/** قوة الشريط نسبةً إلى قوة البؤرة. */
private const val STREAK_GAIN = 0.60f

/**
 * يرسم لمعاناً داخل السطح الزجاجي من عنصرين:
 * 1. بؤرة ناعمة مائلة نحو جهة الضوء، تعطي إحساس انحناء السطح.
 * 2. شريطاً رفيعاً قريباً من الحافة المواجهة للضوء، يشبه انعكاس نافذة على الزجاج.
 *
 * القيم مضبوطة بحيث لا تتجاوز [GlassStyle.specularAlpha] (≈ 0.10 إلى 0.18)
 * فلا تفسد وضوح المحتوى فوقها. يُقص الرسم على الشكل تلقائياً.
 *
 * @param light دالة تعيد مصدر الضوء الحالي، وتُقرأ أثناء الرسم فقط.
 * @param color لون اللمعان.
 */
fun Modifier.glassSpecularSheen(
    shape: Shape,
    style: GlassStyle,
    light: () -> LightSource,
    color: Color = Color.White,
): Modifier {
    if (style.specularAlpha <= 0f) return this

    return drawWithCache {
        if (size.minDimension <= 0f) return@drawWithCache onDrawBehind {}

        val outline = shape.createOutline(size, layoutDirection, this)
        val center = Offset(size.width / 2f, size.height / 2f)
        val reach = maxOf(size.width, size.height)

        onDrawBehind {
            val source = light()
            val dir = source.towardLight
            val peak = (style.specularAlpha * source.intensity).coerceIn(0f, 1f)

            // 1) البؤرة.
            val hotspot = Brush.radialGradient(
                colors = listOf(color.withScaledAlpha(peak), Color.Transparent),
                center = Offset(
                    x = center.x + dir.x * size.width * HOTSPOT_SHIFT,
                    y = center.y + dir.y * size.height * HOTSPOT_SHIFT,
                ),
                radius = reach * HOTSPOT_RADIUS,
            )
            drawOutline(outline = outline, brush = hotspot)

            // 2) الشريط.
            val half = 0.5f * (abs(dir.x) * size.width + abs(dir.y) * size.height)
            if (half > 0f) {
                val streak = Brush.linearGradient(
                    0f to Color.Transparent,
                    STREAK_FADE_IN to Color.Transparent,
                    STREAK_PEAK to color.withScaledAlpha(peak * STREAK_GAIN),
                    STREAK_FADE_OUT to Color.Transparent,
                    1f to Color.Transparent,
                    start = center + dir * half,
                    end = center - dir * half,
                )
                drawOutline(outline = outline, brush = streak)
            }
        }
    }
}

private fun Color.withScaledAlpha(factor: Float): Color =
    copy(alpha = (alpha * factor).coerceIn(0f, 1f))
