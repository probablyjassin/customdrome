package com.jassin.customdrome.playback

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlaybackManager(
    private val coverFetcher: suspend (songId: String) -> ByteArray? = { null },
) {
    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun playQueue(
        queue: List<PlaybackItem>,
        startIndex: Int = 0,
        startPlaying: Boolean = true,
    ) {
        if (queue.isEmpty()) {
            clearQueue()
            return
        }

        val resolvedIndex = startIndex.coerceIn(0, queue.lastIndex)
        _state.value =
            PlaybackState(
                queue = queue,
                currentIndex = resolvedIndex,
                isPlaying = startPlaying,
                positionMs = 0L,
            )

        // Kick off async cover fetch for the newly selected item
        refreshCoverForCurrent()

        // TODO: trigger media source preparation/decoding once audio engine is integrated.
    }

    fun play() {
        _state.update { current ->
            if (current.currentItem == null) current else current.copy(isPlaying = true)
        }
        // TODO: resume decoder/audio output here.
    }

    fun pause() {
        _state.update { current -> current.copy(isPlaying = false) }
        // TODO: pause decoder/audio output here.
    }

    fun togglePlayPause() {
        _state.update { current ->
            if (current.currentItem == null) current else current.copy(isPlaying = !current.isPlaying)
        }
        // TODO: toggle decoder/audio output here.
    }

    fun next() {
        _state.update { current ->
            val nextIndex = current.currentIndex + 1
            if (nextIndex in current.queue.indices) {
                current.copy(currentIndex = nextIndex, positionMs = 0L)
            } else {
                current
            }
        }
        refreshCoverForCurrent()
        // TODO: seek/load next track in playback engine.
    }

    fun previous() {
        _state.update { current ->
            val prevIndex = current.currentIndex - 1
            if (prevIndex in current.queue.indices) {
                current.copy(currentIndex = prevIndex, positionMs = 0L)
            } else {
                current
            }
        }
        refreshCoverForCurrent()
        // TODO: seek/load previous track in playback engine.
    }

    fun seekTo(positionMs: Long) {
        _state.update { current -> current.copy(positionMs = positionMs.coerceAtLeast(0L)) }
        // TODO: perform actual decoder seek.
    }

    fun enqueue(items: List<PlaybackItem>) {
        if (items.isEmpty()) return
        _state.update { current ->
            val mergedQueue = current.queue + items
            if (current.currentIndex == -1) {
                current.copy(queue = mergedQueue, currentIndex = 0)
            } else {
                current.copy(queue = mergedQueue)
            }
        }
        // TODO: notify playback engine queue updates.
    }

    fun clearQueue() {
        _state.value = PlaybackState()
        // ensure cover cleared
        // no need to call cover fetch
        // TODO: stop and release playback engine resources.
    }

    private fun refreshCoverForCurrent() {
        val id = _state.value.currentItem?.coverArtId ?: return
        // clear current cover while fetching
        _state.update { it.copy(currentCoverArt = null) }
        scope.launch {
            try {
                val bytes = coverFetcher(id)
                _state.update { it.copy(currentCoverArt = bytes) }
            } catch (_: Throwable) {
                // ignore fetch errors for now
                _state.update { it.copy(currentCoverArt = null) }
            }
        }
    }
}

