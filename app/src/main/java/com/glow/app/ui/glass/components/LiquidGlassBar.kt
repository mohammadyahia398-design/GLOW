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
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
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
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * كل ما يُعدَّل في شكل الشريط الزجاجي السائل في مكان واحد.
 *
 * @property height ارتفاع الكبسولة.
 * @property innerPadding الفراغ بين حافة الكبسولة والعدسة وهي مسطّحة.
 * @property lensGrow مقدار ازدياد ارتفاع العدسة عند اللمس (فتخرج من الكبسولة).
 * @property lensWiden نسبة ازدياد عرضها عند اللمس الكامل (نسبة إلى عرض العنصر الواحد).
 * @property restWiden نسبة ازدياد عرضها وهي ساكنة؛ تبقى كبسولة أعرض من ارتفاعها ولا تعود دائرة.
 * @property barFill تلوين الكبسولة (خفيف: الزجاج يُظهر ما تحته).
 * @property pressDamping ارتداد انتفاخ العدسة وتسطّحها (أقل = أكثر ارتداداً).
 * @property magnify تكبير مركز العدسة عند اللمس الكامل (1 يعطّله).
 * @property bleed عرض المحتوى الحقيقي الذي تلتقطه العدسة خارج حدّها لتنكسر به حافتها.
 * @property bezel عرض منطقة الحافة المنحنية التي يحدث فيها الانكسار.
 * @property thickness سماكة الزجاج؛ تحدد شدّة الانكسار عند الحافة.
 * @property refractiveIndex معامل الانكسار (الزجاج ≈ 1.5).
 * @property dispersion تشتّت لوني خفيف جداً عند الحافة (0 يعطّله).
 */
