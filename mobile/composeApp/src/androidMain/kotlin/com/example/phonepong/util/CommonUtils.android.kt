package com.example.phonepong.util

actual fun formatFloat(value: Float, decimals: Int): String {
    return String.format("%.${decimals}f", value)
}

actual fun getCurrentTimeMillis(): Long {
    return System.currentTimeMillis()
}
