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
 * Azimuth: 0 deg = North, 90 deg = East, 180 deg = South, 270 deg = West (clockwise from North)
 */
class OrientationSensorProvider(private val context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    // EMA smoothing state
    private var smoothedAzimuth: Float = 0f
    private var smoothInitialized = false

    // Motion detection: detect when phone is being moved via accel magnitude deviation from gravity
    private val GRAVITY_EARTH = 9.80665f
    private val MOTION_THRESHOLD = 2.5f  // m/s^2 deviation = phone is being moved

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
                    var az = it[0]
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

        val accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val mag = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (accel != null && mag != null) {
            sm.registerListener(accelListener, accel, SensorManager.SENSOR_DELAY_UI)
            sm.registerListener(magListener, mag, SensorManager.SENSOR_DELAY_UI)
            useFallback = false
            _available.value = true
        } else {
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
        if (useFallback) return

        val acc = accelValues ?: return
        val mag = magValues ?: return

        val R = FloatArray(9)
        val I = FloatArray(9)

        val success = SensorManager.getRotationMatrix(R, I, acc, mag)
        if (!success) return

        val orientation = FloatArray(3)
        SensorManager.getOrientation(R, orientation)

        var azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
        azimuthDeg = ((azimuthDeg % 360f) + 360f) % 360f
        _azimuth.value = smoothAzimuth(azimuthDeg)
    }

    /**
     * Adaptive EMA filter with motion-aware dead zone and rate limiting.
     *
     * Key ideas:
     * 1. Detect phone motion via accelerometer magnitude deviating from gravity.
     *    When moving, the rotation matrix is corrupted by linear acceleration,
     *    so we nearly freeze the heading to prevent jitter.
     * 2. When stationary, use a generous dead zone (1 degree) to hide the
     *    slight magnetic noise that causes 0.5-1 degree wobble on a flat table.
     * 3. Clamp per-frame delta to prevent spurious jumps during transition.
     */
    private fun smoothAzimuth(raw: Float): Float {
        if (!smoothInitialized) {
            smoothedAzimuth = raw
            smoothInitialized = true
            return raw
        }

        var delta = raw - smoothedAzimuth

        // Handle wrap-around at 0/360 boundary
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f

        // Detect motion from accelerometer magnitude
        val isMoving = accelValues?.let { acc ->
            val mag = sqrt(acc[0] * acc[0] + acc[1] * acc[1] + acc[2] * acc[2])
            abs(mag - GRAVITY_EARTH) > MOTION_THRESHOLD
        } ?: false

        if (isMoving) {
            // --- IN MOTION (e.g. pickup from table) ---
            // Linear acceleration corrupts the rotation matrix -> wild azimuth jumps.
            // Nearly freeze the heading until the phone is still again.
            val alpha = 0.008f
            val maxFrameChange = 0.8f
            delta = delta.coerceIn(-maxFrameChange, maxFrameChange)
            smoothedAzimuth += alpha * delta
        } else {
            // --- STATIONARY (flat on table, held steady) ---
            // Dead zone absorbs slight magnetic / sensor noise
            val deadZone = 1.0f
            if (abs(delta) < deadZone) return smoothedAzimuth

            val alpha = 0.10f
            smoothedAzimuth += alpha * delta
        }

        smoothedAzimuth = ((smoothedAzimuth % 360f) + 360f) % 360f
        return smoothedAzimuth
    }
}