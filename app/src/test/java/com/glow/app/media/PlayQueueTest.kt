package com.glow.app.media

import com.google.common.truth.Truth.assertThat
import kotlin.random.Random
import org.junit.Test

class PlayQueueTest {

    private fun queue() = PlayQueue(Random(42))

    @Test
    fun empty_queue_returns_null_everywhere() {
        val q = queue()
        assertThat(q.currentIndex).isNull()
        assertThat(q.next(auto = false)).isNull()
        assertThat(q.previous()).isNull()
    }

    @Test
    fun load_starts_at_requested_index() {
        val q = queue()
        q.load(size = 5, startIndex = 3)
        assertThat(q.currentIndex).isEqualTo(3)
    }

    @Test
    fun load_coerces_out_of_range_start_index() {
        val q = queue()
        q.load(size = 3, startIndex = 10)
        assertThat(q.currentIndex).isEqualTo(2)
    }

    @Test
    fun next_moves_forward_in_order() {
        val q = queue()
        q.load(size = 3)
        assertThat(q.next(auto = false)).isEqualTo(1)
        assertThat(q.next(auto = false)).isEqualTo(2)
    }

    @Test
    fun next_at_end_with_repeat_off_returns_null_and_keeps_current() {
        val q = queue()
        q.load(size = 2)
        q.next(auto = false)
        assertThat(q.next(auto = false)).isNull()
        assertThat(q.currentIndex).isEqualTo(1)
    }

    @Test
    fun next_at_end_with_repeat_all_wraps_to_start() {
        val q = queue()
        q.repeat = RepeatMode.All
        q.load(size = 2, startIndex = 1)
        assertThat(q.next(auto = false)).isEqualTo(0)
    }

    @Test
    fun repeat_one_replays_same_track_when_auto() {
        val q = queue()
        q.repeat = RepeatMode.One
        q.load(size = 3, startIndex = 1)
        assertThat(q.next(auto = true)).isEqualTo(1)
    }

    @Test
    fun repeat_one_still_advances_on_manual_next() {
        val q = queue()
        q.repeat = RepeatMode.One
        q.load(size = 3, startIndex = 1)
        assertThat(q.next(auto = false)).isEqualTo(2)
    }

    @Test
    fun previous_goes_back_one_track() {
        val q = queue()
        q.load(size = 3, startIndex = 2)
        assertThat(q.previous()).isEqualTo(1)
    }

    @Test
    fun previous_at_start_with_repeat_off_stays_on_first() {
        val q = queue()
        q.load(size = 3)
        assertThat(q.previous()).isEqualTo(0)
    }

    @Test
    fun previous_at_start_with_repeat_all_wraps_to_last() {
        val q = queue()
        q.repeat = RepeatMode.All
        q.load(size = 3)
        assertThat(q.previous()).isEqualTo(2)
    }

    @Test
    fun shuffle_starts_with_chosen_track_and_visits_each_once() {
        val q = queue()
        q.load(size = 5, startIndex = 2, shuffle = true)
        val visited = mutableListOf(q.currentIndex!!)
        repeat(4) { visited += q.next(auto = false)!! }

        assertThat(visited.first()).isEqualTo(2)
        assertThat(visited.toSet()).containsExactly(0, 1, 2, 3, 4)
        assertThat(q.next(auto = false)).isNull()
    }

    @Test
    fun toggling_shuffle_keeps_current_track() {
        val q = queue()
        q.load(size = 5, startIndex = 3)

        q.setShuffle(true)
        assertThat(q.currentIndex).isEqualTo(3)

        q.setShuffle(false)
        assertThat(q.currentIndex).isEqualTo(3)
        assertThat(q.next(auto = false)).isEqualTo(4)
    }

    @Test
    fun repeat_mode_cycles_off_all_one_off() {
        assertThat(RepeatMode.Off.cycle()).isEqualTo(RepeatMode.All)
        assertThat(RepeatMode.All.cycle()).isEqualTo(RepeatMode.One)
        assertThat(RepeatMode.One.cycle()).isEqualTo(RepeatMode.Off)
    }
}
