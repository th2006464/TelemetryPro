package com.telemetrypro.app.ui.map

import kotlin.math.ln
import kotlin.math.tan

object WorldMapProjection {
    // Asymmetric Mercator bounds: top edge at +85°N (Arctic), bottom edge at -60°S.
    // Antarctica is intentionally excluded — it consumes huge vertical space under
    // Mercator (poles → infinity) and the app will never be used there. Cutting at
    // -60° keeps Cape Horn (-55.7°), Tasmania (-43.7°), and NZ South Island (-46.6°).
    private const val MAX_LAT_NORTH_DEG = 85.0
    private const val MAX_LAT_SOUTH_DEG = -60.0

    private val maxLatNorthRad = (MAX_LAT_NORTH_DEG * Math.PI / 180.0).toFloat()
    private val maxLatSouthRad = (MAX_LAT_SOUTH_DEG * Math.PI / 180.0).toFloat()

    // Mercator y at top and bottom edges (top is positive, bottom is negative).
    private val mercTop = ln(tan(Math.PI.toFloat() / 4f + maxLatNorthRad / 2f))
    private val mercBottom = ln(tan(Math.PI.toFloat() / 4f + maxLatSouthRad / 2f))
    private val mercSpan = mercTop - mercBottom

    fun mercatorY(latDeg: Double, gridHeight: Int): Float {
        val latRad = (latDeg * Math.PI / 180.0).toFloat()
            .coerceIn(maxLatSouthRad, maxLatNorthRad)
        val yMerc = ln(tan(Math.PI.toFloat() / 4f + latRad / 2f))
        // y=0 at +85°N, y=gridHeight at -60°S
        return gridHeight * (mercTop - yMerc) / mercSpan
    }

    fun longitudeToGridX(lngDeg: Double, gridWidth: Int): Float {
        return ((lngDeg + 180.0) / 360.0 * gridWidth).toFloat()
    }
}
