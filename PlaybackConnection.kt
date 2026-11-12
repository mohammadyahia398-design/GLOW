package com.glow.app.media

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** جانب الواجهة: يربط التطبيق بخدمة التشغيل ويكشف [PlayerController] عند جاهزيتها. */
class PlaybackConnection(private val context: Context) {

    private val _controller = MutableStateFlow<PlayerController?>(null)
    val controller: StateFlow<PlayerController?> = _controller.asStateFlow()

    private var bound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            _controller.value = (service as? GlowMediaSessionService.LocalBinder)?.controller
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            _controller.value = null
        }
    }

    fun bind() {
        if (bound) return
        bound = context.bindService(serviceIntent(), connection, Context.BIND_AUTO_CREATE)
    }

    fun unbind() {
        if (bound) {
            context.unbindService(connection)
            bound = false
        }
        _controller.value = null
    }

    /** يبدأ التشغيل ويُبقي الخدمة حيّة في المقدمة بعد مغادرة التطبيق. */
    fun play(tracks: List<Track>, startIndex: Int) {
        val controller = _controller.value ?: return
        ContextCompat.startForegroundService(context, serviceIntent())
        controller.playTracks(tracks, startIndex)
    }

    private fun serviceIntent() = Intent(context, GlowMediaSessionService::class.java)
}
