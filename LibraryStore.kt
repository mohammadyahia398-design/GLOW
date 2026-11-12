package com.glow.app.settings

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Playlist(val id: String, val name: String, val trackIds: List<Long>)

/** تخزين محلي للمفضلة وقوائم التشغيل وسجل التشغيل (الأخيرة والأكثر تشغيلاً). */
class LibraryStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("glow_library", Context.MODE_PRIVATE)

    var favorites by mutableStateOf(readIds("fav").toSet())
        private set

    /** الأحدث أولاً، بحد أقصى 30. */
    var recents by mutableStateOf(readIds("recent"))
        private set

    var playCounts by mutableStateOf(readCounts())
        private set

    var playlists by mutableStateOf(readPlaylists())
        private set

    fun toggleFavorite(trackId: Long) {
        favorites = if (trackId in favorites) favorites - trackId else favorites + trackId
        prefs.edit().putString("fav", favorites.joinToString(",")).apply()
    }

    fun recordPlay(trackId: Long) {
        recents = (listOf(trackId) + recents.filter { it != trackId }).take(MAX_RECENTS)
        playCounts = playCounts + (trackId to ((playCounts[trackId] ?: 0) + 1))
        prefs.edit()
            .putString("recent", recents.joinToString(","))
            .putString("counts", playCounts.entries.joinToString(",") { "${it.key}:${it.value}" })
            .apply()
    }

    fun createPlaylist(name: String, firstTrackId: Long?) {
        val list = Playlist(UUID.randomUUID().toString(), name.trim(), listOfNotNull(firstTrackId))
        playlists = playlists + list
        savePlaylists()
    }

    fun addToPlaylist(playlistId: String, trackId: Long) {
        playlists = playlists.map {
            if (it.id == playlistId && trackId !in it.trackIds) {
                it.copy(trackIds = it.trackIds + trackId)
            } else {
                it
            }
        }
        savePlaylists()
    }

    private fun readIds(key: String): List<Long> =
        prefs.getString(key, "").orEmpty().split(",").mapNotNull { it.toLongOrNull() }

    private fun readCounts(): Map<Long, Int> =
        prefs.getString("counts", "").orEmpty().split(",").mapNotNull {
            val parts = it.split(":")
            val id = parts.getOrNull(0)?.toLongOrNull()
            val n = parts.getOrNull(1)?.toIntOrNull()
            if (id != null && n != null) id to n else null
        }.toMap()

    private fun readPlaylists(): List<Playlist> = runCatching {
        val arr = JSONArray(prefs.getString("playlists", "[]"))
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val ids = o.getJSONArray("ids")
            Playlist(o.getString("id"), o.getString("name"), (0 until ids.length()).map { ids.getLong(it) })
        }
    }.getOrDefault(emptyList())

    private fun savePlaylists() {
        val arr = JSONArray()
        playlists.forEach { p ->
            arr.put(
                JSONObject()
                    .put("id", p.id)
                    .put("name", p.name)
                    .put("ids", JSONArray(p.trackIds)),
            )
        }
        prefs.edit().putString("playlists", arr.toString()).apply()
    }

    private companion object {
        const val MAX_RECENTS = 30
    }
}
