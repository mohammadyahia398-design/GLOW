package com.glow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.glow.app.settings.SettingsStore
import com.glow.app.settings.ThemeMode
import com.glow.app.ui.navigation.GlowApp
import com.glow.app.ui.splash.GlowSplash
import com.glow.app.ui.theme.GlowTheme

/** نقطة الدخول: تعرض التطبيق الحقيقي (المكتبة والألبوم والمشغّل) تحت شاشة البداية المتحركة. */
class MainActivity : ComponentActivity() {

    private val settings by lazy { SettingsStore(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        // شاشة النظام تطابق بداية GlowSplash، فنُزيلها فور الرسم الأول بلا أي انتقال ظاهر.
        installSplashScreen().setOnExitAnimationListener { it.remove() }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val dark = when (settings.themeMode) {
                ThemeMode.AUTO -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            GlowTheme(darkTheme = dark) {
                var showSplash by rememberSaveable { mutableStateOf(true) }
                Box(Modifier.fillMaxSize()) {
                    GlowApp(settings, Modifier.fillMaxSize())
                    if (showSplash) GlowSplash(onFinished = { showSplash = false })
                }
            }
        }
    }
}
