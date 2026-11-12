package com.glow.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * ألوان الواجهة في الوضعين الداكن والفاتح. تُقرأ في أي Composable عبر
 * `LocalGlowSkin.current`.
 *
 * لوحة Glow: 07141C و12303D (الداكن)، 06B6D4 و67E8F9 (التمييز)، F0FDFF (الفاتح).
 * اللون 0E7490 مشتق من 06B6D4 للنصوص على الخلفية الفاتحة، لأن 06B6D4 وحده ضعيف التباين هناك.
 */
@Immutable
data class GlowSkin(
    val isDark: Boolean,
    val bgTop: Color,
    val bgBottom: Color,
    val text: Color,
    val textSecondary: Color,
    /** لون التعبئة: الأزرار والتقدم. */
    val accent: Color,
    /** لون المحتوى فوق [accent]. */
    val onAccent: Color,
    /** لون التمييز للنصوص والأيقونات. */
    val accentText: Color,
    /** لوح القوائم. */
    val panel: Color,
    /** خلفية الأزرار الثانوية وحقل البحث. */
    val chip: Color,
    val divider: Color,
    val glassTint: Color,
    val glassBase: Color,
    val glassBorder: Color,
)

object GlowSkins {
    private val Night = Color(0xFF07141C)
    private val Teal = Color(0xFF12303D)
    private val Cyan = Color(0xFF06B6D4)
    private val Ice = Color(0xFF67E8F9)
    private val Frost = Color(0xFFF0FDFF)
    private val FrostDeep = Color(0xFFC7F7FD)
    private val CyanDeep = Color(0xFF0E7490)

    val Dark = GlowSkin(
        isDark = true,
        bgTop = Night,
        bgBottom = Teal,
        text = Frost,
        textSecondary = Frost.copy(alpha = 0.65f),
        accent = Cyan,
        onAccent = Night,
        accentText = Ice,
        panel = Frost.copy(alpha = 0.06f),
        chip = Frost.copy(alpha = 0.12f),
        divider = Frost.copy(alpha = 0.08f),
        glassTint = Frost,
        glassBase = Teal,
        glassBorder = Color.White,
    )

    val Light = GlowSkin(
        isDark = false,
        bgTop = Frost,
        bgBottom = FrostDeep,
        text = Night,
        textSecondary = Teal.copy(alpha = 0.72f),
        accent = Cyan,
        onAccent = Night,
        accentText = CyanDeep,
        panel = Teal.copy(alpha = 0.06f),
        chip = Teal.copy(alpha = 0.10f),
        divider = Teal.copy(alpha = 0.10f),
        glassTint = Color.White,
        glassBase = Frost,
        glassBorder = Teal,
    )
}

val LocalGlowSkin = staticCompositionLocalOf { GlowSkins.Dark }
