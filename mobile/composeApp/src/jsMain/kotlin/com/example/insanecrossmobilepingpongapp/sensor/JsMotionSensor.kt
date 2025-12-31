package com.example.insanecrossmobilepingpongapp.sensor

import com.example.insanecrossmobilepingpongapp.model.DeviceOrientation
import com.example.insanecrossmobilepingpongapp.util.Log
import com.example.insanecrossmobilepingpongapp.util.formatFloat
import com.example.insanecrossmobilepingpongapp.util.toDegrees
import kotlinx.browser.window
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.dom.events.Event
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.js.Console
import kotlin.math.PI
import kotlin.math.sqrt

// Access browser console
private val console: Console get() = js("console").unsafeCast<Console>()

/**
 * JS/Browser MotionSensor implementation using the DeviceOrientation and DeviceMotion APIs.
 * 
 * The DeviceOrientation API provides:
 * - alpha: rotation around Z-axis (0-360 degrees) - corresponds to yaw
 * - beta: rotation around X-axis (-180 to 180 degrees) - corresponds to pitch
 * - gamma: rotation around Y-axis (-90 to 90 degrees) - corresponds to roll
 * 
 * The DeviceMotion API provides:
 * - accelerationIncludingGravity or acceleration (without gravity)
 */
class JsMotionSensor : MotionSensor {
    private var updateCount = 0
    private val logEveryNUpdates = 50 // Log every 50 updates (~1 second at 50Hz)

    // Track if we have received any events (for availability check)
    private var hasReceivedOrientationEvent = false
    private var hasReceivedMotionEvent = false

    // Store latest values
    private var latestPitch = 0f
    private var latestRoll = 0f
    private var latestYaw = 0f
    private var latestAccelX = 0f
    private var latestAccelY = 0f
    private var latestAccelZ = 0f

    override val orientation: Flow<DeviceOrientation> = callbackFlow {
        Log.i(TAG, "🎮 Motion sensor starting...")

        // Check for API availability
        val hasOrientationAPI = js("typeof DeviceOrientationEvent !== 'undefined'") as Boolean
        val hasMotionAPI = js("typeof DeviceMotionEvent !== 'undefined'") as Boolean

        Log.i(TAG, "📱 DeviceOrientation API available: $hasOrientationAPI")
        Log.i(TAG, "📱 DeviceMotion API available: $hasMotionAPI")

        if (!hasOrientationAPI && !hasMotionAPI) {
            Log.e(TAG, "❌ Neither DeviceOrientation nor DeviceMotion APIs are available!")
            close(IllegalStateException("Device motion APIs not available"))
            return@callbackFlow
        }

        // Request permission on iOS 13+ (required for motion events)
        requestMotionPermissionIfNeeded()

        fun sendCombinedData() {
            val deviceOrientation = DeviceOrientation(
                pitch = latestPitch,
                roll = latestRoll,
                yaw = latestYaw,
                accelerationX = latestAccelX,
                accelerationY = latestAccelY,
                accelerationZ = latestAccelZ
            )

            // Periodic detailed logging
            updateCount++
            if (updateCount % logEveryNUpdates == 0) {
                val accelMagnitude = sqrt(
                    latestAccelX * latestAccelX +
                    latestAccelY * latestAccelY +
                    latestAccelZ * latestAccelZ
                )
                Log.d(
                    TAG, "📊 Orientation → pitch: ${formatFloat(toDegrees(latestPitch.toDouble()).toFloat(), 3)}, " +
                            "roll: ${formatFloat(toDegrees(latestRoll.toDouble()).toFloat(), 3)}, " +
                            "yaw: ${formatFloat(toDegrees(latestYaw.toDouble()).toFloat(), 3)}"
                )
                Log.d(
                    TAG, "🚀 Acceleration → X: ${formatFloat(latestAccelX, 2)}, " +
                            "Y: ${formatFloat(latestAccelY, 2)}, " +
                            "Z: ${formatFloat(latestAccelZ, 2)}, " +
                            "|mag|: ${formatFloat(accelMagnitude, 2)} m/s²"
                )
            }

            trySend(deviceOrientation)
        }

        // DeviceOrientation event handler
        val orientationHandler: (Event) -> Unit = { event ->
            hasReceivedOrientationEvent = true
            val e = event.asDynamic()

            // Convert degrees to radians for consistency with Android/iOS
            val alpha = (e.alpha as? Double) ?: 0.0  // yaw (0-360)
            val beta = (e.beta as? Double) ?: 0.0    // pitch (-180 to 180)
            val gamma = (e.gamma as? Double) ?: 0.0  // roll (-90 to 90)

            // Convert to radians
            latestYaw = (alpha * PI / 180.0).toFloat()
            latestPitch = (beta * PI / 180.0).toFloat()
            latestRoll = (gamma * PI / 180.0).toFloat()

            sendCombinedData()
        }

        // DeviceMotion event handler
        val motionHandler: (Event) -> Unit = { event ->
            hasReceivedMotionEvent = true
            val e = event.asDynamic()

            // Try to get acceleration without gravity first, fall back to accelerationIncludingGravity
            val accel = e.acceleration ?: e.accelerationIncludingGravity
            if (accel != null) {
                // Convert G-force to m/s² (1G ≈ 9.81 m/s²) - same as iOS
                val gravity = 9.81f
                val rawX = ((accel.x as? Double) ?: 0.0).toFloat()
                val rawY = ((accel.y as? Double) ?: 0.0).toFloat()
                val rawZ = ((accel.z as? Double) ?: 0.0).toFloat()
                
                latestAccelX = rawX * gravity
                latestAccelY = rawY * gravity
                latestAccelZ = rawZ * gravity
            }

            // Only send if we don't have orientation events (to avoid double-sending)
            if (!hasReceivedOrientationEvent) {
                sendCombinedData()
            }
        }

        // Add event listeners
        if (hasOrientationAPI) {
            window.addEventListener("deviceorientation", orientationHandler)
            Log.i(TAG, "✅ DeviceOrientation event listener added")
        }

        if (hasMotionAPI) {
            window.addEventListener("devicemotion", motionHandler)
            Log.i(TAG, "✅ DeviceMotion event listener added")
        }

        awaitClose {
            Log.i(TAG, "🛑 Motion sensor stopping...")
            if (hasOrientationAPI) {
                window.removeEventListener("deviceorientation", orientationHandler)
            }
            if (hasMotionAPI) {
                window.removeEventListener("devicemotion", motionHandler)
            }
            Log.i(TAG, "✅ Motion event listeners removed")
        }
    }

