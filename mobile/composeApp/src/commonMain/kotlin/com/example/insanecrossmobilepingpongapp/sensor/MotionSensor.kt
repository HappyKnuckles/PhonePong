package com.example.insanecrossmobilepingpongapp.sensor

import com.example.insanecrossmobilepingpongapp.model.DeviceOrientation
import kotlinx.coroutines.flow.Flow

/**
 * Platform-specific motion sensor interface.
 * Provides access to device orientation data from gyroscope/accelerometer.
 */
interface MotionSensor {
    /**
     * Flow of device orientation updates.
     */
    val orientation: Flow<DeviceOrientation>

    /**
     * Start listening to motion sensor events.
     */
    fun start()

    /**
     * Stop listening to motion sensor events.
     */
    fun stop()

    /**
     * Check if motion sensors are available on this device.
     */
    fun isAvailable(): Boolean
}

/**
 * Factory function to create a platform-specific MotionSensor instance.
 */
expect fun createMotionSensor(): MotionSensor
