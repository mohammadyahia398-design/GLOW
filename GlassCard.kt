package com.glow.app.ui.glass.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.glow.app.ui.glass.core.GlassDepth
import com.glow.app.ui.glass.core.GlassStyle
import com.glow.app.ui.glass.motion.glassClickable
import com.glow.app.ui.glass.motion.rememberGlassInteraction
import com.glow.app.ui.glass.surfaces.GlassBackdrop
import com.glow.app.ui.glass.surfaces.GlassSurface
import com.glow.app.ui.glass.surfaces.GlassSurfaceDefaults

/**
 * بطاقة زجاجية بمحتوى عمودي.
 *
 * عند تمرير [onClick] تصبح قابلة للنقر، ويتبع الضوء اللمس ويصغر الحجم قليلاً
 * عند الضغط. بدون [onClick] تبقى سطحاً ساكناً.
 *
 * @param backdrop الخلفية المشتركة (انظر [GlassSurface]).
 * @param onClickLabel وصف الإجراء لقارئات الشاشة.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    backdrop: GlassBackdrop? = null,
    shape: Shape = GlassSurfaceDefaults.CardShape,
    style: GlassStyle = GlassStyle.Regular,
    depth: GlassDepth = GlassDepth.Raised,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interaction = if (onClick != null) rememberGlassInteraction() else null

    val clickModifier = if (interaction != null && onClick != null) {
        Modifier.glassClickable(
            state = interaction,
            onClickLabel = onClickLabel,
            role = Role.Button,
            onClick = onClick,
        )
    } else {
        Modifier
    }

    GlassSurface(
        modifier = modifier.then(clickModifier),
        backdrop = backdrop,
        shape = shape,
        style = style,
        depth = depth,
        interaction = interaction,
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}
