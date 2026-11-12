package com.glow.music.ui.library

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class LibraryTab(val label: String) {
    TRACKS("الأغاني"),
    ALBUMS("الألبومات"),
    ARTISTS("الفنانون"),
    GENRES("الأنواع"),
    SPOTIFY("Spotify"),
    FAVORITES("المفضلة"),
    PLAYLISTS("قوائم التشغيل"),
}

data class TrackItem(
    val id: Long,
    val title: String,
    val artist: String?,
    val durationMs: Long,
    val coverUri: String?,
)

/** يُستخدم للألبومات والفنانين والأنواع وقوائم التشغيل. */
data class CollectionItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val coverUri: String?,
)

/** (رابط الغلاف، مفتاح ثابت للّون، Modifier). استبدله بمحمّل الصور الحقيقي في مشروعك. */
typealias CoverSlot = @Composable (String?, String, Modifier) -> Unit
