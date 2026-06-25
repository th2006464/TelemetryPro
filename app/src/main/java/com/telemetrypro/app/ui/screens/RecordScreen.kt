package com.telemetrypro.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telemetrypro.app.LocaleHelper
import com.telemetrypro.app.data.LocationState
import com.telemetrypro.app.data.TrackSession
import com.telemetrypro.app.data.TrackPoint
import com.telemetrypro.app.ui.components.NmeaFeed
import com.telemetrypro.app.ui.theme.*
import kotlin.math.*

@Composable
fun RecordScreen(
    state: LocationState,
    isRecording: Boolean,
    distanceKm: Double,
    sessions: List<TrackSession>,
    onStartRecording: (String) -> Unit,
    onStopRecording: () -> Unit,
    onDeleteSession: (Long) -> Unit,
    onRenameSession: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isZh = LocaleHelper.isZh(context)
    var showNameDialog by remember { mutableStateOf(false) }
    var sessionName by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(8.dp))

        // NMEA raw data stream — moved from TrendsScreen
        NmeaFeed(
            lines = state.nmeaLogLines,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // NMEA explanation card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .background(PrimaryContainer.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .border(1.dp, PrimaryContainer.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text(
                "NMEA 0183 是 GNSS 接收器输出的标准数据协议。每行以 \$ 开头，包含定位、卫星、时间等原始信息。例如 \$GPGGA 包含经纬度、海拔、卫星数；\$GPVTG 包含地面速度和方向。本应用通过系统 NMEA 监听器实时捕获这些数据用于分析。",
                style = CodeSm,
                color = OnSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        Spacer(Modifier.height(8.dp))

        // Recording control card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .background(TileBackground, RoundedCornerShape(12.dp))
                .border(1.dp, TileBorder, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    if (isZh) "测距记录" else "DISTANCE TRACKING",
                    style = LabelCaps,
                    color = OnSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))

                if (!isRecording) {
                    // Pre-start: name input + start button
                    var inputName by remember { mutableStateOf("") }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(SurfaceContainerLowest, RoundedCornerShape(8.dp))
                                .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            if (inputName.isEmpty()) {
                                Text(
                                    if (isZh) "输入记录名称…" else "Enter session name…",
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 14.sp
                                    ),
                                    color = OnSurfaceVariant.copy(alpha = 0.4f)
                                )
                            }
                            BasicTextField(
                                value = inputName,
                                onValueChange = { inputName = it },
                                textStyle = TextStyle(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 14.sp,
                                    color = OnSurface
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    PrimaryContainer.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                                .border(1.dp, PrimaryFixedDim, RoundedCornerShape(8.dp))
                                .clickable {
                                    onStartRecording(inputName.ifBlank {
                                        if (isZh) "记录 #${sessions.size + 1}" else "Track #${sessions.size + 1}"
                                    })
                                    inputName = ""
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                if (isZh) "▶ 开始" else "▶ START",
                                style = LabelCaps,
                                color = PrimaryFixedDim
                            )
                        }
                    }
                } else {
                    // Recording active: show status + stop button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                if (isZh) "● 记录中" else "● RECORDING",
                                style = TelemetryMd,
                                color = Secondary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${String.format("%.2f", distanceKm)} km",
                                style = DisplayData,
                                color = PrimaryFixedDim
                            )
                            Row {
                                Text(
                                    "${state.recordingPoints.size} pts",
                                    style = CodeSm,
                                    color = OnSurfaceVariant.copy(alpha = 0.6f)
                                )
                                Spacer(Modifier.width(12.dp))
                                val lastPt = state.recordingPoints.lastOrNull()
                                if (lastPt != null) {
                                    Text(
                                        if (isZh) "方向 ${lastPt.compassDirection} · ${lastPt.bearing.toInt()}°"
                                        else "HDG ${lastPt.compassShort} · ${lastPt.bearing.toInt()}°",
                                        style = CodeSm,
                                        color = Secondary
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .background(ErrorContainer.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .border(1.dp, Error, RoundedCornerShape(8.dp))
                                .clickable { onStopRecording() }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                if (isZh) "■ 停止" else "■ STOP",
                                style = LabelCaps,
                                color = Error
                            )
                        }
                    }
                }

                // Navigation-style mini radar: square, current position centered, bearing arrow
                if (isRecording && state.recordingPoints.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .background(SurfaceContainerLowest, RoundedCornerShape(8.dp))
                                .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                                val pts = state.recordingPoints
                                if (pts.isEmpty()) return@Canvas

                                val current = pts.last()
                                val cw = size.width
                                val ch = size.height
                                val cx = cw / 2f
                                val cy = ch / 2f

                                // Fixed scale: show ~200m radius around current position.
                                // meters-per-pixel adjusted by latitude (cosine correction for longitude).
                                val latRad = Math.toRadians(current.latitude)
                                val metersPerPixel = 0.5f  // 0.5 m/px → ~100m radius visible
                                fun project(lat: Double, lng: Double): Offset {
                                    val dLatM = (lat - current.latitude) * 111320.0
                                    val dLngM = (lng - current.longitude) * 111320.0 * kotlin.math.cos(latRad)
                                    val px = cx + (dLngM / metersPerPixel).toFloat()
                                    val py = cy - (dLatM / metersPerPixel).toFloat()
                                    return Offset(px, py)
                                }

                                // Range rings (50m, 100m)
                                val ring50 = (50f / metersPerPixel)
                                val ring100 = (100f / metersPerPixel)
                                drawCircle(
                                    color = OnSurfaceVariant.copy(alpha = 0.15f),
                                    radius = ring50,
                                    center = Offset(cx, cy),
                                    style = Stroke(1f)
                                )
                                drawCircle(
                                    color = OnSurfaceVariant.copy(alpha = 0.1f),
                                    radius = ring100,
                                    center = Offset(cx, cy),
                                    style = Stroke(1f)
                                )

                                // Cross hair
                                drawLine(OnSurfaceVariant.copy(alpha = 0.2f), Offset(0f, cy), Offset(cw, cy), 1f)
                                drawLine(OnSurfaceVariant.copy(alpha = 0.2f), Offset(cx, 0f), Offset(cx, ch), 1f)

                                // Cardinal labels (N/E/S/W) — small text
                                val labelColor = OnSurfaceVariant.copy(alpha = 0.5f)
                                val cardPaint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.argb(130, 208, 198, 171)
                                    textSize = 11f
                                    isAntiAlias = true
                                }
                                val canvasObj = drawContext.canvas
                                canvasObj.nativeCanvas.drawText("N", cx - 4f, 14f, cardPaint)
                                canvasObj.nativeCanvas.drawText("S", cx - 4f, ch - 4f, cardPaint)
                                canvasObj.nativeCanvas.drawText("E", cw - 12f, cy + 4f, cardPaint)
                                canvasObj.nativeCanvas.drawText("W", 4f, cy + 4f, cardPaint)

                                // Draw trail (only points within visible range)
                                val visibleOffsets = pts.map { project(it.latitude, it.longitude) }
                                if (visibleOffsets.size >= 2) {
                                    val path = Path().apply {
                                        moveTo(visibleOffsets[0].x, visibleOffsets[0].y)
                                        for (i in 1 until visibleOffsets.size) {
                                            lineTo(visibleOffsets[i].x, visibleOffsets[i].y)
                                        }
                                    }
                                    drawPath(path, Secondary.copy(alpha = 0.7f), style = Stroke(width = 2.5f))
                                }

                                // Start point marker (only if within canvas)
                                val startPos = visibleOffsets.first()
                                if (startPos.x in 0f..cw && startPos.y in 0f..ch) {
                                    drawCircle(Secondary.copy(alpha = 0.3f), 8f, startPos)
                                    drawCircle(Secondary, 4f, startPos)
                                }

                                // Current position: large bearing arrow centered
                                val bearingRad = Math.toRadians(current.bearing.toDouble())
                                // Arrow points "up" (north) by default, rotate by bearing clockwise.
                                // Screen Y is down, so rotate: angle from up = bearing.
                                val arrowLen = 24f
                                val arrowHead = 8f
                                // Tip of arrow (in direction of bearing)
                                val tipX = cx + (arrowLen * kotlin.math.sin(bearingRad)).toFloat()
                                val tipY = cy - (arrowLen * kotlin.math.cos(bearingRad)).toFloat()
                                // Tail
                                val tailX = cx - (arrowLen * 0.4 * kotlin.math.sin(bearingRad)).toFloat()
                                val tailY = cy + (arrowLen * 0.4 * kotlin.math.cos(bearingRad)).toFloat()

                                // Arrow shaft (thick line)
                                drawLine(PrimaryFixedDim, Offset(tailX, tailY), Offset(tipX, tipY), 3f)

                                // Arrowhead (triangle)
                                val perpX = kotlin.math.cos(bearingRad).toFloat()
                                val perpY = kotlin.math.sin(bearingRad).toFloat()
                                val headPath = Path().apply {
                                    moveTo(tipX, tipY)
                                    lineTo(tipX - arrowHead * kotlin.math.sin(bearingRad - 0.4).toFloat(),
                                           tipY + arrowHead * kotlin.math.cos(bearingRad - 0.4).toFloat())
                                    lineTo(tipX - arrowHead * kotlin.math.sin(bearingRad + 0.4).toFloat(),
                                           tipY + arrowHead * kotlin.math.cos(bearingRad + 0.4).toFloat())
                                    close()
                                }
                                drawPath(headPath, PrimaryFixed)

                                // Center dot (current position)
                                drawCircle(PrimaryFixedDim, 3f, Offset(cx, cy))
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Session history
        if (sessions.isNotEmpty()) {
            Text(
                if (isZh) "历史记录" else "SESSION HISTORY",
                style = LabelCaps,
                color = OnSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            sessions.forEach { session ->
                SessionCard(
                    session = session,
                    onDelete = { onDeleteSession(session.id) },
                    onRename = { newName -> onRenameSession(session.id, newName) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        } else if (!isRecording) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isZh) "暂无记录，点击上方开始测距" else "No sessions yet. Start tracking above.",
                    style = CodeSm,
                    color = OnSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun SessionCard(
    session: TrackSession,
    onDelete: () -> Unit,
    onRename: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isZh = LocaleHelper.isZh(context)
    var editing by remember { mutableStateOf(false) }
    var editName by remember(session.name) { mutableStateOf(session.name) }
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .background(TileBackground, RoundedCornerShape(10.dp))
            .border(1.dp, TileBorder, RoundedCornerShape(10.dp))
            .clickable { expanded = !expanded }
            .padding(12.dp)
    ) {
        Column {
            // Header row: name + distance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (editing) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .background(SurfaceContainerLowest, RoundedCornerShape(6.dp))
                                .border(1.dp, PrimaryFixedDim, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            BasicTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                textStyle = TextStyle(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 14.sp,
                                    color = OnSurface
                                ),
                                singleLine = true
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Text("✓", style = LabelCaps, color = Secondary,
                            modifier = Modifier.clickable {
                                onRename(editName)
                                editing = false
                            })
                    }
                } else {
                    Text(
                        session.name.ifBlank {
                            if (isZh) "记录" else "Track"
                        },
                        style = TelemetryMd,
                        color = PrimaryFixedDim,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { editing = true }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "${String.format("%.2f", session.totalDistanceKm)} km",
                        style = TelemetryMd,
                        color = Secondary
                    )
                    Text(
                        "×",
                        style = CodeSm,
                        color = OnSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.clickable { onDelete() }
                    )
                }
            }

            // Meta row
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "${session.points.size} pts",
                    style = CodeSm,
                    color = OnSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                    formatDuration(session.startTime, session.endTime),
                    style = CodeSm,
                    color = OnSurfaceVariant.copy(alpha = 0.5f)
                )
            }

            // Expanded: path preview + data columns
            if (expanded && session.points.size >= 2) {
                Spacer(Modifier.height(8.dp))

                // Path canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(SurfaceContainerLowest, RoundedCornerShape(6.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                        val pts = session.points
                        var minLat = Double.MAX_VALUE; var maxLat = Double.MIN_VALUE
                        var minLng = Double.MAX_VALUE; var maxLng = Double.MIN_VALUE
                        pts.forEach {
                            if (it.latitude < minLat) minLat = it.latitude
                            if (it.latitude > maxLat) maxLat = it.latitude
                            if (it.longitude < minLng) minLng = it.longitude
                            if (it.longitude > maxLng) maxLng = it.longitude
                        }
                        val latR = (maxLat - minLat).coerceAtLeast(0.001)
                        val lngR = (maxLng - minLng).coerceAtLeast(0.001)
                        val p = 10f
                        val w = size.width - p * 2
                        val h = size.height - p * 2
                        fun tx(lng: Double) = p + ((lng - minLng) / lngR * w).toFloat()
                        fun ty(lat: Double) = p + h - ((lat - minLat) / latR * h).toFloat()

                        val offs = pts.map { Offset(tx(it.longitude), ty(it.latitude)) }
                        val path = Path().apply {
                            moveTo(offs[0].x, offs[0].y)
                            offs.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(path, PrimaryFixedDim.copy(alpha = 0.5f), style = Stroke(width = 1.5f))
                        drawPoints(offs, PointMode.Points, PrimaryFixedDim.copy(alpha = 0.3f), strokeWidth = 2f)
                        drawCircle(Secondary, 4f, offs.first())
                        drawCircle(PrimaryFixedDim, 4f, offs.last())
                    }
                }

                // Columns: time, lat, lng, alt, speed, dir
                Spacer(Modifier.height(8.dp))
                val previewPts = session.points.takeLast(20)
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf<Pair<String, Float>>(
                        (if (isZh) "时间" else "Time") to 0.2f,
                        (if (isZh) "纬度" else "Lat") to 0.18f,
                        (if (isZh) "经度" else "Lng") to 0.18f,
                        (if (isZh) "高度" else "Alt") to 0.14f,
                        (if (isZh) "速度" else "Spd") to 0.15f,
                        (if (isZh) "方向" else "Dir") to 0.15f
                    ).forEach { (label, wf) ->
                        Text(label, style = TextStyle(
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 8.sp,
                            color = OnSurfaceVariant.copy(alpha = 0.4f)
                        ), modifier = Modifier.weight(wf))
                    }
                }
                Spacer(Modifier.height(2.dp))
                previewPts.forEach { pt ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        val t = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                            .format(java.util.Date(pt.timestamp))
                        listOf<String>(t, "%.4f".format(pt.latitude), "%.4f".format(pt.longitude),
                            "%.0fm".format(pt.altitude), "%.0f".format(pt.speedKmh),
                            pt.compassDirection)
                            .zip(listOf(0.2f, 0.18f, 0.18f, 0.14f, 0.15f, 0.15f))
                            .forEach { pair ->
                                Text(pair.first, style = TextStyle(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 9.sp,
                                    color = OnSurfaceVariant.copy(alpha = 0.7f)
                                ), modifier = Modifier.weight(pair.second), maxLines = 1)
                            }
                    }
                }
            }
        }
    }
}

private fun formatDuration(startMs: Long, endMs: Long): String {
    val end = if (endMs > 0) endMs else System.currentTimeMillis()
    val secs = ((end - startMs) / 1000).coerceAtLeast(0)
    val m = secs / 60
    val s = secs % 60
    return "${m}m ${s}s"
}
