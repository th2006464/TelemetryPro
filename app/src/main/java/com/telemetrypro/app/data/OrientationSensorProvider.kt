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
 * Uses accelerometer + magnetic field sensor fusion via getRotationMatrix.
 * Implements tilt-aware azimuth calculation:
 * - When phone is flat (screen up): standard getOrientation (Y-axis projection)
 * - When phone is upright (like holding a compass): Z-axis projection + 180°
 * - Smooth blend between the two modes for seamless transition
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

    /** Get current display rotation */
    private fun getDisplayRotation(): Int {
        return try {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            wm?.defaultDisplay?.rotation ?: Surface.ROTATION_0
        } catch (e: Exception) {
            Surface.ROTATION_0
        }
    }

    /**
     * Compute fused azimuth from accel + magnetometer with tilt-aware correction.
     *
     * Key insight: SensorManager.getOrientation() computes azimuth as the angle of
     * the device's Y-axis projected onto the horizontal plane. When the phone is
     * held upright (like a compass), the Y-axis points up, making this projection
     * near-zero and unreliable — producing a ~180° error.
     *
     * Fix: When the phone is upright, compute azimuth from the device's Z-axis
     * (out of screen) projected onto the horizontal plane, plus 180° to get the
     * direction the top of the phone points.
     */
    private fun computeFusedAzimuth() {
        if (useFallback) return

        val acc = accelValues ?: return
        val mag = magValues ?: return

        val R = FloatArray(9)
        val I = FloatArray(9)

        val success = SensorManager.getRotationMatrix(R, I, acc, mag)
        if (!success) return

        // Remap based on display rotation
        val rotation = getDisplayRotation()
        val R2 = FloatArray(9)
        when (rotation) {
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

        // R2[8] = Z-component of device Z-axis in world coords
        // ≈ 1 when flat (screen up), ≈ 0 when upright, ≈ -1 when screen down
        val tilt = abs(R2[8])

        // Two azimuth estimates:
        // yAz: standard — angle of device Y-axis (top) in horizontal plane
        // zAz: upright — angle of device Z-axis (screen) + 180° = direction top points
        val yAz = atan2(R2[1], R2[4])
        val zAz = atan2(R2[2], R2[5]) + Math.PI.toFloat()

        // Blend: when flat (tilt→1), use yAz; when upright (tilt→0), use zAz
        val blend = (0.5f - tilt).coerceIn(0f, 1f) * 2f
        val azRad = lerpAngleRad(yAz, zAz, blend)

        var azimuthDeg = Math.toDegrees(azRad.toDouble()).toFloat()
        azimuthDeg = ((azimuthDeg % 360f) + 360f) % 360f
        _azimuth.value = smoothAzimuth(azimuthDeg)
    }

    /** Circular linear interpolation between two angles in radians */
    private fun lerpAngleRad(a: Float, b: Float, t: Float): Float {
        var diff = b - a
        // Wrap to [-PI, PI]
        while (diff > Math.PI.toFloat()) diff -= 2f * Math.PI.toFloat()
        while (diff < -Math.PI.toFloat()) diff += 2f * Math.PI.toFloat()
        return a + t * diff
    }

    /**
     * Adaptive EMA filter with motion-aware dead zone and rate limiting.
     */
    private fun smoothAzimuth(raw: Float): Float {
        if (!smoothInitialized) {
            smoothedAzimuth = raw
            smoothInitialized = true
            return raw
        }

        var delta = raw - smoothedAzimuth

        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f

        val isMoving = accelValues?.let { acc ->
            val mag = sqrt(acc[0] * acc[0] + acc[1] * acc[1] + acc[2] * acc[2])
            abs(mag - GRAVITY_EARTH) > MOTION_THRESHOLD
        } ?: false

        if (isMoving) {
            val alpha = 0.008f
            val maxFrameChange = 0.8f
            delta = delta.coerceIn(-maxFrameChange, maxFrameChange)
            smoothedAzimuth += alpha * delta
        } else {
            val deadZone = 1.0f
            if (abs(delta) < deadZone) return smoothedAzimuth
            val alpha = 0.10f
            smoothedAzimuth += alpha * delta
        }

        smoothedAzimuth = ((smoothedAzimuth % 360f) + 360f) % 360f
        return smoothedAzimuth
    }
}
