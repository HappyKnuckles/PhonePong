package org.tabletennis.project.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.tabletennis.project.network.WebSocketManager

/**
 * JS/Web implementation of sound effects player
 */
@Composable
actual fun SoundEffectsPlayer(webSocketManager: WebSocketManager) {
    val soundManager = SoundManager.getInstance()
    
    // Listen to sound events from server
    val soundEvent by webSocketManager.soundEvent.collectAsState()
    val scoreEvent by webSocketManager.scoreEvent.collectAsState()
    
    // Play sound when server sends sound event
    LaunchedEffect(soundEvent) {
        soundEvent?.let { event ->
            when (event.sound) {
                "hit" -> soundManager.playSound(SoundType.BALL_HIT)
                "bounce" -> soundManager.playSound(SoundType.BALL_BOUNCE)
                "score" -> soundManager.playSound(SoundType.SCORE)
                "start" -> soundManager.playSound(SoundType.GAME_START)
            }
        }
    }
    
    // Play score sound when score changes
    LaunchedEffect(scoreEvent) {
        scoreEvent?.let {
            soundManager.playSound(SoundType.SCORE)
        }
    }
}
