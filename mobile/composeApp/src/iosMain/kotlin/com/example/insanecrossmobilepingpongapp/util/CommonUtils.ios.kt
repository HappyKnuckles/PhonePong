package com.example.insanecrossmobilepingpongapp.util

import platform.Foundation.NSString
import platform.Foundation.stringWithFormat
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual fun formatFloat(value: Float, decimals: Int): String {
    return NSString.stringWithFormat("%.${decimals}f", value)
}

actual fun getCurrentTimeMillis(): Long {
    return (NSDate().timeIntervalSince1970 * 1000).toLong()
}
