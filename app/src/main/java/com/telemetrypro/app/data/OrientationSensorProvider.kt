package com.telemetrypro.app.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*

/**
 * Provides device azimuth (compass heading) using Android SensorManager.
 *
 * Standard approach: getRotationMatrix + getOrientation + remapCoordinateSystem.
 * This is the same method used by Google's own compass implementation.
 *
 * Azimuth: 0° = North, 90° = East, 180° = South, 270° = West (clockwise from North)
 */
class OrientationSensorProvider(private val context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    // EMA smoothing state
    private var smoothedAzimuth: Float = 0f
    private var smoothInitialized = false

    private val GRAVITY_EARTH = 9.80665f
    private val MOTION_THRESHOLD = 2.5f

    private val _azimuth = MutableStateFlow(0f)
    val azimuth: StateFlow<Float> = _azimuth.asStateFlow()

    private val _available = MutableStateFlow(false)
    val available: StateFlow<Boolean> = _available.asStateFlow()

    private var accelValues: FloatArray? = null
    private var magValues: FloatArray? = null

    private val accelListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.values?.let { accelValues = it.clone() }
            computeAzimuth()
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private val magListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.values?.let { magValues = it.clone() }
            computeAzimuth()
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

    fun stop() {
        sensorManager?.let { sm ->
            sm.unregisterListener(accelListener)
            sm.unregisterListener(magListener)
            sm.unregisterListener(orientationListener)
        }
        _available.value = false
    }

    /** Get current display rotation for coordinate remapping */
    private fun getDisplayRotation(): Int {
        return try {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            @Suppress("DEPRECATION")
            wm?.defaultDisplay?.rotation ?: Surface.ROTATION_0
        } catch (e: Exception) {
            Surface.ROTATION_0
        }
    }

    /**
     * Standard Android compass azimuth computation.
     *
     * 1. getRotationMatrix: fuse accel + magnetometer → rotation matrix R
     * 2. remapCoordinateSystem: adjust R for current screen rotation
     * 3. getOrientation: extract azimuth (angle of device Y-axis from North)
     *
     * This is the exact method used by Android's built-in compass and
     * Google Maps. It handles all device orientations correctly.
     */
    private fun computeAzimuth() {
        if (useFallback) return

        val acc = accelValues ?: return
        val mag = magValues ?: return

        val R = FloatArray(9)
        val I = FloatArray(9)

        val success = SensorManager.getRotationMatrix(R, I, acc, mag)
        if (!success) return

        // Remap for display rotation so "top of screen" = device heading
        val R2 = FloatArray(9)
        when (getDisplayRotation()) {
            Surface.ROTATION_0 ->
                SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Y, R2)
            Surface.ROTATION_90 ->
                SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, R2)
            Surface.ROTATION_180 ->
                SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y, R2)
            Surface.ROTATION_270 ->
                SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, R2)
            else -> R.copyInto(R2)
        }

        val orientation = FloatArray(3)
        SensorManager.getOrientation(R2, orientation)

        // orientation[0] = azimuth in radians, range [-π, π]
        // Convert to degrees [0, 360), clockwise from North
        var azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
        azimuthDeg = (azimuthDeg + 360f) % 360f

        _azimuth.value = smoothAzimuth(azimuthDeg)
    }

    /**
     * Adaptive EMA filter: prevents jitter when stationary,
     * freezes heading during violent motion (pickup) to avoid wild swings.
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
            // During motion: rotation matrix corrupted by linear acceleration
            // Nearly freeze heading, allow tiny corrections only
            val alpha = 0.02f
            val maxChange = 1.0f
            delta = delta.coerceIn(-maxChange, maxChange)
            smoothedAzimuth += alpha * delta
        } else {
            // Stationary: dead zone absorbs sensor noise
            val deadZone = 0.8f
            if (abs(delta) < deadZone) return smoothedAzimuth

            val alpha = 0.15f
            smoothedAzimuth += alpha * delta
        }

        smoothedAzimuth = ((smoothedAzimuth % 360f) + 360f) % 360f
        return smoothedAzimuth
    }
}
