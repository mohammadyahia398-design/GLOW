package com.glow.music.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glow.music.settings.SettingsStore
import com.glow.music.settings.ThemeMode

private enum class Dlg { THEME, SPEED, SLEEP, SHORT }

@Composable
fun SettingsScreen(
    store: SettingsStore,
    onBack: () -> Unit,
    onRescan: () -> Unit,
    sleepRemainingMs: Long?,
    onSleepTimer: (Int) -> Unit,
    audioSessionId: Int = 0,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme
    var dlg by remember { mutableStateOf<Dlg?>(null) }

    BackHandler(onBack = onBack)

    Column(
        modifier
            .fillMaxSize()
            .background(cs.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        IconButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp, top = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = cs.onBackground)
        }
        Text(
            "الإعدادات",
            color = cs.onBackground,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 16.dp)
        )

        Section("المظهر") {
            Row2(
                "وضع السمة",
                themeLabel(store.themeMode),
                onClick = { dlg = Dlg.THEME }
            )
        }

        Section("التشغيل") {
            Row2(
                "سرعة التشغيل",
                speedLabel(store.playbackSpeed),
                onClick = { dlg = Dlg.SPEED }
            )
            Divider2()
            Row2(
                "مؤقّت النوم",
                if (sleepRemainingMs == null) "متوقف"
                else "متبقٍ ${((sleepRemainingMs + 59_999) / 60_000)} دقيقة",
                onClick = { dlg = Dlg.SLEEP }
            )
            Divider2()
            Row2(
                "المعادل الصوتي",
                "فتح معادل النظام",
                onClick = {
                    val i = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                        putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
                        putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                        putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                    }
                    try {
                        context.startActivity(i)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, "لا يوجد معادل على هذا الجهاز", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        Section("المكتبة") {
            Row2(
                "تخطي الأغاني القصيرة",
                if (store.skipShortSongs) "أقصر من ${store.skipShortSeconds} ثانية" else "متوقف",
                onClick = { store.updateSkipShortSongs(!store.skipShortSongs) },
                trailing = {
                    Switch(checked = store.skipShortSongs, onCheckedChange = null)
                }
            )
            if (store.skipShortSongs) {
                Divider2()
                Row2(
                    "الحد الأدنى للمدة",
                    "${store.skipShortSeconds} ثانية",
                    onClick = { dlg = Dlg.SHORT }
                )
            }
            Divider2()
            Row2(
                "إعادة فحص المكتبة",
                "تحديث قائمة الأغاني",
                onClick = onRescan
            )
        }

        Section("حول") {
            val ver = remember {
                runCatching {
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName
                }.getOrNull() ?: "-"
            }
            Row2("Glow", "الإصدار $ver", onClick = null)
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
            listOf(0 to "إيقاف", 15 to "15 دقيقة", 30 to "30 دقيقة",
                45 to "45 دقيقة", 60 to "ساعة", 90 to "ساعة ونصف"),
            if (sleepRemainingMs == null) 0 else -1,
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

private fun speedLabel(s: Float) =
    if (s == 1f) "عادية (×1.0)" else "×$s"

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Text(
        title,
        color = cs.primary,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 28.dp, end = 28.dp, top = 12.dp, bottom = 8.dp)
    )
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = cs.onSurface.copy(alpha = 0.06f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column { content() }
    }
}

@Composable
private fun Row2(
    title: String,
    subtitle: String?,
    onClick: (() -> Unit)?,
    trailing: (@Composable () -> Unit)? = null,
) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = cs.onSurface, fontSize = 17.sp)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, color = cs.onSurface.copy(alpha = 0.6f), fontSize = 13.sp)
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}

@Composable
private fun Divider2() {
    Spacer(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = value == selected, onClick = null)
                        Spacer(Modifier.width(12.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
