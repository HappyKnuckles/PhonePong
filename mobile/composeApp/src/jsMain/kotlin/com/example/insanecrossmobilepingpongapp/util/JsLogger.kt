package com.example.insanecrossmobilepingpongapp.util

class JsLogger : Logger {
    override fun debug(tag: String, message: String) {
        console.log("[$tag] DEBUG: $message")
    }

    override fun info(tag: String, message: String) {
        console.info("[$tag] INFO: $message")
    }

    override fun warn(tag: String, message: String) {
        console.warn("[$tag] WARN: $message")
    }

    override fun error(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            console.error("[$tag] ERROR: $message", throwable)
        } else {
            console.error("[$tag] ERROR: $message")
        }
    }
}

actual fun createLogger(): Logger = JsLogger()
