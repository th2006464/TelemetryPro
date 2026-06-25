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
import androidx.compose.ui.graphics.Canvas as GraphicsCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
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
    isRecording: Boolean = false,
    showFullscreenButton: Boolean = false,
    onFullscreenClick: (() -> Unit)? = null,
    isFullscreen: Boolean = false
) {
    val gridWidth = WorldMapGrid.GRID_W
    val gridHeight = WorldMapGrid.GRID_H
    val worldAspect = gridWidth.toFloat() / gridHeight.toFloat()

    var scale by remember { mutableStateOf(5.0f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var initialized by remember { mutableStateOf(false) }
    var centeredOnFix by remember { mutableStateOf(false) }
    var userPanned by remember { mutableStateOf(false) }

    // Track recording: when recording starts, re-center on start point once
    var lastRecordingState by remember { mutableStateOf(false) }

    val pulseRadius = 6f
    val pulseAlpha = 0.35f
    val textMeasurer = rememberTextMeasurer()

    // Pre-render land dots to an ImageBitmap (created once, drawn every frame as a single GPU call).
    // This eliminates per-frame allocation of 40K-160K Offset objects that caused GC pressure during drag.
    val dotColor = OnSurfaceVariant.copy(alpha = 0.5f)
    val mapBitmap = remember(gridWidth, gridHeight, dotColor) {
        val scaleFactor = 2  // each grid cell = 2x2 pixels in the bitmap
        val w = gridWidth * scaleFactor
        val h = gridHeight * scaleFactor
        val bmp = ImageBitmap(w, h)
        val canvas = GraphicsCanvas(bmp)
        val paint = Paint().apply {
            color = dotColor
            isAntiAlias = true
        }
        for (y in 0 until gridHeight) {
            for (x in 0 until gridWidth) {
                if (WorldMapGrid.isLand(x, y)) {
                    canvas.drawCircle(
                        Offset(
                            x * scaleFactor + scaleFactor / 2f,
                            y * scaleFactor + scaleFactor / 2f
                        ),
                        scaleFactor * 0.45f,
                        paint
                    )
                }
            }
        }
        bmp
    }

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
                            val newScale = (oldScale * zoom).coerceIn(0.5f, 30f)
                            val actualZoom = newScale / oldScale

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
                            if (pan.x != 0f || pan.y != 0f) userPanned = true
                            scale = newScale
                        }
                    }
            ) {
                val canvasW = size.width
                val canvasH = size.height
                if (canvasW <= 0f || canvasH <= 0f) return@Canvas

                try {
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
                    val cellW = mapW / gridWidth
                    val cellH = mapH / gridHeight

                    // ---- Determine centering target ----
                    // When recording starts (transition false→true), center on start point and zoom in.
                    // During recording, auto-fit the trail bounding box unless user has manually panned.
                    val recordingJustStarted = isRecording && !lastRecordingState
                    lastRecordingState = isRecording

                    val shouldCenter = !initialized ||
                        (isFixed && !centeredOnFix && !isRecording && latitude != 0.0 && longitude != 0.0) ||
                        (recordingJustStarted && trackPoints.isNotEmpty())

                    if (shouldCenter) {
                        val cLat: Double
                        val cLng: Double
                        if (isRecording && trackPoints.isNotEmpty()) {
                            // Center on start point of the recording
                            cLat = trackPoints.first().latitude
                            cLng = trackPoints.first().longitude
                            if (recordingJustStarted) {
                                scale = 12f  // zoom in for trail following
                                userPanned = false
                            }
                        } else if (isFixed && latitude != 0.0 && longitude != 0.0) {
                            cLat = latitude
                            cLng = longitude
                        } else {
                            cLat = 35.0   // China center fallback
                            cLng = 105.0
                        }
                        // Recompute mapW/H with potentially new scale
                        val newMapH: Float
                        val newMapW: Float
                        if (canvasAspect > worldAspect) {
                            newMapH = canvasH * scale
                            newMapW = newMapH * worldAspect
                        } else {
                            newMapW = canvasW * scale
                            newMapH = newMapW / worldAspect
                        }
                        val newCellW = newMapW / gridWidth
                        val newCellH = newMapH / gridHeight
                        val newMapLeft = (canvasW - newMapW) / 2f
                        val newMapTop = (canvasH - newMapH) / 2f
                        val cGx = WorldMapProjection.longitudeToGridX(cLng, gridWidth)
                        val cGy = WorldMapProjection.mercatorY(cLat, gridHeight)
                        offsetX = canvasW / 2f - newMapLeft - cGx * newCellW
                        offsetY = canvasH / 2f - newMapTop - cGy * newCellH
                        initialized = true
                        if (isFixed && !isRecording) centeredOnFix = true
                    }

                    // ---- Grid origin on canvas (NO modulo — raw offset) ----
                    val gridStartX = mapLeft + offsetX
                    val gridStartY = mapTop + offsetY

                    fun latLngToCanvas(lat: Double, lng: Double): Offset {
                        val gx = WorldMapProjection.longitudeToGridX(lng, gridWidth)
                        val gy = WorldMapProjection.mercatorY(lat, gridHeight)
                        return Offset(
                            gridStartX + gx * cellW,
                            gridStartY + gy * cellH
                        )
                    }

                    // ---- Draw pre-rendered land dot bitmap (single GPU call, no per-frame allocations) ----
                    drawImage(
                        image = mapBitmap,
                        dstOffset = IntOffset(gridStartX.roundToInt(), gridStartY.roundToInt()),
                        dstSize = IntSize(mapW.roundToInt(), mapH.roundToInt())
                    )

                    // ---- Track recording path with direction arrows ----
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
                        drawPath(trackPath, Secondary.copy(alpha = 0.6f), style = Stroke(width = 2f))

                        // Start point: green ring + "S" label
                        val startPos = trackOffsets.first()
                        drawCircle(Secondary.copy(alpha = 0.3f), 8f, startPos)
                        drawCircle(Secondary, 4f, startPos)
                        if (scale >= 4f) {
                            val sLabel = textMeasurer.measure(
                                text = "START",
                                style = TextStyle(
                                    fontSize = (8f / scale).coerceIn(6f, 10f).sp,
                                    color = Secondary,
                                    fontFamily = FontFamily.Default
                                )
                            )
                            drawText(sLabel, topLeft = Offset(
                                startPos.x - sLabel.size.width / 2f,
                                startPos.y + 10f
                            ))
                        }

                        // Direction arrows along the path (every N points)
                        val arrowInterval = when {
                            trackPoints.size < 20 -> 5
                            trackPoints.size < 100 -> 10
                            else -> 20
                        }
                        val arrowColor = Secondary.copy(alpha = 0.8f)
                        for (i in trackOffsets.indices step arrowInterval) {
                            if (i == 0 || i >= trackOffsets.size) continue
                            val prev = trackOffsets[i - 1]
                            val curr = trackOffsets[i]
                            val dx = curr.x - prev.x
                            val dy = curr.y - prev.y
                            val len = sqrt(dx * dx + dy * dy)
                            if (len < 8f) continue  // skip if points too close

                            // Draw small triangle arrow in direction of travel
                            val angle = atan2(dy, dx)
                            val arrowSize = 5f
                            val p1 = Offset(
                                curr.x - arrowSize * cos(angle - 0.4f),
                                curr.y - arrowSize * sin(angle - 0.4f)
                            )
                            val p2 = Offset(
                                curr.x - arrowSize * cos(angle + 0.4f),
                                curr.y - arrowSize * sin(angle + 0.4f)
                            )
                            val arrowPath = Path().apply {
                                moveTo(curr.x, curr.y)
                                lineTo(p1.x, p1.y)
                                lineTo(p2.x, p2.y)
                                close()
                            }
                            drawPath(arrowPath, arrowColor)
                        }

                        // Current position (end of track): yellow dot
                        drawCircle(PrimaryFixedDim.copy(alpha = 0.3f), 8f, trackOffsets.last())
                        drawCircle(PrimaryFixedDim, 4f, trackOffsets.last())
                    }

                    // ---- City labels ----
                    if (showCities && scale >= 0.8f) {
                        for (city in WorldMapCities.majorCities) {
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
            if (scale < 2.5f && !isFullscreen) {
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
