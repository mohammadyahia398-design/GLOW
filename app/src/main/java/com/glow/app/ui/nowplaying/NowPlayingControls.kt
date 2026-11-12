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
import com.glow.app.ui.common.Amber
import com.glow.app.ui.common.GlowIcon
import com.glow.app.ui.common.GlowIconTap
import com.glow.app.ui.common.IconKind
import com.glow.app.ui.common.formatTime
import com.glow.app.ui.glass.components.GlassSlider
import com.glow.app.ui.glass.surfaces.GlassBackdrop

/** شريط التقدم الزجاجي مع الزمن، وصف أزرار: خلط، سابق، تشغيل، تالٍ، تكرار. */
@Composable
fun NowPlayingControls(
    state: PlayerState,
    backdrop: GlassBackdrop,
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
    val dim = Color.White.copy(alpha = 0.75f)

    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        GlassSlider(
            value = value,
            onValueChange = { drag = it },
            onValueChangeFinished = {
                drag?.let { onSeek((it * state.durationMs).toLong()) }
                drag = null
            },
            backdrop = backdrop,
            contentDescription = "التقدم",
            trackHeight = 32.dp,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(formatTime((value * state.durationMs).toLong()), color = dim, fontSize = 12.sp)
            Text(formatTime(state.durationMs), color = dim, fontSize = 12.sp)
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlowIconTap("خلط", onShuffle) {
                GlowIcon(IconKind.Shuffle, if (state.shuffle) Amber else dim)
            }
            GlowIconTap("السابق", onPrevious) { GlowIcon(IconKind.Prev, Color.White, 30.dp) }
            GlowIconTap(
                description = if (state.isPlaying) "إيقاف مؤقت" else "تشغيل",
                onClick = onTogglePlay,
                size = 72.dp,
                background = Amber,
            ) {
                GlowIcon(
                    if (state.isPlaying) IconKind.Pause else IconKind.Play,
                    Color(0xFF0D0E12),
                    34.dp,
                )
            }
            GlowIconTap("التالي", onNext) { GlowIcon(IconKind.Next, Color.White, 30.dp) }
            GlowIconTap("تكرار", onRepeat) {
                GlowIcon(
                    if (state.repeat == RepeatMode.One) IconKind.RepeatOne else IconKind.Repeat,
                    if (state.repeat == RepeatMode.Off) dim else Amber,
                )
            }
        }
    }
}
