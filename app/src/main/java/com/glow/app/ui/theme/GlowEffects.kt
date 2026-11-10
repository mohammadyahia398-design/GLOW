package com.glow.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

/** هالة توهج شعاعية خلف العنصر. */
fun Modifier.glowHalo(
    color: Color,
    radius: Dp = Dp.Unspecified,
    alpha: Float = 0.35f,
): Modifier = drawBehind {
    val r = if (radius == Dp.Unspecified) size.minDimension else radius.toPx()
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = alpha), Color.Transparent),
            center = center,
            radius = r,
        ),
        radius = r,
        center = center,
    )
}

/** إطار شعري رفيع بلون Ivory شفاف. */
fun Modifier.hairlineBorder(shape: Shape, color: Color = GlowPalette.Hairline): Modifier =
    border(width = GlowDimens().hairline, color = color, shape = shape)

/**
 * سطح زجاجي (Glassmorphism): تعبئة شبه شفافة + إطار متدرج يلمع من الأعلى.
 * هذا تقريب بصري خفيف الأداء، وليس Blur حقيقياً للخلفية.
 */
fun Modifier.glassSurface(
    shape: Shape = GlowShapes.Pill,
    fill: Color = GlowPalette.GlassFill,
): Modifier = this
    .clip(shape)
    .background(fill)
    .border(
        width = GlowDimens().hairline,
        brush = Brush.verticalGradient(
            listOf(GlowPalette.GlassHighlight, GlowPalette.Hairline),
        ),
        shape = shape,
    )

/** خلفية التطبيق بجو Glow: تدرج داكن + توهج Amber علوي وTwilight سفلي. */
@Composable
fun GlowBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GlowGradients.Backdrop)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(GlowPalette.Amber.copy(alpha = 0.10f), Color.Transparent),
                        center = Offset(size.width * 0.10f, 0f),
                        radius = size.minDimension * 0.9f,
                    ),
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(GlowPalette.TwilightBlue.copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(size.width * 0.95f, size.height),
                        radius = size.minDimension * 1.1f,
                    ),
                )
            },
        content = content,
    )
}
