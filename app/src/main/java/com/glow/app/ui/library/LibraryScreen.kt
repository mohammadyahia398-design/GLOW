package com.glow.app.ui.library

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glow.app.media.Track
import com.glow.app.ui.common.CoverCell
import com.glow.app.ui.common.GlowIcon
import com.glow.app.ui.common.GlowIconTap
import com.glow.app.ui.common.GlowTopBar
import com.glow.app.ui.common.IconKind
import com.glow.app.ui.common.MediaRow
import com.glow.app.ui.common.formatTime
import com.glow.app.ui.theme.LocalGlowSkin

/** ألبوم أو فنان: مجموعة مقاطع بغلاف واحد. */
data class MediaGroup(
    val title: String,
    val subtitle: String,
    val cover: Track,
    val tracks: List<Track>,
)

private enum class LibraryTab(val label: String) {
    Tracks("الأغاني"), Albums("الألبومات"), Artists("الفنانون")
}

/** شاشة المكتبة على طراز Samsung Music: شريط علوي + تبويبات + لوح قائمة بزوايا مدوّرة. */
@Composable
fun LibraryScreen(
    tracks: List<Track>,
    loaded: Boolean,
    hasAudioAccess: Boolean,
    currentId: Long?,
    onRequestPermission: () -> Unit,
    onPlay: (List<Track>, Int) -> Unit,
    onOpen: (MediaGroup) -> Unit,
    modifier: Modifier = Modifier,
) {
    val skin = LocalGlowSkin.current
    var tab by remember { mutableStateOf(LibraryTab.Tracks) }
    var searching by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    val shown = remember(tracks, query) {
        if (query.isBlank()) tracks else tracks.filter {
            it.title.contains(query, true) || it.artist.contains(query, true) ||
                it.album.contains(query, true)
        }
    }
    val albums = remember(shown) {
        shown.groupBy { it.albumId }.values
            .map { MediaGroup(it[0].album, it[0].artist, it[0], it) }
            .sortedBy { it.title.lowercase() }
    }
    val artists = remember(shown) {
        shown.groupBy { it.artist }
            .map { (name, list) -> MediaGroup(name, "", list[0], list) }
            .sortedBy { it.title.lowercase() }
    }

    Column(modifier.fillMaxSize()) {
        GlowTopBar(
            title = "Glow",
            titleSize = 30.sp,
            titleWeight = FontWeight.Bold,
            actions = {
                GlowIconTap("بحث", {
                    searching = !searching
                    if (!searching) query = ""
                }) { GlowIcon(IconKind.Search, if (searching) skin.accentText else skin.text) }
            },
        )
        if (searching) SearchField(query) { query = it }

        Row(
            Modifier.fillMaxWidth().padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LibraryTab.values().forEach { t ->
                val selected = t == tab
                val size by animateFloatAsState(if (selected) 28f else 16f, label = "tab")
                Text(
                    text = t.label,
                    color = if (selected) skin.text else skin.textSecondary,
                    fontSize = size.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.clickable { tab = t }.padding(8.dp),
                )
            }
        }

        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(skin.panel),
        ) {
            val pad = PaddingValues(top = 8.dp, bottom = 150.dp)
            val gridPad = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 150.dp)
            when {
                !hasAudioAccess -> PermissionPrompt(onRequestPermission)
                loaded && tracks.isEmpty() -> CenteredMessage("لا توجد موسيقى على هذا الجهاز")
                shown.isEmpty() && loaded -> CenteredMessage("لا نتائج")
                tab == LibraryTab.Tracks -> LazyColumn(Modifier.fillMaxSize(), contentPadding = pad) {
                    itemsIndexed(shown, key = { _, t -> t.id }) { i, t ->
                        MediaRow(
                            art = t, title = t.title, subtitle = t.artist,
                            trailing = formatTime(t.durationMs), highlight = t.id == currentId,
                            onClick = { onPlay(shown, i) },
                        )
                    }
                }
                tab == LibraryTab.Albums -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = gridPad,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    gridItems(albums, key = { it.cover.albumId }) { c ->
                        CoverCell(
                            art = c.cover,
                            title = c.title,
                            subtitle = "${c.subtitle} | ${c.tracks.size} أغنية",
                            onClick = { onOpen(c) },
                        )
                    }
                }
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = gridPad,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    gridItems(artists, key = { it.title }) { c ->
                        CoverCell(
                            art = c.cover,
                            title = null,
                            subtitle = "${c.tracks.size} أغنية",
                            overlay = c.title,
                            onClick = { onOpen(c) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(value: String, onChange: (String) -> Unit) {
    val skin = LocalGlowSkin.current
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(skin.chip)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        if (value.isEmpty()) {
            Text("ابحث في مكتبتك", color = skin.textSecondary, fontSize = 15.sp)
        }
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            textStyle = TextStyle(color = skin.text, fontSize = 15.sp),
            cursorBrush = SolidColor(skin.accentText),
            modifier = Modifier.fillMaxWidth().focusRequester(focus),
        )
    }
}

@Composable
private fun PermissionPrompt(onRequestPermission: () -> Unit) {
    val skin = LocalGlowSkin.current
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("يحتاج Glow إذن قراءة الموسيقى لعرض مكتبتك", color = skin.text, fontSize = 16.sp)
        Button(
            onClick = onRequestPermission,
            modifier = Modifier.padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = skin.accent, contentColor = skin.onAccent),
        ) { Text("السماح") }
    }
}

@Composable
private fun CenteredMessage(text: String) {
    val skin = LocalGlowSkin.current
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = skin.textSecondary, fontSize = 16.sp)
    }
}
