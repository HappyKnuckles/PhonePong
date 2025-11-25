package com.example.insanecrossmobilepingpongapp.util

import kotlin.math.PI

/**
 * Format a float value to a string with the specified number of decimal places.
 */
expect fun formatFloat(value: Float, decimals: Int): String

/**
 * Convert radians to degrees.
 */
fun toDegrees(radians: Double): Double {
    return radians * 180.0 / PI
}

/**
 * Get the current time in milliseconds.
 */
expect fun getCurrentTimeMillis(): Long
