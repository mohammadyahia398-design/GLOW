package com.glow.app.ui.glass.components

import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glow.app.ui.glass.core.GlassDepth
import com.glow.app.ui.glass.core.GlassStyle
import com.glow.app.ui.glass.core.LocalGlassConfig
import com.glow.app.ui.glass.motion.NoIndication
import com.glow.app.ui.glass.motion.glassTween
import com.glow.app.ui.glass.surfaces.GlassBackdrop
import com.glow.app.ui.glass.surfaces.GlassSurface
import com.glow.app.ui.glass.surfaces.glassBackdropSource
import com.glow.app.ui.glass.surfaces.rememberGlassBackdrop
import com.glow.app.ui.theme.LocalGlowSkin
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * كل ما يُعدَّل في شكل الشريط الزجاجي السائل في مكان واحد.
 * مرّر نسخة معدّلة: `LiquidBarSpec(magnify = 1.4f, lensGrow = 0.dp)`.
 *
 * @property height ارتفاع الكبسولة.
 * @property innerPadding الفراغ بين حافة الكبسولة والعدسة.
 * @property lensGrow مقدار تضخّم العدسة أثناء السحب (0 يعطّله).
 * @property barStyle نمط زجاج الكبسولة.
 * @property magnify تكبير ما تحت العدسة (1 يعطّله). هو ما يجعل الأيقونة والعنوان يبدوان مكبَّرين داخلها.
 * @property bleed عرض المحتوى الإضافي الذي تلتقطه العدسة خارج حدّها لتنكسر به حافتها.
 * @property edgeWidth عرض حلقة الانكسار عند حافة العدسة.
 * @property dispersion شدّة التشتّت اللوني عند الحافة (0 يعطّله).
 * @property springDamping ارتداد حركة العدسة (أقل = أكثر ارتداداً).
 * @property springStiffness صلابة الحركة (أعلى = أسرع).
 */
@Immutable
data class LiquidBarSpec(
    val height: Dp = 72.dp,
    val innerPadding: Dp = 8.dp,
    val lensGrow: Dp = 12.dp,
    val iconSize: Dp = 26.dp,
    val labelSize: TextUnit = 11.sp,
    val barStyle: GlassStyle = GlassStyle.Regular.copy(
        tintAlpha = 0.26f,
        fallbackTintAlpha = 0.42f,
        borderAlpha = 0.40f,
        highlightAlpha = 0.45f,
    ),
    val lensStyle: GlassStyle = GlassStyle.Clear,
    val lensAccentAlpha: Float = 0.14f,
    val springDamping: Float = 0.74f,
    val springStiffness: Float = 420f,
    val magnify: Float = 1.22f,
    val bleed: Dp = 10.dp,
    val edgeWidth: Dp = 16.dp,
    val dispersion: Float = 0.35f,
)

/**
 * عنصر في [LiquidGlassBar].
 *
 * @property icon يرسم الأيقونة بلون يُمرَّر له (يتغير مع التحديد).
 */
@Immutable
class LiquidBarItem(
    val id: String,
    val label: String,
    val icon: @Composable (tint: Color) -> Unit,
)

/**
 * شريط تنقل Liquid Glass: كبسولة زجاجية فيها «عدسة» حقيقية تنزلق إلى العنصر المحدد.
 *
 * العدسة ليست لوناً مرسوماً فوق الشريط: هي نسخة مكبَّرة من الشريط نفسه (الكبسولة
 * والأيقونات والعناوين) تُرسم داخلها، وحافتها تكسر الضوء بتشتّت لوني على API 33+،
 * وتبقى مكبِّرة بلا انكسار على الأقدم. يمكن سحبها بالإصبع بين العناصر.
 *
 * قواعد التركيب كما في بقية النظام الزجاجي: يوضع كأخٍ لمصدر الخلفية وفوقه،
 * مع [backdrop] المشترك. الهوامش من المستدعي (navigationBarsPadding وغيرها).
 *
 * الترتيب البصري يتبع اتجاه اللغة: في RTL يظهر العنصر الأول على اليمين.
 */
