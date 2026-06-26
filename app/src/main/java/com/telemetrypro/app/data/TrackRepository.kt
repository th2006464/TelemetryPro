package com.telemetrypro.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*

/**
 * Manages GPS tracking sessions: start/stop recording,
 * accumulate distance, maintain session history, mark waypoints,
 * and support backtrack mode (re-tracing a previous session).
 */
class TrackRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("telemetry_pro_tracks", Context.MODE_PRIVATE)

    // --- Active recording state ---
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _currentSessionName = MutableStateFlow("")
    val currentSessionName: StateFlow<String> = _currentSessionName.asStateFlow()

    private val _currentDistanceKm = MutableStateFlow(0.0)
    val currentDistanceKm: StateFlow<Double> = _currentDistanceKm.asStateFlow()

    private val _currentPoints = MutableStateFlow<List<TrackPoint>>(emptyList())
    val currentPoints: StateFlow<List<TrackPoint>> = _currentPoints.asStateFlow()

    private val _currentWaypoints = MutableStateFlow<List<TrackPoint>>(emptyList())
    val currentWaypoints: StateFlow<List<TrackPoint>> = _currentWaypoints.asStateFlow()

    // --- Backtrack state ---
    private val _isBacktracking = MutableStateFlow(false)
    val isBacktracking: StateFlow<Boolean> = _isBacktracking.asStateFlow()

    private val _backtrackSourceId = MutableStateFlow<Long?>(null)
    val backtrackSourceId: StateFlow<Long?> = _backtrackSourceId.asStateFlow()

    private val _backtrackPoints = MutableStateFlow<List<TrackPoint>>(emptyList())
    val backtrackPoints: StateFlow<List<TrackPoint>> = _backtrackPoints.asStateFlow()

    private val _backtrackReversed = MutableStateFlow(false)
    val backtrackReversed: StateFlow<Boolean> = _backtrackReversed.asStateFlow()

    // --- Session history ---
    private val _sessions = MutableStateFlow<List<TrackSession>>(emptyList())
    val sessions: StateFlow<List<TrackSession>> = _sessions.asStateFlow()

    private var lastPoint: TrackPoint? = null
    private var lastBacktrackPoint: TrackPoint? = null

    init {
        loadSessions()
    }

    /** Start a new recording session with a given name */
    fun startRecording(name: String) {
        _isRecording.value = true
        _currentSessionName.value = name
        _currentDistanceKm.value = 0.0
        _currentPoints.value = emptyList()
        _currentWaypoints.value = emptyList()
        lastPoint = null
    }

    /** Stop current recording and save to history */
    fun stopRecording() {
        if (!_isRecording.value) return

        val points = _currentPoints.value
        val waypoints = _currentWaypoints.value
        val distance = _currentDistanceKm.value
        val name = _currentSessionName.value.ifBlank { defaultSessionName() }

        val session = TrackSession(
            name = name,
            startTime = points.firstOrNull()?.timestamp ?: System.currentTimeMillis(),
            endTime = points.lastOrNull()?.timestamp ?: System.currentTimeMillis(),
            points = points,
            waypoints = waypoints,
            totalDistanceKm = distance
        )

        val updated = _sessions.value.toMutableList()
        updated.add(0, session)
        _sessions.value = updated

        saveSessions()

        _isRecording.value = false
        _currentSessionName.value = ""
        _currentDistanceKm.value = 0.0
        _currentPoints.value = emptyList()
        _currentWaypoints.value = emptyList()
        lastPoint = null
    }

    /** Append a GPS fix to the current recording session */
    fun appendPoint(latitude: Double, longitude: Double, altitude: Double,
                    speedKmh: Float, accuracy: Float, timestamp: Long, bearing: Float = 0f) {
        if (!_isRecording.value) return

        val point = TrackPoint(latitude, longitude, altitude, speedKmh, accuracy, timestamp, bearing)
        val points = _currentPoints.value.toMutableList()
        points.add(point)
        _currentPoints.value = points

        lastPoint?.let { prev ->
            val dist = haversineKm(prev.latitude, prev.longitude, latitude, longitude)
            if (dist > 0.001) {
                _currentDistanceKm.value += dist
            }
        }
        lastPoint = point

        if (points.size > 28800) {
            stopRecording()
        }
    }

    /**
     * Mark the current GPS position as a named waypoint.
     * The waypoint is stored both in currentWaypoints and as a flagged point in currentPoints.
     */
    fun markWaypoint(latitude: Double, longitude: Double, altitude: Double,
                     accuracy: Float, timestamp: Long, label: String = "") {
        if (!_isRecording.value) return

        val wp = TrackPoint(
            latitude = latitude,
            longitude = longitude,
            altitude = altitude,
            speedKmh = 0f,
            accuracy = accuracy,
            timestamp = timestamp,
            isWaypoint = true,
            waypointLabel = label.ifBlank { "WP${_currentWaypoints.value.size + 1}" }
        )
        val wps = _currentWaypoints.value.toMutableList()
        wps.add(wp)
        _currentWaypoints.value = wps
    }

    /**
     * Start backtrack mode for a given source session.
     * The source session's path will be shown as reference; user's current
     * GPS positions are recorded as backtrack points.
     *
     * @param reversed If true, traverse the source path from end to start
     *                 (useful for "return to origin" scenarios)
     */
    fun startBacktrack(sourceSessionId: Long, reversed: Boolean = false) {
        val source = _sessions.value.find { it.id == sourceSessionId } ?: return

        _isBacktracking.value = true
        _backtrackSourceId.value = sourceSessionId
        _backtrackReversed.value = reversed
        _backtrackPoints.value = emptyList()
        lastBacktrackPoint = null
    }

    /** Append current GPS position to the active backtrack trail */
    fun appendBacktrackPoint(latitude: Double, longitude: Double, altitude: Double,
                              speedKmh: Float, accuracy: Float, timestamp: Long) {
        if (!_isBacktracking.value) return

        val point = TrackPoint(latitude, longitude, altitude, speedKmh, accuracy, timestamp)
        val pts = _backtrackPoints.value.toMutableList()
        pts.add(point)
        _backtrackPoints.value = pts
        lastBacktrackPoint = point
    }

    /** Stop backtracking and save the trail as a new session linked to its source */
    fun stopBacktrack() {
        if (!_isBacktracking.value) return

        val pts = _backtrackPoints.value
        val sourceId = _backtrackSourceId.value
        val source = _sessions.value.find { it.id == sourceId }
        val sourceName = source?.name ?: "track"

        // Compute distance walked during backtrack
        var distKm = 0.0
        for (i in 1 until pts.size) {
            distKm += haversineKm(pts[i-1].latitude, pts[i-1].longitude,
                                   pts[i].latitude, pts[i].longitude)
        }

        val session = TrackSession(
            name = "回溯 · $sourceName",
            startTime = pts.firstOrNull()?.timestamp ?: System.currentTimeMillis(),
            endTime = pts.lastOrNull()?.timestamp ?: System.currentTimeMillis(),
            points = pts,
            waypoints = source?.waypoints?.reversed()?.takeIf { _backtrackReversed.value }
                       ?: source?.waypoints ?: emptyList(),
            totalDistanceKm = distKm,
            backtrackSourceId = sourceId
        )

        val updated = _sessions.value.toMutableList()
        updated.add(0, session)
        _sessions.value = updated
        saveSessions()

        _isBacktracking.value = false
        _backtrackSourceId.value = null
        _backtrackPoints.value = emptyList()
        _backtrackReversed.value = false
        lastBacktrackPoint = null
    }

    /** Delete a session from history */
    fun deleteSession(sessionId: Long) {
        val updated = _sessions.value.filter { it.id != sessionId }
        _sessions.value = updated
        saveSessions()
    }

    /** Rename a session */
    fun renameSession(sessionId: Long, newName: String) {
        val updated = _sessions.value.map { session ->
            if (session.id == sessionId) session.copy(name = newName)
            else session
        }
        _sessions.value = updated
        saveSessions()
    }

    /** Get the source session for an active backtrack */
    fun getBacktrackSourceSession(): TrackSession? {
        val id = _backtrackSourceId.value ?: return null
        return _sessions.value.find { it.id == id }
    }

    // --- Persistence ---

    private fun saveSessions() {
        val editor = prefs.edit()
        editor.putInt("session_count", _sessions.value.size)

        _sessions.value.forEachIndexed { idx, session ->
            val key = "session_$idx"
            val metas = "${session.id}|${session.name}|${session.startTime}|${session.endTime}|${session.totalDistanceKm}|${session.backtrackSourceId ?: -1L}"
            editor.putString(key, metas)

            // Points: lat,lng,alt,spd,acc,ts,brg,wp,label;...
            val ptsStr = session.points.joinToString(";") { pt ->
                buildString {
                    append("${pt.latitude},${pt.longitude},${pt.altitude},${pt.speedKmh},${pt.accuracy},${pt.timestamp},${pt.bearing}")
                    if (pt.isWaypoint) {
                        append(",1,${pt.waypointLabel.replace(",", " ").replace(";", " ")}")
                    }
                }
            }
            editor.putString("${key}_pts", ptsStr)

            // Waypoints stored separately for quick access
            val wpStr = session.waypoints.joinToString(";") { wp ->
                "${wp.latitude},${wp.longitude},${wp.altitude},${wp.accuracy},${wp.timestamp},${wp.waypointLabel.replace(",", " ").replace(";", " ")}"
            }
            editor.putString("${key}_wp", wpStr)
        }
        editor.apply()
    }

    private fun loadSessions() {
        val count = prefs.getInt("session_count", 0)
        if (count == 0) return

        val loaded = mutableListOf<TrackSession>()
        for (i in 0 until count) {
            val key = "session_$i"
            val meta = prefs.getString(key, null) ?: continue
            val parts = meta.split("|")
            if (parts.size < 5) continue

            val ptsStr = prefs.getString("${key}_pts", "")
            val points = if (!ptsStr.isNullOrBlank()) {
                ptsStr.split(";").mapNotNull { ptStr ->
                    val vals = ptStr.split(",")
                    if (vals.size >= 7) {
                        val isWp = vals.size >= 8 && vals[7] == "1"
                        val label = if (vals.size >= 9) vals[8] else ""
                        TrackPoint(
                            vals[0].toDoubleOrNull() ?: return@mapNotNull null,
                            vals[1].toDoubleOrNull() ?: return@mapNotNull null,
                            vals[2].toDoubleOrNull() ?: 0.0,
                            vals[3].toFloatOrNull() ?: 0f,
                            vals[4].toFloatOrNull() ?: 0f,
                            vals[5].toLongOrNull() ?: 0L,
                            vals[6].toFloatOrNull() ?: 0f,
                            isWp,
                            label
                        )
                    } else null
                }
            } else emptyList()

            val wpStr = prefs.getString("${key}_wp", "")
            val waypoints = if (!wpStr.isNullOrBlank()) {
                wpStr.split(";").mapNotNull { wpStr ->
                    val vals = wpStr.split(",")
                    if (vals.size >= 6) {
                        TrackPoint(
                            vals[0].toDoubleOrNull() ?: return@mapNotNull null,
                            vals[1].toDoubleOrNull() ?: return@mapNotNull null,
                            vals[2].toDoubleOrNull() ?: 0.0,
                            0f,
                            vals[3].toFloatOrNull() ?: 0f,
                            vals[4].toLongOrNull() ?: 0L,
                            0f,
                            true,
                            vals[5]
                        )
                    } else null
                }
            } else emptyList()

            val backtrackSourceId = if (parts.size >= 6) {
                val v = parts[5].toLongOrNull() ?: -1L
                if (v >= 0) v else null
            } else null

            loaded.add(TrackSession(
                id = parts[0].toLongOrNull() ?: System.currentTimeMillis(),
                name = parts[1],
                startTime = parts[2].toLongOrNull() ?: 0L,
                endTime = parts[3].toLongOrNull() ?: 0L,
                points = points,
                waypoints = waypoints,
                totalDistanceKm = parts[4].toDoubleOrNull() ?: 0.0,
                backtrackSourceId = backtrackSourceId
            ))
        }
        _sessions.value = loaded
    }

    private fun defaultSessionName(): String {
        val count = _sessions.value.size + 1
        return "记录 #$count"
    }

    /** Haversine distance in km between two lat/lng points */
    private fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    /** Distance in meters between two GPS points */
    fun distanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        return haversineKm(lat1, lng1, lat2, lng2) * 1000.0
    }

    /**
     * Find the nearest point on the source path to a given position.
     * Returns (point, distanceMeters, index) or null if source empty.
     */
    fun nearestPointOnSource(lat: Double, lng: Double): Triple<TrackPoint, Double, Int>? {
        val source = getBacktrackSourceSession() ?: return null
        val sourcePts = if (_backtrackReversed.value) source.points.reversed() else source.points
        if (sourcePts.isEmpty()) return null

        var bestIdx = 0
        var bestDist = Double.MAX_VALUE
        sourcePts.forEachIndexed { idx, pt ->
            val d = distanceMeters(lat, lng, pt.latitude, pt.longitude)
            if (d < bestDist) { bestDist = d; bestIdx = idx }
        }
        return Triple(sourcePts[bestIdx], bestDist, bestIdx)
    }

    /** Bearing from current position to a target point, in degrees [0,360) */
    fun bearingTo(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val dLng = Math.toRadians(lng2 - lng1)
        val y = sin(dLng) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(dLng)
        var brg = Math.toDegrees(atan2(y, x))
        brg = (brg + 360) % 360
        return brg.toFloat()
    }
}
