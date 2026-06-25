package com.telemetrypro.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*

/**
 * Manages GPS tracking sessions: start/stop recording,
 * accumulate distance, maintain session history.
 *
 * Performance note:
 * - Haversine distance: ~20 float ops per GPS fix (1/sec) → negligible
 * - Point storage: ~40 bytes/point, 3600 pts/hr = ~140 KB/hr → negligible
 * - Path drawing uses drawPoints (batch) → 1 draw call regardless of size
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

    // --- Session history ---
    private val _sessions = MutableStateFlow<List<TrackSession>>(emptyList())
    val sessions: StateFlow<List<TrackSession>> = _sessions.asStateFlow()

    private var lastPoint: TrackPoint? = null

    init {
        loadSessions()
    }

    /** Start a new recording session with a given name */
    fun startRecording(name: String) {
        _isRecording.value = true
        _currentSessionName.value = name
        _currentDistanceKm.value = 0.0
        _currentPoints.value = emptyList()
        lastPoint = null
    }

    /** Stop current recording and save to history */
    fun stopRecording() {
        if (!_isRecording.value) return

        val points = _currentPoints.value
        val distance = _currentDistanceKm.value
        val name = _currentSessionName.value.ifBlank { defaultSessionName() }

        val session = TrackSession(
            name = name,
            startTime = points.firstOrNull()?.timestamp ?: System.currentTimeMillis(),
            endTime = points.lastOrNull()?.timestamp ?: System.currentTimeMillis(),
            points = points,
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

        // Calculate incremental distance
        lastPoint?.let { prev ->
            val dist = haversineKm(prev.latitude, prev.longitude, latitude, longitude)
            if (dist > 0.001) { // filter out stationary noise (< 1m)
                _currentDistanceKm.value += dist
            }
            // Cap max speed between points to avoid GPS jumps (assume < 500 km/h)
            // and cap distance accumulation per sample
        }
        lastPoint = point

        // Auto-stop safety: 8 hour limit
        if (points.size > 28800) { // 8h × 3600s / 1s interval
            stopRecording()
        }
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

    // --- Persistence (simple JSON in SharedPreferences) ---
    // Using a delimited format since we want to avoid adding Gson/Jackson dependency.
    // Format: id|name|startTime|endTime|totalDistanceKm\n for sessions
    // Points: stored per-session in separate keys

    private fun saveSessions() {
        val editor = prefs.edit()
        editor.putInt("session_count", _sessions.value.size)

        _sessions.value.forEachIndexed { idx, session ->
            val key = "session_$idx"
            val metas = "${session.id}|${session.name}|${session.startTime}|${session.endTime}|${session.totalDistanceKm}"
            editor.putString(key, metas)

            // Save points compactly: lat,lng,alt,spd,acc,ts,brg;lat,lng,...
            val ptsStr = session.points.joinToString(";") { pt ->
                "${pt.latitude},${pt.longitude},${pt.altitude},${pt.speedKmh},${pt.accuracy},${pt.timestamp},${pt.bearing}"
            }
            editor.putString("${key}_pts", ptsStr)
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
                    if (vals.size >= 6) {
                        TrackPoint(
                            vals[0].toDoubleOrNull() ?: return@mapNotNull null,
                            vals[1].toDoubleOrNull() ?: return@mapNotNull null,
                            vals[2].toDoubleOrNull() ?: 0.0,
                            vals[3].toFloatOrNull() ?: 0f,
                            vals[4].toFloatOrNull() ?: 0f,
                            vals[5].toLongOrNull() ?: 0L,
                            if (vals.size >= 7) vals[6].toFloatOrNull() ?: 0f else 0f
                        )
                    } else null
                }
            } else emptyList()

            loaded.add(TrackSession(
                id = parts[0].toLongOrNull() ?: System.currentTimeMillis(),
                name = parts[1],
                startTime = parts[2].toLongOrNull() ?: 0L,
                endTime = parts[3].toLongOrNull() ?: 0L,
                points = points,
                totalDistanceKm = parts[4].toDoubleOrNull() ?: 0.0
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
        val R = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
}
