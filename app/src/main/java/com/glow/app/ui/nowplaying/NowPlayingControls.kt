package com.glow.app.ui.nowplaying

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glow.app.media.PlayerState
import com.glow.app.media.RepeatMode
import com.glow.app.ui.common.GlowIcon
import com.glow.app.ui.common.GlowIconTap
import com.glow.app.ui.common.IconKind
import com.glow.app.ui.common.formatTime

/** شريط التقدم مع الزمن، وصف أزرار: خلط، سابق، تشغيل، تالٍ، تكرار. كلها بلون [contentColor]. */
@Composable
fun NowPlayingControls(
    state: PlayerState,
    contentColor: Color,
    onSeek: (Long) -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var drag by remember { mutableStateOf<Float?>(null) }
    val value = drag ?: state.progress
    val dim = contentColor.copy(alpha = 0.75f)

    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        ArtSeekBar(
            value = value,
            onValueChange = { drag = it },
            onValueChangeFinished = {
                drag?.let { onSeek((it * state.durationMs).toLong()) }
                drag = null
            },
            color = contentColor,
            description = "التقدم",
        )
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(formatTime((value * state.durationMs).toLong()), color = dim, fontSize = 13.sp)
            Text(formatTime(state.durationMs), color = dim, fontSize = 13.sp)
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlowIconTap("خلط", onShuffle) {
                GlowIcon(IconKind.Shuffle, contentColor.copy(alpha = if (state.shuffle) 1f else 0.5f))
            }
            GlowIconTap("السابق", onPrevious, size = 56.dp) {
                GlowIcon(IconKind.Prev, contentColor, 32.dp)
            }
            GlowIconTap(
                description = if (state.isPlaying) "إيقاف مؤقت" else "تشغيل",
                onClick = onTogglePlay,
                size = 76.dp,
            ) {
                GlowIcon(
                    if (state.isPlaying) IconKind.Pause else IconKind.Play,
                    contentColor,
                    52.dp,
                )
            }
            GlowIconTap("التالي", onNext, size = 56.dp) {
                GlowIcon(IconKind.Next, contentColor, 32.dp)
            }
            GlowIconTap("تكرار", onRepeat) {
                GlowIcon(
                    if (state.repeat == RepeatMode.One) IconKind.RepeatOne else IconKind.Repeat,
                    contentColor.copy(alpha = if (state.repeat == RepeatMode.Off) 0.5f else 1f),
                )
            }
        }
    }
}
