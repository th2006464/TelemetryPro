package com.telemetrypro.app.data

/**
 * GNSS satellite detailed information.
 */
data class SatelliteInfo(
    /** Android SVID (space vehicle ID, 1-32 for GPS, etc.) */
    val svid: Int,

    /** Which constellation this satellite belongs to */
    val constellation: Constellation,

    /** Signal-to-Noise Ratio in dB-Hz (0 = no signal) */
    val snr: Float,

    /** Elevation angle in degrees (0 = horizon, 90 = zenith) */
    val elevation: Float,

    /** Azimuth angle in degrees (0 = north, 90 = east) */
    val azimuth: Float,

    /** Whether this satellite is used in the current position fix */
    val usedInFix: Boolean,

    /** Whether almanac data is available */
    val hasAlmanac: Boolean = false,

    /** Whether ephemeris data is available */
    val hasEphemeris: Boolean = false
) {
    /** Lock status derived from usedInFix + SNR */
    val lockStatus: LockStatus
        get() = when {
            usedInFix && snr > 0 -> LockStatus.LOCKED
            snr > 0 -> LockStatus.SYNCING
            else -> LockStatus.SEARCHING
        }
}

enum class LockStatus(val label: String) {
    LOCKED("LOCKED"),
    SYNCING("SYNC"),
    SEARCHING("SEARCH")
}

/**
 * Aggregated stats for a single constellation.
 */
data class ConstellationStats(
    val constellation: Constellation,
    val totalVisible: Int,
    val usedInFix: Int,
    val avgSnr: Float,
    val bestSnr: Float,
    val satellites: List<SatelliteInfo>
)
