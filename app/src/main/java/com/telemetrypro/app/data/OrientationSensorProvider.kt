package com.telemetrypro.app.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*

/**
 * Provides device azimuth (compass heading) using Android SensorManager.
 *
 * Uses accelerometer + magnetic field sensor fusion via getRotationMatrix +
 * getOrientation for reliable heading, with fallback to TYPE_ORIENTATION
 * (deprecated but widely available).
 *
 * Azimuth: 0° = North, 90° = East, 180° = South, 270° = West (clockwise from North)
 */
class OrientationSensorProvider(private val context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    // EMA smoothing state
    private var smoothedAzimuth: Float = 0f
    private var smoothInitialized = false

    private val _azimuth = MutableStateFlow(0f)
    val azimuth: StateFlow<Float> = _azimuth.asStateFlow()

    private val _available = MutableStateFlow(false)
    val available: StateFlow<Boolean> = _available.asStateFlow()

    // Sensor fusion buffers
    private var accelValues: FloatArray? = null
    private var magValues: FloatArray? = null

    private val accelListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.values?.let { accelValues = it.clone() }
            computeFusedAzimuth()
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private val magListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.values?.let { magValues = it.clone() }
            computeFusedAzimuth()
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    // Fallback: deprecated TYPE_ORIENTATION listener
    private val orientationListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.values?.let {
                if (it.size > 0) {
                    // values[0] = azimuth (heading), range [0, 360)
                    var az = it[0]
                    // Normalize to 0-360
                    az = ((az % 360f) + 360f) % 360f
                    _azimuth.value = smoothAzimuth(az)
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private var useFallback = false

    /** Start listening for orientation updates */
    fun start() {
        val sm = sensorManager ?: run {
            _available.value = false
            return
        }

        // Try accelerometer + magnetometer fusion first (accurate)
        val accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val mag = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (accel != null && mag != null) {
            sm.registerListener(accelListener, accel, SensorManager.SENSOR_DELAY_UI)
            sm.registerListener(magListener, mag, SensorManager.SENSOR_DELAY_UI)
            useFallback = false
            _available.value = true
        } else {
            // Fallback to deprecated TYPE_ORIENTATION
            val orient = sm.getDefaultSensor(Sensor.TYPE_ORIENTATION)
            if (orient != null) {
                sm.registerListener(orientationListener, orient, SensorManager.SENSOR_DELAY_UI)
                useFallback = true
                _available.value = true
            } else {
                _available.value = false
            }
        }
    }

    /** Stop listening to conserve battery */
    fun stop() {
        sensorManager?.let { sm ->
            sm.unregisterListener(accelListener)
            sm.unregisterListener(magListener)
            sm.unregisterListener(orientationListener)
        }
        _available.value = false
    }

    /** Compute fused azimuth from accel + magnetometer */
    private fun computeFusedAzimuth() {
        if (useFallback) return // fallback handles itself

        val acc = accelValues ?: return
        val mag = magValues ?: return

        val R = FloatArray(9)
        val I = FloatArray(9)

        val success = SensorManager.getRotationMatrix(R, I, acc, mag)
        if (!success) return

        val orientation = FloatArray(3)
        SensorManager.getOrientation(R, orientation)

        // orientation[0] = azimuth in radians, range [-π, +π]
        // Convert to degrees [0, 360), clockwise from North
        var azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
        azimuthDeg = ((azimuthDeg % 360f) + 360f) % 360f
        _azimuth.value = smoothAzimuth(azimuthDeg)
    }

    /**
     * Exponential Moving Average (EMA) filter with circular wrap-around handling.
     * Smooths out sensor noise to prevent compass jitter when device is still.
     *
     * - alpha: smoothing factor (0.15 = heavy smoothing, responsive but dampened)
     * - deadZone: changes smaller than this (in degrees) are ignored entirely
     */
    private fun smoothAzimuth(raw: Float): Float {
        if (!smoothInitialized) {
            smoothedAzimuth = raw
            smoothInitialized = true
            return raw
        }

        val deadZone = 0.5f
        var delta = raw - smoothedAzimuth

        // Handle wrap-around at 0/360 boundary
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f

        // Dead zone: ignore tiny fluctuations when phone is still
        if (abs(delta) < deadZone) return smoothedAzimuth

        // EMA smoothing
        val alpha = 0.15f
        smoothedAzimuth += alpha * delta

        // Normalize back to [0, 360)
        smoothedAzimuth = ((smoothedAzimuth % 360f) + 360f) % 360f
        return smoothedAzimuth
    }
}
