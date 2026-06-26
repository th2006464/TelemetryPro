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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.telemetrypro.app.data.TrackPoint
import com.telemetrypro.app.ui.theme.*
import kotlin.math.*

private data class CardinalInfo(
    val chinese: String,
    val english: String,
    val deg: Float,
    val isNorth: Boolean
)

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

            val rotDeg = -azimuth
            val canvasObj = drawContext.canvas

            drawCircle(
                color = OnSurfaceVariant.copy(alpha = 0.12f),
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5f)
            )

            withTransform({
                rotate(rotDeg, pivot = Offset(cx, cy))
            }) {
            for (i in 1..4) {
                val r = radius * i / 5f
                drawCircle(
                    color = OnSurfaceVariant.copy(alpha = 0.08f),
                    radius = r,
                    center = Offset(cx, cy),
                    style = Stroke(width = if (i == 5) 1.5f else 0.8f)
                )
            }

            val lineLen = radius
            drawLine(
                color = OnSurfaceVariant.copy(alpha = 0.15f),
                start = Offset(cx, cy - lineLen),
                end = Offset(cx, cy + lineLen),
                strokeWidth = 1.0f
            )
            drawLine(
                color = OnSurfaceVariant.copy(alpha = 0.15f),
                start = Offset(cx - lineLen, cy),
                end = Offset(cx + lineLen, cy),
                strokeWidth = 1.0f
            )

            // --- Cardinal directions with Chinese + English labels ---
            val cardinals = listOf(
                CardinalInfo("北", "N", 0f, true),
                CardinalInfo("", "", 22.5f, false),
                CardinalInfo("东北", "NE", 45f, false),
                CardinalInfo("", "", 67.5f, false),
                CardinalInfo("东", "E", 90f, false),
                CardinalInfo("", "", 112.5f, false),
                CardinalInfo("东南", "SE", 135f, false),
                CardinalInfo("", "", 157.5f, false),
                CardinalInfo("南", "S", 180f, false),
                CardinalInfo("", "", 202.5f, false),
                CardinalInfo("西南", "SW", 225f, false),
                CardinalInfo("", "", 247.5f, false),
                CardinalInfo("西", "W", 270f, false),
                CardinalInfo("", "", 292.5f, false),
                CardinalInfo("西北", "NW", 315f, false),
                CardinalInfo("", "", 337.5f, false),
            )

            val labelPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(220, 208, 198, 171)
                textSize = 22f
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
            }
            val labelPaintHighlight = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(255, 239, 108, 68)
                textSize = 25f
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
            }
            val labelEnPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(180, 208, 198, 171)
                textSize = 12f
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
            }
            val labelEnPaintHighlight = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(230, 239, 108, 68)
                textSize = 14f
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
            }

            for (info in cardinals) {
                val angleRad = Math.toRadians(info.deg.toDouble())
                val sinA = sin(angleRad).toFloat()
                val cosA = cos(angleRad).toFloat()

                val tickInner = radius * 0.76f
                val tickOuter = radius * 0.96f
                val x1 = cx + tickInner * sinA
                val y1 = cy - tickInner * cosA
                val x2 = cx + tickOuter * sinA
                val y2 = cy - tickOuter * cosA

                val tickColor = if (info.isNorth) PrimaryFixedDim.copy(alpha = 0.9f)
                                else OnSurfaceVariant.copy(alpha = 0.4f)
                val tickWidth = if (info.isNorth) 2.8f else 1.5f
                drawLine(tickColor, Offset(x1, y1), Offset(x2, y2), strokeWidth = tickWidth)

                if (info.chinese.isNotEmpty()) {
                    val labelR = radius * 0.58f
                    val lx = cx + labelR * sinA
                    val ly = cy - labelR * cosA

                    val chPaint = if (info.isNorth) labelPaintHighlight else labelPaint
                    val enPaint = if (info.isNorth) labelEnPaintHighlight else labelEnPaint

                    canvasObj.nativeCanvas.drawText(info.chinese, lx, ly + 6f, chPaint)
                    canvasObj.nativeCanvas.drawText(info.english, lx, ly + 6f + 24f + 3f, enPaint)
                }
            }

            // --- Degree markings: 5 deg small, 10 deg medium ---
            for (deg in 0 until 360 step 5) {
                if (deg % 45 == 0) continue
                val angleRad = Math.toRadians(deg.toDouble())
                val sinA = sin(angleRad).toFloat()
                val cosA = cos(angleRad).toFloat()

                val is10Deg = (deg % 10 == 0)
                val tIn = radius * if (is10Deg) 0.84f else 0.91f
                val tOut = radius * 0.96f
                val tx1 = cx + tIn * sinA
                val ty1 = cy - tIn * cosA
                val tx2 = cx + tOut * sinA
                val ty2 = cy - tOut * cosA

                val alpha = if (is10Deg) 0.35f else 0.18f
                val strokeW = if (is10Deg) 1.4f else 0.9f
                drawLine(
                    OnSurfaceVariant.copy(alpha = alpha),
                    Offset(tx1, ty1), Offset(tx2, ty2),
                    strokeWidth = strokeW
                )
            }

            // --- Trajectory ---
            if (points.size >= 2) {
                val current = points.last()
                val latRad = Math.toRadians(current.latitude)
                val metersPerPixel = max(0.3f, radius / 120f)

                fun project(lat: Double, lng: Double): Offset {
                    val dLatM = (lat - current.latitude) * 111320.0
                    val dLngM = (lng - current.longitude) * 111320.0 * cos(latRad)
                    return Offset(
                        cx + (dLngM / metersPerPixel).toFloat(),
                        cy - (dLatM / metersPerPixel).toFloat()
                    )
                }

                val visibleOffsets = points.map { project(it.latitude, it.longitude) }

                val path = Path().apply {
                    moveTo(visibleOffsets[0].x, visibleOffsets[0].y)
                    for (i in 1 until visibleOffsets.size) {
                        lineTo(visibleOffsets[i].x, visibleOffsets[i].y)
                    }
                }
                drawPath(path, Color(0xFFFFC107), style = Stroke(width = 3f))

                val startPos = visibleOffsets.first()
                drawCircle(Color(0xFFFFC107).copy(alpha = 0.25f), 8f, startPos)
                drawCircle(Color(0xFFFFC107), 4f, startPos)
            }

            // --- Center dot ---
            if (showCenterDot) {
                drawCircle(PrimaryFixedDim.copy(alpha = 0.15f), 10f, Offset(cx, cy))
                drawCircle(Color.White, 6f, Offset(cx, cy))
                drawCircle(PrimaryFixedDim.copy(alpha = 0.6f), 2.5f, Offset(cx, cy))
            }

            } // END withTransform

            // --- North indicator arrow ---
            val triSize = 24f
            val triCy = cy - radius + 22f
            val northPath = Path().apply {
                moveTo(cx, triCy - triSize)
                lineTo(cx - triSize * 0.6f, triCy + triSize * 0.4f)
                lineTo(cx + triSize * 0.6f, triCy + triSize * 0.4f)
                close()
            }
            drawPath(northPath, Color(0xFFE86A33))

            val nLabelPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(255, 232, 106, 51)
                textSize = 12f
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
            }
            canvasObj.nativeCanvas.drawText("N", cx, triCy + triSize + 12f, nLabelPaint)
        }
    }
}

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

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row {
                Text("\u26F0", color = OnSurfaceVariant.copy(alpha = 0.6f))
                Spacer(Modifier.width(4.dp))
                Text(altitudeText, style = TelemetryMd, color = OnSurfaceVariant)
            }
            if (speedText.isNotEmpty()) {
                Text(speedText, style = CodeSm, color = OnSurfaceVariant.copy(alpha = 0.6f))
            }
        }

        Spacer(Modifier.weight(1f))

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
                Text("\u25CF", style = LabelCaps, color = Secondary)
            }
        }
    }
}
