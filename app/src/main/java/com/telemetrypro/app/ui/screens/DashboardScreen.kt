package com.telemetrypro.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.telemetrypro.app.R
import com.telemetrypro.app.data.GpsFixStatus
import com.telemetrypro.app.data.LocationState
import com.telemetrypro.app.ui.components.*
import com.telemetrypro.app.ui.map.DotMatrixMap
import com.telemetrypro.app.ui.theme.*

@Composable
fun DashboardScreen(
    state: LocationState,
    isOnlineMode: Boolean = false,
    onFullscreenClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Fixed header area — map never scrolls, gestures are isolated
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
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

        Spacer(Modifier.height(8.dp))

        if (state.flightMode.label.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .background(PrimaryContainer.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .border(1.dp, PrimaryFixedDim.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.flight_mode_detected),
                    style = LabelCaps,
                    color = PrimaryFixedDim
                )
            }
        }

        // World Map — dot-matrix style (fixed, NOT scrollable — isolated gesture zone)
        DotMatrixMap(
            latitude = state.latitude,
            longitude = state.longitude,
            isFixed = state.fixStatus == GpsFixStatus.FIXED,
            showCities = true,
            mapLabel = if (state.isRecording)
                "${stringResource(R.string.recording_label)} ${String.format("%.2f", state.recordingDistanceKm)} km"
            else
                stringResource(R.string.map_world_position),
            trackPoints = if (state.isRecording) state.recordingPoints else emptyList(),
            showFullscreenButton = true,
            onFullscreenClick = onFullscreenClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Scrollable content below the map
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // GNSS coordinates + Accuracy
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReadoutTile(
                    label = stringResource(R.string.dashboard_gnss_coords),
                    value = String.format("%.4f", state.latitude),
                    unit = if (state.latitude >= 0) "°N" else "°S",
                    subLabel = String.format("%.4f", state.longitude) +
                            if (state.longitude >= 0) "°E" else "°W",
                    modifier = Modifier.weight(1f)
                )
                ReadoutTile(
                    label = stringResource(R.string.dashboard_accuracy),
                    value = String.format("%.1f", state.accuracy),
                    unit = stringResource(R.string.dashboard_unit_m),
                    unitInline = true,
                    valueColor = if (state.accuracy < 10f) Secondary else OnSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }


            // Altitude & Speed
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReadoutTile(
                    label = stringResource(R.string.dashboard_altitude),
                    value = String.format("%.0f", state.altitudeMeters),
                    unit = stringResource(R.string.dashboard_unit_msl),
                    modifier = Modifier.weight(1f)
                )
                ReadoutTile(
                    label = stringResource(R.string.dashboard_ground_speed),
                    value = String.format("%.1f", state.speedKmh),
                    unit = stringResource(R.string.dashboard_unit_kmh),
                    modifier = Modifier.weight(1f)
                )
            }

            // Constellation stats + Sat count (left-right)
            if (state.constellationStats.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ConstellationStatsCard(
                        stats = state.constellationStats,
                        modifier = Modifier.weight(1f)
                    )
                    ReadoutTile(
                        label = stringResource(R.string.dashboard_sat_count),
                        value = "${state.usedSatellites}/${state.totalSatellites}",
                        valueColor = Secondary,
                        modifier = Modifier.weight(0.35f)
                    )
                }
            }


            // SNR bar graph
            if (state.satellites.isNotEmpty()) {
                SnrBarGraph(
                    satellites = state.satellites,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}