@Immutable
data class LiquidBarSpec(
    val height: Dp = 72.dp,
    val innerPadding: Dp = 5.dp,
    val lensGrow: Dp = 16.dp,
    val lensWiden: Float = 0.80f,
    val restWiden: Float = 0.25f,
    val iconSize: Dp = 26.dp,
    val labelSize: TextUnit = 12.sp,
    val barStyle: GlassStyle = GlassStyle.Regular.copy(
        tintAlpha = 0.06f,
        fallbackTintAlpha = 0.30f,
        borderAlpha = 0.40f,
        highlightAlpha = 0.40f,
        specularAlpha = 0.10f,
    ),
    val barFill: Color = Color(0xFF1B2A33).copy(alpha = 0.45f),
    val springDamping: Float = 0.72f,
    val springStiffness: Float = 380f,
    val pressDamping: Float = 0.60f,
    val pressStiffness: Float = 300f,
    val magnify: Float = 1.25f,
    val bleed: Dp = 24.dp,
    val bezel: Dp = 12.dp,
    val thickness: Dp = 22.dp,
    val refractiveIndex: Float = 1.5f,
    val dispersion: Float = 0.02f,
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
 * شريط تنقل Liquid Glass: كبسولة زجاجية فيها عدسة زجاجية تنزلق إلى العنصر المحدد.
 *
 * العدسة وهي مسطّحة (بلا لمس) قطعة زجاج رقيقة. عند اللمس أو السحب تنتفخ كقطعة زجاج
 * محدّبة: ترتفع فتخرج من الكبسولة، وتنكسر عند حافتها الصور الحقيقية التي تحتها
 * (محتوى التطبيق خلف الشريط والكبسولة نفسها) وفق قانون سنيل، ويبقى مركزها شبه ثابت.
 * عند الإفلات تعود مسطّحة بحركة نابضية.
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
    accent: Color = Color(0xFF22D3EE),
    unselected: Color = Color(0xFFF2F6F8),
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
    var pressed by remember { mutableStateOf(false) }
    var barWidthPx by remember { mutableFloatStateOf(0f) }
    var barRoot by remember { mutableStateOf(Offset.Zero) }
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

    // درجة انتفاخ العدسة: 0 مسطّحة، 1 منتفخة كلياً. نابض فيتجاوز قليلاً ثم يستقر.
    val pressSpec: AnimationSpec<Float> = if (reduceMotion) {
        snap()
    } else {
        spring<Float>(dampingRatio = spec.pressDamping, stiffness = spec.pressStiffness)
    }
    val pressState = animateFloatAsState(
        targetValue = if (dragging || pressed) 1f else 0f,
        animationSpec = pressSpec,
        label = "liquidPress",
    )
    val activeIndex by remember(count) {
        derivedStateOf { position.value.roundToInt().coerceIn(0, lastIndex) }
    }

    val padPx = with(density) { spec.innerPadding.toPx() }
    val growPx = with(density) { spec.lensGrow.toPx() }
    val heightPx = with(density) { spec.height.toPx() }
    val bleedPx = with(density) { spec.bleed.toPx() }
    val bezelPx = with(density) { spec.bezel.toPx() }
    val thickPx = with(density) { spec.thickness.toPx() }
    val shape = RoundedCornerShape(percent = 50)

    fun itemWidthPx(): Float =
        if (count > 0) ((barWidthPx - 2f * padPx) / count).coerceAtLeast(0f) else 0f

    // أبعاد العدسة وهي مسطّحة، وأكبر أبعاد تبلغها (مع تجاوز النابض 1.3).
    val lensW = itemWidthPx()
    val lensH = (heightPx - 2f * padPx).coerceAtLeast(0f)
    val maxG = 1.3f
    // معامل العرض: restWiden وهي ساكنة، ويصل إلى lensWiden عند اللمس الكامل (g = 1).
    fun widenAt(g: Float): Float = 1f + spec.restWiden + (spec.lensWiden - spec.restWiden) * g
    val layerW = lensW * widenAt(maxG) + 2f * bleedPx
    val layerH = lensH + growPx * maxG + 2f * bleedPx

    fun grownG(): Float = pressState.value.coerceIn(0f, maxG)
    fun halfWidth(g: Float): Float = lensW / 2f * widenAt(g)
    fun halfHeight(g: Float): Float = lensH / 2f + growPx / 2f * g

    // يمنع خروج العدسة المنتفخة يمين الكبسولة أو يسارها.
    fun layerLeft(): Float {
        val hw = halfWidth(grownG())
        val raw = padPx + (position.value + 0.5f) * itemWidthPx()
        val cx = max(hw, min(raw, barWidthPx - hw))
        return cx - layerW / 2f
    }
    fun layerTop(): Float = (heightPx - layerH) / 2f

    val effectHolder = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) LensEffectHolder() else null
    }

    // مصدر خاص بالعدسة: الكبسولة وعناصرها (فوق الخلفية الحقيقية).
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
            .onGloballyPositioned { barRoot = it.positionInRoot() }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        try {
                            tryAwaitRelease()
                        } finally {
                            pressed = false
                        }
                    },
                )
            }
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
            // تلوين خفيف؛ داخل المصدر فتراه العدسة معه.
            Box(Modifier.matchParentSize().background(spec.barFill, shape))
            // لمعة علوية ناعمة تعطي الكبسولة عمقاً زجاجياً.
            Box(
                Modifier.matchParentSize().background(
                    Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
                        endY = with(density) { spec.height.toPx() } * 0.5f,
                    ),
                    shape,
                ),
            )

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

        // 2) العدسة. كل شيء فيها يُقرأ في مرحلة التخطيط والرسم فلا تُعاد التركيبة كل إطار.
        if (lensW > 0f && layerW > 0f && layerH > 0f) {
            Box(
                // الطبقة أطول من الكبسولة: unbounded وإلا قصّها التخطيط ووسّطها فأزاحها للأعلى.
                Modifier
                    .wrapContentSize(Alignment.TopStart, unbounded = true)
                    .absoluteOffset { IntOffset(layerLeft().roundToInt(), layerTop().roundToInt()) }
                    .size(with(density) { layerW.toDp() }, with(density) { layerH.toDp() }),
            ) {
                // المحتوى المنكسر: الخلفية الحقيقية تحت الشريط ثم الكبسولة، مع انكسار الحافة.
                Box(
                    Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            val g = grownG()
                            val hw = halfWidth(g)
                            val hh = halfHeight(g)
                            this.shape = LensClipShape(
                                left = layerW / 2f - hw,
                                top = layerH / 2f - hh,
                                right = layerW / 2f + hw,
                                bottom = layerH / 2f + hh,
                                corner = hh,
                            )
                            this.clip = true
                            this.renderEffect =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && effectHolder != null && g > 0.02f) {
                                    effectHolder.effect(
                                        layerW = layerW,
                                        layerH = layerH,
                                        halfW = hw,
                                        halfH = hh,
                                        corner = min(hh, hw),
                                        bezel = bezelPx,
                                        thick = thickPx * g,
                                        index = spec.refractiveIndex,
                                        dispersion = spec.dispersion,
                                    )
                                } else {
                                    null
                                }
                        }
                        .drawBehind {
                            val g = grownG()
                            val mag = 1f + (spec.magnify - 1f) * min(g, 1f)
                            val left = layerLeft()
                            val top = layerTop()
                            val pivot = Offset(size.width / 2f, size.height / 2f)
                            scale(mag, mag, pivot) {
                                val source = backdrop
                                if (source != null && !source.layer.isReleased) {
                                    val delta = source.sourceOrigin - (barRoot + Offset(left, top))
                                    translate(left = delta.x, top = delta.y) {
                                        drawLayer(source.layer)
                                    }
                                }
                                if (!barBackdrop.layer.isReleased) {
                                    translate(left = -left, top = -top) {
                                        drawLayer(barBackdrop.layer)
                                    }
                                }
                            }
                        },
                )
                // الحافة اللامعة والإضاءة: فوق المحتوى المنكسر فتبقى حادّة.
                Box(
                    Modifier.matchParentSize().drawBehind {
                        val g = grownG()
                        val hw = halfWidth(g)
                        val hh = halfHeight(g)
                        drawLensRim(
                            left = size.width / 2f - hw,
                            top = size.height / 2f - hh,
                            width = hw * 2f,
                            height = hh * 2f,
                            press = g,
                        )
                    },
                )
            }
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
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** مستطيل مدوّر بإحداثيات صريحة: حدّ العدسة المرئي داخل طبقتها الأكبر. */
private class LensClipShape(
    private val left: Float,
    private val top: Float,
    private val right: Float,
    private val bottom: Float,
    private val corner: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val r = corner.coerceAtLeast(0f)
        return Outline.Rounded(RoundRect(left, top, right, bottom, CornerRadius(r, r)))
    }
}

