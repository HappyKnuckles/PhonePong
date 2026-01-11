package com.example.phonepong.util

/**
 * Platform-specific sound player.
 */
expect class SoundPlayer() {
    /**
     * Play a sound indicating a swing.
     */
    fun playSwingSound()

    /**
     * Play a sound indicating a hit (from server).
     */
    fun playHitSound()
}
