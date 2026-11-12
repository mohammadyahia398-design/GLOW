package com.glow.app.ui.nowplaying

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.graphics.Color
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

/** الكبسولة السفلية: بلون الغلاف الحالي، فيها الغلاف والعنوان وأزرار سابق/تشغيل/تالٍ. */
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
    val background by animateColorAsState(palette.primary, label = "miniBackground")
    val content = if (background.luminance() > 0.5f) Color(0xFF07141C) else Color.White
    val shape = RoundedCornerShape(36.dp)

    Box(
        modifier
            .fillMaxWidth()
            .height(72.dp)
            .shadow(8.dp, shape)
            .clip(shape)
            .drawBehind { drawRect(background) }
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 36.dp, vertical = 4.dp)
                .height(2.dp)
                .drawBehind {
                    val radius = CornerRadius(size.height / 2f)
                    drawRoundRect(color = content.copy(alpha = 0.25f), cornerRadius = radius)
                    val fill = size.width * progress().coerceIn(0f, 1f)
                    if (fill > 0f) {
                        drawRoundRect(
                            color = content,
                            topLeft = Offset.Zero,
                            size = Size(fill, size.height),
                            cornerRadius = radius,
                        )
                    }
                },
        )
        Row(
            Modifier.fillMaxWidth().height(72.dp).padding(start = 10.dp, end = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AlbumArt(track, Modifier.size(52.dp), radius = 26.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    color = content,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = track.artist,
                    color = content.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            GlowIconTap("السابق", onPrevious, size = 44.dp) { GlowIcon(IconKind.Prev, content, 24.dp) }
            GlowIconTap(if (isPlaying) "إيقاف مؤقت" else "تشغيل", onTogglePlay, size = 48.dp) {
                GlowIcon(if (isPlaying) IconKind.Pause else IconKind.Play, content, 30.dp)
            }
            GlowIconTap("التالي", onNext, size = 44.dp) { GlowIcon(IconKind.Next, content, 24.dp) }
        }
    }
}
