package com.telemetrypro.app.ui.map

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telemetrypro.app.R
import com.telemetrypro.app.ui.theme.*
import kotlin.math.*

/**
 * Dot-matrix (halftone/stipple) world map rendered on Compose Canvas.
 * Supports pinch-to-zoom, pan, and shows GPS position as a glowing dot.
 *
 * Renders simplified continent outlines as dots, with cities as optional labels.
 * Color scheme follows Astra Precision dark aviation theme.
 */
@Composable
fun DotMatrixMap(
    latitude: Double,
    longitude: Double,
    isFixed: Boolean,
    showCities: Boolean = true,
    modifier: Modifier = Modifier,
    mapLabel: String = ""
) {
    // Zoom and pan state
    var scale by remember { mutableFloatStateOf(1.0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Animation for GPS dot pulse
    val infiniteTransition = rememberInfiniteTransition(label = "gpcdot")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .background(Surface, RoundedCornerShape(12.dp))
            .border(1.dp, SurfaceVariant, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Map label bar
            if (mapLabel.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceVariant.copy(alpha = 0.3f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = mapLabel,
                        style = com.telemetrypro.app.ui.theme.LabelCaps,
                        color = OnSurfaceVariant
                    )
                }
            }

            // Map canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp)
                    .pointerInput(Unit) {
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            // Clamp scale between 0.5x and 5x
                            val newScale = (scale * zoom).coerceIn(0.5f, 5f)
                            scale = newScale
                            offsetX += pan.x
                            offsetY += pan.y
                        }
                    }
            ) {
                val canvasW = size.width
                val canvasH = size.height
                val centerX = canvasW / 2f + offsetX
                val centerY = canvasH / 2f + offsetY

                // Equirectangular projection: x = lon, y = -lat (inverted for Canvas)
                val mapScale = min(canvasW / 360f, canvasH / 180f) * scale

                // Draw continent dots
                for (continent in WorldMapData.continents) {
                    for (polygon in continent.polygons) {
                        drawStippledOutline(
                            points = polygon.points,
                            centerX = centerX,
                            centerY = centerY,
                            mapScale = mapScale,
                            canvasW = canvasW,
                            canvasH = canvasH
                        )
                    }
                }

                // Draw cities if zoomed in enough
                if (showCities && scale >= 0.8f) {
                    for (city in WorldMapData.majorCities) {
                        val px = centerX + city.lon * mapScale
                        val py = centerY - city.lat * mapScale

                        // Only draw if in visible area
                        if (px in -50f..(canvasW + 50f) && py in -50f..(canvasH + 50f)) {
                            val alpha = when {
                                scale < 1.0f -> 0.3f
                                scale < 1.5f -> 0.5f
                                else -> 0.7f
                            }

                            // City dot
                            drawCircle(
                                color = OnSurfaceVariant.copy(alpha = alpha),
                                radius = 1.8f,
                                center = Offset(px, py)
                            )

                            // City label
                            if (scale >= 1.3f) {
                                val textLayoutResult = textMeasurer.measure(
                                    text = city.name,
                                    style = TextStyle(
                                        fontSize = (9f / scale).coerceIn(6f, 11f).sp,
                                        color = OnSurfaceVariant.copy(alpha = alpha),
                                        fontFamily = FontFamily.Default
                                    )
                                )
                                drawText(
                                    textLayoutResult = textLayoutResult,
                                    topLeft = Offset(
                                        px - textLayoutResult.size.width / 2f,
                                        py + 4f
                                    )
                                )
                            }
                        }
                    }
                }

                // Draw GPS position marker if fixed
                if (isFixed && latitude != 0.0 && longitude != 0.0) {
                    val posX = centerX + longitude.toFloat() * mapScale
                    val posY = centerY - latitude.toFloat() * mapScale

                    // Outer pulse ring
                    drawCircle(
                        color = PrimaryFixed.copy(alpha = pulseAlpha),
                        radius = pulseRadius,
                        center = Offset(posX, posY)
                    )

                    // Inner filled dot
                    drawCircle(
                        color = Primary,
                        radius = 5f,
                        center = Offset(posX, posY)
                    )

                    // Core bright spot
                    drawCircle(
                        color = PrimaryFixed,
                        radius = 2.5f,
                        center = Offset(posX, posY)
                    )

                    // Crosshair lines
                    val crossLen = 10f
                    drawLine(
                        color = PrimaryFixed.copy(alpha = 0.6f),
                        start = Offset(posX - crossLen, posY),
                        end = Offset(posX - 3f, posY),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = PrimaryFixed.copy(alpha = 0.6f),
                        start = Offset(posX + 3f, posY),
                        end = Offset(posX + crossLen, posY),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = PrimaryFixed.copy(alpha = 0.6f),
                        start = Offset(posX, posY - crossLen),
                        end = Offset(posX, posY - 3f),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = PrimaryFixed.copy(alpha = 0.6f),
                        start = Offset(posX, posY + 3f),
                        end = Offset(posX, posY + crossLen),
                        strokeWidth = 1f
                    )

                    // Coordinate label
                    if (scale >= 0.8f) {
                        val coordText = String.format("%.2f°, %.2f°", latitude, longitude)
                        val textLayoutResult = textMeasurer.measure(
                            text = coordText,
                            style = TextStyle(
                                fontSize = (9f / scale).coerceIn(7f, 12f).sp,
                                color = Primary,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(
                                posX - textLayoutResult.size.width / 2f,
                                posY - 18f
                            )
                        )
                    }
                }
            }

            // Hint bar at bottom
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

/**
 * Draw continent outlines as a stippled/dot pattern.
 */
private fun DrawScope.drawStippledOutline(
    points: List<Pair<Float, Float>>,
    centerX: Float,
    centerY: Float,
    mapScale: Float,
    canvasW: Float,
    canvasH: Float
) {
    if (points.size < 2) return

    // Convert to canvas coordinates
    val projected = points.map { (lon, lat) ->
        Offset(
            centerX + lon * mapScale,
            centerY - lat * mapScale
        )
    }

    // Draw dots along path segments
    val dotSpacing = when {
        mapScale < 0.5f -> 6f
        mapScale < 1.0f -> 4f
        mapScale < 2.0f -> 3f
        else -> 2.5f
    }

    val dotRadius = when {
        mapScale < 0.5f -> 0.8f
        mapScale < 1.0f -> 1.2f
        mapScale < 2.0f -> 1.4f
        else -> 1.6f
    }

    val dotColor = OnSurfaceVariant.copy(alpha = 0.45f)

    for (i in 0 until projected.size) {
        val start = projected[i]
        val end = projected[(i + 1) % projected.size]

        val dx = end.x - start.x
        val dy = end.y - start.y
        val segLen = sqrt(dx * dx + dy * dy)

        if (segLen < 1f) continue

        val numDots = max(1, (segLen / dotSpacing).toInt())
        for (j in 0..numDots) {
            val t = j.toFloat() / numDots
            val px = start.x + dx * t
            val py = start.y + dy * t

            // Only draw if in visible area (with margin)
            if (px in -20f..(canvasW + 20f) && py in -20f..(canvasH + 20f)) {
                drawCircle(
                    color = dotColor,
                    radius = dotRadius,
                    center = Offset(px, py)
                )
            }
        }
    }
}
