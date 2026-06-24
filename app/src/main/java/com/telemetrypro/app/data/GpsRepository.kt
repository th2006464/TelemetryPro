package com.telemetrypro.app.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.OnNmeaMessageListener
import android.os.Build
import android.os.Looper
import androidx.core.location.LocationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * GPS data repository — wraps Android LocationManager and exposes
 * real-time GNSS data via StateFlow for MVVM consumption.
 *
 * Handles:
 *   - Location updates (lat/lng, altitude, speed, accuracy)
 *   - GNSS satellite status per constellation
 *   - NMEA sentence logging
 *   - Flight mode detection helpers
 *   - Online/offline mode (AGPS toggle)
 */
class GpsRepository(private val context: Context) {

    private val locationManager: LocationManager? = try {
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    } catch (e: Exception) {
        null
    }

    /** Whether network-assisted (AGPS) mode is enabled */
    var onlineMode: Boolean = false

    /** Whether NMEA sentence logging is enabled — off by default */
    private var _nmeaLoggingEnabled: Boolean = false
    val nmeaLoggingEnabled: Boolean get() = _nmeaLoggingEnabled

    // --- StateFlows exposed to ViewModel ---
    private val _locationState = MutableStateFlow(LocationState())
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private val _satellites = MutableStateFlow<List<SatelliteInfo>>(emptyList())
    val satellites: StateFlow<List<SatelliteInfo>> = _satellites.asStateFlow()

    private val _nmeaBuffer = mutableListOf<String>()
    private val maxNmeaLines = 30

