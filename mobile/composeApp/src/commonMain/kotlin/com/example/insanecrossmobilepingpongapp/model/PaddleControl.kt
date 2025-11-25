package com.example.insanecrossmobilepingpongapp.model

import kotlinx.serialization.Serializable

/**
 * Normalized paddle control data to be sent to the backend.
 *
 * @property tiltX Horizontal tilt (left/right). Range: [-1.0, 1.0]
 * @property tiltY Vertical tilt (forward/backward). Range: [-1.0, 1.0]
 * @property intensity Movement intensity/speed. Range: [0.0, 1.0]
 * @property swingSpeed Swing/acceleration magnitude representing racket speed. Range: [0.0, 1.0]
 * @property swingDirectionX Normalized swing direction X component. Range: [-1.0, 1.0]
 * @property swingDirectionY Normalized swing direction Y component. Range: [-1.0, 1.0]
 */
@Serializable
data class PaddleControl(
    val tiltX: Float = 0f,
    val tiltY: Float = 0f,
    val intensity: Float = 0f,
    val swingSpeed: Float = 0f,
    val swingDirectionX: Float = 0f,
    val swingDirectionY: Float = 0f
) {
    init {
        require(tiltX in -1f..1f) { "tiltX must be in range [-1.0, 1.0]" }
        require(tiltY in -1f..1f) { "tiltY must be in range [-1.0, 1.0]" }
        require(intensity in 0f..1f) { "intensity must be in range [0.0, 1.0]" }
        require(swingSpeed in 0f..1f) { "swingSpeed must be in range [0.0, 1.0]" }
        require(swingDirectionX in -1f..1f) { "swingDirectionX must be in range [-1.0, 1.0]" }
        require(swingDirectionY in -1f..1f) { "swingDirectionY must be in range [-1.0, 1.0]" }
    }
}
