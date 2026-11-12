package com.glow.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * الألوان الخام لهوية Glow — Luminous Minimalism.
 *
 * الألوان الستة الأولى مأخوذة حرفياً من المرجع البصري،
 * أما المشتقة (Night, Surface, ...) فهي درجات مدروسة من Charcoal لبناء عمق الواجهة.
 */
object GlowPalette {
    // ── الأساسية (من المرجع البصري) ─────────────────────────
    val Charcoal = Color(0xFF07141C)
    // الاسم Amber محفوظ للتوافق مع بقية الكود؛ القيمة الآن Cyan الهوية الجديدة.
    val Amber = Color(0xFF06B6D4)
    val Coral = Color(0xFFFF6B6B)
    val TwilightBlue = Color(0xFF4A69BD)
    val Ivory = Color(0xFFF0FDFF)
    val Muted = Color(0xFF8C92A4)

    // ── مشتقة ───────────────────────────────────────────────
    val Teal = Color(0xFF5FB3A6)           // الطبيعة والهدوء
    val TwilightSoft = Color(0xFF8FA6E8)   // نسخة مقروءة من Twilight على الخلفية الداكنة
    val Night = Color(0xFF12303D)
    val Surface = Color(0xFF1A1D25)
    val SurfaceLifted = Color(0xFF222632)
    val OutlineSolid = Color(0xFF2A2E3A)

    // ── شفافيات الزجاج ──────────────────────────────────────
    val Hairline = Color(0x1FF5F6FA)        // Ivory 12%
    val GlassFill = Color(0xA6141620)       // Night 65%
    val GlassHighlight = Color(0x4DF5F6FA)  // Ivory 30%
}

/** ألوان الحالات المزاجية (Mood) للبطاقات والتدرجات. */
object GlowMoodColors {
    val Sleep = GlowPalette.Amber
    val Focus = GlowPalette.TwilightBlue
    val Reflect = GlowPalette.Coral
    val Nature = GlowPalette.Teal
}

/** تدرجات جاهزة. */
object GlowGradients {
    /** خلفية التطبيق الأساسية. */
    val Backdrop: Brush = Brush.verticalGradient(
        listOf(GlowPalette.Charcoal, GlowPalette.Night)
    )

    /** ألوان الشفق للتوهج الشبكي (Aurō Mesh Glow). */
    val Aurora: List<Color> = listOf(
        GlowPalette.Amber,
        GlowPalette.Coral,
        GlowPalette.TwilightBlue,
    )

    /** توهج شعاعي ناعم يتلاشى إلى الشفافية. */
    fun halo(color: Color, alpha: Float = 0.35f, radius: Float = Float.POSITIVE_INFINITY): Brush =
        Brush.radialGradient(
            colors = listOf(color.copy(alpha = alpha), Color.Transparent),
            radius = radius,
        )
}

/** رموز لونية إضافية خارج Material ColorScheme، متاحة عبر GlowTheme.colors. */
@Immutable
data class GlowColors(
    val amberGlow: Color = GlowPalette.Amber,
    val coralGlow: Color = GlowPalette.Coral,
    val twilightGlow: Color = GlowPalette.TwilightBlue,
    val tealGlow: Color = GlowPalette.Teal,
    val hairline: Color = GlowPalette.Hairline,
    val glassFill: Color = GlowPalette.GlassFill,
    val glassHighlight: Color = GlowPalette.GlassHighlight,
    val textPrimary: Color = GlowPalette.Ivory,
    val textSecondary: Color = GlowPalette.Muted,
)

internal val LocalGlowColors = staticCompositionLocalOf { GlowColors() }