@Composable
fun LiquidGlassBar(
    items: List<LiquidBarItem>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: GlassBackdrop? = null,
    spec: LiquidBarSpec = LiquidBarSpec(),
    accent: Color = LocalGlowSkin.current.accentText,
    unselected: Color = LocalGlowSkin.current.textSecondary,
) {
    if (items.isEmpty()) return
    val systemRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val ordered = remember(items, systemRtl) { if (systemRtl) items.asReversed() else items }

    // الحساب الداخلي كله بترتيب LTR ثابت لتبسيط الإحداثيات؛ الترتيب يُعكس يدوياً أعلاه.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        LiquidBarBody(ordered, selectedId, onSelect, modifier, backdrop, spec, accent, unselected)
    }
}

@Composable
private fun LiquidBarBody(
    ordered: List<LiquidBarItem>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier,
    backdrop: GlassBackdrop?,
    spec: LiquidBarSpec,
    accent: Color,
    unselected: Color,
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val reduceMotion = LocalGlassConfig.current.reduceMotion
    val scope = rememberCoroutineScope()
    val count = ordered.size
    val lastIndex = (count - 1).coerceAtLeast(0)
    val selectedIndex = ordered.indexOfFirst { it.id == selectedId }.coerceAtLeast(0)

    val position = remember { Animatable(selectedIndex.toFloat()) }
    var dragging by remember { mutableStateOf(false) }
    var barWidthPx by remember { mutableFloatStateOf(0f) }
    val dragTarget = remember { floatArrayOf(0f) }
    val lastHapticIndex = remember { intArrayOf(-1) }

    val currentOrdered by rememberUpdatedState(ordered)
    val currentSelectedId by rememberUpdatedState(selectedId)
    val currentOnSelect by rememberUpdatedState(onSelect)

    val moveSpec: AnimationSpec<Float> = if (reduceMotion) {
        snap()
    } else {
        spring<Float>(dampingRatio = spec.springDamping, stiffness = spec.springStiffness)
    }
    val currentMoveSpec by rememberUpdatedState(moveSpec)

    // اختيار من الخارج أو بالنقر: تنزلق العدسة إلى الموضع الجديد.
    LaunchedEffect(selectedIndex, count) {
        if (!dragging) position.animateTo(selectedIndex.toFloat(), currentMoveSpec)
    }

    val growState = animateFloatAsState(
        targetValue = if (dragging) 1f else 0f,
        animationSpec = glassTween(reduceMotion, 160),
        label = "liquidGrow",
    )
    val activeIndex by remember(count) {
        derivedStateOf { position.value.roundToInt().coerceIn(0, lastIndex) }
    }

    val padPx = with(density) { spec.innerPadding.toPx() }
    val growPx = with(density) { spec.lensGrow.toPx() }
    val heightPx = with(density) { spec.height.toPx() }
    val bleedPx = with(density) { spec.bleed.toPx() }
    val edgePx = with(density) { spec.edgeWidth.toPx() }
    val shape = RoundedCornerShape(percent = 50)

    fun itemWidthPx(): Float =
        if (count > 0) ((barWidthPx - 2f * padPx) / count).coerceAtLeast(0f) else 0f

    // أبعاد العدسة المرئية، وطبقتها الأكبر منها بمقدار bleed من كل جهة.
    val lensW = itemWidthPx()
    val lensH = (heightPx - 2f * padPx).coerceAtLeast(0f)
    val layerW = lensW + 2f * bleedPx
    val layerH = lensH + 2f * bleedPx
    val lensEffect = rememberLensEffect(
        layerWidth = layerW,
        layerHeight = layerH,
        insetPx = bleedPx,
        cornerPx = lensH / 2f,
        edgePx = edgePx,
        bendPx = bleedPx * 0.95f,
        dispersion = spec.dispersion,
    )
    val lensShape = remember(bleedPx) { InsetCapsuleShape(bleedPx) }

    // مصدر خاص بالعدسة: الكبسولة وعناصرها، لتُرسم مكبَّرة داخلها.
    val barBackdrop = rememberGlassBackdrop()

    fun finishDrag() {
        dragging = false
        val target = dragTarget[0].roundToInt().coerceIn(0, lastIndex)
        val item = currentOrdered[target]
        if (item.id != currentSelectedId) currentOnSelect(item.id)
        scope.launch { position.animateTo(target.toFloat(), currentMoveSpec) }
    }

    Box(
        modifier
            .height(spec.height)
            .onSizeChanged { barWidthPx = it.width.toFloat() }
            .pointerInput(count, padPx) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        dragging = true
                        dragTarget[0] = position.value
                        lastHapticIndex[0] = position.value.roundToInt()
                    },
                    onDragEnd = { finishDrag() },
                    onDragCancel = { finishDrag() },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        val w = itemWidthPx()
                        if (w > 0f) {
                            val next = (dragTarget[0] + dragAmount / w).coerceIn(0f, lastIndex.toFloat())
                            dragTarget[0] = next
                            scope.launch { position.snapTo(next) }
                            val idx = next.roundToInt()
                            if (idx != lastHapticIndex[0]) {
                                lastHapticIndex[0] = idx
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                    },
                )
            }
            .selectableGroup(),
    ) {
        // 1) الكبسولة والعناصر: تُسجَّل في طبقة تلتقطها العدسة.
        Box(Modifier.matchParentSize().glassBackdropSource(barBackdrop)) {
            GlassSurface(
                modifier = Modifier.matchParentSize(),
                backdrop = backdrop,
                shape = shape,
                style = spec.barStyle,
                depth = GlassDepth.Floating,
            ) {}

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(spec.innerPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ordered.forEachIndexed { index, item ->
                    LiquidBarTab(
                        item = item,
                        active = index == activeIndex,
                        selected = item.id == selectedId,
                        accent = accent,
                        unselected = unselected,
                        spec = spec,
                        reduceMotion = reduceMotion,
                        onClick = { onSelect(item.id) },
                    )
                }
            }
        }

        // 2) العدسة: تتحرك وتتضخم داخل مرحلة التخطيط والرسم فلا تُعاد التركيبة كل إطار.
        Box(
            Modifier
                .absoluteOffset {
                    IntOffset(
                        (padPx + position.value * itemWidthPx() - bleedPx).roundToInt(),
                        (padPx - bleedPx).roundToInt(),
                    )
                }
                .layout { measurable, _ ->
                    val w = layerW.roundToInt().coerceAtLeast(0)
                    val h = layerH.roundToInt().coerceAtLeast(0)
                    val placeable = measurable.measure(Constraints.fixed(w, h))
                    layout(w, h) { placeable.place(0, 0) }
                }
                .graphicsLayer {
                    val extra = growPx * growState.value
                    scaleX = if (lensW > 0f) 1f + extra / lensW else 1f
                    scaleY = if (lensH > 0f) 1f + extra / lensH else 1f
                },
        ) {
            // المحتوى المكبَّر + الانكسار عند الحافة.
            Box(
                Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        this.shape = lensShape
                        this.clip = true
                        this.renderEffect = lensEffect
                    }
                    .drawBehind {
                        if (barBackdrop.layer.isReleased) return@drawBehind
                        val left = padPx + position.value * itemWidthPx() - bleedPx
                        val top = padPx - bleedPx
                        val pivot = Offset(left + size.width / 2f, top + size.height / 2f)
                        translate(left = -left, top = -top) {
                            scale(spec.magnify, spec.magnify, pivot) {
                                drawLayer(barBackdrop.layer)
                            }
                        }
                    },
            )
            // الحافة اللامعة والإضاءة: فوق المحتوى المنكسر فتبقى حادّة.
            Box(Modifier.matchParentSize().drawBehind { drawLensRim(bleedPx) })
        }
    }
}

