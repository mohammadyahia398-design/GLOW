package com.glow.app.ui.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glow.app.media.PlayerState
import com.glow.app.ui.common.AlbumArt
import com.glow.app.ui.common.GlowTopBar
import com.glow.app.ui.common.IconKind
import com.glow.app.ui.common.averageColor
import com.glow.app.ui.common.rememberArt
import com.glow.app.ui.glass.surfaces.glassBackdropSource
import com.glow.app.ui.glass.surfaces.rememberGlassBackdrop

/** المشغّل الكامل: غلاف كبير، عنوان، وعناصر التحكم، بخلفية متدرجة من لون الغلاف. */
@Composable
fun NowPlayingScreen(
    state: PlayerState,
    onClose: () -> Unit,
    onSeek: (Long) -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val track = state.current ?: return
    val bmp = rememberArt(track, 800)
    val tint = remember(bmp) { bmp?.averageColor() ?: Color(0xFF4A69BD) }
    val backdrop = rememberGlassBackdrop()

    Box(modifier.fillMaxSize()) {
        Box(
            Modifier
                .matchParentSize()
                .glassBackdropSource(backdrop)
                .background(
                    Brush.verticalGradient(listOf(tint.copy(alpha = 0.75f), Color(0xFF0D0E12))),
                ),
        )
        Column(
            Modifier.fillMaxSize().navigationBarsPadding().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GlowTopBar(title = null, onBack = onClose, backIcon = IconKind.Down)
            Spacer(Modifier.height(8.dp))
            AlbumArt(track, Modifier.fillMaxWidth().aspectRatio(1f), radius = 32.dp, px = 800)
            Spacer(Modifier.height(24.dp))
            Text(
                text = track.title,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = track.artist,
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp),
            )
            Spacer(Modifier.weight(1f))
            NowPlayingControls(
                state = state,
                backdrop = backdrop,
                onSeek = onSeek,
                onTogglePlay = onTogglePlay,
                onNext = onNext,
                onPrevious = onPrevious,
                onShuffle = onShuffle,
                onRepeat = onRepeat,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
