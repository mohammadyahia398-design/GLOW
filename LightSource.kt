package com.glow.app.ui.glass.lighting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * مصدر ضوء ثنائي الأبعاد.
 *
 * الزاوية بالدرجات ومقيسة في إحداثيات الشاشة (محور y للأسفل):
 * 0 = يمين، 90 = أسفل، 180 = يسار، 270 = أعلى.
 * الافتراضي 225° أي ضوء قادم من أعلى اليسار.
 *
 * @property angleDegrees اتجاه الضوء من السطح نحو المصدر.
 * @property intensity شدة الإضاءة من 0 إلى 2 (1 = عادي).
 */
@Immutable
data class LightSource(
    val angleDegrees: Float,
    val intensity: Float = 1f,
) {
    init {
        require(intensity in 0f..2f) { "intensity must be in 0..2" }
    }

    /** متجه وحدة يشير من السطح نحو المصدر. */
    val towardLight: Offset
        get() {
            val radians = Math.toRadians(angleDegrees.toDouble())
            return Offset(cos(radians).toFloat(), sin(radians).toFloat())
        }

    /**
     * مدى مواجهة حافة للضوء، من -1 (معاكسة تماماً) إلى 1 (مواجهة تماماً).
     *
     * @param normal العمودي الخارج من الحافة، لا يشترط أن يكون وحدة.
     */
    fun facing(normal: Offset): Float {
        val length = sqrt(normal.x * normal.x + normal.y * normal.y)
        if (length < EPSILON) return 0f
        val light = towardLight
        val dot = (normal.x / length) * light.x + (normal.y / length) * light.y
        return dot.coerceIn(-1f, 1f)
    }

    /**
     * قوة إضاءة الحافة من 0 إلى [intensity]، بحيث تكون الحافة المواجهة أقوى
     * والمعاكسة معدومة. تُستخدم لرسم Edge Highlight.
     */
    fun rimStrength(normal: Offset): Float =
        facing(normal).coerceAtLeast(0f) * intensity

    /** يمزج مع مصدر آخر عبر أقصر قوس زاوي. */
    fun lerp(other: LightSource, fraction: Float): LightSource {
        val f = fraction.coerceIn(0f, 1f)
        val delta = ((other.angleDegrees - angleDegrees + 540f) % 360f) - 180f
        return LightSource(
            angleDegrees = normalizeDegrees(angleDegrees + delta * f),
            intensity = intensity + (other.intensity - intensity) * f,
        )
    }

    companion object {
        private const val EPSILON = 1e-6f

        /** ضوء من أعلى اليسار. */
        val Default = LightSource(angleDegrees = 225f, intensity = 1f)

        /** ينشئ مصدراً من متجه اتجاه (لا يشترط أن يكون وحدة). */
        fun fromVector(direction: Offset, intensity: Float = 1f): LightSource {
            val length = sqrt(direction.x * direction.x + direction.y * direction.y)
            if (length < EPSILON) return Default.copy(intensity = intensity)
            val degrees = Math.toDegrees(atan2(direction.y, direction.x).toDouble()).toFloat()
            return LightSource(normalizeDegrees(degrees), intensity)
        }
    }
}

private fun normalizeDegrees(value: Float): Float {
    val m = value % 360f
    return if (m < 0f) m + 360f else m
}

/**
 * حالة إضاءة تفاعلية: تنتقل نحو نقطة اللمس وتعود إلى وضع الراحة.
 *
 * الانتقال هنا فوري. التنعيم الزمني يتولاه GlassInteraction بتحديث الحالة
 * عبر [snapTo] في كل إطار، فلا تتكرر منطق الحركة في طبقتين.
 */
@Stable
class LightState(private val rest: LightSource = LightSource.Default) {

    private var angle by mutableFloatStateOf(rest.angleDegrees)
    private var strength by mutableFloatStateOf(rest.intensity)

    /** المصدر الحالي؛ قراءته داخل Composable أو رسم تشترك في الحالة. */
    val source: LightSource
        get() = LightSource(angleDegrees = angle, intensity = strength)

    /**
     * يوجّه الضوء نحو نقطة اللمس.
     *
     * @param touch موضع اللمس.
     * @param center مركز السطح بنفس نظام الإحداثيات.
     * @param boost زيادة الشدة أثناء اللمس.
     */
    fun pointToward(touch: Offset, center: Offset, boost: Float = 0.25f) {
        val dx = touch.x - center.x
        val dy = touch.y - center.y
        if (sqrt(dx * dx + dy * dy) < 1f) return
        val degrees = Math.toDegrees(atan2(dy, dx).toDouble()).toFloat()
        angle = normalizeDegrees(degrees)
        strength = (rest.intensity + boost).coerceIn(0f, 2f)
    }

    /** يضبط الحالة مباشرة على مصدر معيّن. */
    fun snapTo(target: LightSource) {
        angle = target.angleDegrees
        strength = target.intensity
    }

    /** يعيد الضوء إلى وضع الراحة. */
    fun release() = snapTo(rest)
}

/** ينشئ [LightState] ويحتفظ به عبر إعادة التركيب. */
@Composable
fun rememberLightState(rest: LightSource = LightSource.Default): LightState =
    remember(rest) { LightState(rest) }

/** مصدر الإضاءة العام للشاشة، يمكن تغييره لكل شاشة أو مكوّن. */
val LocalLightSource = compositionLocalOf { LightSource.Default }
