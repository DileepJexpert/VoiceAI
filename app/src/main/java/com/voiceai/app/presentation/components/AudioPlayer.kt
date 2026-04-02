package com.voiceai.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun AudioPlayer(
    currentPosition: Long,
    duration: Long,
    isPlaying: Boolean,
    playbackSpeed: Float,
    onPlayPauseClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onSpeedChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play/Pause button
                IconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Seek bar
                val sliderValue = if (duration > 0) {
                    currentPosition.toFloat() / duration.toFloat()
                } else {
                    0f
                }

                Slider(
                    value = sliderValue,
                    onValueChange = { fraction ->
                        onSeek((fraction * duration).toLong())
                    },
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Speed control
                FilledTonalButton(
                    onClick = onSpeedChange,
                    modifier = Modifier.size(width = 52.dp, height = 32.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = formatSpeed(playbackSpeed),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Duration labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 44.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(currentPosition),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatTime(duration),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}

private fun formatSpeed(speed: Float): String {
    return if (speed == speed.toLong().toFloat()) {
        "${speed.toLong()}x"
    } else {
        "${speed}x"
    }
}

val PlaybackSpeeds = listOf(0.5f, 1.0f, 1.5f, 2.0f)

fun nextPlaybackSpeed(current: Float): Float {
    val currentIndex = PlaybackSpeeds.indexOf(current)
    return if (currentIndex == -1 || currentIndex == PlaybackSpeeds.lastIndex) {
        PlaybackSpeeds.first()
    } else {
        PlaybackSpeeds[currentIndex + 1]
    }
}
