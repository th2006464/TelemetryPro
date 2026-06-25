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
    /** Compass direction as 16-point bilingual label: 北 N / 东北 NE / 东 E ... */
    val compassDirection: String
        get() {
            // 中文 / 英文 双语对照
            val directions = arrayOf(
                "北 N", "东北偏北 NNE", "东北 NE", "东北偏东 ENE",
                "东 E", "东南偏东 ESE", "东南 SE", "东南偏南 SSE",
                "南 S", "西南偏南 SSW", "西南 SW", "西南偏西 WSW",
                "西 W", "西北偏西 WNW", "西北 NW", "西北偏北 NNW"
            )
            val normalized = ((bearing % 360f) + 360f) % 360f
            val idx = ((normalized + 11.25f) / 22.5f).toInt() % 16
            return directions[idx]
        }

    /** Short English-only compass label: N / NE / E ... */
    val compassShort: String
        get() {
            val directions = arrayOf("N","NNE","NE","ENE","E","ESE","SE","SSE","S","SSW","SW","WSW","W","WNW","NW","NNW")
            val normalized = ((bearing % 360f) + 360f) % 360f
            val idx = ((normalized + 11.25f) / 22.5f).toInt() % 16
            return directions[idx]
        }
}
