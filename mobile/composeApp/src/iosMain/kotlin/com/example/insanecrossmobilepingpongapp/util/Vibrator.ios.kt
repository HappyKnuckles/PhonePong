package com.example.insanecrossmobilepingpongapp.util

import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.AudioToolbox.kSystemSoundID_Vibrate

actual class Vibrator {
    actual fun vibrate(milliseconds: Long) {
        // iOS doesn't support custom duration for standard vibration easily.
        // We trigger the standard vibration.
        AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)
    }
}
