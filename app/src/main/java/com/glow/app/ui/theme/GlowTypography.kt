package com.glow.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * الخطوط.
 *
 * ملاحظة مهمة للعربية: letterSpacing دائماً 0.sp، لأن أي تباعد غير صفري
 * يقطع اتصال الحروف العربية ويشوّه الكلمات.
 * ويمكن لاحقاً استبدال Family بخط مخصص (مثل خط يدعم العربية واللاتينية).
 */
object GlowTypography {
    private val Family = FontFamily.Default

    private fun style(size: Int, line: Int, weight: FontWeight) = TextStyle(
        fontFamily = Family,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = line.sp,   // ارتفاع سطر سخي لراحة القراءة بالعربية
        letterSpacing = 0.sp,
    )

    val Material = Typography(
        displayLarge = style(57, 68, FontWeight.Light),
        displayMedium = style(45, 56, FontWeight.Light),
        displaySmall = style(36, 46, FontWeight.Normal),
        headlineLarge = style(32, 42, FontWeight.SemiBold),
        headlineMedium = style(28, 38, FontWeight.SemiBold),
        headlineSmall = style(24, 34, FontWeight.SemiBold),
        titleLarge = style(22, 30, FontWeight.SemiBold),
        titleMedium = style(16, 26, FontWeight.Medium),
        titleSmall = style(14, 22, FontWeight.Medium),
        bodyLarge = style(16, 26, FontWeight.Normal),
        bodyMedium = style(14, 22, FontWeight.Normal),
        bodySmall = style(12, 18, FontWeight.Normal),
        labelLarge = style(14, 20, FontWeight.Medium),
        labelMedium = style(12, 18, FontWeight.Medium),
        labelSmall = style(11, 16, FontWeight.Medium),
    )

    /** أرقام الوقت والعدادات: أرقام متساوية العرض حتى لا يهتز النص أثناء العد. */
    val TimeDisplay = TextStyle(
        fontFamily = Family,
        fontWeight = FontWeight.Light,
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = 0.sp,
        fontFeatureSettings = "tnum",
    )
}
