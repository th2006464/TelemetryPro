package com.telemetrypro.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telemetrypro.app.LocaleHelper
import com.telemetrypro.app.data.LocationState
import com.telemetrypro.app.data.TrackPoint
import com.telemetrypro.app.data.TrackRepository
import com.telemetrypro.app.data.TrackSession
import com.telemetrypro.app.data.GpsFixStatus
import com.telemetrypro.app.ui.components.GridInfoBar
import com.telemetrypro.app.ui.components.GridMap
import com.telemetrypro.app.ui.components.NmeaFeed
import com.telemetrypro.app.ui.theme.*
import kotlin.math.*

@Composable
fun RecordScreen(
    state: LocationState,
    isRecording: Boolean,
    distanceKm: Double,
    sessions: List<TrackSession>,
    trackRepository: TrackRepository,
    onStartRecording: (String) -> Unit,
    onStopRecording: () -> Unit,
    onMarkWaypoint: (String) -> Unit,
    onStartBacktrack: (Long, Boolean) -> Unit,
    onStopBacktrack: () -> Unit,
    onDeleteSession: (Long) -> Unit,
    onRenameSession: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isZh = LocaleHelper.isZh(context)

    var selectedSubTab by remember { mutableIntStateOf(0) }

    // Collect backtrack state reactively
    val isBacktracking by trackRepository.isBacktracking.collectAsState()
    val backtrackPoints by trackRepository.backtrackPoints.collectAsState()
    val backtrackSourceId by trackRepository.backtrackSourceId.collectAsState()
    val backtrackReversed by trackRepository.backtrackReversed.collectAsState()
    val currentWaypoints by trackRepository.currentWaypoints.collectAsState()

    Column(modifier = modifier.fillMaxSize().background(Background)) {
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = SurfaceContainerLow,
            contentColor = OnSurfaceVariant,
            indicator = {},
            divider = {}
        ) {
            val tabs = listOf(
                if (isZh) "记录" else "RECORD",
                if (isZh) "回溯" else "BACKTRACK"
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
                currentWaypoints = currentWaypoints,
                onStartRecording = onStartRecording,
                onStopRecording = onStopRecording,
                onMarkWaypoint = onMarkWaypoint
            )
            1 -> PlaybackTab(
                state = state,
                sessions = sessions,
                trackRepository = trackRepository,
                isBacktracking = isBacktracking,
                backtrackPoints = backtrackPoints,
                backtrackSourceId = backtrackSourceId,
                backtrackReversed = backtrackReversed,
                onStartBacktrack = onStartBacktrack,
                onStopBacktrack = onStopBacktrack,
                onDelete = onDeleteSession,
                onRename = onRenameSession
            )
        }
    }
}

// ============================================================
// TAB 0: RECORDING — Grid map + waypoint marking
// ============================================================

