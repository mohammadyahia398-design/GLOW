package com.glow.app.media

import kotlin.random.Random

enum class RepeatMode {
    Off, All, One;

    fun cycle(): RepeatMode = when (this) {
        Off -> All
        All -> One
        One -> Off
    }
}

/**
 * منطق ترتيب التشغيل (التالي والسابق والخلط والتكرار).
 * يتعامل مع فهارس القائمة فقط، ولا يعتمد على Android، فيُختبر بوحدة اختبار عادية.
 */
class PlayQueue(private val random: Random = Random.Default) {

    private var order: List<Int> = emptyList()
    private var position: Int = -1

    var shuffle: Boolean = false
        private set

    var repeat: RepeatMode = RepeatMode.Off

    val size: Int get() = order.size

    /** فهرس المقطع الحالي في القائمة الأصلية، أو null إن كانت فارغة. */
    val currentIndex: Int? get() = order.getOrNull(position)

    fun load(size: Int, startIndex: Int = 0, shuffle: Boolean = false) {
        this.shuffle = shuffle
        if (size <= 0) {
            order = emptyList()
            position = -1
            return
        }
        val start = startIndex.coerceIn(0, size - 1)
        order = buildOrder(size, start)
        position = order.indexOf(start)
    }

    /**
     * @param auto true عند انتهاء المقطع تلقائياً (فيُعاد المقطع نفسه إن كان التكرار One).
     * @return فهرس المقطع التالي، أو null عند نهاية القائمة دون تكرار.
     */
    fun next(auto: Boolean): Int? {
        if (order.isEmpty()) return null
        if (auto && repeat == RepeatMode.One) return currentIndex

        if (position + 1 < order.size) {
            position++
        } else if (repeat == RepeatMode.All) {
            val first = if (shuffle) random.nextInt(order.size) else 0
            order = buildOrder(order.size, first)
            position = order.indexOf(first)
        } else {
            return null
        }
        return currentIndex
    }

    fun previous(): Int? {
        if (order.isEmpty()) return null
        if (position > 0) {
            position--
        } else if (repeat == RepeatMode.All) {
            position = order.lastIndex
        }
        return currentIndex
    }

    /** يبدّل الخلط مع الإبقاء على المقطع الحالي. */
    fun setShuffle(enabled: Boolean) {
        if (enabled == shuffle) return
        shuffle = enabled
        val current = currentIndex ?: return
        order = buildOrder(order.size, current)
        position = order.indexOf(current)
    }

    private fun buildOrder(size: Int, first: Int): List<Int> =
        if (shuffle) {
            listOf(first) + (0 until size).filter { it != first }.shuffled(random)
        } else {
            (0 until size).toList()
        }
}
