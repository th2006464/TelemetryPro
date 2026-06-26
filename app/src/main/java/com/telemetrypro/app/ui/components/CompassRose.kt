package com.telemetrypro.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telemetrypro.app.data.TrackPoint
import com.telemetrypro.app.ui.theme.*
import kotlin.math.*

/**
 * A full-screen compass rose display for track recording/playback.
 *
 * Shows:
 * - Rotating compass rose with Chinese cardinal labels (北东南西+偏方向)
 * - Concentric range rings
 * - Green trajectory trail
 * - Center position marker (white dot)
 * - Red north indicator triangle (always pointing up = phone's heading = North)
 *
 * @param points Track points to draw as trajectory (empty = no trail)
 * @param azimuth Device compass heading in degrees (0=N, 90=E). Used to rotate rose.
 * @param showCenterDot Whether to show center position dot
 * @param modifier Modifier
 */
@Composable
fun CompassRose(
    points: List<TrackPoint> = emptyList(),
    azimuth: Float = 0f,
    showCenterDot: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(SurfaceContainerLowest, CircleShape)
            .border(1.5.dp, OutlineVariant.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            val cw = size.width
            val ch = size.height
            val cx = cw / 2f
            val cy = ch / 2f
            val radius = min(cx, cy) - 4f

            // --- Rotate everything so that "North" (azimuth=0) always points UP ---
            // The canvas rotates by -azimuth so that when device turns right (azimuth increases),
            // the compass rose rotates left, keeping the north mark at screen-top.
            val rotDeg = -azimuth

            // ===== 1. OUTER COMPASS RING =====
            drawCircle(
                color = OnSurfaceVariant.copy(alpha = 0.12f),
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5f)
            )

            // --- Rotate compass rose content (rings, ticks, labels, trajectory) by azimuth ---
            // The north indicator arrow stays FIXED (outside this transform)
            val canvasObj = drawContext.canvas

            withTransform({
                rotate(rotDeg, pivot = Offset(cx, cy))
            }) {
            // ===== 2. CONCENTRIC RANGE RINGS (4 rings) =====
            for (i in 1..4) {
                val r = radius * i / 5f
                drawCircle(
                    color = OnSurfaceVariant.copy(alpha = 0.08f),
                    radius = r,
                    center = Offset(cx, cy),
                    style = Stroke(width = if (i == 5) 1.5f else 0.8f)
                )
            }

            // ===== 3. CROSS HAIR (N-S & E-W lines through center) =====
            // These rotate with the compass
            val lineLen = radius
            drawLine(
                color = OnSurfaceVariant.copy(alpha = 0.15f),
                start = Offset(cx, cy - lineLen),
                end = Offset(cx, cy + lineLen),
                strokeWidth = 0.8f
            )
            drawLine(
                color = OnSurfaceVariant.copy(alpha = 0.15f),
                start = Offset(cx - lineLen, cy),
                end = Offset(cx + lineLen, cy),
                strokeWidth = 0.8f
            )

            // ===== 4. CARDINAL DIRECTION TICKS & LABELS =====
            // 8 main directions: N, NE, E, SE, S, SW, W, NW
            val cardinals = listOf(
                Triple("北", 0f, true),       // North - highlighted
                Triple("", 22.5f, false),     // NNE
                Triple("东北", 45f, false),   // NE
                Triple("", 67.5f, false),     // ENE
                Triple("东", 90f, false),      // East
                Triple("", 112.5f, false),    // ESE
                Triple("东南", 135f, false),   // SE
                Triple("", 157.5f, false),    // SSE
                Triple("南", 180f, false),     // South
                Triple("", 202.5f, false),    // SSW
                Triple("西南", 225f, false),   // SW
                Triple("", 247.5f, false),    // WSW
                Triple("西", 270f, false),     // West
                Triple("", 292.5f, false),    // WNW
                Triple("西北", 315f, false),   // NW
                Triple("", 337.5f, false),    // NNW
            )

            val labelPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(200, 208, 198, 171)
                textSize = 16f
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
            }
            val labelPaintHighlight = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(255, 239, 108, 68)  // Safety Orange for North
                textSize = 18f
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
            }

            for ((label, deg, isNorth) in cardinals) {
                val angleRad = Math.toRadians(deg.toDouble())
                // Direction angle in compass coordinates (clockwise from North/Up)
                // Screen coords: Y down, so North(0°) = -Y direction
                val tickInner = radius * 0.82f
                val tickOuter = radius * 0.96f
                val x1 = cx + (tickInner * sin(angleRad)).toFloat()
                val y1 = cy - (tickInner * cos(angleRad)).toFloat()
                val x2 = cx + (tickOuter * sin(angleRad)).toFloat()
                val y2 = cy - (tickOuter * cos(angleRad)).toFloat()

                // Draw tick
                val tickColor = if (isNorth) PrimaryFixedDim.copy(alpha = 0.8f)
                                else OnSurfaceVariant.copy(alpha = 0.35f)
                val tickWidth = if (isNorth) 2.5f else 1.2f
                drawLine(tickColor, Offset(x1, y1), Offset(x2, y2), strokeWidth = tickWidth)

                // Draw label
                if (label.isNotEmpty()) {
                    val labelR = radius * 0.68f
                    val lx = cx + (labelR * sin(angleRad)).toFloat()
                    val ly = cy - (labelR * cos(angleRad)).toFloat()
                    val paint = if (isNorth) labelPaintHighlight else labelPaint
                    canvasObj.nativeCanvas.drawText(label, lx, ly + 4f, paint)
                }
            }

            // ===== 5. DEGREE MARKINGS (every 10° small ticks) =====
            for (deg in 0 until 360 step 10) {
                if (deg % 45 == 0) continue // already drawn as cardinal
                val angleRad = Math.toRadians(deg.toDouble())
                val tIn = radius * 0.92f
                val tOut = radius * 0.96f
                val tx1 = cx + (tIn * sin(angleRad)).toFloat()
                val ty1 = cy - (tIn * cos(angleRad)).toFloat()
                val tx2 = cx + (tOut * sin(angleRad)).toFloat()
                val ty2 = cy - (tOut * cos(angleRad)).toFloat()
                drawLine(
                    OnSurfaceVariant.copy(alpha = 0.2f),
                    Offset(tx1, ty1), Offset(tx2, ty2),
                    strokeWidth = 0.8f
                )
            }

            // ===== 6. TRAJECTORY / PATH LINE =====
            // Project lat/lng onto compass plane centered on current (or last) position
            if (points.size >= 2) {
                // Use last point as center (current position)
                val current = points.last()
                val latRad = Math.toRadians(current.latitude)

                // Scale: show ~200m around current position
                val metersPerPixel = max(0.3f, radius / 120f) // adaptive scale

                fun project(lat: Double, lng: Double): Offset {
                    val dLatM = (lat - current.latitude) * 111320.0
                    val dLngM = (lng - current.longitude) * 111320.0 * cos(latRad)
                    return Offset(
                        cx + (dLngM / metersPerPixel).toFloat(),
                        cy - (dLatM / metersPerPixel).toFloat()
                    )
                }

                val visibleOffsets = points.map { project(it.latitude, it.longitude) }

                // Draw path
                val path = Path().apply {
                    moveTo(visibleOffsets[0].x, visibleOffsets[0].y)
                    for (i in 1 until visibleOffsets.size) {
                        lineTo(visibleOffsets[i].x, visibleOffsets[i].y)
                    }
                }
                drawPath(path, Color(0xFFFFC107), style = Stroke(width = 3f))

                // Start point
                val startPos = visibleOffsets.first()
                drawCircle(Color(0xFFFFC107).copy(alpha = 0.25f), 8f, startPos)
                drawCircle(Color(0xFFFFC107), 4f, startPos)
            }

            // ===== 7. CENTER DOT (current position) =====
            if (showCenterDot) {
                // Outer glow
                drawCircle(PrimaryFixedDim.copy(alpha = 0.15f), 10f, Offset(cx, cy))
                // White center
                drawCircle(Color.White, 6f, Offset(cx, cy))
                // Inner dot
                drawCircle(PrimaryFixedDim.copy(alpha = 0.6f), 2.5f, Offset(cx, cy))
            }

            } // END withTransform (rotate)

            // ===== 8. NORTH INDICATOR (direction arrow — fixed at TOP, does NOT rotate) =====
            // Large prominent heading arrow showing current phone direction
            val triSize = 24f
            val triCy = cy - radius + 22f
            val northPath = Path().apply {
                moveTo(cx, triCy - triSize)
                lineTo(cx - triSize * 0.6f, triCy + triSize * 0.4f)
                lineTo(cx + triSize * 0.6f, triCy + triSize * 0.4f)
                close()
            }
            // Solid arrow (large, prominent)
            drawPath(northPath, Color(0xFFE86A33)) // Safety Orange-Red

            // N label under triangle
            val nLabelPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(255, 232, 106, 51)
                textSize = 12f
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
            }
            canvasObj.nativeCanvas.drawText("N", cx, triCy + triSize + 12f, nLabelPaint)
        }

        // Overlay info: top-right time area (for external composable to fill)
        // Bottom info bar is handled by parent
    }
}

