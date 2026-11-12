package com.glow.app.ui.glass.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glow.app.ui.glass.core.GlassDepth
import com.glow.app.ui.glass.core.GlassStyle
import com.glow.app.ui.glass.core.LocalGlassConfig
import com.glow.app.ui.glass.motion.NoIndication
import com.glow.app.ui.glass.motion.glassTween
import com.glow.app.ui.glass.surfaces.GlassBackdrop
import com.glow.app.ui.glass.surfaces.GlassSurface

/** ألوان وأبعاد الشريط الافتراضية، مأخوذة من لوحة Glow. */
object GlassBarDefaults {
    /** Amber. */
    val Accent = Color(0xFF06B6D4)

    /** Ivory. */
    val SelectedContent = Color(0xFFF5F6FA)

    /** Muted. */
    val UnselectedContent = Color(0xFF8C92A4)

    val Height = 64.dp
}

/**
 * عنصر في شريط التنقل.
 *
 * @property id معرّف ثابت للعنصر.
 * @property label النص الظاهر تحت الأيقونة.
 * @property icon الأيقونة، تستقبل حالة التحديد لتتغير بحسبها.
 */
@Immutable
class GlassBarItem(
    val id: String,
    val label: String,
    val icon: @Composable (selected: Boolean) -> Unit,
)

/**
 * شريط تنقل زجاجي عائم. يُوضع فوق المحتوى وليس بداخله، مع هوامش من المستدعي
 * (مثلاً navigationBarsPadding وpadding أفقي).
 *
 * العنصر المحدد يظهر عليه مؤشر بلون [accent]. الحركة فورية عند تقليل الحركة.
 */
@Composable
fun GlassFloatingBar(
    items: List<GlassBarItem>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: GlassBackdrop? = null,
    accent: Color = GlassBarDefaults.Accent,
    selectedContent: Color = GlassBarDefaults.SelectedContent,
    unselectedContent: Color = GlassBarDefaults.UnselectedContent,
) {
    val reduceMotion = LocalGlassConfig.current.reduceMotion

    GlassSurface(
        modifier = modifier.height(GlassBarDefaults.Height),
        backdrop = backdrop,
        shape = RoundedCornerShape(32.dp),
        style = GlassStyle.Regular,
        depth = GlassDepth.Floating,
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
                .selectableGroup(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            for (item in items) {
                BarItem(
                    item = item,
                    selected = item.id == selectedId,
                    accent = accent,
                    selectedContent = selectedContent,
                    unselectedContent = unselectedContent,
                    reduceMotion = reduceMotion,
                    onClick = { onSelect(item.id) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun BarItem(
    item: GlassBarItem,
    selected: Boolean,
    accent: Color,
    selectedContent: Color,
    unselectedContent: Color,
    reduceMotion: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val indicator by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = glassTween(reduceMotion),
        label = "barIndicator",
    )
    val tint by animateColorAsState(
        targetValue = if (selected) selectedContent else unselectedContent,
        animationSpec = glassTween(reduceMotion),
        label = "barTint",
    )
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(26.dp))
            // القراءة داخل drawBehind: تغيّر المؤشر لا يعيد التركيب.
            .drawBehind { drawRect(accent.copy(alpha = 0.18f * indicator)) }
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = NoIndication,
                role = Role.Tab,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                item.icon(selected)
            }
            Text(
                text = item.label,
                color = tint,
                fontSize = 11.sp,
                letterSpacing = 0.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
