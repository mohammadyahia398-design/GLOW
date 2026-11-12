package com.glow.app.media

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.PowerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * يشغّل الصوت بـ MediaPlayer ويدير الطابور وتركيز الصوت.
 * يجب استدعاء كل دوال هذا الصنف من الخيط الرئيسي.
 */
class PlayerController(
    private val context: Context,
    private val scope: CoroutineScope,
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val queue = PlayQueue()
    private var tracks: List<Track> = emptyList()

    private var player: MediaPlayer? = null
    private var prepared = false
    private var consecutiveErrors = 0
    private var resumeOnFocusGain = false
    private var ticker: Job? = null
    private var focusRequest: AudioFocusRequest? = null

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                resumeOnFocusGain = false
                pause()
                abandonFocus()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (isPlayingNow()) {
                    resumeOnFocusGain = true
                    pause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> player?.setVolume(0.3f, 0.3f)
            AudioManager.AUDIOFOCUS_GAIN -> {
                player?.setVolume(1f, 1f)
                if (resumeOnFocusGain) {
                    resumeOnFocusGain = false
                    resume()
                }
            }
        }
    }

    // ── التحكم العام ────────────────────────────────────────────

    fun playTracks(list: List<Track>, startIndex: Int) {
        if (list.isEmpty()) return
        tracks = list
        queue.load(list.size, startIndex, queue.shuffle)
        consecutiveErrors = 0
        playCurrent()
    }

    fun togglePlay() {
        if (isPlayingNow()) pause() else resume()
    }

    fun resume() {
        val mp = player
        if (mp == null) {
            if (tracks.isNotEmpty()) playCurrent()
            return
        }
        if (prepared && !mp.isPlaying && requestFocus()) {
            mp.start()
            startTicker()
            publish()
        }
    }

    fun pause() {
        val mp = player
        if (mp != null && prepared && mp.isPlaying) mp.pause()
        stopTicker()
        publish()
    }

    fun next() {
        if (queue.next(auto = false) != null) playCurrent()
    }

    fun previous() {
        val mp = player
        if (mp != null && prepared && mp.currentPosition > RESTART_THRESHOLD_MS) {
            seekTo(0L)
        } else if (queue.previous() != null) {
            playCurrent()
        }
    }

    fun seekTo(positionMs: Long) {
        val mp = player ?: return
        if (!prepared) return
        mp.seekTo(positionMs.toInt())
        publish()
    }

    fun setShuffle(enabled: Boolean) {
        queue.setShuffle(enabled)
        publish()
    }

    fun cycleRepeat() {
        queue.repeat = queue.repeat.cycle()
        publish()
    }

    fun release() {
        stopTicker()
        releasePlayer()
        abandonFocus()
    }

    // ── الداخل ─────────────────────────────────────────────────

    private fun isPlayingNow(): Boolean = prepared && player?.isPlaying == true

    private fun playCurrent() {
        val index = queue.currentIndex ?: return
        val track = tracks.getOrNull(index) ?: return

        releasePlayer()
        if (!requestFocus()) {
            publish()
            return
        }

        val mp = MediaPlayer()
        try {
            mp.setAudioAttributes(audioAttributes)
            mp.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
            mp.setDataSource(context, track.uri)
            mp.setOnPreparedListener {
                prepared = true
                consecutiveErrors = 0
                it.start()
                startTicker()
                publish()
            }
            mp.setOnCompletionListener { onCompleted() }
            mp.setOnErrorListener { _, _, _ ->
                handleError()
                true
            }
            mp.prepareAsync()
        } catch (_: Exception) {
            mp.release()
            handleError()
            return
        }
        player = mp
        prepared = false
        publish()
    }

    private fun onCompleted() {
        if (queue.next(auto = true) != null) playCurrent() else stopPlayback()
    }

    /** مقطع تالف أو غير قابل للقراءة: نتخطاه، ونتوقف إن تعطلت القائمة كلها. */
    private fun handleError() {
        releasePlayer()
        consecutiveErrors++
        if (consecutiveErrors >= tracks.size || queue.next(auto = false) == null) {
            stopPlayback()
        } else {
            playCurrent()
        }
    }

    private fun stopPlayback() {
        stopTicker()
        releasePlayer()
        abandonFocus()
        publish()
    }

    private fun releasePlayer() {
        player?.let {
            it.setOnPreparedListener(null)
            it.setOnCompletionListener(null)
            it.setOnErrorListener(null)
            it.release()
        }
        player = null
        prepared = false
    }

    private fun requestFocus(): Boolean {
        val request = focusRequest ?: AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(audioAttributes)
            .setOnAudioFocusChangeListener(focusListener)
            .build()
            .also { focusRequest = it }
        return audioManager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonFocus() {
        focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
    }

    private fun startTicker() {
        if (ticker?.isActive == true) return
        ticker = scope.launch {
            while (isActive) {
                publish()
                delay(TICK_MS)
            }
        }
    }

    private fun stopTicker() {
        ticker?.cancel()
        ticker = null
    }

    private fun publish() {
        val track = queue.currentIndex?.let { tracks.getOrNull(it) }
        val mp = player
        val ready = prepared && mp != null
        _state.value = PlayerState(
            queue = tracks,
            current = track,
            isPlaying = ready && mp?.isPlaying == true,
            positionMs = if (ready) mp?.currentPosition?.toLong() ?: 0L else 0L,
            durationMs = if (ready) mp?.duration?.toLong() ?: 0L else track?.durationMs ?: 0L,
            shuffle = queue.shuffle,
            repeat = queue.repeat,
        )
    }

    private companion object {
        const val TICK_MS = 500L
        const val RESTART_THRESHOLD_MS = 3_000
    }
}
