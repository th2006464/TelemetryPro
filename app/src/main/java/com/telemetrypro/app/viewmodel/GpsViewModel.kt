package com.telemetrypro.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.telemetrypro.app.data.GpsRepository
import com.telemetrypro.app.data.LocationState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Central ViewModel — manages GPS lifecycle and exposes UI state.
 * All screens observe this single ViewModel.
 */
class GpsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GpsRepository(application)

    /** Live GPS state consumed by all Compose screens */
    val state: StateFlow<LocationState> = repository.locationState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LocationState()
        )

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
