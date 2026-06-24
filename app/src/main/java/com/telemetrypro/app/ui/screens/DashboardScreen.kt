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
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.telemetrypro.app.data.GpsFixStatus
import com.telemetrypro.app.data.LocationState
import com.telemetrypro.app.ui.components.*
import com.telemetrypro.app.ui.theme.*

/**
 * Dashboard Screen — main overview with coordinates, map, constellations,
 * altitude, speed, SNR bars, and NMEA feed.
 */
@Composable
fun DashboardScreen(
    state: LocationState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {
        // TopAppBar
        TopAppBar(
            fixLabel = when (state.fixStatus) {
                GpsFixStatus.FIXED -> "3D FIX"
                GpsFixStatus.SEARCHING -> "ACQUIRING"
                GpsFixStatus.NO_SIGNAL -> "NO SIGNAL"
            },
            isFixed = state.fixStatus == GpsFixStatus.FIXED
        )

        Spacer(Modifier.height(8.dp))

        // Flight mode banner
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
                    text = state.flightMode.label,
                    style = LabelCaps,
                    color = PrimaryFixedDim
                )
            }
        }

        // ---- Row 1: Coordinates + Mini Map ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Coordinates
            ReadoutTile(
                label = "GNSS COORDINATES",
                value = String.format("%.4f", state.latitude),
                unit = if (state.latitude >= 0) "°N" else "°S",
                subLabel = String.format("%.4f", state.longitude) +
                        if (state.longitude >= 0) "°E" else "°W",
                modifier = Modifier.weight(1f)
            )
        }

        // ---- Row 2: Altitude + Speed ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ReadoutTile(
                label = "ALTITUDE",
                value = String.format("%.0f", state.altitudeMeters),
                unit = "M MSL",
                modifier = Modifier.weight(1f)
            )
            ReadoutTile(
                label = "GROUND SPEED",
                value = String.format("%.1f", state.speedKmh),
                unit = "KM/H",
                modifier = Modifier.weight(1f)
            )
        }

        // ---- Row 3: Constellation Stats ----
        if (state.constellationStats.isNotEmpty()) {
            ConstellationStatsCard(
                stats = state.constellationStats,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // ---- Row 4: SNR Bar Graph ----
        if (state.satellites.isNotEmpty()) {
            SnrBarGraph(
                satellites = state.satellites,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // ---- Row 5: Fix metadata ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ReadoutTile(
                label = "SAT COUNT",
                value = "${state.usedSatellites}/${state.totalSatellites}",
                valueColor = Secondary,
                modifier = Modifier.weight(1f)
            )
            ReadoutTile(
                label = "ACCURACY",
                value = String.format("%.1f", state.accuracy),
                unit = "M",
                valueColor = if (state.accuracy < 10f) Secondary else OnSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }

        // ---- NMEA Feed ----
        NmeaFeed(
            lines = state.nmeaLogLines,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(Modifier.height(80.dp)) // Bottom nav spacing
    }
}
