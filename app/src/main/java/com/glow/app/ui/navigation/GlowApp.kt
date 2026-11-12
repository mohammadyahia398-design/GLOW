package com.glow.app.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.glow.app.media.MediaStoreRepository
import com.glow.app.media.PlaybackConnection
import com.glow.app.media.PlayerState
import com.glow.app.media.Track
import com.glow.app.ui.album.CollectionScreen
import com.glow.app.ui.glass.core.GlassConfig
import com.glow.app.ui.glass.core.ProvideGlassConfig
import com.glow.app.ui.glass.core.rememberGlassTier
import com.glow.app.ui.glass.core.rememberSystemReduceMotion
import com.glow.app.ui.glass.surfaces.glassBackdropSource
import com.glow.app.ui.glass.surfaces.rememberGlassBackdrop
import com.glow.app.ui.library.MediaGroup
import com.glow.app.ui.library.LibraryScreen
import com.glow.app.ui.nowplaying.ArtMiniPlayer
import com.glow.app.ui.nowplaying.NowPlayingScreen
import com.glow.app.ui.theme.LocalGlowSkin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.random.Random

private val idleState = MutableStateFlow(PlayerState())

/**
 * جذر التطبيق: صلاحيات + قراءة المكتبة + ربط الخدمة + التنقل (مكتبة ← ألبوم ← مشغّل كامل).
 * التنقل بحالة داخلية، وزر الرجوع يغلق المشغّل ثم الألبوم.
 */
@Composable
fun GlowApp(modifier: Modifier = Modifier) {
    val skin = LocalGlowSkin.current
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
    val permissions = remember(audioPermission) {
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
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result -> hasAudioAccess = result[audioPermission] == true }

    var tracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }
    LaunchedEffect(hasAudioAccess) {
        if (hasAudioAccess) {
            tracks = repository.loadTracks()
            loaded = true
        }
    }

    var collection by remember { mutableStateOf<MediaGroup?>(null) }
    var showPlayer by remember { mutableStateOf(false) }
    BackHandler(enabled = showPlayer) { showPlayer = false }
    BackHandler(enabled = !showPlayer && collection != null) { collection = null }

    val tier = rememberGlassTier()
    val reduceMotion = rememberSystemReduceMotion()
    val glassConfig = remember(tier, reduceMotion, skin) {
        GlassConfig(
            tier = tier,
            tint = skin.glassTint,
            fallbackBase = skin.glassBase,
            reduceMotion = reduceMotion,
        )
    }
    val backdrop = rememberGlassBackdrop()
    val current = playerState.current

    ProvideGlassConfig(glassConfig) {
        Box(modifier.fillMaxSize()) {
            Box(
                Modifier
                    .matchParentSize()
                    .glassBackdropSource(backdrop)
                    .background(Brush.verticalGradient(listOf(skin.bgTop, skin.bgBottom))),
            ) {
                val open = collection
                if (open == null) {
                    LibraryScreen(
                        tracks = tracks,
                        loaded = loaded,
                        hasAudioAccess = hasAudioAccess,
                        currentId = current?.id,
                        onRequestPermission = { launcher.launch(permissions) },
                        onPlay = { list, i -> connection.play(list, i) },
                        onOpen = { collection = it },
                    )
                } else {
                    CollectionScreen(
                        collection = open,
                        currentId = current?.id,
                        onBack = { collection = null },
                        onPlay = { i -> connection.play(open.tracks, i) },
                        onShuffle = {
                            controller?.setShuffle(true)
                            connection.play(open.tracks, Random.nextInt(open.tracks.size))
                        },
                    )
                }
            }

            if (current != null && !showPlayer) {
                ArtMiniPlayer(
                    track = current,
                    isPlaying = playerState.isPlaying,
                    progress = { playerState.progress },
                    onTogglePlay = { controller?.togglePlay() },
                    onNext = { controller?.next() },
                    onPrevious = { controller?.previous() },
                    onClick = { showPlayer = true },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                )
            }

            AnimatedVisibility(
                visible = showPlayer && current != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                NowPlayingScreen(
                    state = playerState,
                    onClose = { showPlayer = false },
                    onSeek = { controller?.seekTo(it) },
                    onTogglePlay = { controller?.togglePlay() },
                    onNext = { controller?.next() },
                    onPrevious = { controller?.previous() },
                    onShuffle = { controller?.setShuffle(!playerState.shuffle) },
                    onRepeat = { controller?.cycleRepeat() },
                )
            }
        }
    }
}
