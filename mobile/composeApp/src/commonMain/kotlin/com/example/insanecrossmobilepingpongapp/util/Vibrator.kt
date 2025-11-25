package com.example.insanecrossmobilepingpongapp.util

/**
 * Platform-specific vibrator.
 */
expect class Vibrator() {
    /**
     * Vibrate for the specified duration.
     * @param milliseconds Duration in milliseconds.
     */
    fun vibrate(milliseconds: Long)
}