@Composable
private fun RowScope.LiquidBarTab(
    item: LiquidBarItem,
    active: Boolean,
    selected: Boolean,
    accent: Color,
    unselected: Color,
    spec: LiquidBarSpec,
    reduceMotion: Boolean,
    onClick: () -> Unit,
) {
    val tint by animateColorAsState(
        targetValue = if (active) accent else unselected,
        animationSpec = glassTween(reduceMotion),
        label = "liquidTint",
    )
    val interaction = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .selectable(
                selected = selected,
                interactionSource = interaction,
                indication = NoIndication,
                role = Role.Tab,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(spec.iconSize), contentAlignment = Alignment.Center) {
            item.icon(tint)
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = item.label,
            color = tint,
            fontSize = spec.labelSize,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** كبسولة مزاحة إلى الداخل بمقدار [inset] من كل جهة: حدّ العدسة المرئي داخل طبقتها الأكبر. */
private class InsetCapsuleShape(private val inset: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val w = (size.width - 2f * inset).coerceAtLeast(0f)
        val h = (size.height - 2f * inset).coerceAtLeast(0f)
        val r = h / 2f
        return Outline.Rounded(RoundRect(inset, inset, inset + w, inset + h, CornerRadius(r, r)))
    }
}

/** يرسم حافة العدسة: لمعة علوية، إطار مضيء، إضاءة داخلية، وطيف لوني خفيف. */
private fun DrawScope.drawLensRim(inset: Float) {
    val w = size.width - 2f * inset
    val h = size.height - 2f * inset
    if (w <= 0f || h <= 0f) return
    val tl = Offset(inset, inset)
    val lensSize = Size(w, h)
    val radius = CornerRadius(h / 2f)
    val center = Offset(inset + w / 2f, inset + h / 2f)

    // لمعة علوية ناعمة
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.White.copy(alpha = 0.30f), Color.White.copy(alpha = 0f)),
            startY = inset,
            endY = inset + h * 0.45f,
        ),
        topLeft = Offset(inset + w * 0.12f, inset + 2.dp.toPx()),
        size = Size(w * 0.76f, h * 0.40f),
        cornerRadius = CornerRadius(h * 0.20f),
    )

    // إضاءة داخلية عريضة
    val glow = 4.dp.toPx()
    drawRoundRect(
        color = Color.White.copy(alpha = 0.08f),
        topLeft = tl + Offset(glow / 2f + 1.dp.toPx(), glow / 2f + 1.dp.toPx()),
        size = Size(w - glow - 2.dp.toPx(), h - glow - 2.dp.toPx()),
        cornerRadius = CornerRadius((h - glow) / 2f),
        style = Stroke(width = glow),
    )

    // طيف لوني خفيف على الحافة
    val iris = 1.2.dp.toPx()
    drawRoundRect(
        brush = Brush.sweepGradient(
            colors = listOf(
                Color(0xFFFF6AD5).copy(alpha = 0.34f),
                Color(0xFF8C7CFF).copy(alpha = 0.34f),
                Color(0xFF5AE1FF).copy(alpha = 0.34f),
                Color(0xFFFFE66D).copy(alpha = 0.34f),
                Color(0xFFFF6AD5).copy(alpha = 0.34f),
            ),
            center = center,
        ),
        topLeft = tl + Offset(1.5.dp.toPx(), 1.5.dp.toPx()),
        size = Size(w - 3.dp.toPx(), h - 3.dp.toPx()),
        cornerRadius = CornerRadius((h - 3.dp.toPx()) / 2f),
        style = Stroke(width = iris),
    )

    // الإطار الخارجي المضيء: أقوى عند أعلى اليسار وأسفل اليمين
    val rim = 1.5.dp.toPx()
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.95f),
                Color.White.copy(alpha = 0.18f),
                Color.White.copy(alpha = 0.60f),
            ),
            start = tl,
            end = tl + Offset(w, h),
        ),
        topLeft = tl + Offset(rim / 2f, rim / 2f),
        size = Size(w - rim, h - rim),
        cornerRadius = CornerRadius((h - rim) / 2f),
        style = Stroke(width = rim),
    )
}

