package com.example.insanecrossmobilepingpongapp.util

import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSBundle
import platform.Foundation.NSURL

import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual class SoundPlayer {
    private var audioPlayer: AVAudioPlayer? = null
    private var hitPlayer: AVAudioPlayer? = null

    init {
        val soundUrl = NSBundle.mainBundle.URLForResource("swing", "mp3")
        if (soundUrl != null) {
            audioPlayer = AVAudioPlayer(contentsOfURL = soundUrl, error = null)
            audioPlayer?.prepareToPlay()
        }

        val hitUrl = NSBundle.mainBundle.URLForResource("hit", "mp3")
        if (hitUrl != null) {
            hitPlayer = AVAudioPlayer(contentsOfURL = hitUrl, error = null)
            hitPlayer?.prepareToPlay()
        }
    }

    actual fun playSwingSound() {
        playAudio(audioPlayer)
    }

    actual fun playHitSound() {
        playAudio(hitPlayer)
    }

    private fun playAudio(player: AVAudioPlayer?) {
        player?.currentTime = 0.0
        player?.play()
    }
}
