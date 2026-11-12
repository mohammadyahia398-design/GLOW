package com.glow.app.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glow.app.settings.SettingsStore
import com.glow.app.settings.ThemeMode
import com.glow.app.ui.common.GlowTopBar
import com.glow.app.ui.theme.LocalGlowSkin
import kotlinx.coroutines.delay

private enum class Dlg { THEME, SPEED, SLEEP, SHORT }

/** شاشة الإعدادات على طراز Samsung: عنوان كبير وبطاقات مدوّرة مجمّعة بأقسام. */
@Composable
fun SettingsScreen(
    store: SettingsStore,
    onBack: () -> Unit,
    onRescan: () -> Unit,
    sleepEndsAt: Long?,
    onSleepTimer: (Int) -> Unit,
    audioSessionId: () -> Int,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val skin = LocalGlowSkin.current
    var dlg by remember { mutableStateOf<Dlg?>(null) }

    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(sleepEndsAt) {
        while (sleepEndsAt != null) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }
    val remainingMin = sleepEndsAt?.let { (((it - now).coerceAtLeast(0)) + 59_999) / 60_000 }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = 32.dp),
    ) {
        GlowTopBar(title = null, onBack = onBack)
        Text(
            "الإعدادات",
            color = skin.text,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
        )

        Section("المظهر") {
            Item("وضع السمة", themeLabel(store.themeMode)) { dlg = Dlg.THEME }
        }

        Section("التشغيل") {
            Item("سرعة التشغيل", speedLabel(store.playbackSpeed)) { dlg = Dlg.SPEED }
            Divider()
            Item(
                "مؤقّت النوم",
                if (remainingMin == null) "متوقف" else "متبقٍ $remainingMin دقيقة",
            ) { dlg = Dlg.SLEEP }
            Divider()
            Item("المعادل الصوتي", "فتح معادل النظام") {
                val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                    putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId())
                    putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                    putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                }
                try {
                    context.startActivity(intent)
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(context, "لا يوجد معادل على هذا الجهاز", Toast.LENGTH_SHORT).show()
                }
            }
        }

        Section("المكتبة") {
            Item(
                "تخطي الأغاني القصيرة",
                if (store.skipShortSongs) "أقصر من ${store.skipShortSeconds} ثانية" else "متوقف",
                onClick = { store.updateSkipShortSongs(!store.skipShortSongs) },
                trailing = {
                    Switch(
                        checked = store.skipShortSongs,
                        onCheckedChange = null,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = skin.onAccent,
                            checkedTrackColor = skin.accent,
                        ),
                    )
                },
            )
            if (store.skipShortSongs) {
                Divider()
                Item("الحد الأدنى للمدة", "${store.skipShortSeconds} ثانية") { dlg = Dlg.SHORT }
            }
            Divider()
            Item("إعادة فحص المكتبة", "تحديث قائمة الأغاني", onClick = onRescan)
        }

        Section("حول") {
            val version = remember {
                runCatching {
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName
                }.getOrNull() ?: "-"
            }
            Item("Glow", "الإصدار $version", onClick = null)
        }
    }

    when (dlg) {
        Dlg.THEME -> ChoiceDialog(
            "وضع السمة",
            listOf(
                ThemeMode.AUTO to "تلقائي (حسب النظام)",
                ThemeMode.LIGHT to "فاتح",
                ThemeMode.DARK to "داكن",
            ),
            store.themeMode,
            { store.updateThemeMode(it) },
        ) { dlg = null }

        Dlg.SPEED -> ChoiceDialog(
            "سرعة التشغيل",
            listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f).map { it to speedLabel(it) },
            store.playbackSpeed,
            { store.updatePlaybackSpeed(it) },
        ) { dlg = null }

        Dlg.SLEEP -> ChoiceDialog(
            "مؤقّت النوم",
            listOf(
                0 to "إيقاف", 15 to "15 دقيقة", 30 to "30 دقيقة",
                45 to "45 دقيقة", 60 to "ساعة", 90 to "ساعة ونصف",
            ),
            if (sleepEndsAt == null) 0 else -1,
            { onSleepTimer(it) },
        ) { dlg = null }

        Dlg.SHORT -> ChoiceDialog(
            "الحد الأدنى للمدة",
            listOf(15, 30, 45, 60).map { it to "$it ثانية" },
            store.skipShortSeconds,
            { store.updateSkipShortSeconds(it) },
        ) { dlg = null }

        null -> Unit
    }
}

private fun themeLabel(m: ThemeMode) = when (m) {
    ThemeMode.AUTO -> "تلقائي (حسب النظام)"
    ThemeMode.LIGHT -> "فاتح"
    ThemeMode.DARK -> "داكن"
}

private fun speedLabel(s: Float) = if (s == 1f) "عادية (×1.0)" else "×$s"

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    val skin = LocalGlowSkin.current
    Text(
        title,
        color = skin.accentText,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 28.dp, end = 28.dp, top = 12.dp, bottom = 8.dp),
    )
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(skin.panel),
    ) { content() }
}

@Composable
private fun Item(
    title: String,
    subtitle: String?,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)?,
) {
    val skin = LocalGlowSkin.current
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = skin.text, fontSize = 17.sp)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, color = skin.textSecondary, fontSize = 13.sp)
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}

@Composable
private fun Divider() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(0.5.dp)
            .background(LocalGlowSkin.current.divider),
    )
}

@Composable
private fun <T> ChoiceDialog(
    title: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    val skin = LocalGlowSkin.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (value, label) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(value); onDismiss() }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = value == selected,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = skin.accentText),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } },
    )
}
