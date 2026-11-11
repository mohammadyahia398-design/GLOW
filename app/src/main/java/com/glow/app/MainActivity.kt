package com.glow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.glow.app.ui.glass.preview.GlassPreviewGallery

/**
 * نقطة الدخول المؤقتة: تعرض معرض الزجاج حتى تجهز شاشات التطبيق الحقيقية.
 * عند بناء الشاشة الرئيسية يُستبدل [GlassPreviewGallery] بها.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GlassPreviewGallery(modifier = Modifier.fillMaxSize())
        }
    }
}
