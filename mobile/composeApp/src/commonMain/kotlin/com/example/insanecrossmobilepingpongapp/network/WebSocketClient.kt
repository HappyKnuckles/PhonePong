package com.example.insanecrossmobilepingpongapp.network

import com.example.insanecrossmobilepingpongapp.model.SwingEvent
import com.example.insanecrossmobilepingpongapp.util.Log
import com.example.insanecrossmobilepingpongapp.util.formatFloat
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Connection state for WebSocket.
 */
enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

/**
 * WebSocket client for sending swing event data to backend.
 * Production server: ws://131.159.222.93:3000
 */
class WebSocketClient {
    private val client = HttpClient {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<String>()
    val incomingMessages: SharedFlow<String> = _incomingMessages.asSharedFlow()

    private var session: DefaultClientWebSocketSession? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Connect to WebSocket server.
     *
     * @param url WebSocket server URL (e.g., "ws://131.159.222.93:3000")
     * @param token Optional authentication token (not required for production server)
     */
    fun connect(url: String, token: String = "") {
        if (_connectionState.value == ConnectionState.CONNECTING ||
            _connectionState.value == ConnectionState.CONNECTED) {
            Log.w(TAG, "⚠️ Already connecting or connected")
            return
        }

        scope.launch {
            try {
                _connectionState.value = ConnectionState.CONNECTING

                // Build URL with optional token
                val fullUrl = if (token.isNotBlank()) "$url/?token=$token" else url
                Log.i(TAG, "🔌 Connecting to WebSocket: $fullUrl")

                client.webSocket(urlString = fullUrl) {
                    session = this
                    _connectionState.value = ConnectionState.CONNECTED
                    Log.i(TAG, "✅ WebSocket connected")

                    // Listen for incoming messages (optional)
                    try {
                        for (frame in incoming) {
                            when (frame) {
                                is Frame.Text -> {
                                    val text = frame.readText()
                                    Log.d(TAG, "📨 Received: $text")
                                    _incomingMessages.emit(text)
                                }
                                is Frame.Close -> {
                                    val reason = frame.readReason()
                                    Log.i(TAG, "🔌 WebSocket closed: code=${reason?.code} reason=${reason?.message}")
                                }
                                else -> {
                                    // Ignore other frame types
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error receiving messages: ${e.message}", e)
                    }
                }

                // Connection closed normally
                Log.i(TAG, "🔌 WebSocket connection closed")
                _connectionState.value = ConnectionState.DISCONNECTED
                session = null

            } catch (e: Exception) {
                Log.e(TAG, "❌ WebSocket connection failed: ${e.message}", e)
                _connectionState.value = ConnectionState.ERROR
                session = null
            }
        }
    }

    /**
     * Send swing event to server (called ONLY on swing detection).
     * Sends minimal payload with just swing speed.
     *
     * @param swingSpeed The detected swing speed (0.0 - 1.0)
     */
    fun sendSwingEvent(swingSpeed: Float) {
        val currentSession = session
        if (currentSession == null) {
            Log.w(TAG, "⚠️ Cannot send swing – WebSocket not connected")
            return
        }

        scope.launch {
            try {
                val swingEvent = SwingEvent(speed = swingSpeed)
                val json = Json.encodeToString(swingEvent)
                currentSession.send(Frame.Text(json))

                // Log the swing send event
                Log.i(TAG, "🏓 Sending swing event → speed=${formatFloat(swingSpeed, 2)}")
                Log.d(TAG, "📦 Payload: $json")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to send swing event: ${e.message}", e)
            }
        }
    }

    /**
     * Disconnect from WebSocket server.
     */
    fun disconnect() {
        scope.launch {
            try {
                Log.i(TAG, "🔌 Disconnecting WebSocket...")
                session?.close(CloseReason(CloseReason.Codes.NORMAL, "Client disconnect"))
                session = null
                _connectionState.value = ConnectionState.DISCONNECTED
                Log.i(TAG, "✅ WebSocket disconnected")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error during disconnect: ${e.message}", e)
            }
        }
    }

    /**
     * Close the HTTP client (cleanup).
     */
    fun close() {
        disconnect()
        client.close()
        Log.i(TAG, "🛑 WebSocket client closed")
    }

    companion object {
        private const val TAG = "WebSocketClient"
    }
}
