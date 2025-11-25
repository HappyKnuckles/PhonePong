package com.example.insanecrossmobilepingpongapp.util

import platform.Foundation.NSLog

class IosLogger : Logger {
    override fun debug(tag: String, message: String) {
        NSLog("[DEBUG] [$tag] $message")
    }

    override fun info(tag: String, message: String) {
        NSLog("[INFO] [$tag] $message")
    }

    override fun warn(tag: String, message: String) {
        NSLog("[WARN] [$tag] $message")
    }

    override fun error(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            NSLog("[ERROR] [$tag] $message: ${throwable.message}")
        } else {
            NSLog("[ERROR] [$tag] $message")
        }
    }
}

actual fun createLogger(): Logger = IosLogger()
