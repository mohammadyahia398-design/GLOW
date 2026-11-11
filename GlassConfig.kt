package com.glow.app.ui.glass.core

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * رموز تصميم لسطح زجاجي واحد. كل القيم نسب بين 0 و1 ما لم يُذكر غير ذلك.
 *
 * @property blurRadius نصف قطر التمويه (يُتجاهل في [GlassTier.Fallback]).
 * @property tintAlpha شفافية التلوين عندما يتوفر Blur.
 * @property fallbackTintAlpha شفافية أعلى تعوّض غياب Blur في المستوى البديل.
 * @property borderAlpha شفافية الحد الزجاجي الرفيع.
 * @property highlightAlpha قوة إضاءة الحواف.
 * @property specularAlpha قوة اللمعان الداخلي.
 * @property refractionStrength قوة الانكسار (تُستخدم في [GlassTier.Liquid] فقط).
 */
@Immutable
data class GlassStyle(
    val blurRadius: Dp,
    val tintAlpha: Float,
    val fallbackTintAlpha: Float,
    val borderAlpha: Float,
    val highlightAlpha: Float,
    val specularAlpha: Float,
    val refractionStrength: Float,
) {
    init {
        require(blurRadius >= 0.dp) { "blurRadius must be >= 0" }
        require(tintAlpha in 0f..1f) { "tintAlpha must be in 0..1" }
        require(fallbackTintAlpha in 0f..1f) { "fallbackTintAlpha must be in 0..1" }
        require(borderAlpha in 0f..1f) { "borderAlpha must be in 0..1" }
        require(highlightAlpha in 0f..1f) { "highlightAlpha must be in 0..1" }
        require(specularAlpha in 0f..1f) { "specularAlpha must be in 0..1" }
        require(refractionStrength in 0f..1f) { "refractionStrength must be in 0..1" }
    }

    /** نصف قطر التمويه الفعلي بحسب المستوى. */
    fun effectiveBlur(tier: GlassTier): Dp =
        if (tier.supportsBlur) blurRadius else 0.dp

    /** شفافية التلوين الفعلية بحسب المستوى. */
    fun effectiveTintAlpha(tier: GlassTier): Float =
        if (tier.supportsBlur) tintAlpha else fallbackTintAlpha

    /** قوة الانكسار الفعلية بحسب المستوى. */
    fun effectiveRefraction(tier: GlassTier): Float =
        if (tier.supportsRefraction) refractionStrength else 0f

    companion object {
        /** زجاج رقيق: عناصر صغيرة وأزرار. */
        val Thin = GlassStyle(
            blurRadius = 12.dp,
            tintAlpha = 0.06f,
            fallbackTintAlpha = 0.16f,
            borderAlpha = 0.14f,
            highlightAlpha = 0.28f,
            specularAlpha = 0.12f,
            refractionStrength = 0.40f,
        )

        /** الزجاج القياسي: بطاقات وشريط التنقل. */
        val Regular = GlassStyle(
            blurRadius = 24.dp,
            tintAlpha = 0.10f,
            fallbackTintAlpha = 0.22f,
            borderAlpha = 0.18f,
            highlightAlpha = 0.35f,
            specularAlpha = 0.16f,
            refractionStrength = 0.60f,
        )

        /** زجاج سميك: أسطح كبيرة ولوحات منبثقة. */
        val Thick = GlassStyle(
            blurRadius = 40.dp,
            tintAlpha = 0.16f,
            fallbackTintAlpha = 0.32f,
            borderAlpha = 0.22f,
            highlightAlpha = 0.40f,
            specularAlpha = 0.18f,
            refractionStrength = 0.80f,
        )

        /** زجاج شبه شفاف: يُبرز الانكسار على حساب التمويه. */
        val Clear = GlassStyle(
            blurRadius = 6.dp,
            tintAlpha = 0.03f,
            fallbackTintAlpha = 0.10f,
            borderAlpha = 0.12f,
            highlightAlpha = 0.25f,
            specularAlpha = 0.10f,
            refractionStrength = 1.00f,
        )
    }
}

/**
 * إعدادات نظام الزجاج الحالية، تُمرَّر عبر [LocalGlassConfig].
 *
 * @property tier المستوى المتاح على الجهاز.
 * @property tint لون التلوين الزجاجي (الافتراضي Ivory من لوحة Glow).
 * @property fallbackBase قاعدة داكنة تُرسم تحت التلوين في المستوى البديل.
 * @property shadowColor لون الظلال.
 * @property reduceMotion عندما تكون true تُلغى الحركة الزخرفية.
 */
@Immutable
data class GlassConfig(
    val tier: GlassTier,
    val tint: Color = DefaultTint,
    val fallbackBase: Color = DefaultFallbackBase,
    val shadowColor: Color = Color.Black,
    val reduceMotion: Boolean = false,
) {
    companion object {
        /** Ivory #F5F6FA. */
        val DefaultTint = Color(0xFFF5F6FA)

        /** قاعدة داكنة قريبة من Charcoal للمستوى البديل. */
        val DefaultFallbackBase = Color(0xFF14161C)
    }
}

/** القيمة الافتراضية متحفظة: المستوى البديل، إلى أن يوفّر [ProvideGlassConfig] القيمة الحقيقية. */
val LocalGlassConfig = staticCompositionLocalOf { GlassConfig(tier = GlassTier.Fallback) }

/** يوفّر [GlassConfig] لكل ما بداخله. */
@Composable
fun ProvideGlassConfig(
    config: GlassConfig = GlassConfig(
        tier = rememberGlassTier(),
        reduceMotion = rememberSystemReduceMotion(),
    ),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalGlassConfig provides config, content = content)
}

/**
 * true عندما يكون مقياس مدة الحركة في النظام مساوياً للصفر
 * (خيار «إزالة الرسوم المتحركة» أو إعداد المطور).
 */
@Composable
fun rememberSystemReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }
}
