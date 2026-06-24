package com.telemetrypro.app.ui.map

/**
 * Simplified continent outlines for dot-matrix world map rendering.
 * Each continent is a list of polygon rings, each ring is a list of (longitude, latitude) points.
 * Coordinates stored as Float pairs for memory efficiency.
 */
object WorldMapData {

    data class Polygon(val points: List<Pair<Float, Float>>)
    data class Continent(val name: String, val polygons: List<Polygon>)

    val continents: List<Continent> = listOf(
        Continent("North America", listOf(
            Polygon(listOf(
                -168.1f to 65.6f, -166.1f to 62.3f, -162.1f to 61.4f, -158.0f to 58.0f,
                -152.5f to 58.0f, -148.0f to 60.5f, -143.0f to 60.2f, -140.0f to 59.8f,
                -137.0f to 58.8f, -135.0f to 58.5f, -133.0f to 57.0f, -131.5f to 55.5f,
                -130.0f to 54.8f, -129.0f to 54.0f, -128.0f to 52.8f, -127.0f to 51.5f,
                -126.0f to 50.2f, -125.0f to 49.0f, -124.5f to 48.0f, -124.7f to 46.8f,
                -124.2f to 45.2f, -124.0f to 43.0f, -121.0f to 37.0f, -120.5f to 36.5f,
                -117.3f to 33.0f, -117.0f to 32.5f, -116.3f to 31.0f, -115.0f to 30.0f,
                -113.0f to 29.0f, -111.0f to 28.0f, -109.0f to 26.5f, -107.5f to 26.0f,
                -97.5f to 25.8f, -97.0f to 26.0f, -95.0f to 28.0f, -92.0f to 29.3f,
                -90.0f to 29.5f, -89.0f to 29.7f, -85.0f to 29.8f, -83.0f to 29.5f,
                -82.0f to 28.5f, -81.0f to 27.0f, -80.5f to 26.0f, -80.0f to 25.3f,
                -80.2f to 24.0f, -81.0f to 22.5f, -82.5f to 21.0f, -83.0f to 19.5f,
                -84.0f to 16.0f, -85.0f to 14.0f, -86.0f to 13.0f, -87.0f to 12.0f,
                -88.0f to 10.5f, -87.0f to 9.0f, -85.0f to 9.5f, -83.5f to 9.5f,
                -82.0f to 9.0f, -80.5f to 8.5f, -79.0f to 9.0f, -78.0f to 8.0f,
                -77.5f to 7.5f, -78.0f to 6.5f, -80.0f to 6.0f, -82.0f to 5.5f,
                -84.0f to 7.0f, -86.0f to 10.0f, -88.0f to 12.0f, -90.0f to 13.5f,
                -90.5f to 15.0f, -90.0f to 17.0f, -89.0f to 18.0f, -88.5f to 19.0f,
                -88.0f to 20.0f, -90.0f to 21.5f, -92.0f to 22.0f, -94.0f to 22.5f,
                -96.0f to 24.0f, -97.0f to 25.5f, -100.0f to 26.5f, -103.0f to 27.0f,
                -105.0f to 27.0f, -107.0f to 29.0f, -110.0f to 30.0f, -113.0f to 31.0f,
                -115.0f to 32.0f, -117.0f to 32.0f, -117.5f to 33.5f, -118.5f to 34.0f,
                -120.0f to 34.5f, -121.0f to 36.0f, -122.0f to 37.5f, -122.5f to 38.5f,
                -123.0f to 40.0f, -124.0f to 41.5f, -124.5f to 43.0f, -124.0f to 44.5f,
                -124.0f to 46.0f, -124.5f to 47.5f, -124.0f to 48.8f, -123.0f to 49.2f,
                -122.0f to 49.0f, -120.0f to 49.0f, -118.0f to 49.0f, -116.0f to 49.0f,
                -114.0f to 49.0f, -112.0f to 49.0f, -110.0f to 48.5f, -108.0f to 48.0f,
                -105.0f to 49.0f, -102.0f to 49.0f, -100.0f to 49.0f, -98.0f to 49.5f,
                -97.0f to 49.0f, -95.0f to 50.0f, -94.0f to 48.5f, -93.0f to 48.0f,
                -92.0f to 47.5f, -90.0f to 47.0f, -88.0f to 47.5f, -86.0f to 47.0f,
                -85.0f to 46.0f, -84.0f to 46.0f, -82.5f to 45.5f, -82.0f to 44.0f,
                -81.0f to 43.0f, -80.0f to 42.0f, -79.0f to 41.5f, -78.0f to 40.5f,
                -75.0f to 40.0f, -74.0f to 40.5f, -72.0f to 41.0f, -70.5f to 41.5f,
                -70.0f to 42.5f, -69.0f to 43.5f, -68.0f to 44.5f, -67.0f to 44.5f,
                -66.5f to 45.0f, -66.0f to 44.5f, -65.5f to 44.0f, -65.0f to 43.5f,
                -64.0f to 44.0f, -63.0f to 44.5f, -62.0f to 45.0f, -60.0f to 46.5f,
                -58.0f to 47.5f, -56.0f to 48.0f, -54.0f to 49.0f, -53.0f to 48.0f,
                -52.0f to 48.5f, -52.0f to 47.0f, -53.0f to 46.5f, -54.0f to 46.0f,
                -55.0f to 47.0f, -56.0f to 47.0f, -57.0f to 47.5f, -58.0f to 48.0f,
                -59.0f to 47.5f, -60.0f to 47.0f, -61.0f to 46.5f, -63.0f to 46.0f,
                -64.0f to 45.5f, -65.0f to 45.0f, -66.0f to 44.0f, -67.0f to 43.0f,
                -68.0f to 43.0f, -69.0f to 44.0f, -70.0f to 44.5f, -71.0f to 45.0f,
                -72.0f to 45.5f, -73.0f to 45.5f, -74.0f to 45.0f, -75.0f to 44.5f,
                -76.0f to 44.0f, -77.0f to 43.0f, -78.0f to 43.0f, -79.0f to 43.0f,
                -80.0f to 43.5f, -81.0f to 44.0f, -82.0f to 44.5f, -83.0f to 45.0f,
                -84.0f to 46.0f, -85.0f to 46.5f, -86.0f to 47.5f, -88.0f to 48.0f,
                -89.0f to 48.0f, -90.0f to 47.5f, -91.0f to 47.0f, -92.0f to 48.0f,
                -93.0f to 48.5f, -95.0f to 49.0f, -96.0f to 49.5f, -97.0f to 49.0f,
                -98.0f to 49.5f, -100.0f to 50.0f, -102.0f to 50.5f, -104.0f to 52.0f,
                -106.0f to 52.5f, -108.0f to 53.0f, -110.0f to 54.0f, -114.0f to 54.0f,
                -116.0f to 54.0f, -120.0f to 54.5f, -124.0f to 55.0f, -128.0f to 55.2f,
                -130.0f to 55.8f, -132.0f to 56.0f, -134.0f to 57.0f, -136.0f to 58.0f,
                -138.0f to 59.0f, -140.0f to 60.0f, -142.0f to 60.0f, -144.0f to 60.5f,
                -146.0f to 61.0f, -148.0f to 61.5f, -150.0f to 62.0f, -152.0f to 62.5f,
                -155.0f to 63.0f, -157.0f to 63.5f, -160.0f to 64.0f, -162.0f to 64.5f,
                -164.0f to 65.0f, -166.0f to 65.5f, -168.1f to 65.6f
            )),
            // Greenland
            Polygon(listOf(
                -55.0f to 83.0f, -50.0f to 83.0f, -45.0f to 83.0f, -35.0f to 83.0f,
                -20.0f to 82.5f, -15.0f to 82.0f, -12.0f to 81.5f, -14.0f to 81.0f,
                -18.0f to 80.0f, -20.0f to 79.0f, -22.0f to 78.0f, -24.0f to 77.5f,
                -18.0f to 77.0f, -20.0f to 76.0f, -22.0f to 75.0f, -22.0f to 74.0f,
                -24.0f to 73.0f, -22.0f to 72.0f, -24.0f to 71.0f, -28.0f to 70.5f,
                -32.0f to 70.0f, -36.0f to 69.5f, -40.0f to 69.0f, -42.0f to 68.0f,
                -44.0f to 67.0f, -46.0f to 66.0f, -48.0f to 65.0f, -50.0f to 64.0f,
                -52.0f to 64.0f, -54.0f to 65.0f, -56.0f to 66.0f, -58.0f to 68.0f,
                -60.0f to 69.0f, -60.0f to 70.0f, -58.0f to 71.0f, -56.0f to 72.0f,
                -54.0f to 73.0f, -52.0f to 74.0f, -50.0f to 75.0f, -48.0f to 76.0f,
                -46.0f to 77.0f, -44.0f to 78.0f, -42.0f to 79.0f, -40.0f to 80.0f,
                -42.0f to 81.0f, -44.0f to 81.5f, -46.0f to 82.0f, -50.0f to 82.5f,
                -52.0f to 83.0f, -55.0f to 83.0f
            ))
        )),

        Continent("South America", listOf(
            Polygon(listOf(
                -80.0f to 8.5f, -78.0f to 8.0f, -76.8f to 8.0f, -75.5f to 8.0f,
                -73.0f to 8.0f, -71.0f to 8.0f, -70.0f to 8.0f, -68.0f to 8.5f,
                -64.0f to 9.5f, -62.0f to 10.0f, -60.0f to 9.5f, -58.0f to 8.0f,
                -56.0f to 6.0f, -54.0f to 5.0f, -52.0f to 4.0f, -51.0f to 2.5f,
                -50.0f to 1.0f, -49.0f to 0.0f, -48.0f to -1.0f, -45.0f to -1.0f,
                -43.0f to -1.5f, -40.0f to -2.0f, -38.0f to -3.0f, -37.0f to -4.0f,
                -35.5f to -5.5f, -35.0f to -7.0f, -35.5f to -8.5f, -36.0f to -10.0f,
                -37.0f to -12.0f, -38.0f to -13.0f, -39.0f to -14.5f, -40.0f to -15.5f,
                -40.5f to -17.0f, -41.0f to -18.5f, -41.0f to -20.0f, -40.5f to -21.5f,
                -40.0f to -22.5f, -39.0f to -23.0f, -38.0f to -23.5f, -37.0f to -23.5f,
                -36.0f to -23.0f, -35.0f to -22.0f, -34.0f to -22.0f, -33.0f to -23.0f,
                -32.0f to -24.0f, -31.0f to -26.0f, -30.0f to -28.0f, -29.0f to -29.0f,
                -28.0f to -29.0f, -27.0f to -28.0f, -26.0f to -27.0f, -25.0f to -25.0f,
                -25.0f to -23.0f, -27.0f to -22.0f, -28.0f to -22.0f, -30.0f to -21.0f,
                -32.0f to -20.0f, -34.0f to -20.0f, -36.0f to -20.0f, -38.0f to -20.0f,
                -40.0f to -21.0f, -42.0f to -22.0f, -44.0f to -23.0f, -46.0f to -24.0f,
                -48.0f to -25.0f, -50.0f to -26.0f, -52.0f to -27.0f, -54.0f to -28.0f,
                -56.0f to -30.0f, -58.0f to -32.0f, -60.0f to -34.0f, -62.0f to -36.0f,
                -64.0f to -38.0f, -65.0f to -40.0f, -66.0f to -42.0f, -67.0f to -44.0f,
                -68.0f to -46.0f, -68.5f to -48.0f, -69.0f to -50.0f, -70.0f to -51.5f,
                -71.0f to -52.5f, -72.0f to -53.5f, -72.5f to -54.5f, -73.0f to -55.5f,
                -73.5f to -55.0f, -74.0f to -54.0f, -74.5f to -52.5f, -75.0f to -51.0f,
                -75.0f to -49.0f, -75.0f to -47.0f, -75.0f to -45.0f, -74.5f to -43.0f,
                -74.0f to -41.0f, -73.0f to -39.0f, -72.0f to -37.0f, -71.0f to -35.0f,
                -70.5f to -33.0f, -70.0f to -31.0f, -70.0f to -29.0f, -70.0f to -27.0f,
                -70.0f to -25.0f, -69.5f to -23.0f, -69.0f to -21.0f, -68.0f to -19.0f,
                -67.0f to -17.0f, -65.5f to -15.0f, -64.0f to -13.0f, -62.5f to -11.0f,
                -61.0f to -9.0f, -60.0f to -7.0f, -59.0f to -5.0f, -58.0f to -3.0f,
                -58.0f to -1.0f, -59.0f to 0.5f, -61.0f to 1.5f, -63.0f to 2.0f,
                -65.0f to 2.5f, -68.0f to 3.5f, -71.0f to 4.0f, -73.0f to 5.0f,
                -75.0f to 6.0f, -77.0f to 7.0f, -79.0f to 8.0f, -80.0f to 8.5f
            ))
        )),

        Continent("Europe", listOf(
            Polygon(listOf(
                -10.0f to 37.0f, -9.5f to 38.5f, -9.0f to 40.5f, -9.0f to 42.0f,
                -9.0f to 43.5f, -8.0f to 44.0f, -7.0f to 44.5f, -5.0f to 44.0f,
                -3.0f to 43.5f, -2.0f to 43.5f, -1.0f to 43.5f, 1.5f to 43.5f,
                3.5f to 43.0f, 5.5f to 43.5f, 7.5f to 44.0f, 9.0f to 44.5f,
                10.0f to 45.0f, 11.0f to 45.5f, 12.5f to 45.5f, 14.0f to 45.5f,
                14.5f to 45.0f, 15.0f to 44.8f, 16.0f to 44.5f, 17.0f to 44.5f,
                18.0f to 44.5f, 19.0f to 44.5f, 20.0f to 44.5f, 21.0f to 44.5f,
                22.0f to 44.5f, 23.0f to 44.0f, 24.0f to 43.5f, 25.0f to 43.5f,
                26.0f to 44.0f, 27.0f to 44.5f, 28.0f to 45.0f, 29.0f to 45.5f,
                30.0f to 46.0f, 31.0f to 46.5f, 32.0f to 46.5f, 33.0f to 46.0f,
                34.0f to 45.5f, 35.0f to 45.0f, 36.0f to 45.0f, 37.0f to 45.5f,
                39.0f to 46.0f, 40.0f to 46.5f, 41.0f to 45.5f, 41.0f to 44.5f,
                40.0f to 43.5f, 39.0f to 42.0f, 38.0f to 41.0f, 37.0f to 40.0f,
                36.0f to 39.0f, 35.0f to 37.5f, 34.0f to 36.0f, 33.0f to 35.0f,
                32.0f to 34.5f, 31.0f to 34.0f, 30.0f to 33.5f, 29.0f to 33.0f,
                28.0f to 32.5f, 27.0f to 32.0f, 26.0f to 31.5f, 25.0f to 31.5f,
                24.0f to 32.0f, 23.0f to 32.0f, 22.0f to 31.5f, 21.0f to 31.0f,
                20.0f to 30.5f, 19.0f to 30.0f, 18.0f to 30.0f, 17.0f to 30.0f,
                16.0f to 30.0f, 15.0f to 30.0f, 14.0f to 30.5f, 13.0f to 31.0f,
                12.0f to 31.5f, 11.0f to 32.0f, 10.0f to 32.5f, 9.0f to 33.0f,
                8.0f to 33.5f, 7.0f to 34.0f, 6.0f to 34.5f, 5.0f to 35.0f,
                4.0f to 36.0f, 2.0f to 37.0f, 0.0f to 37.0f, -5.0f to 36.5f,
                -6.0f to 37.0f, -7.0f to 37.0f, -8.0f to 37.0f, -9.0f to 37.0f,
                -10.0f to 37.0f
            )),
            // Iceland
            Polygon(listOf(
                -24.0f to 66.0f, -23.5f to 66.5f, -22.0f to 66.5f, -21.0f to 66.0f,
                -20.0f to 65.5f, -19.0f to 65.0f, -18.0f to 64.5f, -17.0f to 64.0f,
                -16.0f to 63.5f, -15.5f to 63.5f, -14.5f to 64.0f, -14.0f to 64.5f,
                -13.5f to 65.0f, -13.5f to 65.5f, -14.0f to 66.0f, -16.0f to 66.0f,
                -18.0f to 66.5f, -20.0f to 66.5f, -22.0f to 66.5f, -23.5f to 66.5f,
                -24.0f to 66.0f
            ))
        )),

        Continent("Africa", listOf(
            Polygon(listOf(
                -17.0f to 14.5f, -16.5f to 14.0f, -16.0f to 13.5f, -16.0f to 12.5f,
                -15.5f to 12.5f, -14.5f to 13.0f, -13.5f to 12.0f, -13.0f to 11.0f,
                -13.0f to 10.0f, -14.0f to 9.5f, -14.5f to 9.0f, -16.0f to 9.5f,
                -17.0f to 9.0f, -17.5f to 8.5f, -17.0f to 8.0f, -16.0f to 7.5f,
                -15.0f to 7.0f, -14.0f to 6.5f, -13.0f to 6.0f, -12.0f to 5.5f,
                -10.0f to 5.0f, -9.0f to 4.5f, -8.0f to 5.0f, -6.0f to 4.5f,
                -5.0f to 5.0f, -3.0f to 5.0f, -1.0f to 5.0f, 1.5f to 4.5f,
                4.5f to 4.5f, 7.0f to 4.5f, 9.0f to 4.5f, 10.0f to 5.0f,
                10.0f to 6.0f, 9.0f to 6.0f, 8.5f to 6.0f, 8.0f to 6.0f,
                9.0f to 7.0f, 9.5f to 8.0f, 10.0f to 9.0f, 10.5f to 10.0f,
                11.0f to 10.5f, 12.5f to 10.5f, 13.0f to 10.5f, 13.5f to 10.0f,
                13.0f to 9.0f, 14.0f to 8.0f, 15.0f to 7.0f, 17.0f to 7.0f,
                20.0f to 7.0f, 22.0f to 7.0f, 25.0f to 7.0f, 28.0f to 7.0f,
                32.0f to 7.0f, 35.0f to 7.0f, 40.0f to 7.0f, 42.0f to 7.0f,
                42.0f to 8.0f, 42.5f to 9.0f, 42.5f to 10.0f, 43.0f to 11.0f,
                43.0f to 11.5f, 44.0f to 12.0f, 45.0f to 11.5f, 46.0f to 10.5f,
                47.0f to 10.0f, 48.0f to 10.0f, 49.0f to 9.0f, 50.0f to 9.0f,
                51.0f to 10.5f, 51.0f to 12.0f, 50.5f to 13.0f, 49.5f to 13.5f,
                48.5f to 13.0f, 47.5f to 12.5f, 46.5f to 12.0f, 45.5f to 12.0f,
                44.5f to 12.0f, 43.5f to 12.0f, 42.5f to 12.0f, 41.5f to 12.0f,
                40.5f to 12.0f, 38.5f to 11.5f, 37.5f to 11.0f, 37.0f to 11.0f,
                37.0f to 15.0f, 37.0f to 20.0f, 37.0f to 25.0f, 36.0f to 28.0f,
                35.0f to 30.0f, 34.5f to 31.0f, 34.0f to 31.5f, 33.0f to 32.0f,
                32.0f to 32.5f, 31.0f to 31.5f, 30.0f to 31.0f, 29.0f to 30.5f,
                28.0f to 29.0f, 27.0f to 28.0f, 26.0f to 27.0f, 25.0f to 26.0f,
                24.0f to 25.0f, 23.0f to 26.0f, 22.0f to 26.5f, 21.0f to 27.0f,
                20.0f to 26.0f, 19.0f to 25.0f, 18.0f to 24.0f, 17.0f to 23.0f,
                16.0f to 22.0f, 15.5f to 21.0f, 15.0f to 20.0f, 14.5f to 19.0f,
                13.5f to 18.0f, 12.5f to 17.5f, 12.0f to 17.0f, 11.5f to 16.0f,
                11.0f to 15.0f, 12.0f to 15.0f, 13.0f to 15.0f, 14.0f to 15.0f,
                15.0f to 15.0f, 16.0f to 15.0f, 17.0f to 14.5f, 17.5f to 14.0f,
                17.0f to 13.5f, 16.0f to 13.0f, 15.0f to 13.0f, 14.0f to 12.5f,
                13.0f to 12.0f, 11.5f to 12.0f, 10.5f to 12.0f, 10.0f to 13.0f,
                9.0f to 13.5f, 8.0f to 14.5f, 7.0f to 15.0f, 5.5f to 15.0f,
                4.0f to 15.0f, 2.5f to 15.5f, 0.5f to 14.5f, -1.0f to 14.0f,
                -2.5f to 14.0f, -4.0f to 14.0f, -5.5f to 14.0f, -7.0f to 14.0f,
                -8.5f to 14.5f, -10.0f to 15.0f, -11.5f to 15.0f, -13.0f to 15.0f,
                -14.5f to 14.5f, -16.0f to 14.5f, -17.0f to 14.5f
            ))
        )),

        Continent("Asia", listOf(
            Polygon(listOf(
                27.0f to 41.5f, 28.0f to 41.5f, 29.0f to 41.0f, 30.0f to 41.5f,
                31.0f to 42.0f, 33.0f to 42.0f, 35.0f to 42.5f, 37.0f to 42.5f,
                39.0f to 42.0f, 41.0f to 41.5f, 43.0f to 41.0f, 44.0f to 40.5f,
                45.0f to 40.0f, 47.0f to 40.0f, 48.0f to 40.0f, 49.0f to 39.0f,
                50.0f to 38.0f, 51.0f to 37.0f, 52.0f to 36.5f, 54.0f to 37.5f,
                56.0f to 37.5f, 58.0f to 37.5f, 60.0f to 38.0f, 62.0f to 38.0f,
                64.0f to 38.0f, 66.0f to 38.0f, 68.0f to 38.0f, 69.0f to 37.5f,
                70.0f to 37.0f, 71.0f to 37.0f, 72.0f to 37.0f, 73.0f to 37.0f,
                74.0f to 37.0f, 75.0f to 37.0f, 76.0f to 37.0f, 77.0f to 37.0f,
                78.0f to 37.0f, 79.0f to 37.5f, 80.0f to 38.0f, 82.0f to 39.0f,
                84.0f to 40.0f, 86.0f to 41.0f, 88.0f to 42.0f, 90.0f to 43.0f,
                92.0f to 44.0f, 95.0f to 45.0f, 98.0f to 46.0f, 100.0f to 46.5f,
                103.0f to 47.0f, 105.0f to 47.5f, 107.0f to 48.0f, 109.0f to 48.0f,
                111.0f to 48.0f, 114.0f to 48.5f, 116.0f to 49.0f, 118.0f to 49.0f,
                120.0f to 49.5f, 122.0f to 50.0f, 124.0f to 50.0f, 126.0f to 50.5f,
                128.0f to 51.0f, 130.0f to 52.0f, 132.0f to 52.5f, 134.0f to 53.0f,
                135.0f to 53.5f, 136.0f to 54.5f, 138.0f to 54.5f, 139.0f to 54.0f,
                140.0f to 53.0f, 141.0f to 52.0f, 141.5f to 50.5f, 142.0f to 49.0f,
                142.0f to 47.5f, 142.0f to 46.0f, 141.5f to 44.5f, 141.0f to 42.5f,
                140.0f to 40.5f, 139.5f to 38.5f, 139.0f to 36.5f, 138.0f to 35.0f,
                137.0f to 34.5f, 136.0f to 34.0f, 135.0f to 34.0f, 133.0f to 34.0f,
                130.0f to 34.0f, 129.0f to 35.0f, 127.0f to 35.5f, 126.0f to 36.0f,
                124.0f to 37.0f, 122.0f to 36.5f, 121.0f to 36.0f, 119.0f to 35.0f,
                117.0f to 34.0f, 115.0f to 33.0f, 113.0f to 32.5f, 111.0f to 33.0f,
                110.0f to 33.5f, 109.0f to 34.5f, 110.0f to 35.5f, 110.0f to 36.5f,
                109.0f to 37.0f, 108.0f to 38.0f, 107.0f to 39.0f, 106.0f to 40.0f,
                105.0f to 41.0f, 104.0f to 42.0f, 103.0f to 43.0f, 102.0f to 44.0f,
                100.0f to 44.0f, 98.0f to 44.0f, 96.0f to 44.0f, 94.0f to 43.5f,
                92.0f to 43.0f, 90.0f to 42.5f, 88.0f to 42.0f, 86.0f to 41.0f,
                84.0f to 40.5f, 82.0f to 40.0f, 80.0f to 39.5f, 78.0f to 39.0f,
                76.0f to 39.0f, 74.0f to 39.0f, 72.0f to 39.0f, 70.0f to 39.0f,
                68.0f to 39.0f, 66.0f to 39.0f, 64.0f to 39.0f, 62.0f to 39.0f,
                60.0f to 39.0f, 58.0f to 39.0f, 56.0f to 39.0f, 54.0f to 39.5f,
                52.0f to 39.5f, 51.0f to 40.0f, 50.0f to 41.0f, 49.0f to 41.5f,
                48.0f to 41.5f, 47.0f to 41.0f, 45.0f to 40.5f, 44.0f to 41.0f,
                43.0f to 41.5f, 42.0f to 41.5f, 41.0f to 41.5f, 40.0f to 41.5f,
                39.0f to 41.5f, 38.0f to 41.0f, 36.0f to 41.0f, 35.0f to 41.5f,
                33.5f to 41.5f, 32.0f to 41.5f, 30.5f to 41.5f, 29.0f to 41.5f,
                27.0f to 41.5f
            )),
            // Japan
            Polygon(listOf(
                130.0f to 33.5f, 131.0f to 33.5f, 132.0f to 34.0f, 133.0f to 34.5f,
                134.0f to 35.0f, 135.0f to 35.0f, 136.0f to 35.5f, 137.0f to 35.5f,
                138.0f to 36.0f, 139.0f to 36.0f, 140.0f to 36.5f, 141.0f to 37.5f,
                141.0f to 39.5f, 141.0f to 41.0f, 140.5f to 41.5f, 140.0f to 41.5f,
                139.5f to 41.0f, 139.0f to 40.0f, 138.0f to 39.0f, 137.0f to 38.0f,
                136.0f to 37.0f, 135.0f to 36.0f, 134.0f to 35.0f, 133.0f to 34.5f,
                132.0f to 34.0f, 131.0f to 33.5f, 130.0f to 33.5f
            )),
            // Southeast Asian islands
            Polygon(listOf(
                95.0f to 6.0f, 97.0f to 6.0f, 99.0f to 7.0f, 101.0f to 7.0f,
                103.0f to 7.0f, 104.0f to 6.5f, 105.0f to 5.0f, 106.0f to 4.0f,
                106.5f to 3.0f, 106.0f to 2.0f, 105.0f to 1.5f, 104.0f to 1.0f,
                103.0f to 1.0f, 102.0f to 1.0f, 101.0f to 1.5f, 100.0f to 2.0f,
                99.0f to 2.0f, 98.0f to 2.5f, 97.0f to 3.0f, 96.5f to 4.0f,
                96.0f to 5.0f, 95.0f to 6.0f
            )),
            Polygon(listOf(
                109.0f to 2.0f, 111.0f to 2.0f, 113.0f to 2.0f, 115.0f to 2.0f,
                117.0f to 3.0f, 119.0f to 2.5f, 119.0f to 1.0f, 118.0f to 0.0f,
                117.0f to -1.0f, 116.0f to -1.5f, 114.0f to -1.0f, 112.0f to 0.0f,
                110.0f to 1.0f, 109.0f to 2.0f
            )),
            // Philippines
            Polygon(listOf(
                117.0f to 8.0f, 119.0f to 10.0f, 121.0f to 12.0f, 122.0f to 13.0f,
                123.0f to 14.0f, 124.0f to 15.0f, 125.0f to 16.0f, 125.0f to 17.5f,
                124.0f to 18.5f, 123.0f to 18.0f, 122.0f to 17.0f, 121.0f to 15.0f,
                120.0f to 14.0f, 119.0f to 12.0f, 118.0f to 10.0f, 117.0f to 8.0f
            )),
            // Sri Lanka
            Polygon(listOf(
                79.5f to 9.0f, 80.5f to 9.5f, 81.5f to 9.5f, 81.5f to 8.5f,
                81.2f to 7.5f, 80.5f to 6.5f, 80.0f to 6.0f, 79.5f to 6.5f,
                79.5f to 7.5f, 79.5f to 8.0f, 79.5f to 9.0f
            ))
        )),

        Continent("Australia", listOf(
            Polygon(listOf(
                113.0f to -22.0f, 115.0f to -22.0f, 117.0f to -21.0f, 119.0f to -20.0f,
                121.0f to -18.0f, 122.0f to -17.0f, 125.0f to -15.0f, 128.0f to -14.0f,
                130.0f to -13.0f, 133.0f to -13.0f, 135.0f to -14.0f, 136.0f to -13.5f,
                137.0f to -13.5f, 138.0f to -14.0f, 140.0f to -16.0f, 142.0f to -17.5f,
                144.0f to -18.5f, 145.0f to -16.5f, 146.0f to -16.5f, 147.0f to -18.0f,
                148.0f to -20.0f, 149.0f to -21.0f, 150.0f to -23.0f, 151.0f to -25.0f,
                152.0f to -27.0f, 153.0f to -28.0f, 153.5f to -30.0f, 153.0f to -32.0f,
                152.0f to -33.0f, 151.0f to -34.0f, 149.0f to -35.0f, 148.0f to -36.0f,
                146.0f to -37.0f, 144.0f to -38.0f, 142.0f to -38.0f, 141.0f to -38.0f,
                140.0f to -37.0f, 138.0f to -36.0f, 137.0f to -35.0f, 136.0f to -33.0f,
                135.0f to -33.0f, 133.0f to -31.0f, 130.0f to -31.0f, 128.0f to -31.0f,
                126.0f to -32.0f, 124.0f to -32.5f, 122.0f to -33.0f, 120.0f to -33.0f,
                118.0f to -33.0f, 115.5f to -32.0f, 115.0f to -31.0f, 114.0f to -29.0f,
                113.0f to -27.0f, 113.0f to -25.0f, 113.0f to -22.0f
            )),
            // Tasmania
            Polygon(listOf(
                144.0f to -41.5f, 145.5f to -41.5f, 147.0f to -42.0f, 148.0f to -43.0f,
                147.5f to -44.0f, 146.0f to -43.5f, 145.0f to -43.0f, 144.0f to -42.5f,
                144.0f to -42.0f, 144.0f to -41.5f
            )),
            // New Zealand (North Island)
            Polygon(listOf(
                172.5f to -35.0f, 174.0f to -35.5f, 175.0f to -36.5f, 176.0f to -37.5f,
                177.0f to -38.5f, 177.5f to -39.5f, 177.0f to -40.5f, 176.0f to -40.5f,
                175.0f to -39.5f, 174.5f to -38.5f, 174.0f to -37.5f, 173.5f to -36.0f,
                172.5f to -35.0f
            )),
            // New Zealand (South Island)
            Polygon(listOf(
                166.5f to -46.0f, 168.0f to -44.5f, 169.5f to -43.5f, 170.5f to -43.5f,
                171.5f to -43.0f, 172.5f to -42.5f, 173.0f to -42.0f, 172.5f to -43.0f,
                172.0f to -44.0f, 171.0f to -44.5f, 170.0f to -45.5f, 169.0f to -46.0f,
                168.0f to -46.5f, 167.0f to -46.0f, 166.5f to -46.0f
            ))
        )),

        Continent("Antarctica", listOf(
            Polygon(listOf(
                -180.0f to -75.0f, -150.0f to -75.0f, -120.0f to -73.0f, -90.0f to -73.0f,
                -60.0f to -75.0f, -30.0f to -72.0f, 0.0f to -70.0f, 30.0f to -70.0f,
                60.0f to -70.0f, 90.0f to -70.0f, 120.0f to -68.0f, 150.0f to -70.0f,
                180.0f to -75.0f, 180.0f to -90.0f, -180.0f to -90.0f, -180.0f to -75.0f
            ))
        ))
    )

