package com.example.phonepong.util

import kotlinx.browser.window

/**
 * JS/Browser implementation of Vibrator using the Vibration API.
 */
actual class Vibrator actual constructor() {
    
    actual fun vibrate(milliseconds: Long) {
        try {
            // Use the Vibration API
            val navigator = window.navigator.asDynamic()
            if (navigator.vibrate != undefined) {
                navigator.vibrate(milliseconds.toInt())
            } else {
                console.log("Vibration API not supported on this device")
            }
        } catch (e: Exception) {
            console.warn("Could not vibrate: ${e.message}")
        }
    }
}
