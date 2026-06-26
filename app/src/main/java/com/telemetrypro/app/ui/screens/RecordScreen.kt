package com.telemetrypro.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.telemetrypro.app.data.TrackPoint
import com.telemetrypro.app.data.TrackSession
import com.telemetrypro.app.ui.components.CompassInfoBar
import com.telemetrypro.app.ui.components.CompassRose
import com.telemetrypro.app.ui.components.NmeaFeed
import com.telemetrypro.app.ui.theme.*
import kotlin.math.*

@Composable
fun RecordScreen(
    state: LocationState,
    isRecording: Boolean,
    distanceKm: Double,
    sessions: List<TrackSession>,
    azimuth: Float = 0f,           // device heading from orientation sensor
    onStartRecording: (String) -> Unit,
    onStopRecording: () -> Unit,
    onDeleteSession: (Long) -> Unit,
    onRenameSession: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isZh = LocaleHelper.isZh(context)

    // Sub-tab state: 0 = 记录 (Record), 1 = 回溯 (Playback)
    var selectedSubTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Tab bar
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = SurfaceContainerLow,
            contentColor = OnSurfaceVariant,
            indicator = {},
            divider = {}
        ) {
            val tabs = listOf(
                if (isZh) "记录" else "RECORD",
                if (isZh) "回溯" else "PLAYBACK"
            )
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { selectedSubTab = index },
                    text = {
                        Text(
                            title,
                            style = LabelCaps.copy(
                                color = if (selectedSubTab == index) PrimaryFixedDim
                                        else OnSurfaceVariant.copy(alpha = 0.5f),
                                fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                )
            }
        }

        when (selectedSubTab) {
            0 -> RecordingTab(
                state = state,
                isRecording = isRecording,
                distanceKm = distanceKm,
                azimuth = azimuth,
                sessions = sessions,
                onStartRecording = onStartRecording,
                onStopRecording = onStopRecording
            )
            1 -> PlaybackTab(
                sessions = sessions,
                azimuth = azimuth,
                onDelete = onDeleteSession,
                onRename = onRenameSession
            )
        }
    }
}

// ============================================================
// TAB 0: RECORDING — Live compass + controls
// ============================================================

@Composable
private fun RecordingTab(
    state: LocationState,
    isRecording: Boolean,
    distanceKm: Double,
    azimuth: Float,
    sessions: List<TrackSession>,
    onStartRecording: (String) -> Unit,
    onStopRecording: () -> Unit
) {
    val context = LocalContext.current
    val isZh = LocaleHelper.isZh(context)

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(8.dp))

        // ===== COMPASS ROSE (main feature) =====
        CompassRose(
            points = if (isRecording) state.recordingPoints else emptyList(),
            azimuth = azimuth,
            showCenterDot = isRecording && state.recordingPoints.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        // ===== INFO BAR below compass =====
        CompassInfoBar(
            azimuth = azimuth,
            altitudeText = "${state.altitudeMeters.toInt()}m",
            speedText = "${String.format("%.0f", state.speedKmh)} km/h",
            distanceText = if (isRecording) "${String.format("%.2f", distanceKm)} km" else "",
            isRecording = isRecording
        )

        Spacer(Modifier.height(12.dp))

        // ===== RECORDING CONTROL CARD =====
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
                    if (isZh) "轨迹记录" else "TRACK RECORDING",
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
                                    style = TextStyle(fontFamily = JetBrainsMonoFamily, fontSize = 14.sp),
                                    color = OnSurfaceVariant.copy(alpha = 0.4f)
                                )
                            }
                            BasicTextField(
                                value = inputName,
                                onValueChange = { inputName = it },
                                textStyle = TextStyle(
                                    fontFamily = JetBrainsMonoFamily, fontSize = 14.sp, color = OnSurface
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .background(PrimaryContainer.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
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
                    // Active recording status + stop
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
                                "${String.format("%.2f", distanceKm)} km · ${state.recordingPoints.size} pts",
                                style = CodeSm,
                                color = OnSurfaceVariant.copy(alpha = 0.6f)
                            )
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
            }
        }

        // ===== NMEA SECTION (collapsed in recording tab) =====
        NmeaFeed(
            lines = state.nmeaLogLines,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(Modifier.height(80.dp))
    }
}

// ============================================================
// TAB 1: PLAYBACK — Session list with compass preview
// ============================================================

