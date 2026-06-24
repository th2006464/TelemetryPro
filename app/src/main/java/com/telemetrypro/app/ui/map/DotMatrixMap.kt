package com.telemetrypro.app.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.input.pointer.pointerInput
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
    trackPoints: List<TrackPoint> = emptyList(),
    showFullscreenButton: Boolean = false,
    onFullscreenClick: (() -> Unit)? = null,
    isFullscreen: Boolean = false
) {
    var scale by remember { mutableStateOf(1.3f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var initialized by remember { mutableStateOf(false) }
    var centeredOnFix by remember { mutableStateOf(false) }

    val pulseRadius = 6f
    val pulseAlpha = 0.35f
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .background(Surface, RoundedCornerShape(if (isFullscreen) 0.dp else 12.dp))
            .then(
                if (isFullscreen)
                    Modifier
                else
                    Modifier.border(1.dp, SurfaceVariant, RoundedCornerShape(12.dp))
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Title label (hidden in fullscreen)
            if (mapLabel.isNotEmpty() && !isFullscreen) {
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
                    .then(
                        if (isFullscreen) Modifier.fillMaxSize()
                        else Modifier.fillMaxWidth().weight(1f).padding(8.dp)
                    )
                    .clipToBounds()
                    .pointerInput(Unit) {
                        // Standard detectTransformGestures: single-finger pan + anchored pinch-zoom.
                        // The anchored zoom formula below compensates for mapLeft/mapTop changes
                        // when scale changes, so the pinch centroid stays stationary on the map.
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            val oldScale = scale
                            val newScale = (oldScale * zoom).coerceIn(0.5f, 5f)
                            val actualZoom = newScale / oldScale

                            val worldAspect = 126f / 60f
                            val cw = size.width.toFloat()
                            val ch = size.height.toFloat()
                            val canvasAspect = cw / ch

                            // Compute mapLeft/mapTop for old and new scales
                            fun mapDims(s: Float): Triple<Float, Float, Float> {
                                val (mw, mh) = if (canvasAspect > worldAspect) {
                                    val h = ch * s; h * worldAspect to h
                                } else {
                                    val w = cw * s; w to w / worldAspect
                                }
                                return Triple((cw - mw) / 2f, (ch - mh) / 2f, mw)
                            }
                            val (oldLeft, oldTop, _) = mapDims(oldScale)
                            val (newLeft, newTop, _) = mapDims(newScale)

                            if (actualZoom != 1f) {
                                // Correct anchored zoom: compensate for mapLeft/mapTop shift
                                offsetX = centroid.x - newLeft - actualZoom * (centroid.x - oldLeft - offsetX)
                                offsetY = centroid.y - newTop - actualZoom * (centroid.y - oldTop - offsetY)
                            }
                            offsetX += pan.x
                            offsetY += pan.y
                            scale = newScale
                        }
                    }
            ) {
                val canvasW = size.width
                val canvasH = size.height
                if (canvasW <= 0f || canvasH <= 0f) return@Canvas

                try {
                    // ---- Mercator projection constants ----
                    val maxLatRad = (85.0 * Math.PI / 180.0).toFloat()
                    val mercMax = ln(tan(Math.PI.toFloat() / 4f + maxLatRad / 2f))

                    fun mercatorY(latDeg: Double): Float {
                        val latRad = (latDeg * Math.PI / 180.0).toFloat().coerceIn(-maxLatRad, maxLatRad)
                        val yMerc = ln(tan(Math.PI.toFloat() / 4f + latRad / 2f))
                        return (60f * (1f - yMerc / mercMax) / 2f)
                    }

                    // ---- Fixed aspect ratio: maintain 126:60 proportions, letterbox extra ----
                    val worldAspect = 126f / 60f
                    val canvasAspect = canvasW / canvasH

                    val mapW: Float
                    val mapH: Float

                    if (canvasAspect > worldAspect) {
                        mapH = canvasH * scale
                        mapW = mapH * worldAspect
                    } else {
                        mapW = canvasW * scale
                        mapH = mapW / worldAspect
                    }

                    val mapLeft = (canvasW - mapW) / 2f
                    val mapTop = (canvasH - mapH) / 2f
                    val cellW = mapW / 126f
                    val cellH = mapH / 60f

                    // ---- Initialize offset to center on GPS position (or China fallback) ----
                    val shouldCenter = !initialized ||
                        (isFixed && !centeredOnFix && latitude != 0.0 && longitude != 0.0)
                    if (shouldCenter) {
                        val cLat: Double
                        val cLng: Double
                        if (isFixed && latitude != 0.0 && longitude != 0.0) {
                            cLat = latitude
                            cLng = longitude
                        } else {
                            cLat = 35.0   // China center fallback
                            cLng = 105.0
                        }
                        val cGx = ((cLng + 180.0) / 360.0 * 126.0).toFloat()
                        val cGy = mercatorY(cLat)
                        offsetX = canvasW / 2f - mapLeft - cGx * cellW
                        offsetY = canvasH / 2f - mapTop - cGy * cellH
                        initialized = true
                        if (isFixed) centeredOnFix = true
                    }

                    // ---- Grid origin on canvas (NO modulo — raw offset) ----
                    val gridStartX = mapLeft + offsetX
                    val gridStartY = mapTop + offsetY

                    // ---- Visible grid bounds ----
                    val colStart = max(0, ((-gridStartX / cellW).toInt() - 1).coerceIn(0, 125))
                    val colEnd = min(125, ((canvasW - gridStartX) / cellW).toInt() + 1)
                    val rowStart = max(0, ((-gridStartY / cellH).toInt() - 1).coerceIn(0, 59))
                    val rowEnd = min(59, ((canvasH - gridStartY) / cellH).toInt() + 1)

                    fun latLngToCanvas(lat: Double, lng: Double): Offset {
                        val gx = ((lng + 180.0) / 360.0 * 126.0).toFloat()
                        val gy = mercatorY(lat)
                        return Offset(
                            gridStartX + gx * cellW,
                            gridStartY + gy * cellH
                        )
                    }

                    // ---- Batch-render land dots ----
                    val dotRadius = (cellW * 0.22f).coerceIn(1f, 4f)
                    val dotColor = OnSurfaceVariant.copy(alpha = 0.45f)

                    if (colStart <= colEnd && rowStart <= rowEnd) {
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
                    }

                    // ---- Track recording path ----
                    if (trackPoints.size >= 2) {
                        val trackOffsets = trackPoints.map { pt ->
                            latLngToCanvas(pt.latitude, pt.longitude)
                        }
                        val trackPath = Path().apply {
                            moveTo(trackOffsets[0].x, trackOffsets[0].y)
                            for (i in 1 until trackOffsets.size) {
                                lineTo(trackOffsets[i].x, trackOffsets[i].y)
                            }
                        }
                        drawPath(trackPath, Secondary.copy(alpha = 0.5f), style = Stroke(width = 1.5f))
                        drawCircle(Secondary.copy(alpha = 0.8f), 4f, trackOffsets.first())
                        drawCircle(PrimaryFixedDim, 4f, trackOffsets.last())
                    }

                    // ---- City labels ----
                    if (showCities && scale >= 0.8f) {
                        for (city in WorldMapData.majorCities) {
                            val pos = latLngToCanvas(city.lat.toDouble(), city.lon.toDouble())

                            if (pos.x in -50f..(canvasW + 50f) && pos.y in -50f..(canvasH + 50f)) {
                                val alpha = when {
                                    scale < 1.0f -> 0.3f
                                    scale < 1.5f -> 0.5f
                                    else -> 0.7f
                                }
                                drawCircle(
                                    color = OnSurfaceVariant.copy(alpha = alpha),
                                    radius = 2f,
                                    center = pos,
                                    style = Stroke(1f)
                                )
                                if (scale >= 2.0f) {
                                    val tl = textMeasurer.measure(
                                        text = city.name,
                                        style = TextStyle(
                                            fontSize = (9f / scale).coerceIn(6f, 11f).sp,
                                            color = OnSurfaceVariant.copy(alpha = alpha),
                                            fontFamily = FontFamily.Default
                                        )
                                    )
                                    drawText(tl, topLeft = Offset(pos.x - tl.size.width / 2f, pos.y + 4f))
                                }
                            }
                        }
                    }

                    // ---- GPS position marker ----
                    if (isFixed && latitude != 0.0 && longitude != 0.0) {
                        val pos = latLngToCanvas(latitude, longitude)

                        drawCircle(PrimaryFixed.copy(alpha = pulseAlpha), pulseRadius, pos)
                        drawCircle(Primary, 5f, pos)
                        drawCircle(PrimaryFixed, 2.5f, pos)

                        val crossLen = 10f
                        val crossColor = PrimaryFixed.copy(alpha = 0.6f)
                        drawLine(crossColor, Offset(pos.x - crossLen, pos.y), Offset(pos.x - 3f, pos.y), 1f)
                        drawLine(crossColor, Offset(pos.x + 3f, pos.y), Offset(pos.x + crossLen, pos.y), 1f)
                        drawLine(crossColor, Offset(pos.x, pos.y - crossLen), Offset(pos.x, pos.y - 3f), 1f)
                        drawLine(crossColor, Offset(pos.x, pos.y + 3f), Offset(pos.x, pos.y + crossLen), 1f)
                    }
                } catch (_: Exception) { }
            }

            // Pinch hint (hidden in fullscreen)
            if (scale < 1.5f && !isFullscreen) {
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

        // Fullscreen button overlay (top-right, hidden in fullscreen)
        if (showFullscreenButton && onFullscreenClick != null && !isFullscreen) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(SurfaceContainerHigh.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                    .border(1.dp, OutlineVariant.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    .clickable { onFullscreenClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "\u26F6",
                    style = CodeSm,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}
