package com.example.insanecrossmobilepingpongapp.util

import android.media.MediaPlayer
import com.example.insanecrossmobilepingpongapp.R
import com.example.insanecrossmobilepingpongapp.util.ContextProvider

actual class SoundPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var hitPlayer: MediaPlayer? = null

    init {
        try {
            val context = ContextProvider.getContext()
            mediaPlayer = MediaPlayer.create(context, R.raw.swing)
            hitPlayer = MediaPlayer.create(context, R.raw.hit)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun playSwingSound() {
        playSound(mediaPlayer)
    }

    actual fun playHitSound() {
        playSound(hitPlayer)
    }

    private fun playSound(player: MediaPlayer?) {
        try {
            if (player?.isPlaying == true) {
                player.seekTo(0)
            }
            player?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
