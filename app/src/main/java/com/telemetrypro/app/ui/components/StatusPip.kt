package com.telemetrypro.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.telemetrypro.app.ui.theme.PrimaryFixedDim
import com.telemetrypro.app.ui.theme.Secondary
import com.telemetrypro.app.ui.theme.WarningAmber
import com.telemetrypro.app.ui.theme.MutedGray

/**
 * LED-style status indicator pip with breathing pulse animation.
 */
@Composable
fun StatusPip(
    status: PipStatus = PipStatus.ACTIVE,
    size: Int = 8,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val color = when (status) {
        PipStatus.ACTIVE -> Secondary
        PipStatus.WARNING -> WarningAmber
        PipStatus.INACTIVE -> MutedGray
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pip_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (status == PipStatus.ACTIVE) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pip_scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (status == PipStatus.ACTIVE) 0.5f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pip_alpha"
    )

    Canvas(
        modifier = modifier
            .size(size.dp)
            .scale(if (status == PipStatus.ACTIVE) scale else 1f)
    ) {
        // Glow shadow circle
        drawCircle(
            color = color.copy(alpha = alpha * 0.3f),
            radius = size.toFloat() + 4f
        )
        // Core circle
        drawCircle(
            color = color.copy(alpha = alpha),
        )
    }
}

enum class PipStatus { ACTIVE, WARNING, INACTIVE }