/**
 * Compact info bar shown below the compass rose.
 */
@Composable
fun CompassInfoBar(
    bearingText: String,
    altitudeText: String,
    speedText: String = "",
    distanceText: String = "",
    isRecording: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: direction/bearing
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(SurfaceContainerLowest, CircleShape)
                .border(1.dp, OutlineVariant.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(bearingText, style = TelemetryMd, color = PrimaryFixedDim)
        }

        Spacer(Modifier.weight(1f))

        // Center: altitude + optional speed
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row {
                Text("\u26F0", color = OnSurfaceVariant.copy(alpha = 0.6f)) // mountain
                Spacer(Modifier.width(4.dp))
                Text(altitudeText, style = TelemetryMd, color = OnSurfaceVariant)
            }
            if (speedText.isNotEmpty()) {
                Text(speedText, style = CodeSm, color = OnSurfaceVariant.copy(alpha = 0.6f))
            }
        }

        Spacer(Modifier.weight(1f))

        // Right: distance or record button placeholder
        if (distanceText.isNotEmpty()) {
            Text(distanceText, style = TelemetryMd, color = Secondary)
        } else if (isRecording) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Secondary.copy(alpha = 0.2f), CircleShape)
                    .border(1.5.dp, Secondary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("\u25CF", style = LabelCaps, color = Secondary) // recording dot
            }
        }
    }
}
