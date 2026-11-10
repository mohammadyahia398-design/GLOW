package com.glow.app.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Glow داكن دائماً: الإضاءة هي جوهر الهوية. (يتطلب material3 بإصدار 1.2 أو أحدث). */
private val GlowColorScheme = darkColorScheme(
    primary = GlowPalette.Amber,
    onPrimary = GlowPalette.Charcoal,
    primaryContainer = Color(0xFF3A2814),
    onPrimaryContainer = Color(0xFFFFD9B0),

    secondary = GlowPalette.TwilightSoft,
    onSecondary = GlowPalette.Charcoal,
    secondaryContainer = Color(0xFF1E2748),
    onSecondaryContainer = Color(0xFFDCE3FF),

    tertiary = GlowPalette.Coral,
    onTertiary = GlowPalette.Charcoal,
    tertiaryContainer = Color(0xFF47201F),
    onTertiaryContainer = Color(0xFFFFDAD6),

    background = GlowPalette.Charcoal,
    onBackground = GlowPalette.Ivory,

    surface = GlowPalette.Night,
    onSurface = GlowPalette.Ivory,
    surfaceVariant = GlowPalette.Surface,
    onSurfaceVariant = GlowPalette.Muted,
    surfaceTint = GlowPalette.Amber,

    surfaceContainerLowest = GlowPalette.Charcoal,
    surfaceContainerLow = GlowPalette.Night,
    surfaceContainer = GlowPalette.Surface,
    surfaceContainerHigh = GlowPalette.SurfaceLifted,
    surfaceContainerHighest = Color(0xFF2B3040),

    outline = GlowPalette.Muted,
    outlineVariant = GlowPalette.OutlineSolid,

    error = Color(0xFFFF8A80),
    onError = GlowPalette.Charcoal,
    errorContainer = Color(0xFF4A1F1D),
    onErrorContainer = Color(0xFFFFDAD6),

    scrim = Color.Black,
)

/** وصول مختصر للرموز الخاصة بـ Glow: GlowTheme.colors و GlowTheme.dimens. */
object GlowTheme {
    val colors: GlowColors
        @Composable @ReadOnlyComposable get() = LocalGlowColors.current

    val dimens: GlowDimens
        @Composable @ReadOnlyComposable get() = LocalGlowDimens.current
}

@Composable
fun GlowTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            // خلفية داكنة ← أيقونات فاتحة في شريط الحالة وشريط التنقل
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
    }

    CompositionLocalProvider(
        LocalGlowColors provides GlowColors(),
        LocalGlowDimens provides GlowDimens(),
    ) {
        MaterialTheme(
            colorScheme = GlowColorScheme,
            typography = GlowTypography.Material,   // مربوط، على عكس المشروع القديم
            shapes = GlowShapes.Material,           // مربوط، على عكس المشروع القديم
            content = content,
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
