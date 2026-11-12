package com.glow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.glow.app.ui.navigation.GlowApp
import com.glow.app.ui.theme.GlowTheme

/** نقطة الدخول: تعرض التطبيق الحقيقي (المكتبة والألبوم والمشغّل). */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GlowTheme {
                GlowApp(Modifier.fillMaxSize())
            }
        }
    }
}
