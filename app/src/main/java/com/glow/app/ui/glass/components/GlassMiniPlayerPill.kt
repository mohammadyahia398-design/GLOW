package com.glow.app.ui.glass.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glow.app.ui.glass.core.GlassDepth
import com.glow.app.ui.glass.core.GlassStyle
import com.glow.app.ui.glass.motion.glassClickable
import com.glow.app.ui.glass.motion.rememberGlassInteraction
import com.glow.app.ui.glass.surfaces.GlassBackdrop
import com.glow.app.ui.glass.surfaces.GlassSurface
import com.glow.app.ui.theme.LocalGlowSkin

private val Ivory = Color(0xFFF5F6FA)
private val Muted = Color(0xFF8C92A4)
private val Charcoal = Color(0xFF0D0E12)
private val DefaultAccent = Color(0xFF06B6D4)

private val PillHeight = 64.dp

/**
 * كبسولة المشغل المصغر العائمة: غلاف وعنوان وفنان وشريط تقدم رفيع وأزرار تشغيل.
 *
 * @param progress دالة تعيد التقدم من 0 إلى 1. تُقرأ أثناء الرسم، فتحديث
 * التقدم كل ثانية لا يعيد تركيب الكبسولة. في RTL يمتلئ الشريط من اليمين.
 * @param onClick فتح المشغل الكامل.
 * @param onNext زر التالي؛ لا يظهر عند null.
 * @param artwork خانة الغلاف (دائرة 48dp)؛ تُترك فارغة عند null.
 * @param playLabel وصف زر التشغيل أو الإيقاف لقارئات الشاشة، بحسب الحالة.
 */
@Composable
fun GlassMiniPlayerPill(
    title: String,
    subtitle: String,
    isPlaying: Boolean,
    progress: () -> Float,
    onTogglePlay: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backdrop: GlassBackdrop? = null,
    onNext: (() -> Unit)? = null,
    accent: Color = LocalGlowSkin.current.accent,
    playLabel: String = if (isPlaying) "Pause" else "Play",
    nextLabel: String = "Next",
    artwork: (@Composable () -> Unit)? = null,
) {
    val skin = LocalGlowSkin.current
    val interaction = rememberGlassInteraction()

    GlassSurface(
        modifier = modifier
            .height(PillHeight)
            .glassClickable(
                state = interaction,
                role = Role.Button,
                onClick = onClick,
            ),
        backdrop = backdrop,
        shape = RoundedCornerShape(32.dp),
        style = GlassStyle.Regular,
        depth = GlassDepth.Floating,
        interaction = interaction,
    ) {
        Box(Modifier.fillMaxSize()) {
            // شريط التقدم الرفيع في أسفل الكبسولة.
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 5.dp)
                    .height(2.dp)
                    .drawBehind {
                        val fraction = progress().coerceIn(0f, 1f)
                        val radius = CornerRadius(size.height / 2f)
                        drawRoundRect(color = skin.chip, cornerRadius = radius)
                        val fillWidth = size.width * fraction
                        if (fillWidth > 0f) {
                            val rtl = layoutDirection == LayoutDirection.Rtl
                            drawRoundRect(
                                color = accent,
                                topLeft = Offset(if (rtl) size.width - fillWidth else 0f, 0f),
                                size = Size(fillWidth, size.height),
                                cornerRadius = radius,
                            )
                        }
                    },
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp, end = 10.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(skin.chip),
                    contentAlignment = Alignment.Center,
                ) {
                    artwork?.invoke()
                }

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = title,
                        color = skin.text,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = subtitle,
                        color = skin.textSecondary,
                        fontSize = 12.sp,
                        letterSpacing = 0.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (onNext != null) {
                    PillControl(
                        onClick = onNext,
                        description = nextLabel,
                        background = Color.Transparent,
                    ) {
                        NextIcon(color = skin.text)
                    }
                }

                PillControl(
                    onClick = onTogglePlay,
                    description = playLabel,
                    background = accent,
                ) {
                    PlayPauseIcon(isPlaying = isPlaying, color = skin.onAccent)
                }
            }
        }
    }
}

/** زر دائري خفيف داخل الكبسولة (ليس زجاجاً متداخلاً، حفاظاً على الأداء). */
@Composable
private fun PillControl(
    onClick: () -> Unit,
    description: String,
    background: Color,
    content: @Composable () -> Unit,
) {
    val interaction = rememberGlassInteraction()
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(background)
            .semantics { this.contentDescription = description }
            .glassClickable(state = interaction, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun PlayPauseIcon(isPlaying: Boolean, color: Color) {
    Canvas(Modifier.size(18.dp)) {
        val w = size.width
        val h = size.height
        if (isPlaying) {
            val barWidth = w * 0.28f
            val corner = CornerRadius(barWidth * 0.3f)
            drawRoundRect(
                color = color,
                topLeft = Offset(w * 0.12f, 0f),
                size = Size(barWidth, h),
                cornerRadius = corner,
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(w * 0.60f, 0f),
                size = Size(barWidth, h),
                cornerRadius = corner,
            )
        } else {
            val triangle = Path().apply {
                moveTo(w * 0.18f, 0f)
                lineTo(w * 0.95f, h / 2f)
                lineTo(w * 0.18f, h)
                close()
            }
            drawPath(triangle, color)
        }
    }
}

/** أيقونة «التالي»، تنعكس أفقياً في RTL لتشير إلى اتجاه التقدم. */
@Composable
private fun NextIcon(color: Color) {
    Canvas(Modifier.size(18.dp)) {
        val w = size.width
        val h = size.height
        val mirror = layoutDirection == LayoutDirection.Rtl
        scale(
            scaleX = if (mirror) -1f else 1f,
            scaleY = 1f,
            pivot = Offset(w / 2f, h / 2f),
        ) {
            val triangle = Path().apply {
                moveTo(0f, 0f)
                lineTo(w * 0.72f, h / 2f)
                lineTo(0f, h)
                close()
            }
            drawPath(triangle, color)
            drawRoundRect(
                color = color,
                topLeft = Offset(w * 0.82f, 0f),
                size = Size(w * 0.16f, h),
                cornerRadius = CornerRadius(w * 0.05f),
            )
        }
    }
}
