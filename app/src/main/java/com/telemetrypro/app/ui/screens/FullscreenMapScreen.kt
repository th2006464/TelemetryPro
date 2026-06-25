package com.telemetrypro.app.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.telemetrypro.app.R
import com.telemetrypro.app.data.GpsFixStatus
import com.telemetrypro.app.data.LocationState
import com.telemetrypro.app.ui.components.TopAppBar
import com.telemetrypro.app.ui.map.DotMatrixMap
import com.telemetrypro.app.ui.theme.*

/**
 * Fullscreen dot-matrix world map overlay.
 * Forces landscape orientation for optimal map viewing.
 * Uses fixed-aspect map with letterboxing to avoid distortion.
 */
@Composable
fun FullscreenMapScreen(
    state: LocationState,
    isOnlineMode: Boolean = false,
    onClose: () -> Unit = {}
) {
    val activity = LocalContext.current as? Activity

    DisposableEffect(Unit) {
        val original = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = original
                ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Map fills entire screen in fullscreen mode
        DotMatrixMap(
            latitude = state.latitude,
            longitude = state.longitude,
            isFixed = state.fixStatus == GpsFixStatus.FIXED,
            showCities = true,
            mapLabel = "",
            trackPoints = if (state.isRecording) state.recordingPoints else emptyList(),
            isRecording = state.isRecording,
            isFullscreen = true,
            modifier = Modifier.fillMaxSize()
        )

        // Top-left: GPS fix status
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .background(SurfaceContainerHigh.copy(alpha = 0.65f), shape = CircleShape)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            TopAppBar(
                fixLabel = when (state.fixStatus) {
                    GpsFixStatus.FIXED -> stringResource(R.string.fix_status_fixed)
                    GpsFixStatus.SEARCHING -> stringResource(R.string.fix_status_searching)
                    GpsFixStatus.NO_SIGNAL -> stringResource(R.string.fix_status_no_signal)
                },
                isFixed = state.fixStatus == GpsFixStatus.FIXED,
                isOnline = isOnlineMode
            )
        }

        // Top-right: coordinate info
        if (state.fixStatus == GpsFixStatus.FIXED) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(SurfaceContainerHigh.copy(alpha = 0.65f), shape = CircleShape)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = String.format("%.4f\u00B0, %.4f\u00B0", state.latitude, state.longitude),
                    style = CodeSm,
                    color = Primary
                )
            }
        }

        // Bottom-center: close button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .background(SurfaceContainerHigh.copy(alpha = 0.75f), shape = CircleShape)
                .clickable { onClose() }
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.fullscreen_close),
                style = LabelCaps,
                color = OnSurfaceVariant
            )
        }
    }
}
