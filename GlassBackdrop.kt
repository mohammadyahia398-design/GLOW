package com.glow.app.ui.glass.surfaces

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo

/**
 * يحتفظ بنسخة حيّة من المحتوى الذي يقع خلف الأسطح الزجاجية.
 *
 * طريقة الاستخدام:
 * 1. أنشئ الكائن بـ [rememberGlassBackdrop].
 * 2. ضع [glassBackdropSource] على المحتوى الذي يجب أن يظهر خلف الزجاج
 *    (الخلفية، قائمة الأغلفة، ...).
 * 3. ضع الأسطح الزجاجية كإخوة للمصدر وفوقه في الترتيب (Box)، وليس بداخله،
 *    وإلا نشأت حلقة تغذية راجعة (الزجاج يلتقط نفسه).
 * 4. يرسم كل سطح زجاجي الجزء الذي يقع خلفه عبر [drawGlassBackdrop].
 *
 * يتطلب Compose UI بإصدار 1.7 أو أحدث (GraphicsLayer).
 */
@Stable
class GlassBackdrop internal constructor(
    internal val layer: GraphicsLayer,
) {
    /** موضع المصدر في جذر الشاشة، يُحدَّث عند كل تغيّر في التخطيط. */
    internal var sourceOrigin: Offset by mutableStateOf(Offset.Zero)
}

/** ينشئ [GlassBackdrop] ويحتفظ به عبر إعادة التركيب، ويحرّر طبقته عند الخروج من التركيب. */
@Composable
fun rememberGlassBackdrop(): GlassBackdrop {
    val layer = rememberGraphicsLayer()
    return remember(layer) { GlassBackdrop(layer) }
}

/**
 * يعلّم هذا العنصر مصدراً للخلفية: يُرسم كالمعتاد، ويُسجَّل أيضاً في طبقة
 * يستعملها الزجاج. لا يغيّر مظهر العنصر نفسه.
 */
fun Modifier.glassBackdropSource(backdrop: GlassBackdrop): Modifier =
    this
        .onGloballyPositioned { coordinates ->
            val origin = coordinates.positionInRoot()
            if (origin != backdrop.sourceOrigin) backdrop.sourceOrigin = origin
        }
        .drawWithContent {
            backdrop.layer.record { this@drawWithContent.drawContent() }
            drawLayer(backdrop.layer)
        }

/**
 * يرسم، خلف محتوى هذا العنصر، الجزء من الخلفية الواقع تحته تماماً (دون تمويه).
 *
 * يُقصّ الرسم على حدود العنصر فقط. للقص على شكل معيّن ولإضافة التمويه يوضع
 * قبله `glassBlurLayer` من ملف BlurEffect.
 *
 * يُستخدم على عنصر خلفية مخصص داخل السطح الزجاجي، لا على عنصر يحوي
 * المحتوى، حتى لا يُموَّه المحتوى نفسه.
 */
fun Modifier.drawGlassBackdrop(backdrop: GlassBackdrop): Modifier =
    this then GlassBackdropDrawElement(backdrop)

private class GlassBackdropDrawElement(
    private val backdrop: GlassBackdrop,
) : ModifierNodeElement<GlassBackdropDrawNode>() {

    override fun create(): GlassBackdropDrawNode = GlassBackdropDrawNode(backdrop)

    override fun update(node: GlassBackdropDrawNode) {
        node.updateBackdrop(backdrop)
    }

    override fun equals(other: Any?): Boolean =
        other is GlassBackdropDrawElement && other.backdrop === backdrop

    override fun hashCode(): Int = System.identityHashCode(backdrop)

    override fun InspectorInfo.inspectableProperties() {
        name = "drawGlassBackdrop"
    }
}

private class GlassBackdropDrawNode(
    private var backdrop: GlassBackdrop,
) : Modifier.Node(), DrawModifierNode, GlobalPositionAwareModifierNode {

    /** موضع هذا السطح في جذر الشاشة. */
    private var glassOrigin: Offset = Offset.Zero

    fun updateBackdrop(newBackdrop: GlassBackdrop) {
        if (backdrop !== newBackdrop) {
            backdrop = newBackdrop
            invalidateDraw()
        }
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        val origin = coordinates.positionInRoot()
        if (origin != glassOrigin) {
            glassOrigin = origin
            invalidateDraw()
        }
    }

    override fun ContentDrawScope.draw() {
        val source = backdrop
        if (!source.layer.isReleased) {
            // قراءة sourceOrigin هنا تجعل الرسم يُعاد تلقائياً عند تحرك المصدر.
            val delta = source.sourceOrigin - glassOrigin
            clipRect(left = 0f, top = 0f, right = size.width, bottom = size.height) {
                translate(left = delta.x, top = delta.y) {
                    drawLayer(source.layer)
                }
            }
        }
        drawContent()
    }
}
