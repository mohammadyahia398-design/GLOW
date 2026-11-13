package com.glow.app.ui.album

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glow.app.media.displayTitle
import com.glow.app.ui.common.AlbumArt
import com.glow.app.ui.common.GlowIcon
import com.glow.app.ui.common.GlowIconTap
import com.glow.app.ui.common.GlowTopBar
import com.glow.app.ui.common.IconKind
import com.glow.app.ui.common.MediaRow
import com.glow.app.ui.common.formatTime
import com.glow.app.ui.library.MediaGroup
import com.glow.app.ui.theme.LocalGlowSkin

/** شاشة ألبوم أو فنان: غلاف كبير، عنوان، زرّا خلط وتشغيل، ثم المقاطع مرقّمة. */
@Composable
fun CollectionScreen(
    collection: MediaGroup,
    currentId: Long?,
    onBack: () -> Unit,
    onPlay: (Int) -> Unit,
    onShuffle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val skin = LocalGlowSkin.current
    val tracks = collection.tracks
    val info = listOfNotNull(
        collection.subtitle.takeIf { it.isNotBlank() },
        "${tracks.size} أغنية",
    ).joinToString(" • ")

    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 150.dp)) {
        item {
            Column(
                Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                GlowTopBar(title = null, onBack = onBack)
                AlbumArt(collection.cover, Modifier.size(180.dp), radius = 28.dp, px = 512)
                Text(
                    text = collection.title,
                    color = skin.text,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp),
                )
                Text(
                    text = info,
                    color = skin.textSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Row(
                    Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    GlowIconTap("تشغيل عشوائي", onShuffle, size = 56.dp, background = skin.chip) {
                        GlowIcon(IconKind.Shuffle, skin.text)
                    }
                    GlowIconTap("تشغيل", { onPlay(0) }, size = 56.dp, background = skin.accent) {
                        GlowIcon(IconKind.Play, skin.onAccent, 28.dp)
                    }
                }
            }
        }
        itemsIndexed(tracks, key = { _, t -> t.id }) { i, t ->
            MediaRow(
                art = null,
                title = t.displayTitle,
                subtitle = null,
                trailing = formatTime(t.durationMs),
                highlight = t.id == currentId,
                onClick = { onPlay(i) },
                leading = {
                    Text(
                        text = "${i + 1}",
                        color = if (t.id == currentId) skin.accentText else skin.textSecondary,
                        fontSize = 15.sp,
                    )
                },
            )
        }
    }
}
