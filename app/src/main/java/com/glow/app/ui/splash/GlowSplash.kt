package com.glow.app.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.glow.app.R
import kotlin.math.PI
import kotlin.math.sin

private const val TotalMs = 1700f
private val BackOut = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

/**
 * شاشة البداية: البلاطة المتدرجة تبدأ مطابقة لشاشة النظام، ثم:
 * نبضة خفيفة وهالة ملوّنة ← كشف كلمة GLOW من اليسار ← لمعة تعبر البلاطة ← تكبير وتلاشٍ يكشفان التطبيق.
 */
@Composable
fun GlowSplash(onFinished: () -> Unit, modifier: Modifier = Modifier) {
    val bg = if (isSystemInDarkTheme()) Color(0xFF0B0B0D) else Color(0xFFECECEC)
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(TotalMs.toInt(), easing = LinearEasing))
        onFinished()
    }
    val wordmark = painterResource(R.drawable.ic_glow_wordmark)

    val ms = progress.value * TotalMs
    fun seg(from: Float, to: Float, easing: Easing = FastOutSlowInEasing): Float =
        easing.transform(((ms - from) / (to - from)).coerceIn(0f, 1f))

    val halo = seg(0f, 700f)
    val pop = sin(PI.toFloat() * seg(0f, 650f, LinearEasing)) * 0.06f
    val exit = seg(1250f, 1700f, FastOutLinearInEasing)
    val tileScale = 1f + pop + 0.14f * exit
    val wipe = seg(250f, 850f)
    val gloss = seg(550f, 1200f)
    val glossAlpha = sin(PI.toFloat() * seg(550f, 1200f, LinearEasing))

    Box(
        modifier
            .fillMaxSize()
            .graphicsLayer { alpha = 1f - seg(1300f, 1700f) }
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(360.dp).graphicsLayer { scaleX = tileScale; scaleY = tileScale }) {
            val side = 140.dp.toPx()
            val tl = Offset(center.x - side / 2f, center.y - side / 2f)
            val radius = side * 0.22f

            // هالة ملوّنة خلف البلاطة
            if (halo > 0f) {
                val haloR = side * (0.75f + 0.45f * halo)
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            Color(0xFF67ED54).copy(alpha = 0.45f * halo * (1f - exit)),
                            Color(0xFFB6AEFB).copy(alpha = 0.25f * halo * (1f - exit)),
                            Color.Transparent,
                        ),
                        center = center,
                        radius = haloR,
                    ),
                    radius = haloR,
                    center = center,
                )
            }

            val tile = Path().apply {
                addRoundRect(RoundRect(tl.x, tl.y, tl.x + side, tl.y + side, CornerRadius(radius)))
            }
            clipPath(tile) {
                drawTileFill(tl, side)

                // لمعة قطرية تعبر البلاطة
                if (glossAlpha > 0f) {
                    val shift = (-0.5f + 1.7f * gloss) * side
                    val start = tl + Offset(shift, shift)
                    drawRect(
                        brush = Brush.linearGradient(
                            listOf(Color.Transparent, Color.White.copy(alpha = 0.55f * glossAlpha), Color.Transparent),
                            start = start,
                            end = start + Offset(side * 0.5f, side * 0.5f),
                        ),
                        topLeft = tl,
                        size = Size(side, side),
                    )
                }
            }

            // كلمة GLOW: كشف من اليسار مع صعود طفيف
            if (wipe > 0f) {
                val w = side * 0.86f
                val h = w * 89f / 269f
                val left = tl.x + (side - w) / 2f
                val top = tl.y + (side - h) / 2f + 6.dp.toPx() * (1f - wipe)
                clipRect(left, top - 4f, left + w * wipe, top + h + 4f) {
                    translate(left, top) {
                        with(wordmark) { draw(size = Size(w, h), alpha = wipe.coerceIn(0f, 1f)) }
                    }
                }
            }
        }
    }
}

/** نفس طبقات التدرج الموجودة في ic_launcher_bg_gradient / ic_splash_tile. */
private fun DrawScope.drawTileFill(tl: Offset, side: Float) {
    val size = Size(side, side)
    drawRect(
        brush = Brush.verticalGradient(
            0f to Color(0xFFC8D2FC),
            0.45f to Color(0xFFB4CCF2),
            0.62f to Color(0xFF7CE8A8),
            1f to Color(0xFF5CEB7A),
            startY = tl.y,
            endY = tl.y + side,
        ),
        topLeft = tl,
        size = size,
    )
    fun glow(fx: Float, fy: Float, fr: Float, color: Color) = drawRect(
        brush = Brush.radialGradient(
            listOf(color, color.copy(alpha = 0f)),
            center = tl + Offset(fx * side, fy * side),
            radius = fr * side,
        ),
        topLeft = tl,
        size = size,
    )
    glow(0.15f, 0.12f, 0.60f, Color(0xFFFFFFFF))
    glow(0.90f, 0.10f, 0.55f, Color(0xFFC3BEFB))
    glow(0.10f, 0.92f, 0.60f, Color(0xFF41EC9F))
    glow(0.92f, 0.95f, 0.55f, Color(0xFFB8F900))
}
