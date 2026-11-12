package com.glow.app.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private fun glowColorScheme(skin: GlowSkin): ColorScheme =
    if (skin.isDark) {
        darkColorScheme(
            primary = skin.accent,
            onPrimary = skin.onAccent,
            primaryContainer = Color(0xFF12303D),
            onPrimaryContainer = Color(0xFF67E8F9),
            secondary = Color(0xFF67E8F9),
            onSecondary = skin.onAccent,
            background = skin.bgTop,
            onBackground = skin.text,
            surface = skin.bgTop,
            onSurface = skin.text,
            surfaceVariant = skin.bgBottom,
            onSurfaceVariant = skin.textSecondary,
            surfaceTint = skin.accent,
            outline = skin.textSecondary,
            outlineVariant = Color(0xFF1E4352),
        )
    } else {
        lightColorScheme(
            primary = skin.accent,
            onPrimary = skin.onAccent,
            primaryContainer = Color(0xFF67E8F9),
            onPrimaryContainer = Color(0xFF07141C),
            secondary = skin.accentText,
            onSecondary = Color.White,
            background = skin.bgTop,
            onBackground = skin.text,
            surface = skin.bgTop,
            onSurface = skin.text,
            surfaceVariant = skin.bgBottom,
            onSurfaceVariant = skin.textSecondary,
            surfaceTint = skin.accent,
            outline = skin.textSecondary,
            outlineVariant = Color(0xFFA5E6F0),
        )
    }

/** وصول مختصر للرموز الخاصة بـ Glow: GlowTheme.colors و GlowTheme.dimens. */
object GlowTheme {
    val colors: GlowColors
        @Composable @ReadOnlyComposable get() = LocalGlowColors.current

    val dimens: GlowDimens
        @Composable @ReadOnlyComposable get() = LocalGlowDimens.current
}

/** يتبع الوضع الداكن أو الفاتح في النظام افتراضياً. */
@Composable
fun GlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val skin = if (darkTheme) GlowSkins.Dark else GlowSkins.Light
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            // خلفية فاتحة ← أيقونات داكنة في شريط الحالة وشريط التنقل، والعكس
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalGlowColors provides GlowColors(),
        LocalGlowDimens provides GlowDimens(),
        LocalGlowSkin provides skin,
    ) {
        MaterialTheme(
            colorScheme = glowColorScheme(skin),
            typography = GlowTypography.Material,
            shapes = GlowShapes.Material,
            content = content,
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
