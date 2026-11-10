package com.glow.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** المسافات والأبعاد المركزية. لا تستخدم أرقاماً مباشرة في الشاشات؛ استخدم هذه الرموز. */
@Immutable
data class GlowDimens(
    // مقياس المسافات (قاعدة 4dp)
    val spaceXxs: Dp = 2.dp,
    val spaceXs: Dp = 4.dp,
    val spaceSm: Dp = 8.dp,
    val spaceMd: Dp = 12.dp,
    val spaceLg: Dp = 16.dp,
    val spaceXl: Dp = 24.dp,
    val spaceXxl: Dp = 32.dp,
    val spaceXxxl: Dp = 48.dp,

    val screenPadding: Dp = 20.dp,

    // مكونات
    val miniPlayerHeight: Dp = 68.dp,
    val navBarHeight: Dp = 72.dp,
    val minTouchTarget: Dp = 48.dp,
    val hairline: Dp = 1.dp,

    // أغلفة الألبومات
    val artworkSmall: Dp = 48.dp,
    val artworkMedium: Dp = 64.dp,
    val artworkLarge: Dp = 320.dp,

    // التوهج
    val glowBlur: Dp = 32.dp,
    val haloRadius: Dp = 160.dp,
)

internal val LocalGlowDimens = staticCompositionLocalOf { GlowDimens() }
