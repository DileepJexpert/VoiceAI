package com.voiceai.app.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class TtsState {
    IDLE,
    PLAYING,
    PAUSED
}

@Composable
fun ReadAloudButton(
    ttsState: TtsState,
    progress: Float,
    playbackSpeed: Float,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onSpeedChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = ttsState != TtsState.IDLE,
        transitionSpec = {
            (slideInHorizontally { it } + fadeIn()) togetherWith
                (slideOutHorizontally { -it } + fadeOut())
        },
        label = "read_aloud_expand",
        modifier = modifier
    ) { isActive ->
        if (!isActive) {
            // Collapsed: just a speaker icon button
            FilledTonalIconButton(
                onClick = onPlayPause
            ) {
                Icon(
                    imageVector = Icons.Filled.VolumeUp,
                    contentDescription = "Read aloud"
                )
            }
        } else {
            // Expanded: mini player bar
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play/Pause
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (ttsState == TtsState.PLAYING) {
                                Icons.Filled.Pause
                            } else {
                                Icons.Filled.PlayArrow
                            },
                            contentDescription = if (ttsState == TtsState.PLAYING) "Pause" else "Resume",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .width(80.dp)
                            .height(4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outline,
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Speed control
                    FilledTonalButton(
                        onClick = onSpeedChange,
                        modifier = Modifier.size(width = 44.dp, height = 28.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = formatTtsSpeed(playbackSpeed),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Stop button
                    IconButton(
                        onClick = onStop,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Stop,
                            contentDescription = "Stop",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTtsSpeed(speed: Float): String {
    return if (speed == speed.toLong().toFloat()) {
        "${speed.toLong()}x"
    } else {
        "${speed}x"
    }
}
