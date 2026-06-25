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
    val timestamp: Long,
    val bearing: Float = 0f
) {
    /** Compass direction as 16-point label: N, NNE, NE, ENE, E, ... */
    val compassDirection: String
        get() {
            val directions = arrayOf("N","NNE","NE","ENE","E","ESE","SE","SSE","S","SSW","SW","WSW","W","WNW","NW","NNW")
            val normalized = ((bearing % 360f) + 360f) % 360f
            val idx = ((normalized + 11.25f) / 22.5f).toInt() % 16
            return directions[idx]
        }
}
