package com.glow.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glow.app.media.Track
import com.glow.app.ui.theme.LocalGlowSkin

fun formatTime(ms: Long): String {
    val s = ms / 1000
    return "%d:%02d".format(s / 60, s % 60)
}

/**
 * صف موحّد: مقطع أو ألبوم أو فنان. يظهر [leading] (مثل رقم المقطع) بدل الغلاف إن وُجد.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaRow(
    art: Track?,
    title: String,
    subtitle: String?,
    trailing: String?,
    highlight: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val skin = LocalGlowSkin.current
    Column(modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (leading != null) {
                Box(Modifier.width(32.dp), contentAlignment = Alignment.Center) { leading() }
            } else {
                AlbumArt(art, Modifier.size(52.dp), radius = 14.dp)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (highlight) skin.accentText else skin.text,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = skin.textSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (trailing != null) {
                Text(text = trailing, color = skin.textSecondary, fontSize = 13.sp)
            }
        }
        Box(
            Modifier
                .padding(start = 82.dp, end = 20.dp)
                .fillMaxWidth()
                .height(0.5.dp)
                .background(skin.divider),
        )
    }
}
