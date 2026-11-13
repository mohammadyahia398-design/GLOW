package com.glow.app.ui.glass.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
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
import com.glow.app.ui.theme.LocalGlowSkin
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * كل ما يُعدَّل في شكل الشريط الزجاجي السائل في مكان واحد.
 * مرّر نسخة معدّلة: `LiquidBarSpec(height = 72.dp, lensGrow = 0.dp)`.
 *
 * @property height ارتفاع الكبسولة.
 * @property innerPadding الفراغ بين حافة الكبسولة والعدسة.
 * @property lensGrow مقدار تضخّم العدسة أثناء السحب (0 يعطّله).
 * @property barStyle نمط زجاج الكبسولة.
 * @property lensStyle نمط زجاج العدسة (الانكسار فيها أقوى).
 * @property lensAccentAlpha شفافية صبغة لون التمييز داخل العدسة.
 * @property springDamping ارتداد حركة العدسة (أقل = أكثر ارتداداً).
 * @property springStiffness صلابة الحركة (أعلى = أسرع).
 */
@Immutable
data class LiquidBarSpec(
    val height: Dp = 68.dp,
    val innerPadding: Dp = 6.dp,
    val lensGrow: Dp = 10.dp,
    val iconSize: Dp = 26.dp,
    val labelSize: TextUnit = 11.sp,
    val barStyle: GlassStyle = GlassStyle.Regular.copy(
        tintAlpha = 0.14f,
        fallbackTintAlpha = 0.30f,
        borderAlpha = 0.26f,
    ),
    val lensStyle: GlassStyle = GlassStyle.Clear.copy(
        tintAlpha = 0.10f,
        fallbackTintAlpha = 0.20f,
        borderAlpha = 0.34f,
        highlightAlpha = 0.50f,
    ),
    val lensAccentAlpha: Float = 0.14f,
    val springDamping: Float = 0.74f,
    val springStiffness: Float = 420f,
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
 * شريط تنقل Liquid Glass: كبسولة زجاجية فيها «عدسة» زجاجية تنزلق إلى العنصر
 * المحدد، ويمكن سحبها بالإصبع بين العناصر (تتضخم أثناء السحب وتستقر بحركة نابضة).
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
    val shape = RoundedCornerShape(percent = 50)

    fun itemWidthPx(): Float =
        if (count > 0) ((barWidthPx - 2f * padPx) / count).coerceAtLeast(0f) else 0f

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
        // 1) الكبسولة
        GlassSurface(
            modifier = Modifier.matchParentSize(),
            backdrop = backdrop,
            shape = shape,
            style = spec.barStyle,
            depth = GlassDepth.Floating,
        ) {}

        // 2) العدسة: تتحرك وتتضخم داخل مرحلة التخطيط فلا تُعاد التركيبة كل إطار.
        GlassSurface(
            modifier = Modifier
                .absoluteOffset {
                    val extra = growPx * growState.value
                    IntOffset(
                        (padPx + position.value * itemWidthPx() - extra / 2f).roundToInt(),
                        (padPx - extra / 2f).roundToInt(),
                    )
                }
                .layout { measurable, _ ->
                    val extra = growPx * growState.value
                    val w = (itemWidthPx() + extra).roundToInt().coerceAtLeast(0)
                    val h = (heightPx - 2f * padPx + extra).roundToInt().coerceAtLeast(0)
                    val placeable = measurable.measure(Constraints.fixed(w, h))
                    layout(w, h) { placeable.place(0, 0) }
                },
            backdrop = backdrop,
            shape = shape,
            style = spec.lensStyle,
            depth = GlassDepth.Raised,
        ) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(accent.copy(alpha = spec.lensAccentAlpha)),
            )
        }

        // 3) العناصر فوق العدسة
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
