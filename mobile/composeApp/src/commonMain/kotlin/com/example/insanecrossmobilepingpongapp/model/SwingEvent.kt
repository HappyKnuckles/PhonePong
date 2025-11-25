package com.example.insanecrossmobilepingpongapp.model

import kotlinx.serialization.Serializable

/**
 * Minimal swing event payload sent to the backend.
 * Only contains essential swing information to reduce bandwidth.
 *
 * @property event Event type identifier (always "swing")
 * @property speed Swing speed magnitude (0.0 - 1.0)
 */
@Serializable
data class SwingEvent(
    val event: String = "swing",
    val speed: Float
) {
    init {
        require(speed in 0f..1f) { "speed must be in range [0.0, 1.0]" }
    }
}
