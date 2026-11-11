package com.glow.app.ui.glass.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * اختبار وحدة (JVM) لمنطق اختيار مستوى الزجاج.
 * يعتمد على [GlassCapabilities.resolveTier] النقية، فلا يحتاج جهازاً ولا Compose.
 */
class GlassTierTest {

    private fun tier(
        sdk: Int,
        lowRam: Boolean = false,
        enabled: Boolean = true,
        ceiling: GlassTier = GlassTier.Liquid,
    ): GlassTier = GlassCapabilities.resolveTier(
        sdkInt = sdk,
        isLowRamDevice = lowRam,
        enabled = enabled,
        ceiling = ceiling,
    )

    // ── الحدود الدنيا للإصدارات ─────────────────────────────────────

    @Test
    fun blur_min_sdk_is_31() {
        assertThat(GlassCapabilities.BLUR_MIN_SDK).isEqualTo(31)
    }

    @Test
    fun refraction_min_sdk_is_33() {
        assertThat(GlassCapabilities.REFRACTION_MIN_SDK).isEqualTo(33)
    }

    // ── الاختيار حسب إصدار API ─────────────────────────────────────

    @Test
    fun api_33_resolves_to_liquid() {
        assertThat(tier(33)).isEqualTo(GlassTier.Liquid)
    }

    @Test
    fun api_35_resolves_to_liquid() {
        assertThat(tier(35)).isEqualTo(GlassTier.Liquid)
    }

    @Test
    fun api_32_resolves_to_frosted() {
        assertThat(tier(32)).isEqualTo(GlassTier.Frosted)
    }

    @Test
    fun api_31_resolves_to_frosted() {
        assertThat(tier(31)).isEqualTo(GlassTier.Frosted)
    }

    @Test
    fun api_30_resolves_to_fallback() {
        assertThat(tier(30)).isEqualTo(GlassTier.Fallback)
    }

    @Test
    fun api_26_resolves_to_fallback() {
        assertThat(tier(26)).isEqualTo(GlassTier.Fallback)
    }

    // ── التعطيل والذاكرة المنخفضة ──────────────────────────────────

    @Test
    fun low_ram_device_forces_fallback_even_on_new_api() {
        assertThat(tier(35, lowRam = true)).isEqualTo(GlassTier.Fallback)
    }

    @Test
    fun disabled_forces_fallback_even_on_new_api() {
        assertThat(tier(35, enabled = false)).isEqualTo(GlassTier.Fallback)
    }

    // ── السقف الأعلى ───────────────────────────────────────────────

    @Test
    fun ceiling_frosted_caps_liquid_device() {
        assertThat(tier(35, ceiling = GlassTier.Frosted)).isEqualTo(GlassTier.Frosted)
    }

    @Test
    fun ceiling_fallback_caps_any_device() {
        assertThat(tier(35, ceiling = GlassTier.Fallback)).isEqualTo(GlassTier.Fallback)
    }

    @Test
    fun ceiling_does_not_raise_a_weaker_device() {
        assertThat(tier(31, ceiling = GlassTier.Liquid)).isEqualTo(GlassTier.Frosted)
    }

    // ── قدرات كل مستوى وترتيبها ────────────────────────────────────

    @Test
    fun fallback_supports_neither_blur_nor_refraction() {
        assertThat(GlassTier.Fallback.supportsBlur).isFalse()
        assertThat(GlassTier.Fallback.supportsRefraction).isFalse()
    }

    @Test
    fun frosted_supports_blur_only() {
        assertThat(GlassTier.Frosted.supportsBlur).isTrue()
        assertThat(GlassTier.Frosted.supportsRefraction).isFalse()
    }

    @Test
    fun liquid_supports_blur_and_refraction() {
        assertThat(GlassTier.Liquid.supportsBlur).isTrue()
        assertThat(GlassTier.Liquid.supportsRefraction).isTrue()
    }

    @Test
    fun tiers_are_ordered_from_weakest_to_richest() {
        assertThat(GlassTier.entries)
            .containsExactly(GlassTier.Fallback, GlassTier.Frosted, GlassTier.Liquid)
            .inOrder()
    }
}
