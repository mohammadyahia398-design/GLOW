package com.glow.music.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** غلاف افتراضي متدرّج بلون ثابت لكل عنصر، حتى لا تتشابه الأغلفة. */
val DefaultCover: CoverSlot = { _, key, modifier ->
    val h = (key.hashCode() and 0x7fffffff) % 360
    val c1 = Color.hsv(h.toFloat(), 0.55f, 0.85f)
    val c2 = Color.hsv(((h + 40) % 360).toFloat(), 0.7f, 0.55f)
    Box(modifier.background(Brush.linearGradient(listOf(c1, c2))), Alignment.Center) {
        Text("♪", color = Color.White.copy(alpha = 0.9f), fontSize = 22.sp)
    }
}

@Composable
fun LibraryScreen(
    tracks: List<TrackItem>,
    albums: List<CollectionItem>,
    artists: List<CollectionItem>,
    genres: List<CollectionItem>,
    playlists: List<CollectionItem>,
    favorites: List<TrackItem>,
    recentCount: Int,
    mostPlayedCount: Int,
    selectedTab: LibraryTab,
    onTabChange: (LibraryTab) -> Unit,
    onTrackClick: (TrackItem) -> Unit,
    onCollectionClick: (LibraryTab, CollectionItem) -> Unit,
    onRecentClick: () -> Unit,
    onMostPlayedClick: () -> Unit,
    onSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    cover: CoverSlot = DefaultCover,
    bottomPadding: androidx.compose.ui.unit.Dp = 96.dp,
    rtl: Boolean = true,
) {
    val cs = MaterialTheme.colorScheme
    val dir = if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(LocalLayoutDirection provides dir) {
        Column(modifier.fillMaxSize().background(cs.background).statusBarsPadding()) {
            Header(onSearch, onOpenSettings)
            TabsRow(selectedTab, onTabChange)
            Surface(
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = lerp(cs.background, cs.onBackground, 0.09f),
                modifier = Modifier.fillMaxSize()
            ) {
                when (selectedTab) {
                    LibraryTab.TRACKS -> TrackList(tracks, cover, bottomPadding, onTrackClick,
                        "لا توجد أغانٍ")
                    LibraryTab.FAVORITES -> TrackList(favorites, cover, bottomPadding, onTrackClick,
                        "لا توجد أغانٍ مفضلة بعد")
                    LibraryTab.ALBUMS -> CoverGrid(albums, cover, bottomPadding, false,
                        "لا توجد ألبومات") { onCollectionClick(LibraryTab.ALBUMS, it) }
                    LibraryTab.ARTISTS -> CoverGrid(artists, cover, bottomPadding, true,
                        "لا يوجد فنانون") { onCollectionClick(LibraryTab.ARTISTS, it) }
                    LibraryTab.GENRES -> CollectionList(genres, cover, bottomPadding,
                        "لا توجد أنواع") { onCollectionClick(LibraryTab.GENRES, it) }
                    LibraryTab.PLAYLISTS -> PlaylistsTab(
                        playlists, recentCount, mostPlayedCount, cover, bottomPadding,
                        onRecentClick, onMostPlayedClick
                    ) { onCollectionClick(LibraryTab.PLAYLISTS, it) }
                    LibraryTab.SPOTIFY -> Empty("تكامل Spotify غير متاح بعد")
                }
            }
        }
    }
}

@Composable
private fun Header(onSearch: () -> Unit, onSettings: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    var menu by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().padding(start = 24.dp, end = 12.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Glow", color = cs.onBackground, fontSize = 34.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f))
        IconButton(onClick = onSearch) {
            Icon(Icons.Default.Search, "بحث", tint = cs.onBackground)
        }
        Box {
            IconButton(onClick = { menu = true }) {
                Icon(Icons.Default.MoreVert, "المزيد", tint = cs.onBackground)
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(
                    text = { Text("الإعدادات") },
                    onClick = { menu = false; onSettings() }
                )
            }
        }
    }
}

@Composable
private fun TabsRow(selected: LibraryTab, onChange: (LibraryTab) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val tabs = LibraryTab.values()
    val state = rememberLazyListState()
    LaunchedEffect(selected) { state.animateScrollToItem(selected.ordinal) }
    LazyRow(
        state = state,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(22.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().height(72.dp)
    ) {
        items(tabs.toList()) { t ->
            val sel = t == selected
            Text(
                t.label,
                color = if (sel) cs.onBackground else cs.onBackground.copy(alpha = 0.55f),
                fontSize = if (sel) 30.sp else 17.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                modifier = Modifier.clickable { onChange(t) }
            )
        }
    }
}

@Composable
private fun Empty(text: String) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center)
    }
}

private fun fmt(ms: Long): String {
    val s = ms / 1000
    return "%d:%02d".format(s / 60, s % 60)
}

