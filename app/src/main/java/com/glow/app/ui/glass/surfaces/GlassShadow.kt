package com.glow.app.ui.glass.surfaces

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.glow.app.ui.glass.core.GlassDepth
import com.glow.app.ui.glass.core.GlassTier

/**
 * طبقة ظل واحدة.
 *
 * @property offsetY إزاحة رأسية للأسفل.
 * @property blur مدى التلاشي خارج الحافة.
 * @property spread توسيع (أو تقليص إذا كان سالباً) الشكل قبل التلاشي.
 * @property alpha أقصى شفافية عند الحافة.
 */
@Immutable
data class ShadowLayer(
    val offsetY: Dp,
    val blur: Dp,
    val spread: Dp,
    val alpha: Float,
)

/** مواصفات الظلال لكل عمق، وعدد خطوات التلاشي لكل مستوى جودة. */
object GlassShadowSpec {

    private val raised = listOf(
        ShadowLayer(offsetY = 2.dp, blur = 6.dp, spread = 0.dp, alpha = 0.18f),
        ShadowLayer(offsetY = 1.dp, blur = 2.dp, spread = 0.dp, alpha = 0.12f),
    )

    private val floating = listOf(
        ShadowLayer(offsetY = 12.dp, blur = 24.dp, spread = (-2).dp, alpha = 0.30f),
        ShadowLayer(offsetY = 4.dp, blur = 10.dp, spread = 0.dp, alpha = 0.20f),
        ShadowLayer(offsetY = 1.dp, blur = 2.dp, spread = 0.dp, alpha = 0.12f),
    )

    private val overlay = listOf(
        ShadowLayer(offsetY = 20.dp, blur = 40.dp, spread = (-4).dp, alpha = 0.38f),
        ShadowLayer(offsetY = 8.dp, blur = 16.dp, spread = 0.dp, alpha = 0.24f),
        ShadowLayer(offsetY = 2.dp, blur = 4.dp, spread = 0.dp, alpha = 0.14f),
    )

    fun layersFor(depth: GlassDepth): List<ShadowLayer> = when (depth) {
        GlassDepth.Flush -> emptyList()
        GlassDepth.Raised -> raised
        GlassDepth.Floating -> floating
        GlassDepth.Overlay -> overlay
    }

    /** عدد خطوات التلاشي لكل طبقة؛ أقل في المستويات الأضعف لتوفير الرسم. */
    fun stepsFor(tier: GlassTier): Int = when (tier) {
        GlassTier.Liquid -> 8
        GlassTier.Frosted -> 6
        GlassTier.Fallback -> 4
    }
}

/**
 * يرسم ظلاً ناعماً متعدد الطبقات خلف الشكل [shape].
 *
 * الطريقة: كل طبقة تُرسم كعدة أشكال موسّعة تدريجياً بشفافية متراكمة،
 * فتنتج تلاشياً خطياً دون الاعتماد على setShadowLayer الذي لا يدعم
 * التسريع العتادي لغير النص قبل API 28.
 *
 * داخل الشكل نفسه يُقتطع الظل (ClipOp.Difference)، لأن السطح زجاجي شفاف
 * ولا يجوز أن يظهر الظل من خلاله.
 *
 * الأشكال المستطيلة والمستديرة (ومنها الكبسولة والدائرة) تُوسَّع هندسياً بدقة.
 * الأشكال العامة ([Outline.Generic]) تُقرَّب بالتعبئة مع حد عريض، وقد تظهر
 * فيها كثافة أعلى قليلاً قرب الحافة.
 *
 * يُرسم الظل خارج حدود المكوّن، فيجب ألا يقصّه أب يستخدم clipToBounds.
 *
 * @param tier مستوى الجودة؛ يحدد عدد خطوات التلاشي.
 * @param color لون الظل.
 * @param enabled false لتعطيل الظل بالكامل.
 */
