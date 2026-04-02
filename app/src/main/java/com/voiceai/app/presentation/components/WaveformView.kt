package com.voiceai.app.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun WaveformView(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    barCount: Int = 50,
    barWidth: Dp = 3.dp,
    barSpacing: Dp = 2.dp,
    minBarHeight: Float = 0.05f,
    animationDurationMs: Int = 300
) {
    val normalizedAmplitudes = remember(amplitudes, barCount) {
        if (amplitudes.isEmpty()) {
            List(barCount) { 0f }
        } else {
            val step = amplitudes.size.toFloat() / barCount
            List(barCount) { i ->
                val startIndex = (i * step).toInt().coerceIn(0, amplitudes.lastIndex)
                val endIndex = ((i + 1) * step).toInt().coerceIn(0, amplitudes.size)
                if (startIndex < endIndex) {
                    amplitudes.subList(startIndex, endIndex).average().toFloat()
                } else {
                    amplitudes.getOrElse(startIndex) { 0f }
                }
            }
        }
    }

    val animatedHeights = remember(barCount) {
        List(barCount) { Animatable(0f) }
    }

    LaunchedEffect(normalizedAmplitudes) {
        normalizedAmplitudes.forEachIndexed { index, target ->
            if (index < animatedHeights.size) {
                launch {
                    animatedHeights[index].animateTo(
                        targetValue = target.coerceIn(minBarHeight, 1f),
                        animationSpec = tween(
                            durationMillis = animationDurationMs,
                            delayMillis = index * 10
                        )
                    )
                }
            }
        }
    }

    val resolvedBarColor = barColor

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barWidthPx = barWidth.toPx()
        val barSpacingPx = barSpacing.toPx()
        val totalBarWidth = barWidthPx + barSpacingPx
        val startX = (canvasWidth - totalBarWidth * barCount + barSpacingPx) / 2f

        animatedHeights.forEachIndexed { index, animatable ->
            val barHeight = animatable.value * canvasHeight
            val x = startX + index * totalBarWidth
            val y = (canvasHeight - barHeight) / 2f

            drawRoundRect(
                color = resolvedBarColor,
                topLeft = Offset(x, y),
                size = Size(barWidthPx, barHeight),
                cornerRadius = CornerRadius(barWidthPx / 2f, barWidthPx / 2f)
            )
        }
    }
}
