package com.glow.app.media

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.media.session.MediaSession
import com.glow.app.MainActivity

/** يبني إشعار التشغيل (MediaStyle) وقناته. */
class PlaybackNotifier(private val context: Context) {

    fun ensureChannel() {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "التشغيل",
                NotificationManager.IMPORTANCE_LOW,
            ).apply { setShowBadge(false) }
            manager.createNotificationChannel(channel)
        }
    }

    fun build(state: PlayerState, token: MediaSession.Token): Notification {
        val track = state.current
        val toggleIcon =
            if (state.isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val toggleLabel = if (state.isPlaying) "إيقاف مؤقت" else "تشغيل"

        return Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_headset)
            .setContentTitle(track?.title ?: "Glow")
            .setContentText(track?.artist.orEmpty())
            .setContentIntent(openAppIntent())
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setOngoing(state.isPlaying)
            .addAction(action(android.R.drawable.ic_media_previous, "السابق", GlowMediaSessionService.ACTION_PREVIOUS, 1))
            .addAction(action(toggleIcon, toggleLabel, GlowMediaSessionService.ACTION_TOGGLE, 2))
            .addAction(action(android.R.drawable.ic_media_next, "التالي", GlowMediaSessionService.ACTION_NEXT, 3))
            .setStyle(
                Notification.MediaStyle()
                    .setMediaSession(token)
                    .setShowActionsInCompactView(0, 1, 2),
            )
            .build()
    }

    private fun action(iconRes: Int, label: String, action: String, requestCode: Int): Notification.Action {
        val intent = Intent(context, GlowMediaSessionService::class.java).setAction(action)
        val pending = PendingIntent.getService(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return Notification.Action.Builder(Icon.createWithResource(context, iconRes), label, pending).build()
    }

    private fun openAppIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    companion object {
        const val CHANNEL_ID = "glow_playback"
    }
}
