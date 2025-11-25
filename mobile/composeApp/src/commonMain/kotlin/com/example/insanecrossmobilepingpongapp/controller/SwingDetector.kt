package com.example.insanecrossmobilepingpongapp.controller

import com.example.insanecrossmobilepingpongapp.model.PaddleControl
import com.example.insanecrossmobilepingpongapp.util.Log
import com.example.insanecrossmobilepingpongapp.util.formatFloat

/**
 * Swing detection configuration.
 */
object SwingDetectionConfig {
    /** Minimum swing speed to trigger detection (0.0-1.0) */
    const val SWING_SPEED_THRESHOLD = 0.5f

    /** Minimum time between swings in milliseconds (debounce) */
    const val SWING_MIN_INTERVAL_MS = 200L

    /** Optional: Additional threshold for acceleration magnitude (m/s²) */
    const val ACCEL_MAGNITUDE_THRESHOLD = 8.0f
}

/**
 * Helper class for detecting ping pong swing gestures.
 *
 * Analyzes motion data to determine when the user performs a swing motion,
 * avoiding false positives and handling debouncing.
 */
class SwingDetector {
    private var lastSwingTimestamp: Long = 0L
    private var consecutiveHighSpeedCount = 0
    private val requiredConsecutiveFrames = 2 // Require 2+ frames above threshold to confirm

    /**
     * Detect if a swing gesture has occurred.
     *
     * @param control Current paddle control data
     * @param currentTimeMs Current timestamp in milliseconds
     * @return true if a swing was detected, false otherwise
     */
    fun detectSwing(control: PaddleControl, currentTimeMs: Long): Boolean {
        val timeSinceLastSwing = currentTimeMs - lastSwingTimestamp

        // Check if we're within cooldown period
        if (timeSinceLastSwing < SwingDetectionConfig.SWING_MIN_INTERVAL_MS) {
            if (control.swingSpeed > SwingDetectionConfig.SWING_SPEED_THRESHOLD) {
                Log.d(TAG, "⏱️ Swing ignored – within cooldown window (Δt=${timeSinceLastSwing}ms)")
            }
            consecutiveHighSpeedCount = 0
            return false
        }

        // Check if swing speed exceeds threshold
        if (control.swingSpeed >= SwingDetectionConfig.SWING_SPEED_THRESHOLD) {
            consecutiveHighSpeedCount++

            // Log candidate movement below threshold
            if (consecutiveHighSpeedCount == 1) {
                Log.d(TAG, "🔍 Swing candidate – speed=${formatFloat(control.swingSpeed, 2)} (>= ${formatFloat(SwingDetectionConfig.SWING_SPEED_THRESHOLD, 2)}), confirming...")
            }

            // Require multiple consecutive frames to confirm (reduces noise)
            if (consecutiveHighSpeedCount >= requiredConsecutiveFrames) {
                // Swing detected!
                lastSwingTimestamp = currentTimeMs
                consecutiveHighSpeedCount = 0

                Log.i(TAG, "🏓 Swing detected! speed=${formatFloat(control.swingSpeed, 2)} dirX=${formatFloat(control.swingDirectionX, 2)} dirY=${formatFloat(control.swingDirectionY, 2)} intensity=${formatFloat(control.intensity, 2)}")

                return true
            }
        } else {
            // Speed below threshold
            if (consecutiveHighSpeedCount > 0) {
                Log.d(TAG, "💤 No swing – speed=${formatFloat(control.swingSpeed, 2)} (< ${formatFloat(SwingDetectionConfig.SWING_SPEED_THRESHOLD, 2)}), resetting counter")
            }
            consecutiveHighSpeedCount = 0
        }

        return false
    }

    /**
     * Reset the swing detector state.
     */
    fun reset() {
        lastSwingTimestamp = 0L
        consecutiveHighSpeedCount = 0
        Log.i(TAG, "🔄 Swing detector reset")
    }

    /**
     * Get time since last detected swing.
     */
    fun getTimeSinceLastSwing(currentTimeMs: Long): Long {
        return currentTimeMs - lastSwingTimestamp
    }

    companion object {
        private const val TAG = "SwingDetector"
    }
}
