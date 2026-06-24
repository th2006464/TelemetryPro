package com.telemetrypro.app.data

import androidx.compose.ui.graphics.Color
import com.telemetrypro.app.ui.theme.ConstellationColors

/**
 * GNSS constellation enumeration.
 * Maps to Android GnssStatus.CONSTELLATION_* constants.
 */
enum class Constellation(
    val label: String,
    val constellationType: Int,
    val color: Color
) {
    GPS("GPS", 1, ConstellationColors.GPS),
    SBAS("SBAS", 2, ConstellationColors.SBAS),
    GLONASS("GLONASS", 3, ConstellationColors.GLONASS),
    QZSS("QZSS", 4, ConstellationColors.QZSS),
    BEIDOU("BEIDOU", 5, ConstellationColors.BEIDOU),
    GALILEO("GALILEO", 6, ConstellationColors.GALILEO),
    IRNSS("IRNSS", 7, ConstellationColors.IRNSS),
    UNKNOWN("UNKN", 0, ConstellationColors.UNKNOWN);

    companion object {
        /**
         * Map Android GnssStatus constellation type constant to our enum.
         * Constants from android.location.GnssStatus:
         *   CONSTELLATION_GPS = 1, CONSTELLATION_SBAS = 2,
         *   CONSTELLATION_GLONASS = 3, CONSTELLATION_QZSS = 4,
         *   CONSTELLATION_BEIDOU = 5, CONSTELLATION_GALILEO = 6,
         *   CONSTELLATION_IRNSS = 7, CONSTELLATION_UNKNOWN = 0
         */
        fun fromConstellationType(type: Int): Constellation = when (type) {
            1 -> GPS
            2 -> SBAS
            3 -> GLONASS
            4 -> QZSS
            5 -> BEIDOU
            6 -> GALILEO
            7 -> IRNSS
            else -> UNKNOWN
        }
    }
}
