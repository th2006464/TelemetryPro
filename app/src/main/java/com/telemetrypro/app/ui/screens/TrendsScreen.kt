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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.telemetrypro.app.R
import com.telemetrypro.app.data.LocationState
import com.telemetrypro.app.ui.components.NmeaFeed
import com.telemetrypro.app.ui.components.TopAppBar
import com.telemetrypro.app.ui.theme.*
import kotlin.math.min as mathMin

@Composable
fun TrendsScreen(
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
            fixLabel = stringResource(R.string.fix_status_tracking),
            isFixed = state.speedKmh > 0
        )

        Spacer(Modifier.height(8.dp))

        SpeedometerCard(
            speedKmh = state.speedKmh,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )

        AltitudeTrendCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VsiCard(modifier = Modifier.weight(0.35f))
            TerrainCard(
                altitude = state.altitudeMeters,
                latitude = state.latitude,
                longitude = state.longitude,
                modifier = Modifier.weight(0.65f)
            )
        }

        NmeaFeed(
            lines = state.nmeaLogLines,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun SpeedometerCard(
    speedKmh: Float,
    modifier: Modifier = Modifier
) {
    val maxSpeed = 160f
    val ratio = (speedKmh / maxSpeed).coerceIn(0f, 1f)
    val animatedRatio by animateFloatAsState(
        targetValue = ratio,
        animationSpec = tween(350),
        label = "speed_ratio"
    )

    Box(
        modifier = modifier
            .background(TileBackground, RoundedCornerShape(12.dp))
            .border(1.dp, TileBorder, RoundedCornerShape(12.dp))
            .aspectRatio(1.5f)
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val cx: Float = size.width / 2f
            val cy: Float = size.height * 0.55f
            val radius: Float = mathMin(cx, cy) * 0.75f
            val arcSize = Size(radius * 2f, radius * 2f)
            val arcOffset = Offset(cx - radius, cy - radius)

            drawArc(
                color = SurfaceContainerHighest,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = arcOffset,
                size = arcSize,
                style = Stroke(width = 16f)
            )

            drawArc(
                color = PrimaryFixedDim,
                startAngle = 135f,
                sweepAngle = 270f * animatedRatio,
                useCenter = false,
                topLeft = arcOffset,
                size = arcSize,
                style = Stroke(width = 16f)
            )
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = String.format("%.1f", speedKmh),
                style = DisplayData,
                color = PrimaryFixedDim
            )
            Text(
                text = stringResource(R.string.dashboard_unit_kmh),
                style = LabelCaps,
                color = OnSurfaceVariant
            )
        }

        Text(
            text = stringResource(R.string.trends_velocity_profile),
            style = LabelCaps,
            color = OnSurfaceVariant,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        )
    }
}

@Composable
private fun AltitudeTrendCard(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(TileBackground, RoundedCornerShape(12.dp))
            .border(1.dp, TileBorder, RoundedCornerShape(12.dp))
            .aspectRatio(2f)
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val w: Float = size.width
            val h: Float = size.height * 0.7f
            val topY: Float = size.height * 0.15f

            for (i in 0..4) {
                val y: Float = topY + h * i / 4f
                drawLine(
                    color = OutlineVariant.copy(alpha = 0.1f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
            }

            val path = Path().apply {
                moveTo(0f, topY + h * 0.6f)
                val points = listOf(0.6f, 0.55f, 0.5f, 0.45f, 0.4f, 0.3f, 0.35f, 0.25f, 0.2f, 0.15f)
                points.forEachIndexed { i, p ->
                    val x: Float = w * (i + 1) / points.size.toFloat()
                    lineTo(x, topY + h * p)
                }
            }
            drawPath(path = path, color = PrimaryFixedDim, style = Stroke(width = 2f))

            val fillPath = Path().apply {
                addPath(path)
                lineTo(w, topY + h)
                lineTo(0f, topY + h)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PrimaryFixedDim.copy(alpha = 0.2f),
                        PrimaryFixedDim.copy(alpha = 0f)
                    ),
                    startY = topY,
                    endY = topY + h
                )
            )
        }

        Text(
            stringResource(R.string.trends_altitude_trend),
            style = LabelCaps,
            color = OnSurfaceVariant,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        )
    }
}

@Composable
private fun VsiCard(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(TileBackground, RoundedCornerShape(12.dp))
            .border(1.dp, TileBorder, RoundedCornerShape(12.dp))
            .aspectRatio(0.6f)
    ) {
        Text(
            stringResource(R.string.trends_vsi),
            style = LabelCaps,
            color = OnSurfaceVariant,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 28.dp, bottom = 12.dp, start = 8.dp, end = 8.dp)
        ) {
            val cw: Float = size.width
            val ch: Float = size.height
            val barW: Float = cw * 0.25f
            val barX: Float = (cw - barW) / 2f

            drawRoundRect(
                color = SurfaceContainerHighest,
                topLeft = Offset(barX, 0f),
                size = Size(barW, ch)
            )

            drawRoundRect(
                color = Secondary.copy(alpha = 0.6f),
                topLeft = Offset(barX, ch * 0.6f),
                size = Size(barW, ch * 0.25f)
            )

            drawLine(
                color = OnSurfaceVariant,
                start = Offset(0f, ch / 2f),
                end = Offset(cw, ch / 2f),
                strokeWidth = 1f
            )
        }

        Text(
            "+2.4",
            style = TelemetryMd,
            color = Secondary,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 28.dp)
        )
    }
}

@Composable
private fun TerrainCard(
    altitude: Double,
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(TileBackground, RoundedCornerShape(12.dp))
            .border(1.dp, TileBorder, RoundedCornerShape(12.dp))
            .aspectRatio(1.2f)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 40f
            var x: Float = 0f
            while (x < size.width) {
                drawLine(
                    color = OutlineVariant.copy(alpha = 0.08f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
                x += step
            }
            var y: Float = 0f
            while (y < size.height) {
                drawLine(
                    color = OutlineVariant.copy(alpha = 0.08f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += step
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
        ) {
            Text(stringResource(R.string.trends_terrain_context), style = LabelCaps, color = PrimaryFixedDim)
            Text(
                stringResource(R.string.trends_alt_label).replace("%d", altitude.toInt().toString()),
                style = TelemetryMd,
                color = OnSurfaceVariant
            )
            Text(
                String.format("%.4f, %.4f", latitude, longitude),
                style = CodeSm,
                color = OnSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
