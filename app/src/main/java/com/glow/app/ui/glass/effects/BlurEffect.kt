package com.glow.app.ui.glass.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.glow.app.ui.glass.core.GlassStyle
import com.glow.app.ui.glass.core.GlassTier

/** أقصى نصف قطر تمويه مسموح، لحماية الأداء على الأجهزة المتوسطة. */
private val MaxBlurRadius = 48.dp

/**
 * يبني تأثير التمويه الحقيقي ويحتفظ به عبر إعادة التركيب (لا يُنشأ في كل إطار).
 *
 * يعيد null عندما لا يتوفر تمويه حقيقي (المستوى [GlassTier.Fallback]) أو عندما
 * يكون نصف القطر صفراً.
 *
 * @param edge معالجة الحواف. الافتراضي [TileMode.Mirror] لأنه يقرّب سلوك
 * الجوار الحقيقي أكثر من [TileMode.Clamp] الذي يمدّ بكسلات الحافة.
 */
@Composable
fun rememberGlassBlurEffect(
    style: GlassStyle,
    tier: GlassTier,
    edge: TileMode = TileMode.Mirror,
): RenderEffect? {
    val density = LocalDensity.current
    return remember(style, tier, edge, density) {
        val radius: Dp = style.effectiveBlur(tier).let { if (it > MaxBlurRadius) MaxBlurRadius else it }
        if (!tier.supportsBlur || radius <= 0.dp) {
            null
        } else {
            val px = with(density) { radius.toPx() }
            BlurEffect(px, px, edge)
        }
    }
}

/**
 * طبقة مقصوصة على [shape] تطبّق [effect] على كل ما يُرسم داخلها.
 *
 * يوضع هذا المعدِّل على عنصر الخلفية فقط، ويليه `drawGlassBackdrop`:
 *
 * ```
 * Box(
 *     Modifier
 *         .matchParentSize()
 *         .glassBlurLayer(shape, effect)
 *         .drawGlassBackdrop(backdrop)
 * )
 * ```
 *
 * لا يوضع على عنصر يحوي المحتوى، وإلا تموّه المحتوى معه.
 *
 * قيد معروف: يقع التمويه داخل حدود العنصر فقط، فلا تصل إليه بكسلات من خارج
 * حدوده، ويعوّض ذلك [TileMode.Mirror] عند الحواف.
 *
 * عندما يكون [effect] مساوياً null (لا تمويه متاح) يبقى القص على الشكل فقط.
 */
fun Modifier.glassBlurLayer(shape: Shape, effect: RenderEffect?): Modifier =
    graphicsLayer {
        this.shape = shape
        this.clip = true
        this.renderEffect = effect
    }
