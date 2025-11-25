package com.example.insanecrossmobilepingpongapp.model

/**
 * Represents the device's orientation and motion in 3D space.
 *
 * @property pitch Rotation around the X-axis (forward/backward tilt), in radians. Range: [-π, π]
 * @property roll Rotation around the Y-axis (left/right tilt), in radians. Range: [-π, π]
 * @property yaw Rotation around the Z-axis (rotation), in radians. Range: [-π, π]
 * @property accelerationX Linear acceleration along X-axis (left/right), in m/s²
 * @property accelerationY Linear acceleration along Y-axis (forward/backward), in m/s²
 * @property accelerationZ Linear acceleration along Z-axis (up/down), in m/s²
 */
data class DeviceOrientation(
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val yaw: Float = 0f,
    val accelerationX: Float = 0f,
    val accelerationY: Float = 0f,
    val accelerationZ: Float = 0f
)
