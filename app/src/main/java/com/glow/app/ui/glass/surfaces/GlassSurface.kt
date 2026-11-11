package com.glow.app.ui.glass.surfaces

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.glow.app.ui.glass.core.GlassDepth
import com.glow.app.ui.glass.core.GlassStyle
import com.glow.app.ui.glass.core.LocalGlassConfig
import com.glow.app.ui.glass.effects.glassBlurLayer
import com.glow.app.ui.glass.effects.glassFallbackBase
import com.glow.app.ui.glass.effects.glassTint
import com.glow.app.ui.glass.effects.rememberGlassBlurEffect
import com.glow.app.ui.glass.effects.rememberGlassRefractionEffect
import com.glow.app.ui.glass.lighting.LightSource
import com.glow.app.ui.glass.lighting.LocalLightSource
import com.glow.app.ui.glass.lighting.glassEdgeHighlight
import com.glow.app.ui.glass.lighting.glassSpecularSheen
import com.glow.app.ui.glass.motion.GlassInteractionState
import com.glow.app.ui.glass.motion.glassPressLight
import com.glow.app.ui.glass.motion.glassPressScale

/** القيم الافتراضية للأسطح الزجاجية. */
object GlassSurfaceDefaults {
    /** نصف قطر 28dp، كما في المرجع البصري. */
    val CardShape: Shape = RoundedCornerShape(28.dp)
}

/**
 * السطح الزجاجي الأساسي. طبقاته من الأسفل إلى الأعلى:
 *
 * 1. ظل متعدد الطبقات خارج الحدود.
 * 2. الخلفية الملتقطة، مموّهة ومنكسرة عند الحواف بحسب مستوى الجهاز
 *    (تُرسم فقط عند تمرير [backdrop] ودعم المستوى للتمويه).
 * 3. قاعدة داكنة (المستوى البديل فقط) ثم التلوين.
 * 4. لمعان وإضاءة حواف وحد رفيع.
 * 5. المحتوى [content] فوق كل ذلك، غير مموَّه.
 *
 * قاعدة التركيب: يوضع هذا السطح كأخٍ للمصدر وفوقه في الترتيب، لا بداخله.
 *
 * @param backdrop الخلفية المشتركة؛ null يعني زجاجاً بلا التقاط (تلوين وإضاءة فقط).
 * @param interaction حالة تفاعل اختيارية؛ عند تمريرها يتبع الضوء اللمس ويتغير الحجم عند الضغط.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    backdrop: GlassBackdrop? = null,
    shape: Shape = GlassSurfaceDefaults.CardShape,
    style: GlassStyle = GlassStyle.Regular,
    depth: GlassDepth = GlassDepth.Raised,
    interaction: GlassInteractionState? = null,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit,
) {
    val config = LocalGlassConfig.current
    val tier = config.tier
    val ambientLight = LocalLightSource.current

    var size by remember { mutableStateOf(IntSize.Zero) }
    val blur = rememberGlassBlurEffect(style, tier)
    val effect = rememberGlassRefractionEffect(style, tier, shape, size, blur)

    // الضوء يُقرأ أثناء الرسم عبر هذه الدالة، فتحريكه لا يعيد التركيب.
    val lightProvider: () -> LightSource = remember(interaction, ambientLight) {
        val lightState = interaction?.light
        val provider: () -> LightSource = { lightState?.source ?: ambientLight }
        provider
    }

    val activeDepth = if (interaction?.isPressed == true) depth.pressed else depth

    val interactionModifier = if (interaction != null) {
        Modifier
            .glassPressScale(interaction)
            .glassPressLight(interaction)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(interactionModifier)
            .glassShadow(shape, activeDepth, tier, config.shadowColor)
            .onSizeChanged { size = it }
            .clip(shape),
        contentAlignment = contentAlignment,
    ) {
        if (backdrop != null && tier.supportsBlur) {
            Box(
                Modifier
                    .matchParentSize()
                    .glassBlurLayer(shape, effect)
                    .drawGlassBackdrop(backdrop),
            )
        }

        Box(
            Modifier
                .matchParentSize()
                .glassFallbackBase(shape, config)
                .glassTint(shape, style, config)
                .glassSpecularSheen(shape, style, lightProvider)
                .border(0.5.dp, Color.White.copy(alpha = style.borderAlpha), shape)
                .glassEdgeHighlight(shape, style, lightProvider),
        )

        content()
    }
}
