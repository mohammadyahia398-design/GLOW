package com.glow.app.ui.glass.lighting

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.glow.app.ui.glass.core.GlassStyle
import kotlin.math.abs

/** مضاعفات شفافية الإضاءة في وسط الحد وفي الجهة المعاكسة للضوء. */
private const val MID_FADE = 0.12f
private const val FAR_RIM = 0.40f

/**
 * يرسم حداً رفيعاً مضيئاً داخل حافة الشكل، تتغير قوته بحسب اتجاه الضوء:
 * الحافة المواجهة للمصدر أقوى، والحافة المعاكسة تحتفظ بإضاءة ثانوية خافتة
 * تشبه انعكاس الضوء الخارج من حافة الزجاج.
 *
 * يُرسم الحد بكامله داخل الشكل (لا يتجاوز حدوده).
 *
 * @param light دالة تعيد مصدر الضوء الحالي. تُقرأ أثناء الرسم لا أثناء التركيب،
 * فتغيّر الضوء (مثلاً أثناء اللمس) يعيد الرسم فقط دون إعادة تركيب.
 * @param strokeWidth سُمك الحد المرئي.
 * @param color لون الإضاءة.
 */
fun Modifier.glassEdgeHighlight(
    shape: Shape,
    style: GlassStyle,
    light: () -> LightSource,
    strokeWidth: Dp = 1.dp,
    color: Color = Color.White,
): Modifier {
    if (style.highlightAlpha <= 0f) return this

    return drawWithCache {
        if (size.minDimension <= 0f) return@drawWithCache onDrawBehind {}

        val outline = shape.createOutline(size, layoutDirection, this)
        val clip = outline.toClipPath()
        // الحد يتمركز على الحافة، ونصفه الخارجي يُقص، لذا نضاعف العرض.
        val stroke = Stroke(width = strokeWidth.toPx() * 2f)
        val center = Offset(size.width / 2f, size.height / 2f)

        onDrawBehind {
            val source = light()
            val dir = source.towardLight
            // نصف امتداد الشكل على محور الضوء.
            val half = 0.5f * (abs(dir.x) * size.width + abs(dir.y) * size.height)
            if (half <= 0f) return@onDrawBehind

            val peak = (style.highlightAlpha * source.intensity).coerceIn(0f, 1f)
            val brush = Brush.linearGradient(
                0f to color.withScaledAlpha(peak),
                0.45f to color.withScaledAlpha(peak * MID_FADE),
                0.55f to color.withScaledAlpha(peak * MID_FADE),
                1f to color.withScaledAlpha(peak * FAR_RIM),
                start = center + dir * half,
                end = center - dir * half,
            )

            clipPath(clip) {
                drawOutline(outline = outline, brush = brush, style = stroke)
            }
        }
    }
}

private fun Color.withScaledAlpha(factor: Float): Color =
    copy(alpha = (alpha * factor).coerceIn(0f, 1f))

private fun Outline.toClipPath(): Path = when (this) {
    is Outline.Rectangle -> Path().apply { addRect(rect) }
    is Outline.Rounded -> Path().apply { addRoundRect(roundRect) }
    is Outline.Generic -> path
}
