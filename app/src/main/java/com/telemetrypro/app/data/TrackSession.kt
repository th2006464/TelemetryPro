package com.telemetrypro.app.data

/**
 * A recorded GPS tracking session with distance measurement.
 */
data class TrackSession(
    val id: Long = System.currentTimeMillis(),
    val name: String = "",
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = 0L,
    val points: List<TrackPoint> = emptyList(),
    val totalDistanceKm: Double = 0.0
)

/**
 * A single GPS point recorded during a tracking session.
 */
data class TrackPoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speedKmh: Float,
    val accuracy: Float,
    val timestamp: Long
)
