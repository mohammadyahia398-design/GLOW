package com.glow.app.ui.glass.effects

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import com.glow.app.ui.glass.core.GlassConfig
import com.glow.app.ui.glass.core.GlassStyle
import com.glow.app.ui.glass.core.GlassTier

/** شفافية القاعدة الداكنة في أعلى السطح وأسفله (المستوى البديل). */
private const val BASE_ALPHA_TOP = 0.72f
private const val BASE_ALPHA_BOTTOM = 0.84f

/** مضاعفا شفافية التلوين عند بداية التدرج الأمامية ونهايته. */
private const val TINT_START_BOOST = 1.4f
private const val TINT_END_FADE = 0.6f

/**
 * قاعدة داكنة شبه معتمة تُرسم تحت التلوين في [GlassTier.Fallback] فقط.
 *
 * في هذا المستوى لا يوجد تمويه حي، فلو بقيت الخلفية ظاهرة بحدّتها لتشوّش النص.
 * القاعدة المعتمة جزئياً تعوّض ذلك وتبقي إحساس الشفافية.
 *
 * في [GlassTier.Frosted] و[GlassTier.Liquid] لا يفعل هذا المعدِّل شيئاً.
 *
 * ملاحظة: خلفية مموّهة مسبقاً (صورة ثابتة) خيار أغنى للمستوى البديل، وهو غير
 * منفّذ هنا لأنه يحتاج مصدر صورة محدداً يُقرَّر مع الشاشات.
 */
fun Modifier.glassFallbackBase(shape: Shape, config: GlassConfig): Modifier {
    if (config.tier != GlassTier.Fallback) return this
    val base = config.fallbackBase

    return drawWithCache {
        if (size.height <= 0f) return@drawWithCache onDrawBehind {}

        val outline = shape.createOutline(size, layoutDirection, this)
        val brush = Brush.verticalGradient(
            colors = listOf(
                base.copy(alpha = (base.alpha * BASE_ALPHA_TOP).coerceIn(0f, 1f)),
                base.copy(alpha = (base.alpha * BASE_ALPHA_BOTTOM).coerceIn(0f, 1f)),
            ),
            startY = 0f,
            endY = size.height,
        )
        onDrawBehind { drawOutline(outline, brush) }
    }
}

/**
 * التلوين الزجاجي: تدرج قطري خفيف يبدأ أقوى من الأعلى-اليسار ويخفت نحو الأسفل-اليمين.
 *
 * يُستخدم في كل المستويات، وتُحدَّد شفافيته بحسب [GlassStyle.effectiveTintAlpha]
 * (أعلى في المستوى البديل لتعويض غياب التمويه).
 */
fun Modifier.glassTint(shape: Shape, style: GlassStyle, config: GlassConfig): Modifier {
    val alpha = style.effectiveTintAlpha(config.tier)
    if (alpha <= 0f) return this
    val tint = config.tint

    return drawWithCache {
        if (size.minDimension <= 0f) return@drawWithCache onDrawBehind {}

        val outline = shape.createOutline(size, layoutDirection, this)
        val brush = Brush.linearGradient(
            colors = listOf(
                tint.copy(alpha = (tint.alpha * alpha * TINT_START_BOOST).coerceIn(0f, 1f)),
                tint.copy(alpha = (tint.alpha * alpha * TINT_END_FADE).coerceIn(0f, 1f)),
            ),
            start = Offset.Zero,
            end = Offset(size.width, size.height),
        )
        onDrawBehind { drawOutline(outline, brush) }
    }
}
