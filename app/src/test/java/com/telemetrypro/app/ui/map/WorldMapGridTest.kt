package com.telemetrypro.app.ui.map

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.floor

class WorldMapGridTest {
    private fun toGrid(lon: Double, lat: Double): Pair<Int, Int> {
        val gx = floor(WorldMapProjection.longitudeToGridX(lon, WorldMapGrid.GRID_W).toDouble())
            .toInt()
            .coerceIn(0, WorldMapGrid.GRID_W - 1)
        val gy = floor(WorldMapProjection.mercatorY(lat, WorldMapGrid.GRID_H).toDouble())
            .toInt()
            .coerceIn(0, WorldMapGrid.GRID_H - 1)
        return gx to gy
    }

    private fun isLandNear(lon: Double, lat: Double, distance: Int = 1): Boolean {
        val (gx, gy) = toGrid(lon, lat)
        for (dy in -distance..distance) {
            for (dx in -distance..distance) {
                if (WorldMapGrid.isLand(gx + dx, gy + dy)) {
                    return true
                }
            }
        }
        return false
    }

    @Test
    fun representativeLandPointsStayOnLand() {
        assertTrue(isLandNear(116.4, 39.9))   // Beijing
        assertTrue(isLandNear(139.8, 35.7))   // Tokyo
        assertTrue(isLandNear(127.0, 37.6))   // Seoul
        assertTrue(isLandNear(121.5, 25.0))   // Taipei
        assertTrue(isLandNear(114.2, 22.3))   // Hong Kong
        assertTrue(isLandNear(103.8, 1.4))    // Singapore
        assertTrue(isLandNear(151.2, -33.9))  // Sydney
        assertTrue(isLandNear(-0.1, 51.5))    // London
    }

    @Test
    fun representativeSeaPointsStayAtSea() {
        assertFalse(isLandNear(150.0, 15.0, distance = 0))   // West Pacific
        assertFalse(isLandNear(155.0, 0.0, distance = 0))    // Equatorial Pacific
        assertFalse(isLandNear(135.0, 20.0, distance = 0))   // Philippine Sea
        assertFalse(isLandNear(-35.0, 30.0, distance = 0))   // North Atlantic
        assertFalse(isLandNear(90.0, -55.0, distance = 0))   // Southern Ocean
    }

    @Test
    fun allCitiesStayOnLandOrImmediateCoast() {
        val failures = WorldMapCities.majorCities.filterNot { city ->
            isLandNear(city.lon.toDouble(), city.lat.toDouble())
        }
        assertTrue("Cities not on land: ${failures.joinToString { it.name }}", failures.isEmpty())
    }
}
