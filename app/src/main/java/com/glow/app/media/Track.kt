package com.glow.app.media

import android.net.Uri

/** مقطع صوتي واحد من مكتبة الجهاز. */
data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val uri: Uri,
    val genre: String = "",
    val dateAddedSec: Long = 0,
)
