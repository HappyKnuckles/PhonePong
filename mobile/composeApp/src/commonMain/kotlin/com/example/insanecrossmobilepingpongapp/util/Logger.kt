package com.example.insanecrossmobilepingpongapp.util

/**
 * Common logging interface for cross-platform logging.
 * Provides structured logging with tags and levels.
 */
interface Logger {
    fun debug(tag: String, message: String)
    fun info(tag: String, message: String)
    fun warn(tag: String, message: String)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}

/**
 * Factory function to create a platform-specific logger.
 */
expect fun createLogger(): Logger

/**
 * Global logger instance for convenience.
 */
object Log {
    private val logger: Logger by lazy { createLogger() }

    fun d(tag: String, message: String) = logger.debug(tag, message)
    fun i(tag: String, message: String) = logger.info(tag, message)
    fun w(tag: String, message: String) = logger.warn(tag, message)
    fun e(tag: String, message: String, throwable: Throwable? = null) = logger.error(tag, message, throwable)
}
