package com.glow.app.ui.nowplaying

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.runtime.State
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import com.glow.app.ui.common.ArtPalette
import com.glow.app.ui.glass.core.LocalGlassConfig
import com.glow.app.ui.theme.GlowSkin
import com.glow.app.ui.theme.LocalGlowSkin

private const val ORB_PERIOD_MS = 14_000

private fun bottomTintAlpha(skin: GlowSkin) = if (skin.isDark) 0.55f else 0.75f

/** لون النصوص والأزرار في أسفل المشغّل: داكن أو فاتح بحسب سطوع الخلفية هناك. */
fun playerContentColor(palette: ArtPalette, skin: GlowSkin): Color {
    val bottom = palette.primary.copy(alpha = bottomTintAlpha(skin)).compositeOver(skin.bgBottom)
    return if (bottom.luminance() > 0.5f) Color(0xFF07141C) else Color.White
}

/**
 * خلفية المشغّل: تدرّج ناعم من ألوان الغلاف، فيه كرتان ضبابيتان
 * تنجرفان ببطء. تتوقف الحركة إذا أوقف النظام الرسوم المتحركة.
 */
@Composable
fun AnimatedArtBackground(palette: ArtPalette, modifier: Modifier = Modifier) {
    val skin = LocalGlowSkin.current
    val reduceMotion = LocalGlassConfig.current.reduceMotion

    val phase: State<Float> = if (reduceMotion) {
        remember { mutableStateOf(0.5f) }
    } else {
        rememberInfiniteTransition(label = "artBackdrop").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(ORB_PERIOD_MS, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "artBackdropPhase",
        )
    }
    val orbAlpha = if (skin.isDark) 0.55f else 0.70f

    Canvas(modifier.fillMaxSize()) {
        // الحركة تُقرأ هنا فقط، فلا تُعاد التركيبة في كل إطار.
        val a = phase.value * 2f - 1f
        val w = size.width
        val h = size.height

        drawRect(Brush.verticalGradient(listOf(skin.bgTop, skin.bgBottom)))

        val c1 = Offset(w * (0.22f + 0.14f * a), h * (0.32f + 0.06f * a))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(palette.secondary.copy(alpha = orbAlpha), Color.Transparent),
                center = c1,
                radius = w * 0.85f,
            ),
            radius = w * 0.85f,
            center = c1,
        )

        val c2 = Offset(w * (0.80f - 0.14f * a), h * (0.62f - 0.06f * a))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(palette.primary.copy(alpha = orbAlpha), Color.Transparent),
                center = c2,
                radius = w * 0.80f,
            ),
            radius = w * 0.80f,
            center = c2,
        )

        drawRect(
            Brush.verticalGradient(
                0.45f to Color.Transparent,
                1f to palette.primary.copy(alpha = bottomTintAlpha(skin)),
            ),
        )
    }
}
