package org.tabletennis.project.audio

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Audio

/**
 * Sound Manager for playing game sound effects
 * Uses HTML5 Audio API
 */
class SoundManager {
    
    private val sounds = mutableMapOf<SoundType, Audio>()
    private var isMuted = false
    private var volume = 1.0
    
    init {
        loadSounds()
    }
    
    private fun loadSounds() {
        try {
            // Load sound files from resources
            // Note: You'll need to add actual audio files to composeResources/files/
            sounds[SoundType.BALL_HIT] = createAudio("sounds/ball_hit.mp3")
            sounds[SoundType.BALL_BOUNCE] = createAudio("sounds/ball_bounce.mp3")
            sounds[SoundType.SCORE] = createAudio("sounds/score.mp3")
            sounds[SoundType.GAME_START] = createAudio("sounds/game_start.mp3")
            
            println("🔊 Sound Manager initialized with ${sounds.size} sounds")
        } catch (e: Exception) {
            console.error("Failed to load sounds: ${e.message}")
        }
    }
    
    private fun createAudio(path: String): Audio {
        return Audio(path).apply {
            this.volume = this@SoundManager.volume
            // Preload the audio
            this.load()
        }
    }
    
    /**
     * Play a sound effect
     */
    fun playSound(soundType: SoundType, volumeMultiplier: Double = 1.0) {
        if (isMuted) return
        
        try {
            val audio = sounds[soundType]
            if (audio != null) {
                // Reset to beginning
                audio.currentTime = 0.0
                // Set volume with multiplier
                audio.volume = (volume * volumeMultiplier).coerceIn(0.0, 1.0)
                // Play the sound
                audio.play()
                println("🔊 Playing sound: $soundType")
            } else {
                console.warn("Sound not found: $soundType")
            }
        } catch (e: Exception) {
            console.error("Failed to play sound $soundType: ${e.message}")
        }
    }
    
    /**
     * Set master volume (0.0 to 1.0)
     */
    fun setVolume(newVolume: Double) {
        volume = newVolume.coerceIn(0.0, 1.0)
        sounds.values.forEach { it.volume = volume }
    }
    
    /**
     * Mute/unmute all sounds
     */
    fun setMuted(muted: Boolean) {
        isMuted = muted
    }
    
    /**
     * Toggle mute
     */
    fun toggleMute(): Boolean {
        isMuted = !isMuted
        return isMuted
    }
    
    companion object {
        private var instance: SoundManager? = null
        
        fun getInstance(): SoundManager {
            if (instance == null) {
                instance = SoundManager()
            }
            return instance!!
        }
    }
}

/**
 * Available sound types in the game
 */
enum class SoundType {
    BALL_HIT,      // When paddle hits ball
    BALL_BOUNCE,   // When ball bounces on table
    SCORE,         // When a point is scored
    GAME_START     // When game starts
}
