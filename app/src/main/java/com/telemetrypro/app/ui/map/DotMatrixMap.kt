package com.telemetrypro.app.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telemetrypro.app.R
import com.telemetrypro.app.data.TrackPoint
import com.telemetrypro.app.ui.theme.*
import kotlin.math.*

@Composable
fun DotMatrixMap(
    latitude: Double,
    longitude: Double,
    isFixed: Boolean,
    showCities: Boolean = true,
    modifier: Modifier = Modifier,
    mapLabel: String = "",
    trackPoints: List<TrackPoint> = emptyList()
) {
    var scale by remember { mutableStateOf(1.3f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val pulseRadius = 6f
    val pulseAlpha = 0.35f

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .background(Surface, RoundedCornerShape(12.dp))
            .border(1.dp, SurfaceVariant, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (mapLabel.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceVariant.copy(alpha = 0.3f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = mapLabel, style = LabelCaps, color = OnSurfaceVariant)
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp)
                    .clipToBounds()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 5f)
                            offsetX += pan.x
                            offsetY += pan.y
                        }
                    }
            ) {
                val canvasW = size.width
                val canvasH = size.height
                if (canvasW <= 0f || canvasH <= 0f) return@Canvas

                try {
                    val maxLatRad = (85.0 * Math.PI / 180.0).toFloat()
                    val mercMax = ln(tan(Math.PI.toFloat() / 4f + maxLatRad / 2f))

                    fun mercatorY(latDeg: Double): Float {
                        val latRad = (latDeg * Math.PI / 180.0).toFloat().coerceIn(-maxLatRad, maxLatRad)
                        val yMerc = ln(tan(Math.PI.toFloat() / 4f + latRad / 2f))
                        return (60f * (1f - yMerc / mercMax) / 2f)
                    }

                    val cellW = canvasW / 126f * scale
                    val gridRatio = canvasH / 60f / (canvasW / 126f)
                    val cellH = cellW * gridRatio

                    val gridStartX = offsetX % cellW
                    val gridStartY = offsetY % cellH

                    val colStart = max(0, ((-gridStartX / cellW).toInt() - 1).coerceIn(0, 125))
                    val colEnd = min(125, ((canvasW - gridStartX) / cellW).toInt() + 1)
                    val rowStart = max(0, ((-gridStartY / cellH).toInt() - 1).coerceIn(0, 59))
                    val rowEnd = min(59, ((canvasH - gridStartY) / cellH).toInt() + 1)

                    if (colStart > colEnd || rowStart > rowEnd) return@Canvas

                    // Helper to convert lat/lng to canvas coordinates
                    fun latLngToCanvas(lat: Double, lng: Double): Offset {
                        val gx = ((lng + 180.0) / 360.0 * 126.0).toFloat()
                        val gy = mercatorY(lat)
                        return Offset(
                            gridStartX + gx * cellW + cellW / 2,
                            gridStartY + gy * cellH + cellH / 2
                        )
                    }

                    // ---- Batch-render land dots ----
                    val dotRadius = (cellW * 0.22f).coerceIn(1f, 4f)
                    val dotColor = OnSurfaceVariant.copy(alpha = 0.45f)

                    val landPoints = buildList(capacity = 500) {
                        for (y in rowStart..rowEnd) {
                            for (x in colStart..colEnd) {
                                if (WorldMapGrid.isLand(x, y)) {
                                    add(Offset(
                                        gridStartX + x * cellW + cellW / 2,
                                        gridStartY + y * cellH + cellH / 2
                                    ))
                                }
                            }
                        }
                    }

                    if (landPoints.isNotEmpty()) {
                        drawPoints(landPoints, PointMode.Points, dotColor, dotRadius * 2f, StrokeCap.Round)
                    }

                    // ---- Track recording path ----
                    if (trackPoints.size >= 2) {
                        val trackOffsets = trackPoints.map { pt ->
                            latLngToCanvas(pt.latitude, pt.longitude)
                        }
                        // Path line
                        val trackPath = Path().apply {
                            moveTo(trackOffsets[0].x, trackOffsets[0].y)
                            for (i in 1 until trackOffsets.size) {
                                lineTo(trackOffsets[i].x, trackOffsets[i].y)
                            }
                        }
                        drawPath(trackPath, Secondary.copy(alpha = 0.5f), style = Stroke(width = 1.5f))
                        // Start/end markers
                        drawCircle(Secondary.copy(alpha = 0.8f), 4f, trackOffsets.first())
                        drawCircle(PrimaryFixedDim, 4f, trackOffsets.last())
                    }

                    // ---- City labels ----
                    if (showCities && scale >= 0.8f) {
                        for (city in WorldMapData.majorCities) {
                            val cityPx = latLngToCanvas(city.lat.toDouble(), city.lon.toDouble())
                            val cityPy = cityPx.y
                            val cityX = cityPx.x

                            if (cityX in -50f..(canvasW + 50f) && cityPy in -50f..(canvasH + 50f)) {
                                val alpha = when {
                                    scale < 1.0f -> 0.3f
                                    scale < 1.5f -> 0.5f
                                    else -> 0.7f
                                }
                                drawCircle(
                                    color = OnSurfaceVariant.copy(alpha = alpha),
                                    radius = 2f,
                                    center = Offset(cityX, cityPy),
                                    style = Stroke(1f)
                                )
                                if (scale >= 1.3f) {
                                    val tl = textMeasurer.measure(
                                        text = city.name,
                                        style = TextStyle(
                                            fontSize = (9f / scale).coerceIn(6f, 11f).sp,
                                            color = OnSurfaceVariant.copy(alpha = alpha),
                                            fontFamily = FontFamily.Default
                                        )
                                    )
                                    drawText(tl, topLeft = Offset(cityX - tl.size.width / 2f, cityPy + 4f))
                                }
                            }
                        }
                    }

                    // ---- GPS position marker ----
                    if (isFixed && latitude != 0.0 && longitude != 0.0) {
                        val posX = latLngToCanvas(latitude, longitude).x
                        val posY = latLngToCanvas(latitude, longitude).y

                        drawCircle(PrimaryFixed.copy(alpha = pulseAlpha), pulseRadius, Offset(posX, posY))
                        drawCircle(Primary, 5f, Offset(posX, posY))
                        drawCircle(PrimaryFixed, 2.5f, Offset(posX, posY))

                        val crossLen = 10f
                        val crossColor = PrimaryFixed.copy(alpha = 0.6f)
                        drawLine(crossColor, Offset(posX - crossLen, posY), Offset(posX - 3f, posY), 1f)
                        drawLine(crossColor, Offset(posX + 3f, posY), Offset(posX + crossLen, posY), 1f)
                        drawLine(crossColor, Offset(posX, posY - crossLen), Offset(posX, posY - 3f), 1f)
                        drawLine(crossColor, Offset(posX, posY + 3f), Offset(posX, posY + crossLen), 1f)

                        if (scale >= 0.8f) {
                            val coordText = String.format("%.2f\u00B0, %.2f\u00B0", latitude, longitude)
                            val tl = textMeasurer.measure(
                                text = coordText,
                                style = TextStyle(
                                    fontSize = (9f / scale).coerceIn(7f, 12f).sp,
                                    color = Primary,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                            drawText(tl, topLeft = Offset(posX - tl.size.width / 2f, posY - 18f))
                        }
                    }
                } catch (_: Exception) { }
            }

            if (scale < 1.5f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceVariant.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.map_pinch_hint),
                        style = CodeSm,
                        color = OnSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
