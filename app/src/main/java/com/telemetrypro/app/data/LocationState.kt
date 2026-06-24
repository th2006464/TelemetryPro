package com.telemetrypro.app.data

/**
 * GPS fix quality enum — the three core states.
 */
enum class GpsFixStatus(val label: String) {
    FIXED("3D FIX"),        // Valid lat/lng available
    SEARCHING("ACQUIRING"),  // Satellites visible but no fix yet
    NO_SIGNAL("NO SIGNAL")   // No GNSS at all
}

/**
 * Flight mode detection result.
 */
enum class FlightMode(val label: String) {
    NONE(""),                        // Normal ground movement
    POSSIBLE_FLIGHT("FLIGHT?"),      // Speed > 200 km/h detected
    HIGH_ALTITUDE("HIGH ALT")        // Many satellites but low precision
}

/**
 * UI state class consumed by all screens.
 */
data class LocationState(
    // GPS Fix
    val fixStatus: GpsFixStatus = GpsFixStatus.NO_SIGNAL,
    val flightMode: FlightMode = FlightMode.NONE,

    // Position
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracy: Float = 0f,   // meters

    // Altitude
    val altitude: Double = 0.0,  // meters above WGS84 ellipsoid

    // Speed
    val speed: Float = 0f,       // m/s (convert to km/h for display)

    // Time
    val timestamp: Long = 0L,

    // Satellites
    val totalSatellites: Int = 0,
    val usedSatellites: Int = 0,
    val satellites: List<SatelliteInfo> = emptyList(),
    val constellationStats: List<ConstellationStats> = emptyList(),

    // NMEA log buffer (last ~20 lines)
    val nmeaLogLines: List<String> = emptyList(),

    // Derived display values (computed in ViewModel)
    val speedKmh: Float = 0f,
    val altitudeMeters: Double = 0.0,
    val latitudeDms: String = "",
    val longitudeDms: String = ""
)
