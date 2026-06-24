package com.telemetrypro.app.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
 * Supports pan, anchored pinch-zoom, and all gestures.
 */
@Composable
fun FullscreenMapScreen(
    state: LocationState,
    isOnlineMode: Boolean = false,
    onClose: () -> Unit = {}
) {
    val activity = LocalContext.current as? Activity

    // Force landscape on enter, restore on exit
    DisposableEffect(Unit) {
        val originalOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = originalOrientation
                ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Use Box stacking: map fills entire screen, close button overlays
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Fullscreen map — fills entire screen, no headers/borders
        DotMatrixMap(
            latitude = state.latitude,
            longitude = state.longitude,
            isFixed = state.fixStatus == GpsFixStatus.FIXED,
            showCities = true,
            mapLabel = "",
            trackPoints = if (state.isRecording) state.recordingPoints else emptyList(),
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        )

        // Top-left fix status
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(SurfaceContainerHigh.copy(alpha = 0.6f), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
        }

        // Bottom-right close button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .background(SurfaceContainerHigh.copy(alpha = 0.7f), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                .clickable { onClose() }
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = stringResource(R.string.fullscreen_close),
                style = LabelCaps,
                color = OnSurfaceVariant
            )
        }
    }
}