@Composable
private fun RowDivider() {
    Box(
        Modifier.fillMaxWidth().padding(start = 84.dp, end = 20.dp).height(1.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
    )
}

@Composable
private fun ListRow(
    coverUri: String?, key: String, title: String, subtitle: String?, trailing: String?,
    cover: CoverSlot, onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Column {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 5.dp).height(52.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            cover(coverUri, key, Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = cs.onSurface, fontSize = 18.sp, maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
                if (!subtitle.isNullOrBlank()) {
                    Text(subtitle, color = cs.onSurface.copy(alpha = 0.6f), fontSize = 13.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (trailing != null) {
                Spacer(Modifier.width(12.dp))
                Text(trailing, color = cs.onSurface.copy(alpha = 0.6f), fontSize = 14.sp)
            }
        }
        RowDivider()
    }
}

@Composable
private fun TrackList(
    list: List<TrackItem>, cover: CoverSlot, bottom: androidx.compose.ui.unit.Dp,
    onClick: (TrackItem) -> Unit, emptyText: String,
) {
    if (list.isEmpty()) return Empty(emptyText)
    LazyColumn(contentPadding = PaddingValues(top = 12.dp, bottom = bottom)) {
        items(list, key = { it.id }) { t ->
            val artist = t.artist?.takeIf { it.isNotBlank() && it != "<unknown>" }
            ListRow(t.coverUri, "t${t.id}", t.title, artist, fmt(t.durationMs), cover) { onClick(t) }
        }
    }
}

@Composable
private fun CollectionList(
    list: List<CollectionItem>, cover: CoverSlot, bottom: androidx.compose.ui.unit.Dp,
    emptyText: String, onClick: (CollectionItem) -> Unit,
) {
    if (list.isEmpty()) return Empty(emptyText)
    LazyColumn(contentPadding = PaddingValues(top = 12.dp, bottom = bottom)) {
        items(list, key = { it.id }) { c ->
            ListRow(c.coverUri, c.id, c.title, null, c.subtitle, cover) { onClick(c) }
        }
    }
}

@Composable
private fun CoverGrid(
    list: List<CollectionItem>, cover: CoverSlot, bottom: androidx.compose.ui.unit.Dp,
    nameOnCover: Boolean, emptyText: String, onClick: (CollectionItem) -> Unit,
) {
    if (list.isEmpty()) return Empty(emptyText)
    val cs = MaterialTheme.colorScheme
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = bottom),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        gridItems(list, key = { it.id }) { c ->
            Column(Modifier.clickable { onClick(c) }) {
                Box(Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(24.dp))) {
                    cover(c.coverUri, c.id, Modifier.fillMaxSize())
                    if (nameOnCover) {
                        Box(
                            Modifier.fillMaxSize().background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                                )
                            ),
                            Alignment.BottomStart
                        ) {
                            Text(c.title, color = Color.White, fontSize = 16.sp, maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(12.dp))
                        }
                    }
                }
                if (!nameOnCover) {
                    Spacer(Modifier.height(8.dp))
                    Text(c.title, color = cs.onSurface, fontSize = 16.sp, maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    Text(c.subtitle, color = cs.onSurface.copy(alpha = 0.6f), fontSize = 13.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun PlaylistsTab(
    playlists: List<CollectionItem>, recentCount: Int, mostCount: Int,
    cover: CoverSlot, bottom: androidx.compose.ui.unit.Dp,
    onRecent: () -> Unit, onMost: () -> Unit, onClick: (CollectionItem) -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    LazyColumn(contentPadding = PaddingValues(top = 12.dp, bottom = bottom)) {
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                item { SpecialCard("المشغّلة مؤخراً", recentCount, "recent", cover, onRecent) }
                item { SpecialCard("الأكثر تشغيلاً", mostCount, "most", cover, onMost) }
            }
        }
        if (playlists.isEmpty()) {
            item {
                Text("لا توجد قوائم تشغيل بعد", color = cs.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth().padding(24.dp), textAlign = TextAlign.Center)
            }
        } else {
            items(playlists, key = { it.id }) { p ->
                ListRow(p.coverUri, p.id, p.title, null, p.subtitle, cover) { onClick(p) }
            }
        }
    }
}

@Composable
private fun SpecialCard(
    title: String, count: Int, key: String, cover: CoverSlot, onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.width(150.dp).clickable(onClick = onClick)) {
        cover(null, key, Modifier.size(150.dp).clip(RoundedCornerShape(24.dp)))
        Spacer(Modifier.height(8.dp))
        Text(title, color = cs.onSurface, fontSize = 16.sp, maxLines = 1,
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Text("$count أغنية", color = cs.onSurface.copy(alpha = 0.6f), fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
}