/**
 * يرسم حافة العدسة: جسم زجاجي رقيق مسطّح بلا لمس، وعند اللمس تقوى الإضاءة الحادّة
 * على الحافة. لا ألوان مرسومة: أي لون على الحافة يأتي من انكسار ما تحتها.
 */
private fun DrawScope.drawLensRim(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    press: Float,
) {
    if (width <= 0f || height <= 0f) return
    val p = press.coerceIn(0f, 1f)
    val tl = Offset(left, top)
    val radius = CornerRadius(height / 2f)

    // جسم الزجاج: ضبابي قليلاً وهو مسطّح، وأصفى عند الانتفاخ.
    drawRoundRect(
        color = Color.White.copy(alpha = 0.12f - 0.10f * p),
        topLeft = tl,
        size = Size(width, height),
        cornerRadius = radius,
    )

    // إضاءة داخلية ناعمة على طول الحافة.
    val glow = 3.dp.toPx()
    drawRoundRect(
        color = Color.White.copy(alpha = 0.04f + 0.03f * p),
        topLeft = tl + Offset(glow / 2f, glow / 2f),
        size = Size(width - glow, height - glow),
        cornerRadius = CornerRadius((height - glow) / 2f),
        style = Stroke(width = glow),
    )

    // خط داكن رفيع خارج الحافة: يفصل العدسة عمّا تحتها بحدّ واضح.
    val hair = 1.dp.toPx()
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.10f + 0.22f * p),
        topLeft = tl - Offset(hair / 2f, hair / 2f),
        size = Size(width + hair, height + hair),
        cornerRadius = CornerRadius((height + hair) / 2f),
        style = Stroke(width = hair),
    )

    // الإطار اللامع: أقوى عند الزاويتين المقابلتين للضوء.
    val rim = 1.2.dp.toPx()
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.55f + 0.40f * p),
                Color.White.copy(alpha = 0.10f),
                Color.White.copy(alpha = 0.40f + 0.30f * p),
            ),
            start = tl,
            end = tl + Offset(width, height),
        ),
        topLeft = tl + Offset(rim / 2f, rim / 2f),
        size = Size(width - rim, height - rim),
        cornerRadius = CornerRadius((height - rim) / 2f),
        style = Stroke(width = rim),
    )
}

