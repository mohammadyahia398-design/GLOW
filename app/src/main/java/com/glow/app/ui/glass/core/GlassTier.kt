package com.glow.app.ui.glass.core

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * مستوى جودة الزجاج المتاح على الجهاز الحالي.
 *
 * - [Liquid]   : API 33+ — Blur حقيقي مع انكسار AGSL عند الحواف.
 * - [Frosted]  : API 31-32 — Blur حقيقي دون انكسار.
 * - [Fallback] : API 26-30 أو جهاز ضعيف — تدرجات وحواف مضيئة دون Blur حي.
 *
 * ترتيب العناصر مهم: الأعلى ترتيباً هو الأغنى بصرياً.
 */
enum class GlassTier {
    Fallback,
    Frosted,
    Liquid;

    /** هل يمكن استخدام RenderEffect للتمويه الحقيقي. */
    val supportsBlur: Boolean get() = this != Fallback

    /** هل يمكن استخدام RuntimeShader (AGSL) للانكسار. */
    val supportsRefraction: Boolean get() = this == Liquid
}

/**
 * منطق اختيار المستوى. الدالة [resolveTier] نقية (لا تعتمد على Context)
 * ليسهل اختبارها بوحدة اختبار عادية.
 */
object GlassCapabilities {

    /** أول إصدار يدعم RenderEffect.createBlurEffect (Android 12). */
    const val BLUR_MIN_SDK: Int = Build.VERSION_CODES.S

    /** أول إصدار يدعم RuntimeShader / AGSL (Android 13). */
    const val REFRACTION_MIN_SDK: Int = Build.VERSION_CODES.TIRAMISU

    /**
     * @param sdkInt إصدار API للجهاز.
     * @param isLowRamDevice جهاز منخفض الذاكرة: يُجبر على [GlassTier.Fallback].
     * @param enabled مفتاح تعطيل يدوي (مثلاً من الإعدادات).
     * @param ceiling سقف أعلى للمستوى، مفيد للاختبار أو لتوفير البطارية.
     */
    fun resolveTier(
        sdkInt: Int,
        isLowRamDevice: Boolean,
        enabled: Boolean = true,
        ceiling: GlassTier = GlassTier.Liquid,
    ): GlassTier {
        if (!enabled || isLowRamDevice) return GlassTier.Fallback

        val bySdk = when {
            sdkInt >= REFRACTION_MIN_SDK -> GlassTier.Liquid
            sdkInt >= BLUR_MIN_SDK -> GlassTier.Frosted
            else -> GlassTier.Fallback
        }
        return if (bySdk.ordinal <= ceiling.ordinal) bySdk else ceiling
    }

    /** يكتشف المستوى من الجهاز الفعلي. */
    fun detect(
        context: Context,
        enabled: Boolean = true,
        ceiling: GlassTier = GlassTier.Liquid,
    ): GlassTier {
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        return resolveTier(
            sdkInt = Build.VERSION.SDK_INT,
            isLowRamDevice = activityManager?.isLowRamDevice ?: false,
            enabled = enabled,
            ceiling = ceiling,
        )
    }
}

/** يحسب المستوى مرة واحدة لكل تركيبة ويحتفظ به عبر إعادة التركيب. */
@Composable
fun rememberGlassTier(
    enabled: Boolean = true,
    ceiling: GlassTier = GlassTier.Liquid,
): GlassTier {
    val context = LocalContext.current
    return remember(context, enabled, ceiling) {
        GlassCapabilities.detect(context, enabled, ceiling)
    }
}
