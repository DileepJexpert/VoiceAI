package com.voiceai.app.util

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioPlayer @Inject constructor(
    private val context: Context
) {
    private var player: ExoPlayer? = null
    private var positionUpdateJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed = _playbackSpeed.asStateFlow()

    private fun getOrCreatePlayer(): ExoPlayer {
        return player ?: ExoPlayer.Builder(context).build().also { exoPlayer ->
            exoPlayer.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                    if (isPlaying) {
                        startPositionUpdates()
                    } else {
                        stopPositionUpdates()
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            _duration.value = exoPlayer.duration
                        }
                        Player.STATE_ENDED -> {
                            _isPlaying.value = false
                            _currentPosition.value = 0L
                            stopPositionUpdates()
                        }
                        else -> { /* no-op */ }
                    }
                }
            })
            player = exoPlayer
        }
    }

    fun play(filePath: String) {
        val exoPlayer = getOrCreatePlayer()
        val mediaItem = MediaItem.fromUri(filePath)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.playbackParameters = exoPlayer.playbackParameters.withSpeed(_playbackSpeed.value)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun pause() {
        player?.pause()
    }

    fun resume() {
        player?.play()
    }

    fun stop() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
        player?.stop()
        player?.clearMediaItems()
        _isPlaying.value = false
        _currentPosition.value = 0L
        _duration.value = 0L
    }

    fun seekTo(position: Long) {
        player?.seekTo(position)
        _currentPosition.value = position
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        player?.let { exoPlayer ->
            exoPlayer.playbackParameters = exoPlayer.playbackParameters.withSpeed(speed)
        }
    }

    fun release() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
        player?.release()
        player = null
        _isPlaying.value = false
        _currentPosition.value = 0L
        _duration.value = 0L
        _playbackSpeed.value = 1.0f
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = scope.launch {
            while (isActive) {
                player?.let { exoPlayer ->
                    _currentPosition.value = exoPlayer.currentPosition
                }
                delay(100L)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }
}