/**
 * يبني تأثير الانكسار (API 33+). يحتفظ بالشيدر ويحدّث قيمه في كل استدعاء،
 * فلا يُنشأ شيدر جديد في كل إطار.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private class LensEffectHolder {
    private val shader = RuntimeShader(LensShader.SOURCE)

    fun effect(
        layerW: Float,
        layerH: Float,
        halfW: Float,
        halfH: Float,
        corner: Float,
        bezel: Float,
        thick: Float,
        index: Float,
        dispersion: Float,
    ): RenderEffect {
        shader.setFloatUniform("uSize", layerW, layerH)
        shader.setFloatUniform("uHalf", halfW, halfH)
        shader.setFloatUniform("uCorner", corner)
        shader.setFloatUniform("uBezel", bezel)
        shader.setFloatUniform("uThick", thick)
        shader.setFloatUniform("uIndex", index)
        shader.setFloatUniform("uDisp", dispersion)
        return AndroidRenderEffect.createRuntimeShaderEffect(shader, "content").asComposeRenderEffect()
    }
}

/**
 * شيدر AGSL لزجاج محدّب فيزيائي.
 *
 * الحافة سطح محدّب بمنحنى «سكوِركل» ارتفاعه h(t) حيث t عمق البكسل داخل منطقة الحافة.
 * ميل السطح يعطي زاوية سقوط، وقانون سنيل يعطي زاوية الانكسار، والإزاحة الجانبية =
 * السماكة المتبقية × tan(زاوية السقوط − زاوية الانكسار). تُقرأ الألوان من موضع أبعد
 * نحو الخارج فيُضغط ما وراء الحافة داخلها كما في كرة زجاجية. المركز مسطّح فلا يتشوّه.
 * إن كانت السماكة صفراً (العدسة مسطّحة) لا يتغير شيء.
 */
private object LensShader {
    const val SOURCE: String = """
uniform shader content;
uniform float2 uSize;
uniform float2 uHalf;
uniform float uCorner;
uniform float uBezel;
uniform float uThick;
uniform float uIndex;
uniform float uDisp;

float lensSdf(float2 p) {
    float2 q = abs(p) - uHalf + float2(uCorner);
    return min(max(q.x, q.y), 0.0) + length(max(q, float2(0.0))) - uCorner;
}

float2 clampCoord(float2 c) {
    return clamp(c, float2(0.5), uSize - float2(0.5));
}

half4 main(float2 fragCoord) {
    float2 p = fragCoord - uSize * 0.5;
    float depth = -lensSdf(p);

    if (depth >= uBezel || depth < 0.0 || uThick <= 0.0) {
        return content.eval(fragCoord);
    }

    float dx = lensSdf(p + float2(1.0, 0.0)) - lensSdf(p - float2(1.0, 0.0));
    float dy = lensSdf(p + float2(0.0, 1.0)) - lensSdf(p - float2(0.0, 1.0));
    float2 grad = float2(dx, dy);
    float len = length(grad);
    float2 outward = len > 0.0001 ? grad / len : float2(0.0);

    float t = clamp(depth / uBezel, 0.0, 1.0);
    float u = 1.0 - t;
    float u3 = u * u * u;
    float inner = max(1.0 - u3 * u, 0.0001);
    float h = pow(inner, 0.25);
    float slope = uThick * (u3 * pow(inner, -0.75)) / uBezel;

    float thetaI = atan(slope);
    float thetaT = asin(clamp(sin(thetaI) / uIndex, -1.0, 1.0));
    float shiftLen = uThick * h * tan(thetaI - thetaT);
    float2 shift = outward * shiftLen;

    half4 cr = content.eval(clampCoord(fragCoord + shift * (1.0 + uDisp)));
    half4 cg = content.eval(clampCoord(fragCoord + shift));
    half4 cb = content.eval(clampCoord(fragCoord + shift * (1.0 - uDisp)));
    return half4(cr.r, cg.g, cb.b, cg.a);
}
"""
}
