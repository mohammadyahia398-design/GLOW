package com.glow.app.ui.library

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.glow.app.media.MediaStoreRepository
import com.glow.app.media.PlaybackConnection
import com.glow.app.media.PlayerState
import com.glow.app.media.Track
import com.glow.app.ui.glass.components.GlassMiniPlayerPill
import com.glow.app.ui.glass.core.GlassConfig
import com.glow.app.ui.glass.core.ProvideGlassConfig
import com.glow.app.ui.glass.core.rememberGlassTier
import com.glow.app.ui.glass.core.rememberSystemReduceMotion
import com.glow.app.ui.glass.surfaces.glassBackdropSource
import com.glow.app.ui.glass.surfaces.rememberGlassBackdrop
import kotlinx.coroutines.flow.MutableStateFlow

private val Amber = Color(0xFFFF9F43)
private val idleState = MutableStateFlow(PlayerState())

/**
 * شاشة المكتبة الكاملة: صلاحيات + قراءة MediaStore + ربط الخدمة + الكبسولة الزجاجية.
 * هذه هي الدالة التي تُستدعى من MainActivity.
 */
@Composable
fun LibraryRoute(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val repository = remember { MediaStoreRepository(appContext) }
    val connection = remember { PlaybackConnection(appContext) }

    DisposableEffect(connection) {
        connection.bind()
        onDispose { connection.unbind() }
    }

    val controller by connection.controller.collectAsState()
    val stateFlow = remember(controller) { controller?.state ?: idleState }
    val playerState by stateFlow.collectAsState()

    val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val permissionsToRequest = remember(audioPermission) {
        buildList {
            add(audioPermission)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
    }

    var hasAudioAccess by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, audioPermission) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        hasAudioAccess = result[audioPermission] == true
    }

    var tracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }
    LaunchedEffect(hasAudioAccess) {
        if (hasAudioAccess) {
            tracks = repository.loadTracks()
            loaded = true
        }
    }

    val tier = rememberGlassTier()
    val reduceMotion = rememberSystemReduceMotion()
    val glassConfig = remember(tier, reduceMotion) {
        GlassConfig(tier = tier, reduceMotion = reduceMotion)
    }

    ProvideGlassConfig(glassConfig) {
        LibraryScreen(
            tracks = tracks,
            loaded = loaded,
            hasAudioAccess = hasAudioAccess,
            playerState = playerState,
            onRequestPermission = { permissionLauncher.launch(permissionsToRequest) },
            onTrackClick = { index -> connection.play(tracks, index) },
            onTogglePlay = { controller?.togglePlay() },
            onNext = { controller?.next() },
            modifier = modifier,
        )
    }
}

@Composable
private fun LibraryScreen(
    tracks: List<Track>,
    loaded: Boolean,
    hasAudioAccess: Boolean,
    playerState: PlayerState,
    onRequestPermission: () -> Unit,
    onTrackClick: (Int) -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backdrop = rememberGlassBackdrop()

    Box(modifier.fillMaxSize()) {
        // المصدر: الخلفية مع القائمة. الكبسولة الزجاجية أخٌ له وفوقه، لا بداخله.
        Box(
            Modifier
                .matchParentSize()
                .glassBackdropSource(backdrop)
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF0D0E12), Color(0xFF1B1422))),
                ),
        ) {
            when {
                !hasAudioAccess -> PermissionPrompt(onRequestPermission)
                loaded && tracks.isEmpty() -> CenteredMessage("لا توجد موسيقى على هذا الجهاز")
                else -> TrackList(
                    tracks = tracks,
                    currentId = playerState.current?.id,
                    onTrackClick = onTrackClick,
                )
            }
        }

        val current = playerState.current
        if (current != null) {
            GlassMiniPlayerPill(
                title = current.title,
                subtitle = current.artist,
                isPlaying = playerState.isPlaying,
                progress = { playerState.progress },
                onTogglePlay = onTogglePlay,
                onClick = {},
                onNext = onNext,
                backdrop = backdrop,
                playLabel = if (playerState.isPlaying) "إيقاف مؤقت" else "تشغيل",
                nextLabel = "التالي",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
    }
}

@Composable
private fun TrackList(
    tracks: List<Track>,
    currentId: Long?,
    onTrackClick: (Int) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 140.dp),
    ) {
        itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
            TrackRow(
                track = track,
                isCurrent = track.id == currentId,
                onClick = { onTrackClick(index) },
            )
        }
    }
}

@Composable
private fun TrackRow(track: Track, isCurrent: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = if (isCurrent) Amber else Color.White,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = track.artist,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = formatDuration(track.durationMs),
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun PermissionPrompt(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "يحتاج Glow إذن قراءة الموسيقى لعرض مكتبتك",
            color = Color.White,
            fontSize = 16.sp,
        )
        Button(
            onClick = onRequestPermission,
            modifier = Modifier.padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = Color.Black),
        ) {
            Text("السماح")
        }
    }
}

@Composable
private fun CenteredMessage(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text, color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp)
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
