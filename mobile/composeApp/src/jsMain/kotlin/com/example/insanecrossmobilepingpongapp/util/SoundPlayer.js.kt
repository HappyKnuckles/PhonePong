package com.example.insanecrossmobilepingpongapp.util

import kotlinx.browser.window

/**
 * JS/Browser implementation of SoundPlayer using Web Audio API.
 */
actual class SoundPlayer actual constructor() {
    
    actual fun playSwingSound() {
        try {
            // Use Web Audio API to create a simple beep
            val audioContext = js("new (window.AudioContext || window.webkitAudioContext)()")
            val oscillator = audioContext.createOscillator()
            val gainNode = audioContext.createGain()
            
            oscillator.connect(gainNode)
            gainNode.connect(audioContext.destination)
            
            oscillator.frequency.value = 440 // A4 note
            oscillator.type = "sine"
            
            gainNode.gain.setValueAtTime(0.3, audioContext.currentTime)
            gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.1)
            
            oscillator.start(audioContext.currentTime)
            oscillator.stop(audioContext.currentTime + 0.1)
        } catch (e: Exception) {
            console.warn("Could not play swing sound: ${e.message}")
        }
    }

    actual fun playHitSound() {
        try {
            // Use Web Audio API to create a hit sound
            val audioContext = js("new (window.AudioContext || window.webkitAudioContext)()")
            val oscillator = audioContext.createOscillator()
            val gainNode = audioContext.createGain()
            
            oscillator.connect(gainNode)
            gainNode.connect(audioContext.destination)
            
            oscillator.frequency.value = 880 // A5 note (higher pitch for hit)
            oscillator.type = "square"
            
            gainNode.gain.setValueAtTime(0.4, audioContext.currentTime)
            gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.15)
            
            oscillator.start(audioContext.currentTime)
            oscillator.stop(audioContext.currentTime + 0.15)
        } catch (e: Exception) {
            console.warn("Could not play hit sound: ${e.message}")
        }
    }
}
