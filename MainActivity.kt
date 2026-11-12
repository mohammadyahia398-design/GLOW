package com.glow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import com.glow.app.settings.SettingsStore
import com.glow.app.settings.ThemeMode
import com.glow.app.ui.navigation.GlowApp
import com.glow.app.ui.theme.GlowTheme

/** نقطة الدخول: تعرض التطبيق الحقيقي (المكتبة والألبوم والمشغّل). */
class MainActivity : ComponentActivity() {

    private val settings by lazy { SettingsStore(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val dark = when (settings.themeMode) {
                ThemeMode.AUTO -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            GlowTheme(darkTheme = dark) {
                GlowApp(settings, Modifier.fillMaxSize())
            }
        }
    }
}
