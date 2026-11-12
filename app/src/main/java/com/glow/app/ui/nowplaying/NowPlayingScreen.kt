package com.glow.app.ui.nowplaying

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glow.app.media.PlayerState
import com.glow.app.ui.common.AlbumArt
import com.glow.app.ui.common.GlowTopBar
import com.glow.app.ui.common.IconKind
import com.glow.app.ui.common.rememberArtPalette
import com.glow.app.ui.theme.LocalGlowSkin

/** المشغّل الكامل: غلاف كبير وعنوان وعناصر التحكم، فوق خلفية حيّة بألوان الغلاف. */
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
    val skin = LocalGlowSkin.current
    val track = state.current ?: return
    val palette = rememberArtPalette(track)
    val contentColor = playerContentColor(palette, skin)

    Box(modifier.fillMaxSize()) {
        AnimatedArtBackground(palette, Modifier.matchParentSize())
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
                color = skin.text,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = track.artist,
                color = skin.textSecondary,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp),
            )
            Spacer(Modifier.weight(1f))
            NowPlayingControls(
                state = state,
                contentColor = contentColor,
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
