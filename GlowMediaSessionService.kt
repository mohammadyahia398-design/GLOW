package com.glow.app.media

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Binder
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * خدمة التشغيل: تملك [PlayerController] وجلسة الوسائط والإشعار.
 * تُسجَّل في المانيفست بـ foregroundServiceType="mediaPlayback".
 */
class GlowMediaSessionService : Service() {

    inner class LocalBinder : Binder() {
        val controller: PlayerController get() = this@GlowMediaSessionService.controller
    }

    private val binder = LocalBinder()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private lateinit var controller: PlayerController
    private lateinit var session: MediaSession
    private lateinit var notifier: PlaybackNotifier
    private lateinit var notificationManager: NotificationManager

    private var isForeground = false
    private var lastKey: Pair<Long?, Boolean>? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NotificationManager::class.java)
        notifier = PlaybackNotifier(this)
        notifier.ensureChannel()

        controller = PlayerController(this, scope)

        session = MediaSession(this, SESSION_TAG).apply {
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() = this@GlowMediaSessionService.controller.resume()
                override fun onPause() = this@GlowMediaSessionService.controller.pause()
                override fun onSkipToNext() = this@GlowMediaSessionService.controller.next()
                override fun onSkipToPrevious() = this@GlowMediaSessionService.controller.previous()
                override fun onSeekTo(pos: Long) = this@GlowMediaSessionService.controller.seekTo(pos)
            })
            isActive = true
        }

        scope.launch {
            controller.state.collect { render(it) }
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_TOGGLE -> controller.togglePlay()
            ACTION_NEXT -> controller.next()
            ACTION_PREVIOUS -> controller.previous()
        }
        // startForegroundService يفرض استدعاء startForeground خلال خمس ثوانٍ.
        enterForeground(notifier.build(controller.state.value, session.sessionToken))
        return START_NOT_STICKY
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (controller.state.value.current == null) stopSelf()
        return false
    }

    override fun onDestroy() {
        scope.cancel()
        session.isActive = false
        session.release()
        controller.release()
        super.onDestroy()
    }

    // ── العرض ──────────────────────────────────────────────────

    private fun render(state: PlayerState) {
        updateSession(state)

        val key = state.current?.id to state.isPlaying
        if (key == lastKey) return
        lastKey = key

        val notification = notifier.build(state, session.sessionToken)
        when {
            state.isPlaying -> enterForeground(notification)
            state.current != null -> {
                leaveForeground(detach = true)
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
            else -> {
                leaveForeground(detach = false)
                notificationManager.cancel(NOTIFICATION_ID)
            }
        }
    }

    private fun updateSession(state: PlayerState) {
        val track = state.current
        session.setMetadata(
            MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, track?.title)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, track?.artist)
                .putString(MediaMetadata.METADATA_KEY_ALBUM, track?.album)
                .putLong(MediaMetadata.METADATA_KEY_DURATION, state.durationMs)
                .build(),
        )

        val playbackState = when {
            track == null -> PlaybackState.STATE_NONE
            state.isPlaying -> PlaybackState.STATE_PLAYING
            else -> PlaybackState.STATE_PAUSED
        }
        session.setPlaybackState(
            PlaybackState.Builder()
                .setActions(SESSION_ACTIONS)
                .setState(playbackState, state.positionMs, 1f)
                .build(),
        )
    }

    private fun enterForeground(notification: android.app.Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        isForeground = true
    }

    private fun leaveForeground(detach: Boolean) {
        if (!isForeground) return
        stopForeground(if (detach) STOP_FOREGROUND_DETACH else STOP_FOREGROUND_REMOVE)
        isForeground = false
    }

    companion object {
        const val ACTION_TOGGLE = "com.glow.app.action.TOGGLE"
        const val ACTION_NEXT = "com.glow.app.action.NEXT"
        const val ACTION_PREVIOUS = "com.glow.app.action.PREVIOUS"

        private const val NOTIFICATION_ID = 1001
        private const val SESSION_TAG = "GlowSession"

        private const val SESSION_ACTIONS =
            PlaybackState.ACTION_PLAY or
                PlaybackState.ACTION_PAUSE or
                PlaybackState.ACTION_PLAY_PAUSE or
                PlaybackState.ACTION_SKIP_TO_NEXT or
                PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                PlaybackState.ACTION_SEEK_TO
    }
}
