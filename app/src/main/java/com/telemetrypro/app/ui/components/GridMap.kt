package com.telemetrypro.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telemetrypro.app.data.TrackPoint
import com.telemetrypro.app.ui.theme.*
import kotlin.math.*

/**
 * Tactical navigation grid map for track recording & backtracking.
 *
 * Pure-black background with subtle grid lines, designed for outdoor use
 * without any base map. Shows:
 * - Recorded path (yellow)
 * - Source/backtrack reference path (faded yellow)
 * - Active backtrack trail (green line + dots)
 * - Waypoints (orange diamonds with labels)
 * - Current position (white dot with glow)
 * - North indicator (static N marker at top)
 * - Scale bar
 * - Pinch-to-zoom + pan
 */
@Composable
fun GridMap(
    recordedPoints: List<TrackPoint> = emptyList(),
    waypoints: List<TrackPoint> = emptyList(),
    backtrackPoints: List<TrackPoint> = emptyList(),
    sourcePath: List<TrackPoint> = emptyList(),
    currentPosition: TrackPoint? = null,
    deviationMeters: Double = 0.0,
    offPath: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Pan & zoom state (local to this composable)
    var zoom by remember { mutableFloatStateOf(1f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp)
            .background(Color.Black, RoundedCornerShape(8.dp))
            .border(1.dp, OutlineVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    zoom = (zoom * gestureZoom).coerceIn(0.3f, 8f)
                    panX += pan.x
                    panY += pan.y
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(2.dp)) {
            val cw = size.width
            val ch = size.height
            val cx = cw / 2f
            val cy = ch / 2f

            // ===== 1. GRID LINES =====
            val gridSpacing = 40f * zoom
            val gridColor = Color(0xFF1A1A1A)  // very dark gray
            val gridColorMajor = Color(0xFF2A2A2A)

            var xStart = (panX % gridSpacing)
            if (xStart < 0) xStart += gridSpacing
            var gx = xStart
            while (gx < cw) {
                drawLine(
                    color = if ((gx / gridSpacing).toInt() % 5 == 0) gridColorMajor else gridColor,
                    start = Offset(gx, 0f),
                    end = Offset(gx, ch),
                    strokeWidth = 1f
                )
                gx += gridSpacing
            }
            var yStart = (panY % gridSpacing)
            if (yStart < 0) yStart += gridSpacing
            var gy = yStart
            while (gy < ch) {
                drawLine(
                    color = if ((gy / gridSpacing).toInt() % 5 == 0) gridColorMajor else gridColor,
                    start = Offset(0f, gy),
                    end = Offset(cw, gy),
                    strokeWidth = 1f
                )
                gy += gridSpacing
            }

            // ===== 2. COMPUTE PROJECTION =====
            // Combine all points to find bounding box
            val allPts = recordedPoints + waypoints + backtrackPoints + sourcePath +
                    (if (currentPosition != null) listOf(currentPosition) else emptyList())

            if (allPts.isEmpty()) {
                // No data — show "等待GPS信号" hint via parent composable
                return@Canvas
            }

            var minLat = Double.MAX_VALUE; var maxLat = -Double.MAX_VALUE
            var minLng = Double.MAX_VALUE; var maxLng = -Double.MAX_VALUE
            allPts.forEach {
                if (it.latitude < minLat) minLat = it.latitude
                if (it.latitude > maxLat) maxLat = it.latitude
                if (it.longitude < minLng) minLng = it.longitude
                if (it.longitude > maxLng) maxLng = it.longitude
            }

            val latRange = (maxLat - minLat).coerceAtLeast(0.0001)
            val lngRange = (maxLng - minLng).coerceAtLeast(0.0001)
            val latCenter = (minLat + maxLat) / 2.0
            val lngCenter = (minLng + maxLng) / 2.0
            val latRad = Math.toRadians(latCenter)

            // Scale: fit bounding box with padding
            val pad = 30f
            val availW = cw - pad * 2
            val availH = ch - pad * 2
            val metersPerDegLat = 111320.0
            val metersPerDegLng = 111320.0 * cos(latRad)
            val rangeMetersW = lngRange * metersPerDegLng
            val rangeMetersH = latRange * metersPerDegLat
            val baseScale = min(availW / rangeMetersW.toFloat(), availH / rangeMetersH.toFloat())
            val scale = baseScale * zoom

            fun project(lat: Double, lng: Double): Offset {
                val dLatM = (lat - latCenter) * metersPerDegLat
                val dLngM = (lng - lngCenter) * metersPerDegLng
                return Offset(
                    cx + (dLngM * scale).toFloat() + panX,
                    cy - (dLatM * scale).toFloat() + panY
                )
            }

            // ===== 3. SOURCE PATH (faded yellow, reference for backtrack) =====
            if (sourcePath.size >= 2) {
                val srcPath = Path().apply {
                    val p0 = project(sourcePath[0].latitude, sourcePath[0].longitude)
                    moveTo(p0.x, p0.y)
                    for (i in 1 until sourcePath.size) {
                        val p = project(sourcePath[i].latitude, sourcePath[i].longitude)
                        lineTo(p.x, p.y)
                    }
                }
                drawPath(srcPath, Color(0xFF8B7500).copy(alpha = 0.5f),
                         style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f))))
            }

            // ===== 4. RECORDED PATH (yellow solid) =====
            if (recordedPoints.size >= 2) {
                val recPath = Path().apply {
                    val p0 = project(recordedPoints[0].latitude, recordedPoints[0].longitude)
                    moveTo(p0.x, p0.y)
                    for (i in 1 until recordedPoints.size) {
                        val p = project(recordedPoints[i].latitude, recordedPoints[i].longitude)
                        lineTo(p.x, p.y)
                    }
                }
                drawPath(recPath, Color(0xFFFFC107), style = Stroke(width = 3f))

                // Start point marker
                val sp = project(recordedPoints[0].latitude, recordedPoints[0].longitude)
                drawCircle(Color(0xFFFFC107).copy(alpha = 0.3f), 8f, sp)
                drawCircle(Color(0xFFFFC107), 4f, sp)
            }

            // ===== 5. BACKTRACK TRAIL (green line + dots) =====
            if (backtrackPoints.isNotEmpty()) {
                if (backtrackPoints.size >= 2) {
                    val btPath = Path().apply {
                        val p0 = project(backtrackPoints[0].latitude, backtrackPoints[0].longitude)
                        moveTo(p0.x, p0.y)
                        for (i in 1 until backtrackPoints.size) {
                            val p = project(backtrackPoints[i].latitude, backtrackPoints[i].longitude)
                            lineTo(p.x, p.y)
                        }
                    }
                    drawPath(btPath, Color(0xFF4CAF50), style = Stroke(width = 3f))
                }
                // Dots at each backtrack point
                backtrackPoints.forEach { pt ->
                    val p = project(pt.latitude, pt.longitude)
                    drawCircle(Color(0xFF4CAF50).copy(alpha = 0.6f), 3f, p)
                }
            }

            // ===== 6. WAYPOINTS (orange diamonds with labels) =====
            waypoints.forEach { wp ->
                val p = project(wp.latitude, wp.longitude)
                val diamondSize = 7f
                val diamond = Path().apply {
                    moveTo(p.x, p.y - diamondSize)
                    lineTo(p.x + diamondSize, p.y)
                    lineTo(p.x, p.y + diamondSize)
                    lineTo(p.x - diamondSize, p.y)
                    close()
                }
                drawPath(diamond, Color(0xFFFF6B35))
                drawPath(diamond, Color(0xFFFF6B35).copy(alpha = 0.3f), style = Stroke(width = 6f))

                // Label
                val labelPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(255, 255, 200, 100)
                    textSize = 11f
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.LEFT
                    isFakeBoldText = true
                }
                drawContext.canvas.nativeCanvas.drawText(
                    wp.waypointLabel, p.x + diamondSize + 3f, p.y + 4f, labelPaint
                )
            }

            // ===== 7. CURRENT POSITION (white dot with glow) =====
            currentPosition?.let { pos ->
                val p = project(pos.latitude, pos.longitude)
                // Glow
                drawCircle(Color.White.copy(alpha = 0.15f), 12f, p)
                drawCircle(Color.White.copy(alpha = 0.3f), 8f, p)
                // Solid
                drawCircle(Color.White, 5f, p)
                // Off-path warning ring
                if (offPath) {
                    drawCircle(Color(0xFFFF4444).copy(alpha = 0.5f), 16f, p,
                               style = Stroke(width = 2f))
                }
            }

            // ===== 8. NORTH INDICATOR (top-right) =====
            val nPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(255, 255, 107, 53)
                textSize = 16f
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
            }
            val arrowPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(255, 255, 107, 53)
                isAntiAlias = true
                style = android.graphics.Paint.Style.FILL
            }
            val nX = cw - 24f
            val nY = 24f
            val arrowPath = android.graphics.Path().apply {
                moveTo(nX, nY - 10f)
                lineTo(nX - 6f, nY + 2f)
                lineTo(nX + 6f, nY + 2f)
                close()
            }
            drawContext.canvas.nativeCanvas.drawPath(arrowPath, arrowPaint)
            drawContext.canvas.nativeCanvas.drawText("N", nX, nY + 16f, nPaint)

            // ===== 9. SCALE BAR (bottom-left) =====
            val mPerPx = rangeMetersW / (availW.toDouble() / zoom.toDouble()).coerceAtLeast(0.01)
            val scaleBarMeters = computeNiceScaleMeters(mPerPx)
            val scaleBarPx = (scaleBarMeters / mPerPx).toFloat().coerceAtMost(availW * 0.3f)
            val sbY = ch - 20f
            val sbX = 20f
            drawLine(Color.White, Offset(sbX, sbY), Offset(sbX + scaleBarPx, sbY), strokeWidth = 2f)
            drawLine(Color.White, Offset(sbX, sbY - 4f), Offset(sbX, sbY + 4f), strokeWidth = 2f)
            drawLine(Color.White, Offset(sbX + scaleBarPx, sbY - 4f), Offset(sbX + scaleBarPx, sbY + 4f), strokeWidth = 2f)
            val scalePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(220, 255, 255, 255)
                textSize = 10f
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.LEFT
            }
            val scaleLabel = if (scaleBarMeters >= 1000) "${scaleBarMeters / 1000}km" else "${scaleBarMeters}m"
            drawContext.canvas.nativeCanvas.drawText(scaleLabel, sbX, sbY - 6f, scalePaint)
        }

        // Off-path warning overlay (top center)
        if (offPath) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .background(Color(0xFFFF4444).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFFFF4444), RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("⚠ 偏离路径 ${String.format("%.0f", deviationMeters)}m",
                     color = Color(0xFFFF6666), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** Compute a "nice" scale bar length (10, 20, 50, 100, 200, 500, 1km...) */
private fun computeNiceScaleMeters(metersPerPixel: Double): Int {
    val targetPx = 80.0
    val targetMeters = targetPx * metersPerPixel
    val candidates = intArrayOf(5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000)
    for (c in candidates) {
        if (c >= targetMeters) return c
    }
    return 10000
}

/**
 * Compact info bar shown below the grid map.
 * Left: GPS coordinates | Right: speed + distance
 */
@Composable
fun GridInfoBar(
    coordText: String = "---",
    speedText: String = "",
    distanceText: String = "",
    waypointCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: coordinates
        Column {
            Text(coordText, style = CodeSm, color = PrimaryFixedDim, fontWeight = FontWeight.Bold)
            if (waypointCount > 0) {
                Text("标记点: $waypointCount", style = CodeSm, color = Color(0xFFFF6B35))
            }
        }

        // Right: speed + distance
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (speedText.isNotEmpty()) {
                Text(speedText, style = CodeSm, color = OnSurfaceVariant)
            }
            if (distanceText.isNotEmpty()) {
                Text(distanceText, style = TelemetryMd, color = Secondary)
            }
        }
    }
}
