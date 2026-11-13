package com.glow.app.media

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * يقرأ الموسيقى من MediaStore.
 * يتطلب READ_MEDIA_AUDIO (API 33+) أو READ_EXTERNAL_STORAGE (أقدم)،
 * وعدم وجود الصلاحية يُرجع قائمة فارغة بدل أن ينهار.
 */
class MediaStoreRepository(private val context: Context) {

    suspend fun loadTracks(): List<Track> = withContext(Dispatchers.IO) {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val hasGenre = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        val projection = buildList {
            add(MediaStore.Audio.Media._ID)
            add(MediaStore.Audio.Media.TITLE)
            add(MediaStore.Audio.Media.ARTIST)
            add(MediaStore.Audio.Media.ALBUM)
            add(MediaStore.Audio.Media.ALBUM_ID)
            add(MediaStore.Audio.Media.DURATION)
            add(MediaStore.Audio.Media.DATE_ADDED)
            if (hasGenre) add(MediaStore.Audio.Media.GENRE)
        }.toTypedArray()
        val selection =
            "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        val result = ArrayList<Track>()
        try {
            context.contentResolver.query(collection, projection, selection, null, sortOrder)
                ?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                    val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                    val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                    val genreCol = if (hasGenre) {
                        cursor.getColumnIndex(MediaStore.Audio.Media.GENRE)
                    } else {
                        -1
                    }

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idCol)
                        result += Track(
                            id = id,
                            title = cursor.getString(titleCol) ?: UNTITLED,
                            artist = cleanName(cursor.getString(artistCol), UNKNOWN_ARTIST),
                            album = cleanName(cursor.getString(albumCol), UNKNOWN_ALBUM),
                            albumId = cursor.getLong(albumIdCol),
                            durationMs = cursor.getLong(durationCol),
                            uri = ContentUris.withAppendedId(collection, id),
                            dateAddedSec = cursor.getLong(dateCol),
                            genre = if (genreCol >= 0) cursor.getString(genreCol).orEmpty() else "",
                        )
                    }
                }
        } catch (_: SecurityException) {
            // لا صلاحية قراءة: نُرجع ما جمعناه (فارغ).
        }
        result
    }

    private fun cleanName(raw: String?, fallback: String): String =
        if (raw.isNullOrBlank() || raw == MediaStore.UNKNOWN_STRING) fallback else raw

    private companion object {
        const val UNTITLED = "بدون عنوان"
        const val UNKNOWN_ARTIST = "فنان غير معروف"
        const val UNKNOWN_ALBUM = "ألبوم غير معروف"
    }
}