    /** Major city coordinates for optional labels [longitude, latitude, name] */
    data class City(val name: String, val lon: Float, val lat: Float)

    val majorCities: List<City> = listOf(
        // ===== 中国城市 =====
        City("北京", 116.4f, 39.9f),
        City("上海", 121.5f, 31.2f),
        City("重庆", 106.5f, 29.5f),
        City("广州", 113.3f, 23.1f),
        City("深圳", 114.1f, 22.5f),
        City("香港", 114.2f, 22.3f),
        City("澳门", 113.5f, 22.2f),
        City("台北", 121.5f, 25.0f),
        City("天津", 117.2f, 39.1f),
        City("武汉", 114.3f, 30.6f),
        City("成都", 104.1f, 30.6f),
        City("南京", 118.8f, 32.1f),
        City("杭州", 120.2f, 30.3f),
        City("西安", 108.9f, 34.3f),
        City("郑州", 113.7f, 34.8f),
        City("哈尔滨", 126.6f, 45.8f),
        City("沈阳", 123.4f, 41.8f),
        City("长沙", 113.0f, 28.2f),
        City("青岛", 120.4f, 36.1f),
        City("大连", 121.6f, 38.9f),
        City("厦门", 118.1f, 24.5f),
        City("苏州", 120.6f, 31.3f),
        City("合肥", 117.3f, 31.8f),
        City("昆明", 102.7f, 25.0f),
        City("贵阳", 106.7f, 26.6f),
        City("兰州", 103.8f, 36.1f),
        City("呼和浩特", 111.7f, 40.8f),
        City("乌鲁木齐", 87.6f, 43.8f),
        City("拉萨", 91.1f, 29.7f),
        City("南宁", 108.3f, 22.8f),
        City("福州", 119.3f, 26.1f),
        City("长春", 125.3f, 43.9f),
        City("海口", 110.3f, 20.0f),
        City("西宁", 101.8f, 36.6f),
        City("银川", 106.3f, 38.5f),

        // ===== 东亚 =====
        City("东京", 139.8f, 35.7f),
        City("大阪", 135.5f, 34.7f),
        City("首尔", 127.0f, 37.6f),
        City("釜山", 129.1f, 35.2f),
        City("平壤", 125.8f, 39.0f),
        City("乌兰巴托", 106.9f, 47.9f),

        // ===== 东南亚 =====
        City("新加坡", 103.8f, 1.4f),
        City("曼谷", 100.5f, 13.8f),
        City("吉隆坡", 101.7f, 3.1f),
        City("雅加达", 106.8f, -6.2f),
        City("马尼拉", 121.0f, 14.6f),
        City("河内", 105.9f, 21.0f),
        City("胡志明市", 106.7f, 10.8f),
        City("金边", 104.9f, 11.6f),
        City("万象", 102.6f, 18.0f),
        City("仰光", 96.2f, 16.9f),
        City("斯里巴加湾", 114.9f, 4.9f),

        // ===== 南亚 =====
        City("新德里", 77.2f, 28.6f),
        City("孟买", 72.8f, 19.0f),
        City("加尔各答", 88.4f, 22.6f),
        City("班加罗尔", 77.6f, 12.9f),
        City("达卡", 90.4f, 23.8f),
        City("伊斯兰堡", 73.1f, 33.7f),
        City("卡拉奇", 67.0f, 24.9f),
        City("科伦坡", 79.9f, 6.9f),
        City("加德满都", 85.3f, 27.7f),

        // ===== 中亚 =====
        City("塔什干", 69.3f, 41.3f),
        City("阿斯塔纳", 71.4f, 51.2f),
        City("比什凯克", 74.6f, 42.9f),
        City("杜尚别", 68.8f, 38.6f),
        City("阿什哈巴德", 58.4f, 38.0f),

        // ===== 西亚 / 中东 =====
        City("迪拜", 55.3f, 25.3f),
        City("利雅得", 46.7f, 24.6f),
        City("德黑兰", 51.4f, 35.7f),
        City("巴格达", 44.4f, 33.3f),
        City("喀布尔", 69.2f, 34.5f),
        City("安卡拉", 32.9f, 39.9f),
        City("伊斯坦布尔", 29.0f, 41.0f),
        City("巴库", 49.9f, 40.4f),
        City("第比利斯", 44.8f, 41.7f),
        City("耶路撒冷", 35.2f, 31.8f),
        City("大马士革", 36.3f, 33.5f),
        City("科威特城", 48.0f, 29.4f),
        City("多哈", 51.5f, 25.3f),
        City("马斯喀特", 58.6f, 23.6f),
        City("萨那", 44.2f, 15.4f),

        // ===== 欧洲 =====
        City("伦敦", -0.1f, 51.5f),
        City("巴黎", 2.3f, 48.9f),
        City("柏林", 13.4f, 52.5f),
        City("莫斯科", 37.6f, 55.8f),
        City("罗马", 12.5f, 41.9f),
        City("马德里", -3.7f, 40.4f),
        City("里斯本", -9.1f, 38.7f),
        City("布鲁塞尔", 4.4f, 50.9f),
        City("阿姆斯特丹", 4.9f, 52.4f),
        City("华沙", 21.0f, 52.2f),
        City("基辅", 30.5f, 50.5f),
        City("斯德哥尔摩", 18.1f, 59.3f),
        City("奥斯陆", 10.8f, 60.0f),
        City("赫尔辛基", 24.9f, 60.2f),
        City("雅典", 23.7f, 38.0f),
        City("布达佩斯", 19.1f, 47.5f),
        City("维也纳", 16.4f, 48.2f),
        City("布拉格", 14.4f, 50.1f),
        City("都柏林", -6.3f, 53.3f),
        City("雷克雅未克", -21.9f, 64.1f),
        City("哥本哈根", 12.6f, 55.7f),
        City("苏黎世", 8.5f, 47.4f),
        City("米兰", 9.2f, 45.5f),
        City("巴塞罗那", 2.2f, 41.4f),
        City("圣彼得堡", 30.3f, 59.9f),
        City("布加勒斯特", 26.1f, 44.4f),
        City("贝尔格莱德", 20.5f, 44.8f),

        // ===== 非洲 =====
        City("开罗", 31.2f, 30.0f),
        City("拉各斯", 3.4f, 6.5f),
        City("内罗毕", 36.8f, -1.3f),
        City("开普敦", 18.4f, -33.9f),
        City("约翰内斯堡", 28.0f, -26.2f),
        City("阿尔及尔", 3.1f, 36.8f),
        City("卡萨布兰卡", -7.6f, 33.6f),
        City("亚的斯亚贝巴", 38.7f, 9.0f),
        City("达喀尔", -17.4f, 14.7f),
        City("的黎波里", 13.2f, 32.9f),

        // ===== 北美洲 =====
        City("纽约", -74.0f, 40.7f),
        City("洛杉矶", -118.2f, 34.1f),
        City("芝加哥", -87.6f, 41.9f),
        City("华盛顿", -77.0f, 38.9f),
        City("旧金山", -122.4f, 37.8f),
        City("迈阿密", -80.2f, 25.8f),
        City("多伦多", -79.4f, 43.7f),
        City("温哥华", -123.1f, 49.3f),
        City("蒙特利尔", -73.6f, 45.5f),
        City("墨西哥城", -99.1f, 19.4f),
        City("哈瓦那", -82.4f, 23.1f),

        // ===== 南美洲 =====
        City("圣保罗", -46.6f, -23.5f),
        City("里约热内卢", -43.2f, -22.9f),
        City("布宜诺斯艾利斯", -58.4f, -34.6f),
        City("利马", -77.0f, -12.0f),
        City("圣地亚哥", -70.6f, -33.5f),
        City("波哥大", -74.1f, 4.6f),
        City("加拉加斯", -66.9f, 10.5f),
        City("巴西利亚", -47.9f, -15.8f),

        // ===== 大洋洲 =====
        City("悉尼", 151.2f, -33.9f),
        City("墨尔本", 145.0f, -37.8f),
        City("布里斯班", 153.0f, -27.5f),
        City("珀斯", 115.9f, -32.0f),
        City("奥克兰", 174.8f, -36.9f),
        City("惠灵顿", 174.8f, -41.3f),
        City("努美阿", 166.5f, -22.3f),
    )
}