/**
 * تأثير الانكسار عند حافة العدسة (API 33+)، أو null على الأقدم فيبقى التكبير وحده.
 * يُبنى مرة لكل تغيّر في الأبعاد ولا يُعاد في كل إطار.
 */
@Composable
private fun rememberLensEffect(
    layerWidth: Float,
    layerHeight: Float,
    insetPx: Float,
    cornerPx: Float,
    edgePx: Float,
    bendPx: Float,
    dispersion: Float,
): RenderEffect? = remember(layerWidth, layerHeight, insetPx, cornerPx, edgePx, bendPx, dispersion) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || layerWidth <= 0f || layerHeight <= 0f) {
        null
    } else {
        createLensEffect(layerWidth, layerHeight, insetPx, cornerPx, edgePx, bendPx, dispersion)
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun createLensEffect(
    layerWidth: Float,
    layerHeight: Float,
    insetPx: Float,
    cornerPx: Float,
    edgePx: Float,
    bendPx: Float,
    dispersion: Float,
): RenderEffect {
    val shader = RuntimeShader(LensShader.SOURCE)
    shader.setFloatUniform("uSize", layerWidth, layerHeight)
    shader.setFloatUniform("uInset", insetPx)
    shader.setFloatUniform("uCorner", cornerPx)
    shader.setFloatUniform("uEdgeWidth", edgePx)
    shader.setFloatUniform("uBend", bendPx)
    shader.setFloatUniform("uDispersion", dispersion)
    return AndroidRenderEffect.createRuntimeShaderEffect(shader, "content").asComposeRenderEffect()
}

/**
 * شيدر AGSL لعدسة الشريط.
 *
 * الطبقة أكبر من العدسة المرئية بمقدار uInset من كل جهة، فيوجد محتوى حقيقي خارج حدّها.
 * عند الحافة يُقرأ اللون من موضع أبعد نحو الخارج بإزاحة تقوى عند الحدّ وتتلاشى نحو
 * الداخل، فيُضغط ما وراء الحافة داخل حلقة الانكسار كما في عدسة محدّبة. كل قناة لونية
 * تُقرأ بإزاحة مختلفة قليلاً (تشتّت لوني)، مع تفتيح بسيط عند الحدّ.
 */
private object LensShader {
    const val SOURCE: String = """
uniform shader content;
uniform float2 uSize;
uniform float uInset;
uniform float uCorner;
uniform float uEdgeWidth;
uniform float uBend;
uniform float uDispersion;

float lensSdf(float2 p) {
    float2 halfSize = uSize * 0.5 - float2(uInset);
    float2 q = abs(p) - halfSize + float2(uCorner);
    return min(max(q.x, q.y), 0.0) + length(max(q, float2(0.0))) - uCorner;
}

float2 clampCoord(float2 c) {
    return clamp(c, float2(0.5), uSize - float2(0.5));
}

half4 main(float2 fragCoord) {
    float2 p = fragCoord - uSize * 0.5;
    float depth = -lensSdf(p);

    if (depth >= uEdgeWidth || uBend <= 0.0) {
        return content.eval(fragCoord);
    }

    float dx = lensSdf(p + float2(1.0, 0.0)) - lensSdf(p - float2(1.0, 0.0));
    float dy = lensSdf(p + float2(0.0, 1.0)) - lensSdf(p - float2(0.0, 1.0));
    float2 grad = float2(dx, dy);
    float len = length(grad);
    float2 outward = len > 0.0001 ? grad / len : float2(0.0);

    float t = clamp(depth / uEdgeWidth, 0.0, 1.0);
    float bend = (1.0 - t) * (1.0 - t);
    float2 shift = outward * (bend * uBend);

    half4 cr = content.eval(clampCoord(fragCoord + shift * (1.0 + uDispersion)));
    half4 cg = content.eval(clampCoord(fragCoord + shift));
    half4 cb = content.eval(clampCoord(fragCoord + shift * (1.0 - uDispersion)));

    half lift = half(bend * 0.07);
    return half4(cr.r + lift, cg.g + lift, cb.b + lift, cg.a);
}
"""
}