fun Modifier.glassShadow(
    shape: Shape,
    depth: GlassDepth,
    tier: GlassTier,
    color: Color = Color.Black,
    enabled: Boolean = true,
): Modifier {
    if (!enabled || !depth.castsShadow) return this

    val layers = GlassShadowSpec.layersFor(depth)
    val stepCount = GlassShadowSpec.stepsFor(tier)

    return drawWithCache {
        val outline = shape.createOutline(size, layoutDirection, this)
        val holePath = outline.toClipPath()
        val steps = buildShadowSteps(outline, layers, stepCount, color, this)

        onDrawBehind {
            clipPath(holePath, ClipOp.Difference) {
                for (step in steps) {
                    drawPath(path = step.path, color = step.color)
                    if (step.strokeWidth > 0f) {
                        drawPath(
                            path = step.path,
                            color = step.color,
                            style = Stroke(width = step.strokeWidth, join = StrokeJoin.Round),
                        )
                    }
                }
            }
        }
    }
}

/** خطوة رسم جاهزة: مسار وشفافية وعرض حد (0 = تعبئة فقط). */
private class ShadowStep(
    val path: Path,
    val color: Color,
    val strokeWidth: Float,
)

private fun buildShadowSteps(
    outline: Outline,
    layers: List<ShadowLayer>,
    stepCount: Int,
    color: Color,
    density: Density,
): List<ShadowStep> {
    val result = ArrayList<ShadowStep>(layers.size * stepCount)
    with(density) {
        for (layer in layers) {
            val offsetY = layer.offsetY.toPx()
            val blur = layer.blur.toPx()
            val spread = layer.spread.toPx()
            val stepAlpha = (color.alpha * layer.alpha / stepCount).coerceIn(0f, 1f)
            val stepColor = color.copy(alpha = stepAlpha)

            for (i in 1..stepCount) {
                val extent = spread + blur * (i.toFloat() / stepCount)
                val step = createStep(outline, extent, offsetY, stepColor)
                if (step != null) result += step
            }
        }
    }
    return result
}

private fun createStep(
    outline: Outline,
    extent: Float,
    offsetY: Float,
    color: Color,
): ShadowStep? = when (outline) {
    is Outline.Rectangle -> {
        val r = outline.rect
        if (r.width + 2f * extent <= 0f || r.height + 2f * extent <= 0f) {
            null
        } else {
            val path = Path().apply {
                addRect(
                    Rect(
                        left = r.left - extent,
                        top = r.top - extent + offsetY,
                        right = r.right + extent,
                        bottom = r.bottom + extent + offsetY,
                    ),
                )
            }
            ShadowStep(path, color, 0f)
        }
    }

    is Outline.Rounded -> {
        val inflated = outline.roundRect.inflateBy(extent)
        if (inflated.width <= 0f || inflated.height <= 0f) {
            null
        } else {
            val path = Path().apply {
                addRoundRect(inflated.translate(Offset(0f, offsetY)))
            }
            ShadowStep(path, color, 0f)
        }
    }

    is Outline.Generic -> {
        val path = Path().apply { addPath(outline.path, Offset(0f, offsetY)) }
        ShadowStep(path, color, if (extent > 0f) extent * 2f else 0f)
    }
}

private fun Outline.toClipPath(): Path = when (this) {
    is Outline.Rectangle -> Path().apply { addRect(rect) }
    is Outline.Rounded -> Path().apply { addRoundRect(roundRect) }
    is Outline.Generic -> path
}

private fun RoundRect.inflateBy(delta: Float): RoundRect = RoundRect(
    left = left - delta,
    top = top - delta,
    right = right + delta,
    bottom = bottom + delta,
    topLeftCornerRadius = topLeftCornerRadius.grow(delta),
    topRightCornerRadius = topRightCornerRadius.grow(delta),
    bottomRightCornerRadius = bottomRightCornerRadius.grow(delta),
    bottomLeftCornerRadius = bottomLeftCornerRadius.grow(delta),
)

private fun CornerRadius.grow(delta: Float): CornerRadius =
    CornerRadius((x + delta).coerceAtLeast(0f), (y + delta).coerceAtLeast(0f))
