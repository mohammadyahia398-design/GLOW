package com.glow.app.ui.theme

import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** الحركة: ناعمة، فيزيائية، وبدون مبالغة. */
object GlowMotion {
    const val Instant = 100
    const val Fast = 180
    const val Medium = 320
    const val Slow = 560

    /** دورة التنفس الكاملة 10 ثوانٍ = 6 أنفاس في الدقيقة (إيقاع استرخاء). */
    const val BreathCycleMillis = 10_000

    val Emphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

    /** نابض ناعم للعناصر الكبيرة (الكبسولة، الشريط العائم). */
    fun <T> softSpring(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow,
    )

    /** نابض سريع بلا ارتداد لتفاعلات اللمس. */
    fun <T> snappySpring(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )

    fun <T> fade(durationMillis: Int = Medium): FiniteAnimationSpec<T> =
        tween(durationMillis = durationMillis, easing = Emphasized)

    /** تنفس التوهج: استخدمها مع rememberInfiniteTransition().animateFloat. */
    fun breathing(): InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(durationMillis = BreathCycleMillis / 2, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse,
    )
}

/**
 * هل أوقف المستخدم الحركة من إعدادات النظام؟
 * إن كانت true فلا تشغّل الحركات الزخرفية المتكررة (التنفس، التوهج المتحرك).
 */
@Composable
fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }
}
