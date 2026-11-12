package com.glow.music.settings

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class ThemeMode { AUTO, LIGHT, DARK }

class SettingsStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("glow_settings", Context.MODE_PRIVATE)

    var themeMode by mutableStateOf(
        ThemeMode.values().getOrElse(prefs.getInt("theme_mode", 0)) { ThemeMode.AUTO }
    )
        private set

    var playbackSpeed by mutableStateOf(prefs.getFloat("speed", 1f))
        private set

    var skipShortSongs by mutableStateOf(prefs.getBoolean("skip_short", false))
        private set

    var skipShortSeconds by mutableStateOf(prefs.getInt("skip_short_sec", 30))
        private set

    fun updateThemeMode(v: ThemeMode) {
        themeMode = v
        prefs.edit().putInt("theme_mode", v.ordinal).apply()
    }

    fun updatePlaybackSpeed(v: Float) {
        playbackSpeed = v
        prefs.edit().putFloat("speed", v).apply()
    }

    fun updateSkipShortSongs(v: Boolean) {
        skipShortSongs = v
        prefs.edit().putBoolean("skip_short", v).apply()
    }

    fun updateSkipShortSeconds(v: Int) {
        skipShortSeconds = v
        prefs.edit().putInt("skip_short_sec", v).apply()
    }
}
