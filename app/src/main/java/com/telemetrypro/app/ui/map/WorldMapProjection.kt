package com.telemetrypro.app.ui.map

import kotlin.math.ln
import kotlin.math.tan

object WorldMapProjection {
    private const val MAX_LAT_DEG = 85.0
    private val maxLatRad = (MAX_LAT_DEG * Math.PI / 180.0).toFloat()
    private val mercMax = ln(tan(Math.PI.toFloat() / 4f + maxLatRad / 2f))

    fun mercatorY(latDeg: Double, gridHeight: Int): Float {
        val latRad = (latDeg * Math.PI / 180.0).toFloat().coerceIn(-maxLatRad, maxLatRad)
        val yMerc = ln(tan(Math.PI.toFloat() / 4f + latRad / 2f))
        return (gridHeight * (1f - yMerc / mercMax) / 2f)
    }

    fun longitudeToGridX(lngDeg: Double, gridWidth: Int): Float {
        return ((lngDeg + 180.0) / 360.0 * gridWidth).toFloat()
    }
}
