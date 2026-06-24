package com.telemetrypro.app.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.telemetrypro.app.data.GpsRepository
import com.telemetrypro.app.data.LocationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Central ViewModel — manages GPS lifecycle and exposes UI state.
 * All screens observe this single ViewModel.
 */
class GpsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GpsRepository(application)
    private val prefs = application.getSharedPreferences("telemetry_pro", Context.MODE_PRIVATE)

    /** Whether online/AGPS mode is enabled (persisted) */
    private val _isOnlineMode = MutableStateFlow(
        prefs.getBoolean("online_mode", false)
    )
    val isOnlineMode: StateFlow<Boolean> = _isOnlineMode.asStateFlow()

    /** Live GPS state consumed by all Compose screens */
    val state: StateFlow<LocationState> = repository.locationState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LocationState()
        )

    // Monitor actual network connectivity for status display
    private val _isNetworkAvailable = MutableStateFlow(false)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()

    init {
        // Apply persisted online mode on creation
        repository.onlineMode = _isOnlineMode.value
        monitorNetwork(application)
    }

    private fun monitorNetwork(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isNetworkAvailable.value = true
            }
            override fun onLost(network: Network) {
                _isNetworkAvailable.value = false
            }
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                _isNetworkAvailable.value = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }
        }
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(request, callback)

        // Initial check
        val activeNetwork = cm.activeNetwork
        val caps = if (activeNetwork != null) cm.getNetworkCapabilities(activeNetwork) else null
        _isNetworkAvailable.value = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    /** Toggle online/offline mode and restart GPS */
    fun setOnlineMode(enabled: Boolean) {
        _isOnlineMode.value = enabled
        prefs.edit().putBoolean("online_mode", enabled).apply()
        repository.onlineMode = enabled
        repository.restart()
    }

    /** Start GPS monitoring — call from onResume or composition */
    fun startMonitoring() {
        repository.start()
    }

    /** Stop GPS monitoring — call from onPause or disposal */
    fun stopMonitoring() {
        repository.stop()
    }

    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }
}
