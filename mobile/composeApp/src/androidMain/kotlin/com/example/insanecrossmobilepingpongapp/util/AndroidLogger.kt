package com.example.insanecrossmobilepingpongapp.util

import android.util.Log as AndroidLog

class AndroidLogger : Logger {
    override fun debug(tag: String, message: String) {
        AndroidLog.d(tag, message)
    }

    override fun info(tag: String, message: String) {
        AndroidLog.i(tag, message)
    }

    override fun warn(tag: String, message: String) {
        AndroidLog.w(tag, message)
    }

    override fun error(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            AndroidLog.e(tag, message, throwable)
        } else {
            AndroidLog.e(tag, message)
        }
    }
}

actual fun createLogger(): Logger = AndroidLogger()