    /**
     * Request permission for motion sensors on iOS 13+.
     * On iOS Safari, the DeviceMotionEvent.requestPermission() must be called from a user gesture.
     */
    private fun requestMotionPermissionIfNeeded() {
        try {
            // Check if we need to request permission (iOS 13+)
            val DeviceOrientationEvent = js("window.DeviceOrientationEvent")
            if (DeviceOrientationEvent?.requestPermission != undefined) {
                Log.i(TAG, "📱 iOS 13+ detected, permission request may be needed")
                Log.i(TAG, "⚠️ User must tap a button to grant motion sensor permission")
                // Note: The actual permission request must be triggered by a user gesture
                // This will be handled by the UI when the user taps to start
            }
        } catch (e: Exception) {
            Log.w(TAG, "Permission check error: ${e.message}")
        }
    }

    override fun start() {
        Log.i(TAG, "start() called - Flow handles event registration automatically")
    }

    override fun stop() {
        Log.i(TAG, "stop() called - Flow handles event unregistration automatically")
    }

    override fun isAvailable(): Boolean {
        val hasOrientationAPI = js("typeof DeviceOrientationEvent !== 'undefined'") as Boolean
        val hasMotionAPI = js("typeof DeviceMotionEvent !== 'undefined'") as Boolean
        val available = hasOrientationAPI || hasMotionAPI
        Log.d(TAG, "isAvailable() = $available (orientation=$hasOrientationAPI, motion=$hasMotionAPI)")
        return available
    }

    companion object {
        private const val TAG = "JsMotionSensor"
    }
}

/**
 * Request motion sensor permission from a user gesture (required on iOS 13+).
 * Call this function from an onClick handler.
 * 
 * @return true if permission was granted or not needed, false otherwise
 */
suspend fun requestMotionPermission(): Boolean {
    return try {
        val DeviceOrientationEvent = js("window.DeviceOrientationEvent")
        val DeviceMotionEvent = js("window.DeviceMotionEvent")

        var orientationGranted = true
        var motionGranted = true

        // Request DeviceOrientationEvent permission
        if (DeviceOrientationEvent?.requestPermission != undefined) {
            val result = js("DeviceOrientationEvent.requestPermission()") as kotlin.js.Promise<String>
            orientationGranted = awaitPromise(result) == "granted"
            Log.i("JsMotionSensor", "DeviceOrientation permission: $orientationGranted")
        }

        // Request DeviceMotionEvent permission
        if (DeviceMotionEvent?.requestPermission != undefined) {
            val result = js("DeviceMotionEvent.requestPermission()") as kotlin.js.Promise<String>
            motionGranted = awaitPromise(result) == "granted"
            Log.i("JsMotionSensor", "DeviceMotion permission: $motionGranted")
        }

        orientationGranted && motionGranted
    } catch (e: Exception) {
        Log.e("JsMotionSensor", "Permission request failed: ${e.message}")
        // If an error occurs, assume permission is not needed (non-iOS browsers)
        true
    }
}

// Extension to await JS Promise
private suspend fun <T> awaitPromise(promise: kotlin.js.Promise<T>): T {
    return suspendCancellableCoroutine { cont ->
        promise.then(
            onFulfilled = { value ->
                cont.resume(value)
                value
            },
            onRejected = { error ->
                cont.resumeWithException(Exception(error.toString()))
                null
            }
        )
    }
}

actual fun createMotionSensor(): MotionSensor {
    Log.i("JsMotionSensor", "Creating JsMotionSensor for browser")
    return JsMotionSensor()
}

actual suspend fun requestMotionSensorPermission(): Boolean {
    val granted = js("window.__motionPermissionGranted === true") as Boolean
    Log.i("JsMotionSensor", "requestMotionSensorPermission() - granted: $granted")
    return granted
}

actual fun needsMotionPermissionRequest(): Boolean {
    val granted = js("window.__motionPermissionGranted === true") as Boolean
    Log.i("JsMotionSensor", "needsMotionPermissionRequest() = ${!granted}")
    return !granted
}