    // --- Location Listener ---
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            updateLocationData(location)
        }

        override fun onProviderDisabled(provider: String) {
            if (provider == LocationManager.GPS_PROVIDER) {
                _locationState.value = _locationState.value.copy(
                    fixStatus = GpsFixStatus.NO_SIGNAL,
                    totalSatellites = 0,
                    usedSatellites = 0
                )
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
    }

    // --- GNSS Status Callback (API 24+) ---
    private val gnssCallback = object : GnssStatus.Callback() {
        override fun onStarted() {
            _locationState.value = _locationState.value.copy(
                fixStatus = GpsFixStatus.SEARCHING
            )
        }

        override fun onStopped() {
            _locationState.value = _locationState.value.copy(
                fixStatus = GpsFixStatus.NO_SIGNAL,
                totalSatellites = 0,
                usedSatellites = 0,
                satellites = emptyList(),
                constellationStats = emptyList()
            )
        }

        override fun onFirstFix(ttffMillis: Int) {
            _locationState.value = _locationState.value.copy(
                fixStatus = GpsFixStatus.FIXED
            )
        }

        override fun onSatelliteStatusChanged(status: GnssStatus) {
            updateSatelliteData(status)
        }
    }

    // --- NMEA Listener ---
    private val nmeaListener = OnNmeaMessageListener { message, _ ->
        if (!_nmeaLoggingEnabled) return@OnNmeaMessageListener
        synchronized(_nmeaBuffer) {
            _nmeaBuffer.add(message.trim())
            if (_nmeaBuffer.size > maxNmeaLines) {
                _nmeaBuffer.removeAt(0)
            }
            _locationState.value = _locationState.value.copy(
                nmeaLogLines = _nmeaBuffer.toList()
            )
        }
    }

    // ============================================================
    // Lifecycle
    // ============================================================

    @SuppressLint("MissingPermission")
    fun start() {
        val lm = locationManager ?: return
        try {
            // GPS provider — always active
            if (LocationManagerCompat.hasProvider(lm, LocationManager.GPS_PROVIDER)) {
                lm.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,      // 1 second interval
                    0f,         // 0 meter min distance
                    locationListener,
                    Looper.getMainLooper()
                )
            }

            // NETWORK provider — only when online mode is enabled (AGPS assist)
            if (onlineMode && LocationManagerCompat.hasProvider(lm, LocationManager.NETWORK_PROVIDER)) {
                lm.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    2000L,      // 2 second interval (coarse, just for TTFF speed-up)
                    50f,        // 50 meter min distance
                    locationListener,
                    Looper.getMainLooper()
                )
            }

            // Register GNSS status
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    lm.registerGnssStatusCallback(gnssCallback)
                } catch (e: Exception) {
                    // GNSS callback registration failed — non-critical
                }
            }
            // Register NMEA listener
            try {
                lm.addNmeaListener(nmeaListener)
            } catch (e: Exception) {
                // NMEA listener registration failed — non-critical
            }

            // Update online state in flow
            _locationState.value = _locationState.value.copy(
                isOnlineMode = onlineMode
            )
        } catch (e: SecurityException) {
            _locationState.value = _locationState.value.copy(
                fixStatus = GpsFixStatus.NO_SIGNAL
            )
        } catch (e: Exception) {
            // Catch-all for any unexpected errors during GPS start
            _locationState.value = _locationState.value.copy(
                fixStatus = GpsFixStatus.NO_SIGNAL
            )
        }
    }

    fun stop() {
        val lm = locationManager ?: return
        try {
            lm.removeUpdates(locationListener)
        } catch (e: Exception) {
            // already unregistered or never registered
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                lm.unregisterGnssStatusCallback(gnssCallback)
            } catch (e: Exception) {
                // already unregistered or never registered
            }
        }
        try {
            lm.removeNmeaListener(nmeaListener)
        } catch (e: Exception) {
            // already removed or never added
        }
    }

    /**
     * Restart monitoring with the new online mode setting.
     * Must be called after toggling onlineMode.
     */
    fun restart() {
        stop()
        start()
    }

    /**
     * Enable or disable NMEA sentence logging.
     * When disabled, NMEA listener is still active but data is discarded.
     */
    fun setNmeaLoggingEnabled(enabled: Boolean) {
        _nmeaLoggingEnabled = enabled
        if (!enabled) {
            synchronized(_nmeaBuffer) {
                _nmeaBuffer.clear()
                _locationState.value = _locationState.value.copy(nmeaLogLines = emptyList())
            }
        }
    }

    // ============================================================
    // Internal Update Methods
    // ============================================================

    private fun updateLocationData(location: Location) {
        val speedKmh = location.speed * 3.6f // m/s → km/h
        val latDms = decimalToDms(location.latitude, true)
        val lngDms = decimalToDms(location.longitude, false)
        val flightMode = detectFlightMode(speedKmh, location.altitude)

        _locationState.value = _locationState.value.copy(
            fixStatus = GpsFixStatus.FIXED,
            flightMode = flightMode,
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            altitude = location.altitude,
            speed = location.speed,
            timestamp = location.time,
            speedKmh = speedKmh,
            altitudeMeters = location.altitude,
            latitudeDms = latDms,
            longitudeDms = lngDms
        )
    }

    private fun updateSatelliteData(status: GnssStatus) {
        // Deduplicate by (constellationType, svid) — some devices report the same
        // satellite twice (e.g. as UNKNOWN + real constellation), which would crash
        // Compose LazyColumn due to duplicate keys.
        val satMap = mutableMapOf<Pair<Int, Int>, SatelliteInfo>()
        var usedCount = 0

        for (i in 0 until status.satelliteCount) {
            val constellationType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                status.getConstellationType(i)
            } else {
                GnssStatus.CONSTELLATION_UNKNOWN
            }
            val constellation = Constellation.fromConstellationType(constellationType)
            val usedInFix = status.usedInFix(i)
            val svid = status.getSvid(i)

            if (usedInFix) usedCount++

            val info = SatelliteInfo(
                svid = svid,
                constellation = constellation,
                snr = status.getCn0DbHz(i).let { if (it.isNaN() || it < 0) 0f else it },
                elevation = status.getElevationDegrees(i),
                azimuth = status.getAzimuthDegrees(i),
                usedInFix = usedInFix,
                hasAlmanac = status.hasAlmanacData(i),
                hasEphemeris = status.hasEphemerisData(i)
            )
            // Later entries overwrite earlier ones (keep latest data for duplicate svid)
            satMap[constellationType to svid] = info
        }

        val list = satMap.values.toList()

        // Group by constellation and build stats
        val grouped = list.groupBy { it.constellation }
        val stats = grouped.map { (constellation, sats) ->
            val avgSnr = sats.filter { it.snr > 0 }.map { it.snr }.average().toFloat()
            val bestSnr = sats.maxOfOrNull { it.snr }?.let { if (it.isNaN()) 0f else it } ?: 0f
            ConstellationStats(
                constellation = constellation,
                totalVisible = sats.size,
                usedInFix = sats.count { it.usedInFix },
                avgSnr = if (avgSnr.isNaN()) 0f else avgSnr,
                bestSnr = bestSnr,
                satellites = sats.sortedByDescending { it.snr }
            )
        }.sortedByDescending { it.usedInFix }

        _satellites.value = list
        _locationState.value = _locationState.value.copy(
            totalSatellites = list.size,
            usedSatellites = usedCount,
            satellites = list,
            constellationStats = stats
        )
    }

    // ============================================================
    // Helpers
    // ============================================================

    private fun detectFlightMode(speedKmh: Float, altitude: Double): FlightMode {
        return when {
            speedKmh > 200f -> FlightMode.POSSIBLE_FLIGHT
            altitude > 8000.0 -> FlightMode.HIGH_ALTITUDE
            else -> FlightMode.NONE
        }
    }

    /** Convert decimal degrees to DMS string (e.g. 40°42'46"N) */
    private fun decimalToDms(degrees: Double, isLat: Boolean): String {
        val abs = kotlin.math.abs(degrees)
        val d = abs.toInt()
        val mTotal = (abs - d) * 60
        val m = mTotal.toInt()
        val s = ((mTotal - m) * 60).toInt()
        val dir = when {
            isLat && degrees >= 0 -> "N"
            isLat -> "S"
            !isLat && degrees >= 0 -> "E"
            else -> "W"
        }
        return "${d}°${m}'${s}\"$dir"
    }
}
