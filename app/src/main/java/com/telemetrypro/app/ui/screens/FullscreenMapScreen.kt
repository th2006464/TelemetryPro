package com.telemetrypro.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.telemetrypro.app.R
import com.telemetrypro.app.data.GpsFixStatus
import com.telemetrypro.app.data.LocationState
import com.telemetrypro.app.data.TrackPoint
import com.telemetrypro.app.ui.components.TopAppBar
import com.telemetrypro.app.ui.map.DotMatrixMap
import com.telemetrypro.app.ui.theme.*

/**
 * Fullscreen dot-matrix world map overlay.
 * Supports pan, zoom, and all gestures.
 */
@Composable
fun FullscreenMapScreen(
    state: LocationState,
    isOnlineMode: Boolean = false,
    onClose: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Top bar with close button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
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

        // Fullscreen map — fills all remaining space
        DotMatrixMap(
            latitude = state.latitude,
            longitude = state.longitude,
            isFixed = state.fixStatus == GpsFixStatus.FIXED,
            showCities = true,
            mapLabel = stringResource(R.string.map_world_position),
            trackPoints = if (state.isRecording) state.recordingPoints else emptyList(),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )

        // Close button at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceContainerHigh.copy(alpha = 0.6f))
                .clickable { onClose() }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.fullscreen_close),
                style = LabelCaps,
                color = OnSurfaceVariant
            )
        }
    }
}
