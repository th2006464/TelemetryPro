package com.telemetrypro.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.telemetrypro.app.data.LocationState
import com.telemetrypro.app.data.SatelliteInfo
import com.telemetrypro.app.ui.components.TopAppBar
import com.telemetrypro.app.ui.theme.*
import kotlin.math.*

/**
 * Skyview Screen — radar-style satellite constellation viewer.
 * Shows rotating radar scan, satellite positions by constellation color,
 * and a detailed satellite inventory table.
 */
@Composable
fun SkyviewScreen(
    state: LocationState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            fixLabel = "${state.usedSatellites}/${state.totalSatellites} SVs",
            isFixed = state.usedSatellites > 0
        )

        Spacer(Modifier.height(8.dp))

        // ---- Radar Display ----
        RadarDisplay(
            satellites = state.satellites,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp)
        )

        // ---- Constellation Legend ----
        if (state.constellationStats.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                state.constellationStats.forEach { stat ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Canvas(modifier = Modifier.size(12.dp)) {
                            drawCircle(color = stat.constellation.color)
                        }
                        Text(
                            stat.constellation.label,
                            style = CodeSm,
                            color = stat.constellation.color
                        )
                        Text(
                            "${stat.totalVisible}",
                            style = TelemetryMd,
                            color = stat.constellation.color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ---- Satellite Inventory Table ----
        SatelliteTable(
            satellites = state.satellites,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(80.dp))
    }
}

// ============================================================
// Radar Display
// ============================================================

@Composable
private fun RadarDisplay(
    satellites: List<SatelliteInfo>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar_scan")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_sweep"
    )

    Canvas(
        modifier = modifier
            .background(TileBackground, RoundedCornerShape(16.dp))
            .border(1.dp, TileBorder, RoundedCornerShape(16.dp))
    ) {
        val cx = size.width / 2
        val cy = size.height / 2
        val maxRadius = min(cx, cy) * 0.85f

        // Concentric rings
        for (r in arrayOf(0.25f, 0.5f, 0.75f, 1f)) {
            drawCircle(
                color = OutlineVariant.copy(alpha = 0.3f),
                radius = maxRadius * r,
                center = Offset(cx, cy),
                style = Stroke(width = 1f)
            )
        }

        // Cross hairs
        drawLine(
            color = OutlineVariant.copy(alpha = 0.15f),
            start = Offset(cx - maxRadius, cy),
            end = Offset(cx + maxRadius, cy),
            strokeWidth = 1f
        )
        drawLine(
            color = OutlineVariant.copy(alpha = 0.15f),
            start = Offset(cx, cy - maxRadius),
            end = Offset(cx, cy + maxRadius),
            strokeWidth = 1f
        )

        // Cardinal labels
        val labels = listOf("N" to 0f, "E" to 90f, "S" to 180f, "W" to 270f)
        // Labels drawn via drawContext (not native canvas for simplicity, skip small text)

        // Sweep line
        rotate(sweepAngle, pivot = Offset(cx, cy)) {
            drawArc(
                color = PrimaryFixedDim.copy(alpha = 0.12f),
                startAngle = -45f,
                sweepAngle = 45f,
                useCenter = true,
                topLeft = Offset(cx - maxRadius, cy - maxRadius),
                size = Size(maxRadius * 2, maxRadius * 2)
            )
            drawLine(
                color = PrimaryFixedDim.copy(alpha = 0.6f),
                start = Offset(cx, cy),
                end = Offset(cx, cy - maxRadius),
                strokeWidth = 2f
            )
        }

        // Satellite blips
        satellites.forEach { sat ->
            if (sat.elevation > 0) {
                val elRad = Math.toRadians((90.0 - sat.elevation).toDouble()).toFloat()
                val azRad = Math.toRadians(sat.azimuth.toDouble()).toFloat()
                val r = maxRadius * sin(elRad)
                val dx = r * sin(azRad)
                val dy = -r * cos(azRad)

                val blipCx = cx + dx
                val blipCy = cy + dy

                // Outer glow
                drawCircle(
                    color = sat.constellation.color.copy(alpha = 0.15f),
                    radius = 8f,
                    center = Offset(blipCx, blipCy)
                )
                // Core dot
                drawCircle(
                    color = sat.constellation.color.copy(alpha = 0.9f),
                    radius = 3f,
                    center = Offset(blipCx, blipCy)
                )
            }
        }

        // Center dot
        drawCircle(
            color = PrimaryFixedDim,
            radius = 4f,
            center = Offset(cx, cy)
        )
    }
}

// ============================================================
// Satellite Inventory Table
// ============================================================

@Composable
private fun SatelliteTable(
    satellites: List<SatelliteInfo>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(TileBackground, RoundedCornerShape(12.dp))
            .border(1.dp, TileBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text("SATELLITE INVENTORY", style = LabelCaps, color = OnSurfaceVariant)
        Spacer(Modifier.height(8.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("SV#", style = CodeSm, color = OnSurfaceVariant, modifier = Modifier.width(44.dp))
            Text("SYS", style = CodeSm, color = OnSurfaceVariant, modifier = Modifier.width(60.dp))
            Text("EL", style = CodeSm, color = OnSurfaceVariant, modifier = Modifier.width(36.dp))
            Text("AZ", style = CodeSm, color = OnSurfaceVariant, modifier = Modifier.width(40.dp))
            Text("SNR", style = CodeSm, color = OnSurfaceVariant, modifier = Modifier.weight(0.3f))
            Text("LOCK", style = CodeSm, color = OnSurfaceVariant, modifier = Modifier.width(56.dp))
        }

        Spacer(Modifier.height(2.dp))

        if (satellites.isEmpty()) {
            Text(
                "Searching for satellites...",
                style = CodeSm,
                color = OnSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            val display = satellites.sortedByDescending { it.snr }.take(20)
            display.forEach { sat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${sat.constellation.label.first()}${sat.svid.toString().padStart(2, '0')}",
                        style = CodeSm,
                        color = OnSurfaceVariant,
                        modifier = Modifier.width(44.dp)
                    )
                    Text(
                        sat.constellation.label,
                        style = CodeSm,
                        color = sat.constellation.color,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(60.dp)
                    )
                    Text(
                        "${sat.elevation.toInt()}°",
                        style = CodeSm,
                        color = OnSurfaceVariant,
                        modifier = Modifier.width(36.dp)
                    )
                    Text(
                        "${sat.azimuth.toInt()}°",
                        style = CodeSm,
                        color = OnSurfaceVariant,
                        modifier = Modifier.width(40.dp)
                    )

                    // SNR mini bar
                    Canvas(
                        modifier = Modifier
                            .weight(0.3f)
                            .height(4.dp)
                    ) {
                        val ratio = (sat.snr / 50f).coerceIn(0f, 1f)
                        drawRect(
                            color = SurfaceContainerHighest
                        )
                        drawRect(
                            color = sat.constellation.color,
                            size = Size(size.width * ratio, size.height)
                        )
                    }

                    Text(
                        sat.lockStatus.label,
                        style = CodeSm,
                        color = when (sat.lockStatus) {
                            com.telemetrypro.app.data.LockStatus.LOCKED -> Secondary
                            com.telemetrypro.app.data.LockStatus.SYNCING -> PrimaryFixedDim
                            com.telemetrypro.app.data.LockStatus.SEARCHING -> OnSurfaceVariant
                        },
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(56.dp)
                    )
                }
            }
        }
    }
}
