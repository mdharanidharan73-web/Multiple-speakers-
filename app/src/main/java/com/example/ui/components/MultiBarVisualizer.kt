package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.StatusCyan

/**
 * Dynamic Multi-Bar Equalizer driven by real-time PCM audio amplitude
 * calculated via custom Media3 AudioProcessor in ExoPlayer's pipeline.
 */
@Composable
fun MultiBarVisualizer(
    amplitudes: FloatArray,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 16,
    barSpacing: Dp = 3.dp,
    barColorGradient: List<Color> = listOf(ElectricBlue, NeonViolet, StatusCyan)
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        if (width <= 0 || height <= 0) return@Canvas

        val spacingPx = barSpacing.toPx()
        val totalSpacing = spacingPx * (barCount - 1)
        val barWidth = ((width - totalSpacing) / barCount).coerceAtLeast(2f)
        val minBarHeight = 4.dp.toPx()

        val brush = Brush.verticalGradient(
            colors = barColorGradient,
            startY = height,
            endY = 0f
        )

        for (i in 0 until barCount) {
            val amp = if (isPlaying && i < amplitudes.size) {
                amplitudes[i].coerceIn(0.05f, 1.0f)
            } else {
                0.05f
            }

            val targetHeight = (height * amp).coerceIn(minBarHeight, height)
            val left = i * (barWidth + spacingPx)
            val top = height - targetHeight

            drawRoundRect(
                brush = brush,
                topLeft = Offset(left, top),
                size = Size(barWidth, targetHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
