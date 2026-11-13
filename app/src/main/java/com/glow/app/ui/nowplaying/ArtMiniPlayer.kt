package com.glow.app.ui.nowplaying

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glow.app.media.Track
import com.glow.app.ui.common.AlbumArt
import com.glow.app.ui.common.GlowIcon
import com.glow.app.ui.common.GlowIconTap
import com.glow.app.ui.common.IconKind
import com.glow.app.ui.common.rememberArtPalette

/** ارتفاع المشغّل المصغّر؛ يعتمد عليه `GlowApp` لحساب طبقة التلاشي السفلية. */
val ArtMiniPlayerHeight = 64.dp

private val GlowInk = Color(0xFF080B10)

/**
 * المشغّل المصغّر بهوية التوهّج: كبسولة داكنة يتسرّب إليها لون غلاف الأغنية من الأطراف،
 * بحافة لامعة، وكرة تشغيل متوهّجة، وشريط تقدّم رفيع برأس مضيء.
 * النص دائماً فاتح فوق خلفية داكنة، فالتباين ثابت مهما كان الغلاف.
 */
@Composable
fun ArtMiniPlayer(
    track: Track,
    isPlaying: Boolean,
    progress: () -> Float,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = rememberArtPalette(track)
    val primary by animateColorAsState(palette.primary, tween(450), label = "miniPrimary")
    val secondary by animateColorAsState(palette.secondary, tween(450), label = "miniSecondary")
    val base = lerp(GlowInk, primary, 0.16f)
    val shape = RoundedCornerShape(32.dp)
    val orbIcon = if (lerp(primary, secondary, 0.5f).luminance() > 0.55f) GlowInk else Color.White

    Box(
        modifier
            .fillMaxWidth()
            .height(ArtMiniPlayerHeight)
            .shadow(16.dp, shape, ambientColor = primary.copy(alpha = 0.6f), spotColor = primary)
            .clip(shape)
            .drawBehind {
                drawRect(base)
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(primary.copy(alpha = 0.60f), primary.copy(alpha = 0f)),
                        center = Offset(size.width * 0.10f, size.height * 1.15f),
                        radius = size.width * 0.55f,
                    ),
                )
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(secondary.copy(alpha = 0.34f), secondary.copy(alpha = 0f)),
                        center = Offset(size.width * 0.92f, -size.height * 0.20f),
                        radius = size.width * 0.45f,
                    ),
                )
                drawRect(
                    Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0f)),
                        endY = size.height * 0.5f,
                    ),
                )
            }
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.45f),
                        Color.White.copy(alpha = 0.06f),
                        secondary.copy(alpha = 0.35f),
                    ),
                ),
                shape,
            )
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Row(
            Modifier.fillMaxWidth().height(ArtMiniPlayerHeight).padding(start = 10.dp, end = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AlbumArt(track, Modifier.size(42.dp), radius = 13.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = track.artist,
                    color = Color.White.copy(alpha = 0.70f),
                    fontSize = 12.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            GlowIconTap("السابق", onPrevious, size = 40.dp) {
                GlowIcon(IconKind.Prev, Color.White.copy(alpha = 0.88f), 22.dp)
            }
            GlowIconTap(if (isPlaying) "إيقاف مؤقت" else "تشغيل", onTogglePlay, size = 46.dp) {
                Box(
                    Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .drawBehind {
                            drawCircle(
                                Brush.radialGradient(
                                    colors = listOf(secondary, primary),
                                    center = Offset(size.width * 0.35f, size.height * 0.30f),
                                    radius = size.width * 0.85f,
                                ),
                            )
                            drawCircle(
                                Brush.radialGradient(
                                    colors = listOf(Color.White.copy(alpha = 0.65f), Color.White.copy(alpha = 0f)),
                                    center = Offset(size.width * 0.32f, size.height * 0.26f),
                                    radius = size.width * 0.38f,
                                ),
                            )
                        }
                        .border(
                            1.dp,
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.70f), Color.White.copy(alpha = 0.10f)),
                            ),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    GlowIcon(if (isPlaying) IconKind.Pause else IconKind.Play, orbIcon, 24.dp)
                }
            }
            GlowIconTap("التالي", onNext, size = 40.dp) {
                GlowIcon(IconKind.Next, Color.White.copy(alpha = 0.88f), 22.dp)
            }
        }
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 30.dp, vertical = 6.dp)
                .height(3.dp)
                .drawBehind {
                    val radius = CornerRadius(size.height / 2f)
                    drawRoundRect(Color.White.copy(alpha = 0.14f), cornerRadius = radius)
                    val fill = size.width * progress().coerceIn(0f, 1f)
                    if (fill > 0f) {
                        drawRoundRect(
                            Brush.horizontalGradient(listOf(primary, secondary), startX = 0f, endX = maxOf(fill, 1f)),
                            size = Size(fill, size.height),
                            cornerRadius = radius,
                        )
                        val head = Offset(fill, size.height / 2f)
                        drawCircle(secondary.copy(alpha = 0.30f), radius = 7.dp.toPx(), center = head)
                        drawCircle(Color.White, radius = 2.5.dp.toPx(), center = head)
                    }
                },
        )
    }
}
