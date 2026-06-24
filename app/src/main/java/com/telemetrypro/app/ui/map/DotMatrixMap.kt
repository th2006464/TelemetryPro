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
 * Dot-matrix world map rendered on Compose Canvas.
 * Uses 126x60 grid data from NTag/dotted-map (Mercator projection).
 * Supports pinch-to-zoom, pan, GPS position marker, and city labels.
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
    var scale by remember { mutableFloatStateOf(1.0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

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
                    Text(
                        text = mapLabel,
                        style = LabelCaps,
                        color = OnSurfaceVariant
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp)
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

                // Mercator projection: lon directly maps to x, lat transforms for y
                // Map lon [-180, 180] → x [0, 126]; lat [85, -85] → y [0, 60]
                // In Mercator: y = 60 * (1 - (ln(tan(π/4 + lat*π/360)) / ln(tan(π/4 + 85°*π/360)))) / 2

                val maxLatRad = (85.0 * PI / 180.0)
                val mercMax = ln(tan(PI / 4 + maxLatRad / 2))

                fun mercatorY(latDeg: Double): Float {
                    val latRad = (latDeg * PI / 180.0).coerceIn(-maxLatRad, maxLatRad)
                    val yMerc = ln(tan(PI / 4 + latRad / 2))
                    return (60f * (1f - yMerc.toFloat() / mercMax.toFloat()) / 2f)
                }

                // Grid dot rendering
                val cellW = canvasW / 126f * scale
                val gridRatio = canvasH / 60f / (canvasW / 126f)
                val cellH = cellW * gridRatio

                val gridStartX = offsetX % cellW
                val gridStartY = offsetY % cellH

                // Determine visible grid range
                val colStart = max(0, ((-gridStartX / cellW).toInt() - 1).coerceIn(0, 125))
                val colEnd = min(125, ((canvasW - gridStartX) / cellW).toInt() + 1)
                val rowStart = max(0, ((-gridStartY / cellH).toInt() - 1).coerceIn(0, 59))
                val rowEnd = min(59, ((canvasH - gridStartY) / cellH).toInt() + 1)

                // Dot size: proportional to cell, with min/max
                val dotRadius = (cellW * 0.22f).coerceIn(1f, 4f)
                val dotColor = OnSurfaceVariant.copy(alpha = 0.45f)

                // Draw land dots from grid
                for (y in rowStart..rowEnd) {
                    for (x in colStart..colEnd) {
                        if (WorldMapGrid.isLand(x, y)) {
                            val px = gridStartX + x * cellW + cellW / 2
                            val py = gridStartY + y * cellH + cellH / 2
                            drawCircle(
                                color = dotColor,
                                radius = dotRadius,
                                center = Offset(px, py)
                            )
                        }
                    }
                }

                // City labels
                if (showCities && scale >= 0.8f) {
                    for (city in WorldMapData.majorCities) {
                        val gridX = (city.lon + 180f) / 360f * 126f
                        val gridY = mercatorY(city.lat.toDouble())
                        val cityPx = gridStartX + gridX * cellW + cellW / 2
                        val cityPy = gridStartY + gridY * cellH + cellH / 2

                        if (cityPx in -50f..(canvasW + 50f) && cityPy in -50f..(canvasH + 50f)) {
                            val alpha = when {
                                scale < 1.0f -> 0.3f
                                scale < 1.5f -> 0.5f
                                else -> 0.7f
                            }
                            drawCircle(
                                color = OnSurfaceVariant.copy(alpha = alpha),
                                radius = 2f,
                                center = Offset(cityPx, cityPy)
                            )
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
                                        cityPx - textLayoutResult.size.width / 2f,
                                        cityPy + 4f
                                    )
                                )
                            }
                        }
                    }
                }

                // GPS position marker
                if (isFixed && latitude != 0.0 && longitude != 0.0) {
                    val posGridX = ((longitude + 180f) / 360f * 126f).toFloat()
                    val posGridY = mercatorY(latitude)
                    val posX = gridStartX + posGridX * cellW + cellW / 2
                    val posY = gridStartY + posGridY * cellH + cellH / 2

                    // Pulse ring
                    drawCircle(
                        color = PrimaryFixed.copy(alpha = pulseAlpha),
                        radius = pulseRadius,
                        center = Offset(posX, posY)
                    )
                    // Inner dot
                    drawCircle(color = Primary, radius = 5f, center = Offset(posX, posY))
                    // Core
                    drawCircle(color = PrimaryFixed, radius = 2.5f, center = Offset(posX, posY))

                    // Crosshair
                    val crossLen = 10f
                    val crossColor = PrimaryFixed.copy(alpha = 0.6f)
                    drawLine(crossColor, Offset(posX - crossLen, posY), Offset(posX - 3f, posY), 1f)
                    drawLine(crossColor, Offset(posX + 3f, posY), Offset(posX + crossLen, posY), 1f)
                    drawLine(crossColor, Offset(posX, posY - crossLen), Offset(posX, posY - 3f), 1f)
                    drawLine(crossColor, Offset(posX, posY + 3f), Offset(posX, posY + crossLen), 1f)

                    // Label
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
                            topLeft = Offset(posX - textLayoutResult.size.width / 2f, posY - 18f)
                        )
                    }
                }
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