@Composable
private fun RecordingTab(
    state: LocationState,
    isRecording: Boolean,
    distanceKm: Double,
    currentWaypoints: List<TrackPoint>,
    onStartRecording: (String) -> Unit,
    onStopRecording: () -> Unit,
    onMarkWaypoint: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isZh = LocaleHelper.isZh(context)
    var showWaypointDialog by remember { mutableStateOf(false) }
    var waypointName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(8.dp))

        // ===== GRID MAP =====
        GridMap(
            recordedPoints = if (isRecording) state.recordingPoints else emptyList(),
            waypoints = currentWaypoints,
            currentPosition = if (state.fixStatus == GpsFixStatus.FIXED) {
                TrackPoint(state.latitude, state.longitude, state.altitudeMeters,
                          state.speedKmh, state.accuracy, System.currentTimeMillis())
            } else null,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // ===== INFO BAR =====
        val coordText = if (state.fixStatus == GpsFixStatus.FIXED) {
            "%.5f, %.5f".format(state.latitude, state.longitude)
        } else if (isZh) "搜索GPS中..." else "Searching GPS..."
        GridInfoBar(
            coordText = coordText,
            speedText = "${String.format("%.0f", state.speedKmh)} km/h",
            distanceText = if (isRecording) "${String.format("%.2f", distanceKm)} km" else "",
            waypointCount = currentWaypoints.size
        )

        Spacer(Modifier.height(8.dp))

        // ===== CONTROL CARD =====
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
                                        if (isZh) "记录 #${(System.currentTimeMillis() / 1000) % 1000}"
                                        else "Track #${(System.currentTimeMillis() / 1000) % 1000}"
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
                    // Active recording
                    Column {
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
                                    "${String.format("%.2f", distanceKm)} km · ${state.recordingPoints.size} pts · ${currentWaypoints.size} WP",
                                    style = CodeSm,
                                    color = OnSurfaceVariant.copy(alpha = 0.6f)
                                )
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

                        Spacer(Modifier.height(12.dp))

                        // Mark waypoint button (prominent)
                        val gpsReady = state.fixStatus == GpsFixStatus.FIXED
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (gpsReady) Color(0xFFFF6B35).copy(alpha = 0.15f)
                                    else SurfaceContainerLowest,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (gpsReady) Color(0xFFFF6B35) else OutlineVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable(enabled = gpsReady) {
                                    showWaypointDialog = true
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                if (gpsReady)
                                    (if (isZh) "📍 记录点 (当前位置)" else "📍 MARK WAYPOINT")
                                else
                                    (if (isZh) "等待GPS定位..." else "Waiting for GPS..."),
                                style = LabelCaps,
                                color = if (gpsReady) Color(0xFFFF6B35) else OnSurfaceVariant.copy(alpha = 0.4f),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // ===== NMEA =====
        NmeaFeed(
            lines = state.nmeaLogLines,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(Modifier.height(80.dp))
    }

    // Waypoint naming dialog
    if (showWaypointDialog) {
        AlertDialog(
            onDismissRequest = {
                showWaypointDialog = false
                waypointName = ""
            },
            title = { Text(if (isZh) "记录标记点" else "Mark Waypoint") },
            text = {
                Column {
                    Text(
                        if (isZh) "当前位置: %.5f, %.5f".format(state.latitude, state.longitude)
                        else "Position: %.5f, %.5f".format(state.latitude, state.longitude),
                        style = CodeSm, color = OnSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(if (isZh) "标记名称 (可选):" else "Label (optional):",
                         style = LabelCaps, color = OnSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceContainerLowest, RoundedCornerShape(6.dp))
                            .border(1.dp, PrimaryFixedDim, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        BasicTextField(
                            value = waypointName,
                            onValueChange = { waypointName = it },
                            textStyle = TextStyle(
                                fontFamily = JetBrainsMonoFamily, fontSize = 14.sp, color = OnSurface
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onMarkWaypoint(waypointName)
                    showWaypointDialog = false
                    waypointName = ""
                }) {
                    Text(if (isZh) "记录" else "MARK", color = Color(0xFFFF6B35), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showWaypointDialog = false
                    waypointName = ""
                }) {
                    Text(if (isZh) "取消" else "Cancel")
                }
            }
        )
    }
}

// ============================================================
// TAB 1: PLAYBACK — Session list + backtrack mode
// ============================================================

@Composable
private fun PlaybackTab(
    state: LocationState,
    sessions: List<TrackSession>,
    trackRepository: TrackRepository,
    isBacktracking: Boolean,
    backtrackPoints: List<TrackPoint>,
    backtrackSourceId: Long?,
    backtrackReversed: Boolean,
    onStartBacktrack: (Long, Boolean) -> Unit,
    onStopBacktrack: () -> Unit,
    onDelete: (Long) -> Unit,
    onRename: (Long, String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isZh = LocaleHelper.isZh(context)
    var selectedSessionId by remember { mutableLongStateOf(-1L) }
    var showBacktrackConfirm by remember { mutableLongStateOf(-1L) }
    var confirmReversed by remember { mutableStateOf(false) }

    if (sessions.isEmpty() && !isBacktracking) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (isZh) "暂无轨迹记录\n请先在记录标签页录制轨迹" else "No tracks yet.\nRecord a track first.",
                style = CodeSm, color = OnSurfaceVariant.copy(alpha = 0.4f)
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // ===== ACTIVE BACKTRACK VIEW =====
        if (isBacktracking) {
            item {
                val sourceSession = sessions.find { it.id == backtrackSourceId }
                val sourcePath = sourceSession?.points ?: emptyList()
                val sourceWps = sourceSession?.waypoints ?: emptyList()
                val effectiveSource = if (backtrackReversed) sourcePath.reversed() else sourcePath
                val effectiveWps = if (backtrackReversed) sourceWps.reversed() else sourceWps

                // Compute deviation from source path
                val currentPos = if (state.fixStatus == GpsFixStatus.FIXED) {
                    TrackPoint(state.latitude, state.longitude, state.altitudeMeters,
                              state.speedKmh, state.accuracy, System.currentTimeMillis())
                } else null
                val nearest = currentPos?.let { pos ->
                    trackRepository.nearestPointOnSource(pos.latitude, pos.longitude)
                }
                val deviation = nearest?.second ?: 0.0
                val offPath = deviation > 15.0

                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        if (isZh) "● 回溯中 · ${sourceSession?.name ?: "未知"}" else "● BACKTRACKING · ${sourceSession?.name ?: "unknown"}",
                        style = TelemetryMd, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    GridMap(
                        sourcePath = effectiveSource,
                        waypoints = effectiveWps,
                        backtrackPoints = backtrackPoints,
                        currentPosition = currentPos,
                        deviationMeters = deviation,
                        offPath = offPath,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Backtrack stats
                    BacktrackStatsBar(
                        deviation = deviation,
                        nearestWaypoint = nearest?.let {
                            findNextWaypoint(effectiveWps, it.third, currentPos, trackRepository)
                        },
                        walkedKm = computeWalkedKm(backtrackPoints),
                        remainingKm = computeRemainingKm(effectiveSource, nearest?.third ?: 0),
                        isReversed = backtrackReversed,
                        isZh = isZh
                    )

                    Spacer(Modifier.height(8.dp))

                    // Stop backtrack button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .background(ErrorContainer.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .border(1.dp, Error, RoundedCornerShape(8.dp))
                            .clickable { onStopBacktrack() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (isZh) "■ 停止回溯并保存" else "■ STOP & SAVE",
                            style = LabelCaps, color = Error, fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        // Session list header
        item {
            Text(
                if (isZh) "历史轨迹" else "TRACK HISTORY",
                style = LabelCaps, color = OnSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Session cards
        items(sessions) { session ->
            PlaybackSessionCard(
                session = session,
                isSelected = session.id == selectedSessionId,
                isBacktrackingThis = isBacktracking && backtrackSourceId == session.id,
                onSelect = {
                    selectedSessionId = if (selectedSessionId == session.id) -1L else session.id
                },
                onStartBacktrack = { reversed ->
                    showBacktrackConfirm = session.id
                    confirmReversed = reversed
                },
                onDelete = { onDelete(session.id) },
                onRename = { newName -> onRename(session.id, newName) }
            )
        }
    }

    // GPS position mismatch confirmation dialog
    if (showBacktrackConfirm >= 0) {
        val session = sessions.find { it.id == showBacktrackConfirm }
        val sessionEndPt = if (confirmReversed) session?.points?.firstOrNull() else session?.points?.lastOrNull()
        val mismatchMeters = if (sessionEndPt != null && state.fixStatus == GpsFixStatus.FIXED) {
            trackRepository.distanceMeters(state.latitude, state.longitude,
                                           sessionEndPt.latitude, sessionEndPt.longitude)
        } else null
        val hasMismatch = mismatchMeters != null && mismatchMeters > 50.0

        AlertDialog(
            onDismissRequest = { showBacktrackConfirm = -1L },
            title = { Text(if (isZh) "开始回溯" else "Start Backtrack") },
            text = {
                Column {
                    Text(
                        if (isZh) "轨迹: ${session?.name ?: ""}"
                        else "Track: ${session?.name ?: ""}",
                        style = TelemetryMd, color = PrimaryFixedDim, fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    if (confirmReversed) {
                        Text(if (isZh) "模式: 反向回溯 (终点→起点)" else "Mode: Reverse (end→start)",
                             style = CodeSm, color = OnSurfaceVariant)
                    } else {
                        Text(if (isZh) "模式: 正向回溯 (起点→终点)" else "Mode: Forward (start→end)",
                             style = CodeSm, color = OnSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))

                    if (mismatchMeters != null) {
                        Text(
                            if (isZh) "当前GPS与原始终点距离: ${String.format("%.0f", mismatchMeters)} 米"
                            else "Current GPS to origin: ${String.format("%.0f", mismatchMeters)} m",
                            style = CodeSm,
                            color = if (hasMismatch) Error else Secondary
                        )
                        if (hasMismatch) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (isZh) "⚠ 警告: 当前位置与原始终点相距较远\n建议先到达原始终点再开始回溯"
                                else "⚠ Warning: current position is far from origin\nConsider reaching the origin first",
                                style = CodeSm, color = Error
                            )
                        }
                    } else {
                        Text(if (isZh) "⚠ 无法获取当前GPS定位" else "⚠ No GPS fix available",
                             style = CodeSm, color = Error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onStartBacktrack(showBacktrackConfirm, confirmReversed)
                    showBacktrackConfirm = -1L
                }) {
                    Text(if (isZh) "确认开始" else "Confirm", color = Secondary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBacktrackConfirm = -1L }) {
                    Text(if (isZh) "取消" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun BacktrackStatsBar(
    deviation: Double,
    nearestWaypoint: WaypointInfo?,
    walkedKm: Double,
    remainingKm: Double,
    isReversed: Boolean,
    isZh: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                if (isZh) "已走" else "Walked",
                style = CodeSm, color = OnSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                "${String.format("%.2f", walkedKm)} km",
                style = TelemetryMd, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                if (isZh) "剩余" else "Remaining",
                style = CodeSm, color = OnSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                "${String.format("%.2f", remainingKm)} km",
                style = TelemetryMd, color = PrimaryFixedDim
            )
        }
        if (nearestWaypoint != null) {
            Column(modifier = Modifier.weight(1.2f)) {
                Text(
                    if (isZh) "下一标记" else "Next WP",
                    style = CodeSm, color = OnSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    "${nearestWaypoint.label} ${String.format("%.0f", nearestWaypoint.distanceM)}m ${nearestWaypoint.direction}",
                    style = CodeSm, color = Color(0xFFFF6B35), fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

private data class WaypointInfo(val label: String, val distanceM: Double, val direction: String)

private fun findNextWaypoint(
    waypoints: List<TrackPoint>,
    currentSourceIdx: Int,
    currentPos: TrackPoint?,
    repo: TrackRepository
): WaypointInfo? {
    if (waypoints.isEmpty() || currentPos == null) return null
    // Find first waypoint ahead of current source index
    val next = waypoints.firstOrNull { wp ->
        val wpIdx = waypoints.indexOf(wp)
        wpIdx >= currentSourceIdx / (waypoints.size.toFloat() / waypoints.size)
    } ?: waypoints.lastOrNull()
    ?: return null

    val dist = repo.distanceMeters(currentPos.latitude, currentPos.longitude,
                                    next.latitude, next.longitude)
    val bearing = repo.bearingTo(currentPos.latitude, currentPos.longitude,
                                  next.latitude, next.longitude)
    val dir = bearingToDir(bearing)
    val label = next.waypointLabel.ifBlank { "WP" }
    return WaypointInfo(label, dist, dir)
}

private fun bearingToDir(b: Float): String {
    val dirs = arrayOf("北","东北","东","东南","南","西南","西","西北")
    val idx = (((b + 22.5f) % 360f) / 45f).toInt() % 8
    return dirs[idx]
}

private fun computeWalkedKm(points: List<TrackPoint>): Double {
    if (points.size < 2) return 0.0
    var d = 0.0
    for (i in 1 until points.size) {
        val a = points[i-1]; val b = points[i]
        val r = 6371.0
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLng = Math.toRadians(b.longitude - a.longitude)
        val x = sin(dLat/2)*sin(dLat/2) + cos(Math.toRadians(a.latitude))*cos(Math.toRadians(b.latitude))*sin(dLng/2)*sin(dLng/2)
        d += r * 2 * atan2(sqrt(x), sqrt(1-x))
    }
    return d
}

private fun computeRemainingKm(sourcePts: List<TrackPoint>, currentIdx: Int): Double {
    if (currentIdx >= sourcePts.size - 1) return 0.0
    var d = 0.0
    for (i in currentIdx until sourcePts.size - 1) {
        val a = sourcePts[i]; val b = sourcePts[i+1]
        val r = 6371.0
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLng = Math.toRadians(b.longitude - a.longitude)
        val x = sin(dLat/2)*sin(dLat/2) + cos(Math.toRadians(a.latitude))*cos(Math.toRadians(b.latitude))*sin(dLng/2)*sin(dLng/2)
        d += r * 2 * atan2(sqrt(x), sqrt(1-x))
    }
    return d
}

@Composable
private fun PlaybackSessionCard(
    session: TrackSession,
    isSelected: Boolean,
    isBacktrackingThis: Boolean,
    onSelect: () -> Unit,
    onStartBacktrack: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isZh = LocaleHelper.isZh(context)
    var editing by remember { mutableStateOf(false) }
    var editName by remember(session.name) { mutableStateOf(session.name) }

    val borderColor = if (isSelected) PrimaryFixedDim.copy(alpha = 0.5f) else TileBorder

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .background(
                if (isSelected) PrimaryContainer.copy(alpha = 0.08f) else TileBackground,
                RoundedCornerShape(10.dp)
            )
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onSelect() }
            .padding(12.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelected) {
                        Box(Modifier.width(8.dp).height(8.dp).background(PrimaryFixedDim, RoundedCornerShape(4.dp)))
                        Spacer(Modifier.width(8.dp))
                    }
                    if (editing) {
                        Box(
                            modifier = Modifier.width(140.dp)
                                .background(SurfaceContainerLowest, RoundedCornerShape(6.dp))
                                .border(1.dp, PrimaryFixedDim, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            BasicTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                textStyle = TextStyle(fontFamily = JetBrainsMonoFamily, fontSize = 14.sp, color = OnSurface),
                                singleLine = true
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Text("✓", style = LabelCaps, color = Secondary,
                            modifier = Modifier.clickable { onRename(editName); editing = false })
                    } else {
                        Text(
                            session.name.ifBlank { if (isZh) "记录" else "Track" },
                            style = TelemetryMd, color = PrimaryFixedDim, fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { editing = true }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${String.format("%.2f", session.totalDistanceKm)} km",
                         style = TelemetryMd, color = Secondary)
                    Text("×", style = CodeSm, color = OnSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.clickable { onDelete() })
                }
            }

            // Meta
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("${session.points.size} pts", style = CodeSm, color = OnSurfaceVariant.copy(alpha = 0.6f))
                if (session.waypoints.isNotEmpty()) {
                    Text("${session.waypoints.size} WP", style = CodeSm, color = Color(0xFFFF6B35))
                }
                Text(formatDuration(session.startTime, session.endTime),
                     style = CodeSm, color = OnSurfaceVariant.copy(alpha = 0.5f))
                if (session.backtrackSourceId != null) {
                    Text(if (isZh) "回溯轨迹" else "BACKTRACK", style = CodeSm, color = Color(0xFF4CAF50))
                }
            }

            // Action buttons when selected
            if (isSelected && !isBacktrackingThis) {
                Spacer(Modifier.height(10.dp))

                // Track preview mini map
                if (session.points.size >= 2) {
                    GridMap(
                        recordedPoints = session.points,
                        waypoints = session.waypoints,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Forward backtrack
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF4CAF50).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(8.dp))
                            .clickable { onStartBacktrack(false) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (isZh) "▶ 正向回溯" else "▶ FORWARD",
                            style = LabelCaps, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold
                        )
                    }
                    // Reverse backtrack
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF2196F3).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF2196F3), RoundedCornerShape(8.dp))
                            .clickable { onStartBacktrack(true) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (isZh) "◀ 反向回溯" else "◀ REVERSE",
                            style = LabelCaps, color = Color(0xFF2196F3), fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (isBacktrackingThis) {
                Spacer(Modifier.height(8.dp))
                Text(
                    if (isZh) "● 正在回溯此轨迹" else "● Currently backtracking",
                    style = CodeSm, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold
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
