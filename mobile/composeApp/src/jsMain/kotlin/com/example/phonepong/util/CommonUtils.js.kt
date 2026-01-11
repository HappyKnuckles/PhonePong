package com.example.phonepong.util

import kotlin.js.Date

actual fun formatFloat(value: Float, decimals: Int): String {
    return value.asDynamic().toFixed(decimals) as String
}

actual fun getCurrentTimeMillis(): Long {
    return Date.now().toLong()
}