@Composable
private fun PlaybackTab(
    sessions: List<TrackSession>,
    azimuth: Float,
    onDelete: (Long) -> Unit,
    onRename: (Long, String) -> Unit
) {
    val context = LocalContext.current
    val isZh = LocaleHelper.isZh(context)

    // Selected session for compass view (-1 = none)
    var selectedSessionId by remember { mutableLongStateOf(-1L) }
    val selectedSession = sessions.find { it.id == selectedSessionId }

    if (sessions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (isZh) "暂无轨迹记录" else "No track records yet.",
                style = CodeSm,
                color = OnSurfaceVariant.copy(alpha = 0.4f)
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Compass preview for selected session (or latest)
        item {
            val displaySession = selectedSession ?: sessions.first()
            val hasSelection = selectedSession != null

            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    if (hasSelection) displaySession.name.ifBlank { if (isZh) "记录" else "Track" }
                    else (if (isZh) "最近记录" else "Latest Record"),
                    style = TelemetryMd,
                    color = PrimaryFixedDim,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                CompassRose(
                    points = displaySession.points,
                    azimuth = azimuth,
                    showCenterDot = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                // Session info under compass — use last track point's bearing for playback
                val lastPt = displaySession.points.lastOrNull()
                CompassInfoBar(
                    azimuth = lastPt?.bearing ?: 0f,
                    altitudeText = "${lastPt?.altitude?.toInt() ?: 0}m",
                    speedText = "",
                    distanceText = "${String.format("%.2f", displaySession.totalDistanceKm)} km"
                )

                // Session metadata row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "${displaySession.points.size} pts",
                        style = CodeSm,
                        color = OnSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        formatDuration(displaySession.startTime, displaySession.endTime),
                        style = CodeSm,
                        color = OnSurfaceVariant.copy(alpha = 0.5f)
                    )
                    if (!hasSelection && sessions.size > 1) {
                        Text(
                            if (isZh) "点击下方选择" else "Tap below to select",
                            style = CodeSm,
                            color = Secondary.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }

        // Session list header
        item {
            Text(
                if (isZh) "历史轨迹" else "TRACK HISTORY",
                style = LabelCaps,
                color = OnSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Session cards
        items(sessions) { session ->
            PlaybackSessionCard(
                session = session,
                isSelected = session.id == selectedSessionId,
                onSelect = {
                    selectedSessionId = if (selectedSessionId == session.id) -1L else session.id
                },
                onDelete = { onDelete(session.id) },
                onRename = { newName -> onRename(session.id, newName) }
            )
        }
    }
}

@Composable
private fun PlaybackSessionCard(
    session: TrackSession,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit
) {
    val context = LocalContext.current
    val isZh = LocaleHelper.isZh(context)
    var editing by remember { mutableStateOf(false) }
    var editName by remember(session.name) { mutableStateOf(session.name) }
    var expanded by remember { mutableStateOf(false) }

    val borderColor = if (isSelected) PrimaryFixedDim.copy(alpha = 0.5f) else TileBorder
    val bgAlpha = if (isSelected) 0.08f else 1f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .background(if (isSelected) PrimaryContainer.copy(alpha = bgAlpha) else TileBackground, RoundedCornerShape(10.dp))
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onSelect() }
            .padding(12.dp)
    ) {
        Column {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selection indicator + name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(PrimaryFixedDim, CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                    }

                    if (editing) {
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
                                    fontFamily = JetBrainsMonoFamily, fontSize = 14.sp, color = OnSurface
                                ),
                                singleLine = true
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Text("✓", style = LabelCaps, color = Secondary,
                            modifier = Modifier.clickable { onRename(editName); editing = false })
                    } else {
                        Text(
                            session.name.ifBlank { if (isZh) "记录" else "Track" },
                            style = TelemetryMd,
                            color = PrimaryFixedDim,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { editing = true }
                        )
                    }
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
                Text("${session.points.size} pts", style = CodeSm, color = OnSurfaceVariant.copy(alpha = 0.6f))
                Text(formatDuration(session.startTime, session.endTime), style = CodeSm, color = OnSurfaceVariant.copy(alpha = 0.5f))
            }

            // Expanded: path preview + data table
            if (expanded && session.points.size >= 2) {
                Spacer(Modifier.height(8.dp))

                // Mini path canvas
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

                // Data columns
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
                            fontFamily = JetBrainsMonoFamily, fontSize = 8.sp,
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
                                    fontFamily = JetBrainsMonoFamily, fontSize = 9.sp,
                                    color = OnSurfaceVariant.copy(alpha = 0.7f)
                                ), modifier = Modifier.weight(pair.second), maxLines = 1)
                            }
                    }
                }
            }

            // Tap hint
            if (!expanded) {
                Spacer(Modifier.height(4.dp))
                Text(
                    if (isZh) "点击查看详情 ▸" else "Tap for details ▸",
                    style = CodeSm,
                    color = OnSurfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.clickable { expanded = true }
                )
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
