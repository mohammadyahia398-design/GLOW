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
    /** سطح النوافذ المنبثقة. */
    val sheet: Color,
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
    private val Ink = Color(0xFF040B10)
    private val InkDeep = Color(0xFF0B1E28)

    val Dark = GlowSkin(
        isDark = true,
        bgTop = Ink,
        bgBottom = InkDeep,
        text = Frost,
        textSecondary = Color(0xFF9FB3BC),
        accent = Cyan,
        onAccent = Night,
        accentText = Ice,
        panel = Color(0xFF101C23),
        chip = Color(0xFF1B2B35),
        divider = Frost.copy(alpha = 0.07f),
        sheet = Color(0xFF16252E),
        glassTint = Frost,
        glassBase = InkDeep,
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
        panel = Color.White.copy(alpha = 0.78f),
        chip = Teal.copy(alpha = 0.10f),
        divider = Teal.copy(alpha = 0.10f),
        sheet = Color.White,
        glassTint = Color.White,
        glassBase = Frost,
        glassBorder = Teal,
    )
}

val LocalGlowSkin = staticCompositionLocalOf { GlowSkins.Dark }
