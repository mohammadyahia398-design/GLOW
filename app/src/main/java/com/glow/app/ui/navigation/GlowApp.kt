package com.glow.app.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.glow.app.settings.LibraryStore
import com.glow.app.settings.SettingsStore
import com.glow.app.ui.settings.SettingsScreen
import com.glow.app.ui.album.CollectionScreen
import com.glow.app.ui.glass.core.GlassConfig
import com.glow.app.ui.glass.core.ProvideGlassConfig
import com.glow.app.ui.glass.core.rememberGlassTier
import com.glow.app.ui.glass.core.rememberSystemReduceMotion
import com.glow.app.ui.glass.surfaces.glassBackdropSource
import com.glow.app.ui.glass.surfaces.rememberGlassBackdrop
import com.glow.app.ui.common.GlowIcon
import com.glow.app.ui.glass.components.LiquidBarItem
import com.glow.app.ui.glass.components.LiquidGlassBar
import com.glow.app.ui.library.LibraryTab
import com.glow.app.ui.library.MediaGroup
import com.glow.app.ui.library.LibraryScreen
import com.glow.app.ui.nowplaying.ArtMiniPlayer
import com.glow.app.ui.nowplaying.ArtMiniPlayerHeight
import com.glow.app.ui.nowplaying.NowPlayingScreen
import com.glow.app.ui.theme.LocalGlowSkin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.random.Random

/** ارتفاع الشريط السفلي مع هوامشه؛ يُرفَع به المشغّل المصغّر فوقه. */
private val BarLift = 84.dp

/** ارتفاع المشغّل المصغّر والفراغ تحته (يطابق [ArtMiniPlayer] وهوامشه أدناه). */
private val MiniHeight = ArtMiniPlayerHeight
private val MiniGap = 12.dp

/** طول التلاشي فوق أعلى عنصر سفلي (مشغّل أو شريط). */
private val ScrimFade = 36.dp

private val idleState = MutableStateFlow(PlayerState())
private val idleSleep = MutableStateFlow<Long?>(null)

/**
 * جذر التطبيق: صلاحيات + قراءة المكتبة + ربط الخدمة + التنقل (مكتبة ← ألبوم ← مشغّل كامل).
 * التنقل بحالة داخلية، وزر الرجوع يغلق المشغّل ثم الألبوم.
 */
@Composable
fun GlowApp(settings: SettingsStore, modifier: Modifier = Modifier) {
    val skin = LocalGlowSkin.current
    val context = LocalContext.current
    val appContext = context.applicationContext
    val repository = remember { MediaStoreRepository(appContext) }
    val connection = remember { PlaybackConnection(appContext) }
    val library = remember { LibraryStore(appContext) }

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
    var rescan by remember { mutableIntStateOf(0) }
    LaunchedEffect(hasAudioAccess, rescan) {
        if (hasAudioAccess) {
            tracks = repository.loadTracks()
            loaded = true
        }
    }
    val visibleTracks = remember(tracks, settings.skipShortSongs, settings.skipShortSeconds) {
        if (settings.skipShortSongs) {
            tracks.filter { it.durationMs >= settings.skipShortSeconds * 1000L }
        } else {
            tracks
        }
    }
    val sleepFlow = remember(controller) { controller?.sleepEndsAt ?: idleSleep }
    val sleepEndsAt by sleepFlow.collectAsState()
    LaunchedEffect(controller, settings.playbackSpeed) {
        controller?.setSpeed(settings.playbackSpeed)
    }

    var collection by remember { mutableStateOf<MediaGroup?>(null) }
    var showPlayer by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var libraryTab by remember { mutableStateOf(LibraryTab.Tracks) }
    val barItems = remember {
        LibraryTab.barTabs.map { t ->
            LiquidBarItem(t.name, t.label) { tint -> GlowIcon(t.icon, tint, 26.dp) }
        }
    }
    // أول معالج مسجَّل أقل أولوية: يُغلق المشغّل ثم الألبوم ثم الإعدادات.
    BackHandler(enabled = showSettings) { showSettings = false }
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
    LaunchedEffect(current?.id) { current?.let { library.recordPlay(it.id) } }

    ProvideGlassConfig(glassConfig) {
        val barVisible = !showSettings && collection == null && !showPlayer
        val miniVisible = current != null && !showPlayer
        Box(modifier.fillMaxSize()) {
            Box(
                Modifier
                    .matchParentSize()
                    .glassBackdropSource(backdrop)
                    .background(Brush.verticalGradient(listOf(skin.bgTop, skin.bgBottom))),
            ) {
                val open = collection
                if (showSettings) {
                    SettingsScreen(
                        store = settings,
                        onBack = { showSettings = false },
                        onRescan = { rescan++ },
                        sleepEndsAt = sleepEndsAt,
                        onSleepTimer = { controller?.setSleepTimer(it) },
                        audioSessionId = { controller?.audioSessionId ?: 0 },
                    )
                } else if (open == null) {
                    LibraryScreen(
                        tracks = visibleTracks,
                        loaded = loaded,
                        hasAudioAccess = hasAudioAccess,
                        currentId = current?.id,
                        store = library,
                        tab = libraryTab,
                        onRequestPermission = { launcher.launch(permissions) },
                        onPlay = { list, i -> connection.play(list, i) },
                        onOpen = { collection = it },
                        onOpenSettings = { showSettings = true },
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
                // داخل المصدر: فتُلتقط الخلفية المشتركة وقد تلاشت الصفوف قبل المشغّل والشريط.
                BottomScrim(skin.bgBottom, barVisible, miniVisible)
            }

            val miniLift by animateDpAsState(if (barVisible) BarLift else 0.dp, label = "miniLift")

            if (miniVisible && current != null) {
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
                        .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 12.dp + miniLift),
                )
            }

            // الشريط الزجاجي السفلي: أخٌ للمصدر وفوقه، فيلتقط الخلفية المشتركة.
            AnimatedVisibility(
                visible = barVisible,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                LiquidGlassBar(
                    items = barItems,
                    selectedId = libraryTab.name,
                    onSelect = { id -> libraryTab = LibraryTab.valueOf(id) },
                    backdrop = backdrop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
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

/**
 * طبقة بلون الخلفية أسفل الشاشة: تغطي منطقة المشغّل المصغّر والشريط وشريط التنقل،
 * مع تلاشٍ ناعم فوقها، فلا تظهر الصفوف ولا الأغلفة خلفهما أو بينهما.
 * تُرسم داخل مصدر الخلفية الزجاجية فيلتقطها الزجاج ولا يرى الصفوف.
 */
@Composable
private fun BoxScope.BottomScrim(color: Color, barVisible: Boolean, miniVisible: Boolean) {
    val solidTarget = when {
        barVisible && miniVisible -> BarLift + MiniGap + MiniHeight
        barVisible -> BarLift
        miniVisible -> MiniGap + MiniHeight
        else -> 0.dp
    }
    val solid by animateDpAsState(solidTarget, label = "scrimSolid")
    if (solid <= 0.dp) return
    val fade = minOf(solid, ScrimFade)
    Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(fade)
                .background(Brush.verticalGradient(listOf(color.copy(alpha = 0f), color))),
        )
        Box(Modifier.fillMaxWidth().height(solid).background(color))
        Spacer(
            Modifier
                .fillMaxWidth()
                .windowInsetsBottomHeight(WindowInsets.navigationBars)
                .background(color),
        )
    }
}
