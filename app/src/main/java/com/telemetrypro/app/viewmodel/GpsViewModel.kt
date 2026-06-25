package com.telemetrypro.app.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.telemetrypro.app.data.GpsFixStatus
import com.telemetrypro.app.data.GpsRepository
import com.telemetrypro.app.data.LocationState
import com.telemetrypro.app.data.NetworkCellInfoProvider
import com.telemetrypro.app.data.TrackRepository
import com.telemetrypro.app.data.TrackSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GpsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GpsRepository(application)
    val trackRepository = TrackRepository(application)
    val cellInfoProvider = NetworkCellInfoProvider(application)
    private val prefs = application.getSharedPreferences("telemetry_pro", Context.MODE_PRIVATE)

    private val _isOnlineMode = MutableStateFlow(
        prefs.getBoolean("online_mode", false)
    )
    val isOnlineMode: StateFlow<Boolean> = _isOnlineMode.asStateFlow()

    val isRecording: StateFlow<Boolean> = trackRepository.isRecording
    val recordingDistanceKm: StateFlow<Double> = trackRepository.currentDistanceKm
    val sessions: StateFlow<List<TrackSession>> = trackRepository.sessions

    private val _nmeaLoggingEnabled = MutableStateFlow(false)
    val nmeaLoggingEnabled: StateFlow<Boolean> = _nmeaLoggingEnabled.asStateFlow()

    // 0=DD (decimal degrees), 1=DMS (degrees minutes seconds)
    private val _coordFormatDms = MutableStateFlow(
        prefs.getInt("coord_format", 0)
    )
    val coordFormatDms: StateFlow<Int> = _coordFormatDms.asStateFlow()

    /** Combined GPS + track recording state */
    val state: StateFlow<LocationState> = combine(
        repository.locationState,
        trackRepository.isRecording,
        trackRepository.currentDistanceKm,
        trackRepository.currentPoints
    ) { gpsState, isRecording, distanceKm, trackPoints ->
        gpsState.copy(
            isRecording = isRecording,
            recordingDistanceKm = distanceKm,
            recordingPoints = trackPoints,
            isNetworkAvailable = _isNetworkAvailable.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LocationState()
    )

    private val _isNetworkAvailable = MutableStateFlow(false)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()

    private var lastRecordedTimestamp = 0L

    init {
        try {
            repository.onlineMode = _isOnlineMode.value
            monitorNetwork(application)
            observeGpsForTracking()
        } catch (e: Exception) { }
    }

    /** Feed GPS location updates into track recorder when active */
    private fun observeGpsForTracking() {
        viewModelScope.launch {
            repository.locationState.collect { locState ->
                if (locState.fixStatus == GpsFixStatus.FIXED &&
                    locState.timestamp > lastRecordedTimestamp &&
                    locState.latitude != 0.0 &&
                    locState.longitude != 0.0) {
                    lastRecordedTimestamp = locState.timestamp
                    trackRepository.appendPoint(
                        latitude = locState.latitude,
                        longitude = locState.longitude,
                        altitude = locState.altitudeMeters,
                        speedKmh = locState.speedKmh,
                        accuracy = locState.accuracy,
                        timestamp = locState.timestamp,
                        bearing = locState.bearing
                    )
                }
            }
        }
    }

    private var connectivityManager: ConnectivityManager? = null

    private fun monitorNetwork(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
        connectivityManager = cm
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                checkNetworkImmediate()
            }
            override fun onLost(network: Network) {
                checkNetworkImmediate()
            }
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                _isNetworkAvailable.value = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }
        }
        // Use registerDefaultNetworkCallback for reliable tracking across all networks
        cm.registerDefaultNetworkCallback(callback)
        // Immediate check
        checkNetworkImmediate()
    }

    /** Direct query of current network state — avoids callback race conditions */
    private fun checkNetworkImmediate() {
        val cm = connectivityManager ?: return
        try {
            val active = cm.activeNetwork ?: run { _isNetworkAvailable.value = false; return }
            val caps = cm.getNetworkCapabilities(active) ?: run { _isNetworkAvailable.value = false; return }
            _isNetworkAvailable.value = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            _isNetworkAvailable.value = false
        }
    }

    fun setOnlineMode(enabled: Boolean) {
        _isOnlineMode.value = enabled
        prefs.edit().putBoolean("online_mode", enabled).apply()
        repository.onlineMode = enabled
        repository.restart()
        // Refresh network status when user toggles online mode
        checkNetworkImmediate()
        // Must run on IO — NetworkCellInfoProvider.refresh() calls tm.allCellInfo
        // which is a blocking Binder IPC. Running on main thread causes ANR.
        viewModelScope.launch(Dispatchers.IO) { cellInfoProvider.refresh() }
    }

    /** Trigger a one-shot read of cellular tower info on background thread. */
    fun refreshCellInfo() {
        viewModelScope.launch(Dispatchers.IO) { cellInfoProvider.refresh() }
    }

    fun setNmeaLoggingEnabled(enabled: Boolean) {
        _nmeaLoggingEnabled.value = enabled
        repository.setNmeaLoggingEnabled(enabled)
    }

    fun setCoordFormat(dms: Int) {
        _coordFormatDms.value = dms
        prefs.edit().putInt("coord_format", dms).apply()
    }

    // --- Recording controls ---
    fun startRecording(name: String) {
        trackRepository.startRecording(name)
    }

    fun stopRecording() {
        trackRepository.stopRecording()
    }

    fun deleteSession(sessionId: Long) {
        trackRepository.deleteSession(sessionId)
    }

    fun renameSession(sessionId: Long, newName: String) {
        trackRepository.renameSession(sessionId, newName)
    }

    // --- GPS lifecycle ---
    fun startMonitoring() {
        repository.start()
    }

    fun stopMonitoring() {
        repository.stop()
    }

    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }
}
