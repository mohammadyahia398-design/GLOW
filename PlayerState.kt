package com.glow.app.media

/** لقطة من حالة المشغّل تقرؤها الواجهة والخدمة. */
data class PlayerState(
    val queue: List<Track> = emptyList(),
    val current: Track? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffle: Boolean = false,
    val repeat: RepeatMode = RepeatMode.Off,
) {
    /** نسبة التقدم من 0 إلى 1. */
    val progress: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
}
