package com.glow.app.ui.glass.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.glow.app.ui.glass.core.GlassDepth
import com.glow.app.ui.glass.core.GlassStyle
import com.glow.app.ui.glass.core.LocalGlassConfig
import com.glow.app.ui.glass.motion.glassTween
import com.glow.app.ui.glass.surfaces.GlassBackdrop
import com.glow.app.ui.glass.surfaces.GlassSurface
import com.glow.app.ui.theme.LocalGlowSkin
import kotlin.math.abs

private val DefaultAccent = Color(0xFF06B6D4)

/** أقصى تمدد أفقي عند السحب خارج الطرفين (كنسبة من العرض). */
private const val RUBBER_MAX = 0.06f

/** ليونة المقاومة: كلما كبر الرقم زادت الحرية قبل الوصول للحد الأقصى. */
private const val RUBBER_SOFTNESS = 0.30f

/** زيادة سُمك المنزلق أثناء السحب. */
private const val DRAG_THICKNESS = 1.16f

/**
 * منزلق صوت زجاجي مرن (Flex Volume).
 *
 * السلوك:
 * - لمسة أو سحب يغيّر القيمة من 0 إلى 1.
 * - أثناء السحب يزداد سُمك المنزلق قليلاً.
 * - السحب إلى ما بعد الطرفين يمدّ المنزلق بمقاومة مطاطية، وعند الرفع يرتد بنابض.
 * - في RTL تنعكس الاتجاهات (القيمة الأعلى جهة اليسار).
 * - عند تقليل الحركة لا يوجد سُمك متحرك ولا ارتداد.
 *
 * يدعم إمكانية الوصول: تقدّم قارئات الشاشة القيمة وتغيّرها عبر الإيماءات.
 *
 * @param value القيمة الحالية بين 0 و1.
 * @param onValueChange يُستدعى بقيمة مقيّدة بين 0 و1.
 * @param onValueChangeFinished عند رفع الإصبع.
 * @param icon أيقونة اختيارية في بداية المنزلق.
 */
@Composable
fun GlassSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: GlassBackdrop? = null,
    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,
    contentDescription: String? = null,
    accent: Color = LocalGlowSkin.current.accent,
    trackHeight: Dp = 44.dp,
    icon: (@Composable () -> Unit)? = null,
) {
    val reduceMotion = LocalGlassConfig.current.reduceMotion
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val description = contentDescription

    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentOnFinished by rememberUpdatedState(onValueChangeFinished)

    var dragging by remember { mutableStateOf(false) }
    var stretchTarget by remember { mutableFloatStateOf(0f) }

    // أثناء السحب يتبع التمدد الإصبع فوراً، وعند الرفع يرتد بنابض.
    val stretch by animateFloatAsState(
        targetValue = stretchTarget,
        animationSpec = if (dragging || reduceMotion) {
            snap()
        } else {
            spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow)
        },
        label = "glassSliderStretch",
    )
    val thickness by animateFloatAsState(
        targetValue = if (dragging && !reduceMotion) DRAG_THICKNESS else 1f,
        animationSpec = glassTween(reduceMotion),
        label = "glassSliderThickness",
    )

    val fillShape = RoundedCornerShape(percent = 50)

    GlassSurface(
        modifier = modifier
            .height(trackHeight)
            .graphicsLayer {
                val s = stretch
                scaleX = 1f + abs(s)
                scaleY = thickness
                // يبقى الطرف المعاكس للتمدد ثابتاً.
                val pivotX = if (s >= 0f) {
                    if (rtl) 1f else 0f
                } else {
                    if (rtl) 0f else 1f
                }
                transformOrigin = TransformOrigin(pivotX, 0.5f)
            }
            .semantics {
                if (description != null) this.contentDescription = description
                progressBarRangeInfo = ProgressBarRangeInfo(value.coerceIn(0f, 1f), 0f..1f)
                if (enabled) {
                    setProgress { target ->
                        currentOnValueChange(target.coerceIn(0f, 1f))
                        true
                    }
                }
            }
            .pointerInput(enabled, rtl) {
                if (!enabled) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val width = size.width.toFloat().coerceAtLeast(1f)

                    fun toRaw(x: Float): Float = if (rtl) 1f - x / width else x / width

                    fun report(raw: Float) {
                        val clamped = raw.coerceIn(0f, 1f)
                        stretchTarget = rubberBand(raw - clamped)
                        currentOnValueChange(clamped)
                    }

                    dragging = true
                    report(toRaw(down.position.x))
                    down.consume()

                    horizontalDrag(down.id) { change ->
                        report(toRaw(change.position.x))
                        change.consume()
                    }

                    dragging = false
                    stretchTarget = 0f
                    currentOnFinished?.invoke()
                }
            },
        backdrop = backdrop,
        shape = fillShape,
        style = GlassStyle.Thin,
        depth = GlassDepth.Raised,
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .fillMaxWidth(fraction = value.coerceIn(0f, 1f))
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(accent.copy(alpha = 0.80f), accent),
                    ),
                    shape = fillShape,
                ),
        )
        if (icon != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp),
            ) {
                icon()
            }
        }
    }
}

/**
 * مقاومة مطاطية: كلما زاد التجاوز زاد التمدد لكن بنسبة متناقصة حتى [RUBBER_MAX].
 * الإشارة الموجبة تعني تجاوز الطرف الأعلى، والسالبة تجاوز الطرف الأدنى.
 */
private fun rubberBand(overshoot: Float): Float {
    if (overshoot == 0f) return 0f
    val magnitude = RUBBER_MAX * (1f - 1f / (1f + abs(overshoot) / RUBBER_SOFTNESS))
    return if (overshoot > 0f) magnitude else -magnitude
}
