package com.glow.app.ui.glass.motion

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import com.glow.app.ui.glass.core.LocalGlassConfig
import com.glow.app.ui.glass.lighting.LightSource
import com.glow.app.ui.glass.lighting.LightState
import com.glow.app.ui.glass.lighting.LocalLightSource
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.abs
import kotlin.math.exp

/** سرعة تتبّع الضوء للّمس (1/ثانية). */
private const val LIGHT_SPEED = 14f

/** سرعة تقدّم أثر الضغط (1/ثانية). */
private const val PRESS_SPEED = 22f

/** زيادة شدة الضوء أثناء اللمس. */
private const val TOUCH_BOOST = 0.30f

/** أطول فاصل بين إطارين يُعتد به، لتفادي قفزات بعد التوقف. */
private const val MAX_FRAME_SECONDS = 0.05f

private const val ANGLE_EPS = 0.25f
private const val INTENSITY_EPS = 0.01f
private const val PRESS_EPS = 0.005f

/**
 * بديل «بلا مؤشر» مكتوب النوع صراحةً لتفادي التباس تحميل clickable الزائد عند تمرير null.
 */
internal val NoIndication: Indication? = null

/**
 * مواصفة حركة موحّدة: فورية عند تقليل الحركة، وانتقال ناعم عدا ذلك.
 */
fun <T> glassTween(reduceMotion: Boolean, durationMillis: Int = 220): AnimationSpec<T> =
    if (reduceMotion) snap() else tween(durationMillis)

/**
 * حالة تفاعل سطح زجاجي: ضوء يتبع اللمس وأثر ضغط ناعم.
 *
 * - [light] يقرؤه الرسم عبر `light.source` (أدوات EdgeHighlight وSpecularSheen).
 * - [pressProgress] من 0 إلى 1، يُقرأ في مرحلة الرسم ولا يسبب إعادة تركيب.
 * - عند تقليل الحركة تنتقل القيم فوراً، ولا يُطبَّق تغيير الحجم.
 *
 * يُنشأ عبر [rememberGlassInteraction].
 */
@Stable
class GlassInteractionState internal constructor(
    private val rest: LightSource,
    val reduceMotion: Boolean,
) {
    /** الضوء الحالي (يُحدَّث إطاراً بإطار أثناء الحركة). */
    val light: LightState = LightState(rest)

    /** مصدر تفاعل مشترك مع clickable حتى تعمل إمكانية الوصول ولوحة المفاتيح. */
    val interactionSource: MutableInteractionSource = MutableInteractionSource()

    var isPressed by mutableStateOf(false)
        private set

    var pressProgress by mutableFloatStateOf(0f)
        private set

    /** true عندما توجد حركة تحتاج إلى إطارات. */
    internal var needsFrames by mutableStateOf(false)

    private var current: LightSource = rest
    private var target: LightSource = rest

    internal fun onTouch(touch: Offset, center: Offset) {
        val dx = touch.x - center.x
        val dy = touch.y - center.y
        val intensity = (rest.intensity + TOUCH_BOOST).coerceIn(0f, 2f)
        target = if (dx * dx + dy * dy < 1f) {
            rest.copy(intensity = intensity)
        } else {
            LightSource.fromVector(Offset(dx, dy), intensity)
        }
        if (!isPressed) isPressed = true
        needsFrames = true
    }

    internal fun onRelease() {
        if (!isPressed) return
        isPressed = false
        target = rest
        needsFrames = true
    }

    /**
     * يتقدم الضوء والضغط خطوة. يعيد true إذا بقيت حركة.
     */
    internal fun step(deltaSeconds: Float): Boolean {
        val dt = deltaSeconds.coerceIn(0f, MAX_FRAME_SECONDS)
        val lightK = if (reduceMotion) 1f else 1f - exp(-dt * LIGHT_SPEED)
        val pressK = if (reduceMotion) 1f else 1f - exp(-dt * PRESS_SPEED)

        current = current.lerp(target, lightK)
        light.snapTo(current)

        val goal = if (isPressed) 1f else 0f
        pressProgress += (goal - pressProgress) * pressK

        val settled = angleDelta(current.angleDegrees, target.angleDegrees) < ANGLE_EPS &&
            abs(current.intensity - target.intensity) < INTENSITY_EPS &&
            abs(goal - pressProgress) < PRESS_EPS

        if (settled) {
            current = target
            light.snapTo(target)
            pressProgress = goal
            needsFrames = false
        }
        return !settled
    }
}

private fun angleDelta(a: Float, b: Float): Float =
    abs(((b - a + 540f) % 360f) - 180f)

/**
 * ينشئ [GlassInteractionState] ويشغّل حلقة الإطارات فقط أثناء وجود حركة،
 * فلا يُستهلك أي إطار والسطح ساكن.
 *
 * @param rest ضوء الراحة؛ الافتراضي ضوء الشاشة الحالي.
 * @param reduceMotion الافتراضي من [LocalGlassConfig].
 */
@Composable
fun rememberGlassInteraction(
    rest: LightSource = LocalLightSource.current,
    reduceMotion: Boolean = LocalGlassConfig.current.reduceMotion,
): GlassInteractionState {
    val state = remember(rest, reduceMotion) { GlassInteractionState(rest, reduceMotion) }

    LaunchedEffect(state) {
        snapshotFlow { state.needsFrames }.collectLatest { needs ->
            if (!needs) return@collectLatest
            var last = withFrameNanos { it }
            while (true) {
                val now = withFrameNanos { it }
                val dt = (now - last) / 1_000_000_000f
                last = now
                if (!state.step(dt)) break
            }
        }
    }
    return state
}

/**
 * يراقب اللمس دون استهلاكه (في مرحلة Initial)، فيعمل معه أي clickable.
 * يحرّك الضوء نحو الإصبع أثناء الضغط ويعيده عند الرفع.
 */
fun Modifier.glassPressLight(state: GlassInteractionState): Modifier =
    pointerInput(state) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull() ?: continue
                if (change.pressed) {
                    state.onTouch(
                        touch = change.position,
                        center = Offset(size.width / 2f, size.height / 2f),
                    )
                } else {
                    state.onRelease()
                }
            }
        }
    }

/**
 * يصغّر العنصر قليلاً أثناء الضغط. لا يفعل شيئاً عند تقليل الحركة.
 * القراءة داخل graphicsLayer فلا تسبب إعادة تركيب.
 */
fun Modifier.glassPressScale(
    state: GlassInteractionState,
    pressedScale: Float = 0.97f,
): Modifier {
    if (state.reduceMotion) return this
    return graphicsLayer {
        val scale = 1f + (pressedScale - 1f) * state.pressProgress
        scaleX = scale
        scaleY = scale
    }
}

/**
 * نقر بلا تموّج افتراضي (ملاحظات الضغط تأتي من الضوء والحجم)، مع الحفاظ على
 * دلالات إمكانية الوصول والتركيز.
 */
fun Modifier.glassClickable(
    state: GlassInteractionState,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier = clickable(
    interactionSource = state.interactionSource,
    indication = NoIndication,
    enabled = enabled,
    onClickLabel = onClickLabel,
    role = role,
    onClick = onClick,
)
