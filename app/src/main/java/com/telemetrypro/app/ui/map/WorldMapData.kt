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
        City("北京", 116.4f, 39.9f),
        City("上海", 121.5f, 31.2f),
        City("东京", 139.8f, 35.7f),
        City("首尔", 127.0f, 37.6f),
        City("新加坡", 103.8f, 1.4f),
        City("悉尼", 151.2f, -33.9f),
        City("莫斯科", 37.6f, 55.8f),
        City("伦敦", -0.1f, 51.5f),
        City("巴黎", 2.3f, 48.9f),
        City("柏林", 13.4f, 52.5f),
        City("开罗", 31.2f, 30.0f),
        City("纽约", -74.0f, 40.7f),
        City("洛杉矶", -118.2f, 34.1f),
        City("圣保罗", -46.6f, -23.5f),
        City("新德里", 77.2f, 28.6f),
        City("迪拜", 55.3f, 25.3f),
        City("香港", 114.2f, 22.3f),
        City("曼谷", 100.5f, 13.8f)
    )
}
