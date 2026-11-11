package com.glow.app.ui.glass.core

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * مستويات العمق للأسطح الزجاجية. كل مكوّن يعلن عمقه، والظلال والترتيب
 * يُشتقان منه بدل قيم عشوائية متفرقة.
 *
 * @property level رقم المستوى (0 = ملاصق للخلفية).
 * @property elevation الارتفاع الاسمي، مرجع للتحجيم لا للرسم المباشر.
 * @property zIndex ترتيب الرسم النسبي بين الأسطح المتراكبة.
 */
enum class GlassDepth(
    val level: Int,
    val elevation: Dp,
    val zIndex: Float,
) {
    /** ملاصق للخلفية، دون ظل. */
    Flush(level = 0, elevation = 0.dp, zIndex = 0f),

    /** بطاقات المحتوى. */
    Raised(level = 1, elevation = 4.dp, zIndex = 1f),

    /** الكبسولة وشريط التنقل العائمان. */
    Floating(level = 2, elevation = 12.dp, zIndex = 2f),

    /** لوحات منبثقة وقوائم فوق كل شيء. */
    Overlay(level = 3, elevation = 24.dp, zIndex = 3f);

    /** هل يُرسم لهذا العمق ظل. */
    val castsShadow: Boolean get() = this != Flush

    /** العمق أثناء الضغط: خطوة واحدة أقرب إلى الخلفية. */
    val pressed: GlassDepth
        get() = when (this) {
            Flush -> Flush
            Raised -> Flush
            Floating -> Raised
            Overlay -> Floating
        }

    /** العمق أثناء الرفع أو التحويم: خطوة واحدة أبعد عن الخلفية. */
    val lifted: GlassDepth
        get() = when (this) {
            Flush -> Raised
            Raised -> Floating
            Floating -> Overlay
            Overlay -> Overlay
        }
}
