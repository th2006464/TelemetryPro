package com.telemetrypro.app.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.telemetrypro.app.data.GpsFixStatus
import com.telemetrypro.app.data.GpsRepository
import com.telemetrypro.app.data.LocationState
import com.telemetrypro.app.data.TrackRepository
import com.telemetrypro.app.data.TrackSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GpsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GpsRepository(application)
    val trackRepository = TrackRepository(application)
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
            recordingPoints = trackPoints
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
                        timestamp = locState.timestamp
                    )
                }
            }
        }
    }

    private fun monitorNetwork(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { _isNetworkAvailable.value = true }
            override fun onLost(network: Network) { _isNetworkAvailable.value = false }
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                _isNetworkAvailable.value = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }
        }
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(request, callback)
        val activeNetwork = cm.activeNetwork
        val caps = if (activeNetwork != null) cm.getNetworkCapabilities(activeNetwork) else null
        _isNetworkAvailable.value = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    fun setOnlineMode(enabled: Boolean) {
        _isOnlineMode.value = enabled
        prefs.edit().putBoolean("online_mode", enabled).apply()
        repository.onlineMode = enabled
        repository.restart()
    }

    fun setNmeaLoggingEnabled(enabled: Boolean) {
        _nmeaLoggingEnabled.value = enabled
        repository.setNmeaLoggingEnabled(enabled)
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
